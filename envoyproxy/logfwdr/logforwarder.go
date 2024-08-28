// Outlier Log Forwarder
// Reads the Envoy's outlier event logs via a named pipe and forwards them to
// the manager via GRPC messages.
//
// 2019-09-12 EHSACAH, EEDALA  Initial version
// 2019-12-12 EEDALA           Convert to JSON logging
package main

import (
	"bufio"
	"bytes"
	"context"
	"encoding/json"
	"fmt"
	"time"
	"os"
	"strings"
	"sync"
//    "path"
	"reflect"
	"runtime"
	"strconv"
	"crypto/tls"
	"crypto/x509"
	"io/ioutil"
	"github.com/fsnotify/fsnotify"
	"github.com/golang/protobuf/ptypes/timestamp"
	"github.com/golang/protobuf/ptypes/wrappers"
	log "github.com/sirupsen/logrus"
	"google.golang.org/grpc"
	"google.golang.org/grpc/credentials"


	envoy_cluster "github.com/envoyproxy/go-control-plane/envoy/data/cluster/v3"
	envoy_core "github.com/envoyproxy/go-control-plane/envoy/data/core/v3"
	envoy_config "github.com/envoyproxy/go-control-plane/envoy/config/core/v3"
	"ericsson.com/5g/scp/api/v1/outlier"
	"ericsson.com/5g/scp/api/v1/healthcheck"
	"ericsson.com/5g/scp/service/v1/outlier_service"
)

//------------------------------------------------------------------------
const podPrefix = "envoy"

const LogFileName = "/mnt/pipe.log"
const CONNECTION_TIMEOUT = 50 * time.Minute
const STREAM_TIMEOUT = 55 * time.Minute
const DIAL_TIMEOUT = 5 * time.Second
const RECONECTION_TIMEOUT = 5 * time.Second

var managerAddress = os.Getenv("MANAGER_ADDRESS");
var serviceId = os.Getenv("SERVICE_ID");

// Test settings:
//const LogFileName = "pipe.log"
//const managerAddress = "localhost:9900"

//------------------------------------------------------------------------
var logger *log.Entry
var (
	reloadMutex sync.Mutex
)
//------------------------------------------------------------------------
// Alias for the long type name
type OutlierLogStream = outlier_service.OutlierLogService_StreamOutlierLogEventsClient
type HealthCheckLogStream = outlier_service.HealthCheckLogService_StreamHealthCheckLogEventsClient
type ActiveOutlierAlarms = map[string]OutlierEvent
type HealthCheckAlarms = map[string]HealthCheckEvent

// Enum for the statemachine 
const (
	DISCONNECTED int = iota
	CONNECTED
	CONNECTING
)

//------------------------------------------------------------------------
// Goroutine to read a new line from the outlier logfile. Once a new
// event is received, decode it and send the decoded event on the supplied
// channel
func readLogLine(filename string, outlierChan chan OutlierEvent, hcChan chan HealthCheckEvent) {
	// Open the pipe towards Envoy
	file, err := os.Open(filename)
	if err != nil {
		logger.Fatalf("File opening error: %v", err)
	}

	prefix := []byte("{\"health_checker_type\"")
	reader := bufio.NewReader(file)

	for {
		line, err := reader.ReadBytes('\n')
		if err == nil {
			logger.Debug("Reading pipe: " + string(line))
			if bytes.HasPrefix(line, prefix) {
				logger.Debug("HC line")
				var event HealthCheckEvent
				errJson := json.Unmarshal(line, &event)
				if errJson != nil {
					logger.Errorf("Error decoding JSON from outlier-log: %v", errJson)
				} else {
					logger.Debug("Successfully unmarshalled json HealthCheck")
					if doWeNeedThis(&event) {
						hcChan <- event
					} else {
						logger.Debug("We are not interested")
					}
				}
			} else {
				logger.Debug("Outlier line")
				var event OutlierEvent
				errJson := json.Unmarshal(line, &event)
				if errJson != nil {
					logger.Error(fmt.Sprintf("Error decoding JSON from outlier-log: %v", errJson))
				} else {
					logger.Debug("Successfully unmarshalled json Outlier")
					outlierChan <- event
				}
			}
		}
		reader.Reset(reader)
	}
}
func doWeNeedThis(event *HealthCheckEvent) bool {
	if event.HealthCheckEjectUnhealthy != nil || event.HealthCheckAddHealthy != nil {
		return true
	}
	if event.HealthCheckFailure != nil {
		if event.HealthCheckFailure.FirstCheck {
			return true
		}
	}
	log.Debugf("Skipping event: %v", event)
	return false
}

