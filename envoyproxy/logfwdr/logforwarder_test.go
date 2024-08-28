package main


import (
	"testing"
	//"fmt"
	"runtime"
	"time"
	log "github.com/sirupsen/logrus"
	envoy_core "github.com/envoyproxy/go-control-plane/envoy/data/core/v3"
)

const TestFileNameHc = "hc.log"
const TestFileNameBothLogs = "both.log"

func logSetUp() {
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
	log.SetLevel(log.DebugLevel)
	log.SetReportCaller(true)
    // Always include the "version" and the "service_id" in each log message:
    logger = log.WithFields(log.Fields{"version": "1.1.0", "service_id": serviceId})
}

func TestReadLogLineHealthCheck(t *testing.T) {
	logSetUp()
	timeout := 1 * time.Second

	chLogfileHealthCheck := make(chan HealthCheckEvent)
	chLogfileOutlier := make(chan OutlierEvent)

	go readLogLine(TestFileNameHc, chLogfileOutlier, chLogfileHealthCheck)

	received := 0
	
	for {
		select {
		case event := <- chLogfileHealthCheck:
			received ++
			logger.Debugf("Got Event: %d", received)
	
			if event.CheckerType != "HTTP" {
				t.Errorf("Expected HTTP, but got %s", event.CheckerType)
			}
			if event.Address.SocketAddress.Address != "100.78.181.184" {
				t.Errorf("Wrong Address: %s", event.Address.SocketAddress.Address)
			}
			if event.HealthCheckEjectUnhealthy.FailureType != "ACTIVE" {
				t.Errorf("Wrong FailureType: %s", event.HealthCheckEjectUnhealthy.FailureType)
			}
		case <-time.After(timeout):
			logger.Debugf("Number of hc lines: %d", received)
			if received < 490 {
				t.Errorf("TimeOut")
				return
			} else {
				return
			}
		}
	}
}

func TestAddHealthCheckEvent(t *testing.T) {
	logSetUp()
	timeout := 1 * time.Second
	activeAlarmsHealthCheck := make(HealthCheckAlarms)

	chLogfileHealthCheck := make(chan HealthCheckEvent)
	chLogfileOutlier := make(chan OutlierEvent)

	go readLogLine(TestFileNameHc, chLogfileOutlier, chLogfileHealthCheck)

	select {
	case event := <- chLogfileHealthCheck:
		logger.Debugf("Got Event")
		addHealthCheckEvent(&activeAlarmsHealthCheck, event)
		if len(activeAlarmsHealthCheck) != 1 {
			t.Errorf("Wrong length")
		}
		for key, _ := range activeAlarmsHealthCheck {
			if key != "Pool_NfAmf.100.78.181.184:8290" {
				t.Errorf("Wrong key: %s", key)
				break // Exit the loop after the first iteration
			}
		}
	case <-time.After(timeout):
		t.Errorf("TimeOut")
		return
	}
}

func TestCreateCoreEvent(t *testing.T) {
	logSetUp()
	timeout := 1 * time.Second

	chLogfileHealthCheck := make(chan HealthCheckEvent)
	chLogfileOutlier := make(chan OutlierEvent)

	go readLogLine(TestFileNameHc, chLogfileOutlier, chLogfileHealthCheck)

	select {
	case event := <- chLogfileHealthCheck:
		logger.Debugf("Got Event")
		eventDetails := createCoreEvent(event)
		if eventDetails.GetClusterName() != "Pool_NfAmf" {
			t.Errorf("Wrong ClusterName: %s", eventDetails.ClusterName)
			return
		}
		if eventDetails.GetHealthCheckFailureEvent().GetFailureType() != envoy_core.HealthCheckFailureType_ACTIVE {
			t.Errorf("Wrong FailureType: %s", event.HealthCheckEjectUnhealthy.FailureType)
		}
	case <-time.After(timeout):
		t.Errorf("TimeOut")
		return
	}
}