//------------------------------------------------------------------------
// Send an event to the manager via GRPC
func sendOutlierEvent(outlierStream OutlierLogStream, event OutlierEvent, envoyID string) bool {
	// Parameter: seconds since last action
	sslaInt64, err := strconv.ParseUint(event.SecsSinceLastAction, 10, 64)
	var ssla *wrappers.UInt64Value
	if err == nil {
		ssla = &wrappers.UInt64Value{Value: sslaInt64}
	} else {
		ssla = &wrappers.UInt64Value{Value: 0} // SSLA can be empty, then we get here
	}

	// Parameter: timestamp
	const sec int64 = 0
	const nano int32 = 0
	ts := &timestamp.Timestamp{Seconds: sec, Nanos: nano} // TODO: use timestamp from log

	// Parameter: action
	var action envoy_cluster.Action
	switch event.Action {
	case "EJECT":
		action = envoy_cluster.Action_EJECT
	case "UNEJECT":
		action = envoy_cluster.Action_UNEJECT
	default:
		action = 0 // TODO: is this there a better value to use?
		logger.Errorf("Error: Unknown action %v", event.Action)
	}

	// Parameter: ejection type
	var ejectionType envoy_cluster.OutlierEjectionType
	switch event.EjectionType {
	case "CONSECUTIVE_5XX":
		ejectionType = envoy_cluster.OutlierEjectionType_CONSECUTIVE_5XX
	case "CONSECUTIVE_GATEWAY_FAILURE":
		ejectionType = envoy_cluster.OutlierEjectionType_CONSECUTIVE_GATEWAY_FAILURE
	case "SUCCESS_RATE":
		ejectionType = envoy_cluster.OutlierEjectionType_SUCCESS_RATE
	case "CONSECUTIVE_LOCAL_ORIGIN_FAILURE":
		ejectionType = envoy_cluster.OutlierEjectionType_CONSECUTIVE_LOCAL_ORIGIN_FAILURE
	case "SUCCESS_RATE_LOCAL_ORIGIN":
		ejectionType = envoy_cluster.OutlierEjectionType_SUCCESS_RATE_LOCAL_ORIGIN
	default:
		ejectionType = 0 // TODO: is there a better value?
		logger.Errorf("Error: Unknown outlier ejection type %v", event.EjectionType)
	}

	// Assemble the event:
	eventDetails := &envoy_cluster.OutlierDetectionEvent{
		Type:                ejectionType,
		ClusterName:         event.ClusterName,
		UpstreamUrl:         event.UpstreamURL,
		Action:              action,
		NumEjections:        event.NumEjections,
		Enforced:            event.Enforced,
		Timestamp:           ts,
		SecsSinceLastAction: ssla}

	logger.Debugf("Sending od event %v", eventDetails)

	// ..and send it to the manager:
	sendError := outlierStream.Send(&outlier.OutlierLogEvent{EnvoyId: envoyID, EventDetails: eventDetails})
	_, recvErr := outlierStream.Recv()
	if sendError != nil || recvErr != nil {
		if recvErr != nil {
			logger.Errorf("Error receiving response from manager: %v", recvErr)
		}
		if sendError != nil {
			logger.Errorf("Error sending request to manager: %v", sendError)
		}
		return false
	} else {
		logger.Debug("outlier event was successfully sent.")
		return true
	}
}
// convert failure type string into HealthCheckFailureType of proto
func getFailureType(failure_type string) envoy_core.HealthCheckFailureType {
	switch failure_type {
	case "ACTIVE":
		return envoy_core.HealthCheckFailureType_ACTIVE
	case "PASSIVE":
		return envoy_core.HealthCheckFailureType_PASSIVE
	case "NETWORK":
		return envoy_core.HealthCheckFailureType_NETWORK
	case "NETWORK_TIMEOUT":
		return envoy_core.HealthCheckFailureType_NETWORK_TIMEOUT
	default:
		logger.Errorf("Undefined failure_type: %s", failure_type)
		return envoy_core.HealthCheckFailureType_ACTIVE
	}
}
// convert HealthCheckEvent into proto HealthCheckEvent
func createCoreEvent(event HealthCheckEvent) *envoy_core.HealthCheckEvent {
	checkerType := envoy_core.HealthCheckerType_HTTP
	if event.CheckerType != "HTTP" {
		logger.Errorf("Error: Unsupported health check type: %v", event.CheckerType)
	}

	ts := &timestamp.Timestamp{Seconds: 0, Nanos: 0} // TODO: use timestamp from log

	protocol := envoy_config.SocketAddress_TCP
	switch event.Address.SocketAddress.Protocol {
	case "TCP":
		protocol = envoy_config.SocketAddress_TCP
	case "UDP":
		protocol = envoy_config.SocketAddress_UDP
	default:
		logger.Errorf("Error: Unsupported health check type: %v", event.CheckerType)
	}

	pv := &envoy_config.SocketAddress_PortValue{
		PortValue: event.Address.SocketAddress.PortValue,
	}

	socket_address := &envoy_config.SocketAddress{
		Protocol:      protocol,
		Address:       event.Address.SocketAddress.Address,
		PortSpecifier: pv,
		ResolverName:  event.Address.SocketAddress.ResolverName,
		Ipv4Compat:    event.Address.SocketAddress.IPv4Compat,
	}

	adress := &envoy_config.Address{
		Address: &envoy_config.Address_SocketAddress{
			SocketAddress: socket_address,
		},
	}

	protoEvent := &envoy_core.HealthCheckEvent{
		HealthCheckerType: checkerType,
		Host:              adress,
		ClusterName:       event.ClusterName,
		Timestamp:         ts,
	}

	switch {
	case event.HealthCheckEjectUnhealthy != nil:
		protoEvent.Event = &envoy_core.HealthCheckEvent_EjectUnhealthyEvent{
			EjectUnhealthyEvent: &envoy_core.HealthCheckEjectUnhealthy{
				FailureType: getFailureType(event.HealthCheckEjectUnhealthy.FailureType),
			},
		}
	case event.HealthCheckAddHealthy != nil:
		protoEvent.Event = &envoy_core.HealthCheckEvent_AddHealthyEvent{
			AddHealthyEvent: &envoy_core.HealthCheckAddHealthy{
				FirstCheck: event.HealthCheckAddHealthy.FirstCheck,
			},
		}
	case event.HealthCheckFailure != nil:
		protoEvent.Event = &envoy_core.HealthCheckEvent_HealthCheckFailureEvent{
			HealthCheckFailureEvent: &envoy_core.HealthCheckFailure{
				FailureType: getFailureType(event.HealthCheckFailure.FailureType),
				FirstCheck:  event.HealthCheckFailure.FirstCheck,
			},
		}
	case event.DegradedHealthyHost != nil:
		protoEvent.Event = &envoy_core.HealthCheckEvent_DegradedHealthyHost{
			DegradedHealthyHost: &envoy_core.DegradedHealthyHost{},
		}
	case event.NoLongerDegradedHost != nil:
		protoEvent.Event = &envoy_core.HealthCheckEvent_NoLongerDegradedHost{
			NoLongerDegradedHost: &envoy_core.NoLongerDegradedHost{},
		}
	}

	return protoEvent
}
func sendHealthCheckEvent(healthCheckLogStream HealthCheckLogStream, event HealthCheckEvent, envoyID string) bool {
	eventDetails :=	createCoreEvent(event)

	logger.Debugf("Sending hc event %v", eventDetails)

	//send HealthCheckLogEvent to the manager:
	sendError := healthCheckLogStream.Send(&healthcheck.HealthCheckLogEvent{EnvoyId: envoyID, EventDetails: eventDetails})
	_, recvErr := healthCheckLogStream.Recv()

	if sendError != nil || recvErr != nil {
		if recvErr != nil {
			logger.Errorf("Error receiving reply from manager: %v", recvErr)
		}
		if sendError != nil {
			logger.Errorf("Error sending request to manager: %v", sendError)
		}
		return false
	} else {
		logger.Debug("healthCheck event was successfully sent.")
		return true
	}
}
//------------------------------------------------------------------------


//------------------------------------------------------------------------
// Establish a connection to the manager and store the open GRPC stream.
func connectToManager(managerAddress string, chConnect chan bool, outlierStream *OutlierLogStream, healthCheckStream *HealthCheckLogStream, parentCtx context.Context) {
	// Connect to Manager
	// TODO: loop with a delay if Dial returns an error because the manager might not be up yet
	logger.Info("Connecting to server....")
	certPath := os.Getenv("LOGFWDR_GRPC_CLIENT_CERT_PATH")
	caPath := os.Getenv("SIP_TLS_TRUSTED_ROOT_CA_PATH")
	certFile := certPath + "/cert.pem"
	keyFile := certPath + "/key.pem"
	caFile := caPath + "/cacertbundle.pem"

	tlsConfig, err := loadTLSConfigWithRetries(certFile, keyFile, caFile)
	if err != nil {
		logger.Fatalf("Error loading TLS config: %v", err)
	}

	watcher, err := fsnotify.NewWatcher()
	if err != nil {
		log.Fatalf("Error creating watcher: %v", err)
	}
	defer watcher.Close()

	if err := watcher.Add(certFile); err != nil {
		log.Fatalf("Error adding certificate file to watcher: %v", err)
	}
	if err := watcher.Add(keyFile); err != nil {
		log.Fatalf("Error adding key file to watcher: %v", err)
	}
	if err := watcher.Add(caFile); err != nil {
		log.Fatalf("Error adding CA file to watcher: %v", err)
	}

	//Start a Go routine to handle the reloading of the certificate, key and CA
	go func(parentCtx context.Context){
		var timer *time.Timer
		defer func() {
			if timer != nil {
				timer.Stop()
			}
		}()
		for {
			select{
			case <-parentCtx.Done():
				logger.Info("Context done for reloading certificate, key and CA")
				return
			case event, ok := <-watcher.Events:
				if !ok{
					return
				}
				if isCertOrKeyOrCAFile(event.Name){
					logger.Infof("File modified: %s",event.Name)
					reloadMutex.Lock()
					defer reloadMutex.Unlock()
					//Create the TLS configuration
					tlsConfig, err := loadTLSConfigWithRetries(certFile,keyFile,caFile)	
					if err != nil{
						log.Fatalf("Error reloading TLS config: %v", err)
					}
					//Create new TLS credentials based on the TLS configuration 
					creds := credentials.NewTLS(tlsConfig)
					dialCtx, dialCancel := context.WithTimeout(parentCtx, DIAL_TIMEOUT)
					defer dialCancel()
					conn, err := grpc.DialContext(dialCtx, managerAddress, grpc.WithTransportCredentials(creds), grpc.WithBlock())
					if err != nil {
						fmt.Printf("Error: Could not reconnect to server: %v", err)
						panic(err)
					}
					timer = time.AfterFunc(CONNECTION_TIMEOUT, func() {
						logger.Debug("Closing connection inside watcher")
						if conn != nil {
							logger.Debugf("connection: %v", conn.Target())
							conn.Close()
							chConnect <- false
						}
					})
					outlier_service.NewOutlierLogServiceClient(conn)
					outlier_service.NewHealthCheckLogServiceClient(conn)
					logger.Infof("Reconnected to server. Connection status: %v", conn.GetState())
				}
			case err, ok := <-watcher.Errors:
				if !ok{
					return
				}
				log.Printf("Error: %v",err)
			}
		}
	}(parentCtx)

	//Create new TLS credentials based on the TLS configuration 
	creds := credentials.NewTLS(tlsConfig)
	
	var dialCtx, dialCancel = context.WithTimeout(parentCtx, DIAL_TIMEOUT)
	defer dialCancel()
	var conn, connErr = grpc.DialContext(dialCtx, managerAddress, grpc.WithTransportCredentials(creds), grpc.WithBlock())

	if connErr != nil {
		logger.Errorf("Could not connect to server: %v, retrying in 5 seconds", connErr)
		time.Sleep(RECONECTION_TIMEOUT)
		for retries := 0; retries < 6; retries++ {
			select {
			case <-parentCtx.Done():
				logger.Debug("Parent Context done for reconnecting to server")
				dialCancel()
				return
			default:
				dialCtx, dialCancel = context.WithTimeout(parentCtx, DIAL_TIMEOUT)
				defer dialCancel()
				conn, connErr = grpc.DialContext(dialCtx, managerAddress, grpc.WithTransportCredentials(creds), grpc.WithBlock())
				if connErr != nil && retries < 5{
					logger.Errorf("Error: Could not connect to server: %v, retrying in 5 seconds", connErr)
					time.Sleep(RECONECTION_TIMEOUT)
					continue
				} else if connErr != nil {
					logger.Fatalf("Error: Could not connect to server after 5 retries: %v", connErr)
				} 
				break
			}
		}
	}

	timer := time.AfterFunc(CONNECTION_TIMEOUT, func() {
		logger.Debug("Closing connection inside connecttomanager")
		if conn != nil {
			logger.Debugf("connection: %v", conn.Target())
			conn.Close()
			chConnect <- false
		}
	})

	go func(parentCtx context.Context) {
		ctx, cancel := context.WithTimeout(parentCtx, CONNECTION_TIMEOUT)
		defer cancel()
		select {
		case <-ctx.Done():
			return
		case <-parentCtx.Done():
			logger.Debug("Cancelling the timer and connection")
			if stopped := timer.Stop(); stopped {
				logger.Println("Timer stopped")
			}
			if conn != nil {
				logger.Debugf("stopping connection: %v", conn.Target())
				conn.Close()
			}
			return
		}
	}(parentCtx)

	outlier_client := outlier_service.NewOutlierLogServiceClient(conn)
	health_check_client := outlier_service.NewHealthCheckLogServiceClient(conn)
	
	logger.Infof("Connected to server. Connection status: %v", conn.GetState())

	streamCtx, _ := context.WithTimeout(parentCtx, STREAM_TIMEOUT)

	stream1, err := outlier_client.StreamOutlierLogEvents(streamCtx)
	stream2, err := health_check_client.StreamHealthCheckLogEvents(streamCtx)

	if err != nil {
		logger.Fatalf("Unable to get the stream: %v", err)
	} else {
		*outlierStream = stream1
	}
	if err != nil {
		logger.Fatalf("Unable to get the stream: %v", err)
	} else {
		*healthCheckStream = stream2;
	}

	chConnect <- true
}
//------------------------------------------------------------------------
// Determine and return our EnvoyID, which is "envoy-" followed by the
// random characters after the last dash in the pod-name.
func getEnvoyID() string {
	// Get pod name from env. If the environment variable doesn't exist or
	// does not contain a dash, we use "envoy-unknown" instead.
	podID := os.Getenv("HOSTNAME")
	IDPos := strings.LastIndex(podID, "-")
	extractedPodID := "-unknown"
	if IDPos > 0 {
		extractedPodID = podID[IDPos:]
	}

	return fmt.Sprint(podPrefix, extractedPodID)
}
//------------------------------------------------------------------------
// Add an event to the activeAlarm list
func addOutlierEvent(alarms *ActiveOutlierAlarms, event OutlierEvent) {
	// Use only the name-part of the upstream-URL (the part after the "|")
	// to be able to handle changes in IP-addresses (can be easily tested
	// by re-deploying chfsim)
	host := event.UpstreamURL
	hostPos := strings.Index(host, "|")
	if hostPos > 0 {
		host = host[hostPos:]
	}

	if event.Action == "EJECT" {
		logger.Infof("Adding alarm to alarm-list: %v", event)
		(*alarms)[host] = event
	} else { // UNEJECT
		logger.Infof("Clearing alarm in alarm-list: %v", event)
		delete(*alarms, host)
	}
	logger.Infof("Currently active alarms:: %v", *alarms)
}
func addHealthCheckEvent(alarms *HealthCheckAlarms, event HealthCheckEvent) {
	logger.Debugf("Adding health check event")
	key := event.ClusterName + "." + event.Address.SocketAddress.Address + ":" + strconv.FormatUint(uint64(event.Address.SocketAddress.PortValue), 10)

	if event.HealthCheckAddHealthy != nil {
		logger.Debugf("Clearing alarm in alarm-list: %+v", event.String())
		delete(*alarms, key)
	} else {
		logger.Debugf("Adding alarm to alarm-list: %+v", event.String())
		(*alarms)[key] = event
	}
}
func rememberToSendHealthy(alarms *HealthCheckAlarms, event HealthCheckEvent) {
	if event.HealthCheckAddHealthy != nil {
		key := event.ClusterName + "." + event.Address.SocketAddress.Address + ":" + strconv.FormatUint(uint64(event.Address.SocketAddress.PortValue), 10)
		logger.Debugf("Adding alarm to remember-to-send-list: %s", key)
		(*alarms)[key] = event
	}
}
func clearFromRememberToSendHealthy(alarms *HealthCheckAlarms, key string) {
	logger.Debugf("Clearing alarm from remember-to-send-list: %s", key)
	delete(*alarms, key)
}