func TestBothLogs(t *testing.T){
	logSetUp()
	timeout := 1 * time.Second

	chLogfileHealthCheck := make(chan HealthCheckEvent)
	chLogfileOutlier := make(chan OutlierEvent)

	go readLogLine(TestFileNameBothLogs, chLogfileOutlier, chLogfileHealthCheck)

	outlierLines := 0
	hcLines := 0

	for {
		select {
		case event := <- chLogfileHealthCheck:
			hcLines++
			logger.Debugf("Got Event HealthCheck: %d", hcLines)
	
			if event.CheckerType != "HTTP" {
				t.Errorf("Expected HTTP, but got %s", event.CheckerType)
			}
			if event.Address.SocketAddress.Address != "100.78.181.184" {
				t.Errorf("Wrong Address: %s", event.Address.SocketAddress.Address)
			}
			if event.HealthCheckEjectUnhealthy.FailureType != "ACTIVE" {
				t.Errorf("Wrong FailureType: %s", event.HealthCheckEjectUnhealthy.FailureType)
			}
		case event := <-chLogfileOutlier:
			outlierLines++
			logger.Debugf("Got Event Outlier: %d", outlierLines)
			if event.ClusterName != "Pool_NfAmf" {
				t.Errorf("Wrong ClusterName: %s", event.ClusterName)
			}
		case <-time.After(timeout):
			logger.Debugf("Number of hc lines: %d", hcLines)
			logger.Debugf("Number of outlier lines: %d", outlierLines)
			if hcLines < 19 || outlierLines < 6 {
				t.Errorf("TimeOut")
				return
			}
			return
		}
	}
}

func TestDoWeNeedThis(t *testing.T) {
	logSetUp()
	// Test case 1: event.HealthCheckEjectUnhealthy is not nil
	event1 := &HealthCheckEvent{
		HealthCheckEjectUnhealthy: &HealthCheckEjectUnhealthy{},
	}
	expected1 := true
	result1 := doWeNeedThis(event1)
	if result1 != expected1 {
		t.Errorf("Test case 1 failed. Expected %v, but got %v", expected1, result1)
	}

	// Test case 2: event.HealthCheckAddHealthy is not nil
	event2 := &HealthCheckEvent{
		HealthCheckAddHealthy: &HealthCheckAddHealthy{},
	}
	expected2 := true
	result2 := doWeNeedThis(event2)
	if result2 != expected2 {
		t.Errorf("Test case 2 failed. Expected %v, but got %v", expected2, result2)
	}

	// Test case 3: event.HealthCheckFailure is not nil and FirstCheck is true
	event3 := &HealthCheckEvent{
		HealthCheckFailure: &HealthCheckFailure{
			FirstCheck: true,
		},
	}
	expected3 := true
	result3 := doWeNeedThis(event3)
	if result3 != expected3 {
		t.Errorf("Test case 3 failed. Expected %v, but got %v", expected3, result3)
	}

	// Test case 4: all conditions are false
	event4 := &HealthCheckEvent{}
	expected4 := false
	result4 := doWeNeedThis(event4)
	if result4 != expected4 {
		t.Errorf("Test case 4 failed. Expected %v, but got %v", expected4, result4)
	}
}

func TestAddHealthCheckEvent2(t *testing.T) {
	logSetUp()
	// Test case 1
	alarms := make(HealthCheckAlarms)
	event1 := HealthCheckEvent{
		ClusterName:        "cluster1",
		Address: Host{
			SocketAddress: SocketAddress{
				Address:    "localhost",
				PortValue:  8080,
			},
		},
		HealthCheckAddHealthy: &HealthCheckAddHealthy{},
	}
	addHealthCheckEvent(&alarms, event1)
	if _, ok := alarms["cluster1.localhost:8080"]; ok {
		t.Errorf("Test case 1 failed: expected key to be deleted from alarms map")
	}

	// Test case 2
	event2 := HealthCheckEvent{
		ClusterName:        "cluster2",
		Address: Host{
			SocketAddress: SocketAddress{
				Address:    "localhost",
				PortValue:  8081,
			},
		},
	}
	addHealthCheckEvent(&alarms, event2)
	if _, ok := alarms["cluster2.localhost:8081"]; !ok {
		t.Errorf("Test case 2 failed: expected event to be added to alarms map")
	}
}