//------------------------------------------------------------------------
func main() {
	// Helper to print state names
	stateName := map[int]string{DISCONNECTED: "DISCONNECTED",
		CONNECTED: "CONNECTED", CONNECTING: "CONNECTING"}

	// Logger configuration:
	// - JSON
	// - Our timestamp format
	// - Our fieldnames
	// - Our file/line format
	log.SetFormatter(&log.JSONFormatter{
		TimestampFormat: "2006-01-02T15:04:05.000-07:00",
		FieldMap: log.FieldMap{
			log.FieldKeyLevel: "severity",
			log.FieldKeyMsg:   "message",
			log.FieldKeyTime:  "timestamp",
		},
		CallerPrettyfier: func(f *runtime.Frame) (string, string) {
			//funcname := f.Function
			//_, filename := path.Split(f.File)
			//filename = fmt.Sprintf("%s:%d", filename, f.Line)
			return "", ""
		},
	})
	log.SetReportCaller(true)
	log.SetLevel(log.DebugLevel)
	// Always include the "version" and the "service_id" in each log message:
	logger = log.WithFields(log.Fields{"version": "1.1.0", "service_id": serviceId})
	// Use "logger.Info()" etc. from here on, not "log.Info()"

	envoyID := getEnvoyID()
	logger.Info("Envoy ID is: ", envoyID)
	logger.Info("CONNECTION_TIMEOUT is: ", CONNECTION_TIMEOUT)
	logger.Info("STREAM_TIMEOUT is: ", STREAM_TIMEOUT)

	activeAlarmsOutlier := make(ActiveOutlierAlarms)
	activeAlarmsHealthCheck := make(HealthCheckAlarms)
	rememberToSendHealthyList := make(HealthCheckAlarms)

	// Communication channels:
	chLogfileOutlier := make(chan OutlierEvent)
	chLogfileHealthCheck := make(chan HealthCheckEvent)
	
	// channels to trak connection, I use one chanel (connect and disconnect) for both Services (healthcheck and outlier)
	// when we god disconnected we need to rebuild connection for both services
	chConnect := make(chan bool)

	go readLogLine(LogFileName, chLogfileOutlier, chLogfileHealthCheck)
	
	state := DISCONNECTED
	var outlierStream OutlierLogStream
	var healthCheckStream HealthCheckLogStream
	
	var ctx context.Context
	var cancel context.CancelFunc

	// Main loop: read new event from pipe/Envoy, send to manager as GRPC
	for {
		logger.Debugf("State: %v", stateName[state])
		switch state {
		case DISCONNECTED:
			select {
			case event := <-chLogfileOutlier:
				logger.Debug("DISCONNECTED: got outlier event")
				addOutlierEvent(&activeAlarmsOutlier, event)
				ctx, cancel = context.WithCancel(context.Background())
				go connectToManager(managerAddress, chConnect, &outlierStream, &healthCheckStream, ctx)
				state = CONNECTING
			case event := <-chLogfileHealthCheck:
				addHealthCheckEvent(&activeAlarmsHealthCheck, event)
				rememberToSendHealthy(&rememberToSendHealthyList, event)
				ctx, cancel = context.WithCancel(context.Background())
				go connectToManager(managerAddress, chConnect, &outlierStream, &healthCheckStream, ctx)
				state = CONNECTING
			}
		case CONNECTING:
			select {
			case <-ctx.Done():
				logger.Error("Context cancelled during connecting to manager")
			case event := <-chLogfileOutlier:
				logger.Debug("CONNECTING: got outlier event")
				addOutlierEvent(&activeAlarmsOutlier, event)
			case event := <-chLogfileHealthCheck:
				logger.Debug("CONNECTING: got hc event")
				addHealthCheckEvent(&activeAlarmsHealthCheck, event)
				rememberToSendHealthy(&rememberToSendHealthyList, event)
			case <-chConnect:
				logger.Info("Logfwdr is connected to the manager")
				successOutlierAlarms := sendOutlierActiveAlarms(activeAlarmsOutlier, outlierStream, envoyID)
				successHealthCheckAlarms := sendHealthCheckActiveAlarms(&activeAlarmsHealthCheck, healthCheckStream, envoyID)
				successSendHealthy := sendHealthy(&rememberToSendHealthyList, healthCheckStream, envoyID)
				if !successOutlierAlarms || !successHealthCheckAlarms || !successSendHealthy {
					logger.Error("Manager is not connected")
					if !successOutlierAlarms {
						logger.Error("Failed to send outlier alarms")
					}
					if !successHealthCheckAlarms {
						logger.Error("Failed to send healthcheck alarms")
					}
					if !successSendHealthy {
						logger.Error("Failed to send healthy alarms")
					}
					state = CONNECTING
					cancel()
					ctx, cancel = context.WithCancel(context.Background())
					go connectToManager(managerAddress, chConnect, &outlierStream, &healthCheckStream, ctx)
				} else {
					logger.Debug("All alarms successfully sent")
					state = CONNECTED
				}
			}
		case CONNECTED:
			select {
			case event := <-chLogfileOutlier:
				addOutlierEvent(&activeAlarmsOutlier, event)
				success := sendOutlierEvent(outlierStream, event, envoyID)
				logger.Debugf("Send outlier to manager success: %v", success)
				if !success {
					logger.Error("Manager is not connected")
					state = CONNECTING
					cancel()
					ctx, cancel = context.WithCancel(context.Background())
					go connectToManager(managerAddress, chConnect, &outlierStream, &healthCheckStream, ctx)
				}
			case event := <- chLogfileHealthCheck:
				addHealthCheckEvent(&activeAlarmsHealthCheck, event)
				success := sendHealthCheckEvent(healthCheckStream, event, envoyID)
				logger.Debugf("Send hc to manager success: %v", success)
				if !success {
					logger.Error("Manager is not connected")
					state = CONNECTING
					cancel()
					rememberToSendHealthy(&rememberToSendHealthyList, event)
					ctx, cancel = context.WithCancel(context.Background())
					go connectToManager(managerAddress, chConnect, &outlierStream, &healthCheckStream, ctx)
				}
			case e:=<-chConnect:
				if !e {
					logger.Debug("Got disconnect")
					cancel()
					state = DISCONNECTED
				}
			case <-ctx.Done():
				logger.Error("Root ctx was cancelled")
				state = DISCONNECTED
			}
		}
	}
}

func sendOutlierActiveAlarms(activeAlarms ActiveOutlierAlarms, managerStream OutlierLogStream, envoyID string) bool {
	logger.Info("Sending Outlier Active Alarms to Manager...")
	keys := reflect.ValueOf(activeAlarms).MapKeys()
	for i := 0; i < len(keys); i++ {
		event := activeAlarms[keys[i].String()]
		logger.Debugf("Sending od event %s", keys[i].String())
		success := sendOutlierEvent(managerStream, event, envoyID)
		logger.Debugf("Send outlier to manager success: %v", success)
		if !success {
			return false
		}
	}
	return true
}
func sendHealthCheckActiveAlarms(activeAlarms *HealthCheckAlarms, managerStream HealthCheckLogStream, envoyID string) bool {
	logger.Info("Sending HealthCheck Active Alarms to Manager...")
	for key, event := range *activeAlarms {
		logger.Debugf("Sending hc event %s", key)
		success := sendHealthCheckEvent(managerStream, event, envoyID)
		logger.Debugf("Send outlier to manager success: %v", success)
		if !success {
			return false
		}
	}
	return true
}
func sendHealthy(activeAlarms *HealthCheckAlarms, managerStream HealthCheckLogStream, envoyID string) bool {
	logger.Info("Sending Healthy Events to Manager...")
	for key, event := range *activeAlarms {
		logger.Debugf("Sending hc event %s", key)
		success := sendHealthCheckEvent(managerStream, event, envoyID)
		if success {
			logger.Debug("Sent healthy event")
			clearFromRememberToSendHealthy(activeAlarms, key)
		} else {
			logger.Debug("Failed to send healthy event")
			return false
		}
	}
	return true
}
//---------------------------------------------------------------------------------
// Attempts to load certificates, retrying in case of failure. Retries happen every 10
// sec, with a maximum of 60 sec
func loadTLSConfigWithRetries(certFile, keyFile, caFile string) (*tls.Config, error) {
	for tries :=0; tries < 6; tries++ {
		// Load the certificate and key
		cert, err := tls.LoadX509KeyPair(certFile, keyFile)
		if err != nil {
			logger.Errorf("Error loading TLS config: %v, retrying in 10 seconds", err)
			time.Sleep(10 * time.Second)
			continue
		}

		// Load the CA certificate
		caCert, err := ioutil.ReadFile(caFile)
		if err != nil {
			logger.Errorf("Error loading CA certificate: %v, retrying in 10 seconds", err)
			time.Sleep(10 * time.Second)
			continue
		}

		caCertPool := x509.NewCertPool()
		caCertPool.AppendCertsFromPEM(caCert)
		tlsConfig := &tls.Config{
		  Certificates: []tls.Certificate{cert},
		  RootCAs:      caCertPool,
		  MinVersion:   tls.VersionTLS12,
		  MaxVersion:   tls.VersionTLS13,
		}
		return tlsConfig, nil
	}
	return nil, fmt.Errorf("maximum retries exceeded") 
}
func isCertOrKeyOrCAFile(filename string) bool{
	return strings.HasSuffix(filename, ".pem") && (strings.Contains(filename, "cert") || strings.Contains(filename, "key") || strings.Contains(filename, "cacertbundle"))
} 
