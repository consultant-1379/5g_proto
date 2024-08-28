// vim:ts=4:sw=4
/*
 * COPYRIGHT ERICSSON GMBH 2018
 *
 * The copyright to the computer program(s) herein is the property
 * of Ericsson GmbH, Germany.
 *
 * The program(s) may be used and/or copied only with the written
 * permission of Ericsson GmbH in accordance with
 * the terms and conditions stipulated in the agreement/contract
 * under which the program(s) have been supplied.
 *
 * Created on: Jun 29, 2018
 *     Author: xkorpap
 */
// Alarm Collector for BSF
package ah

const (
	ExampleServiceName string = "service1"
)


type BsfAlarmQueryParameters struct {
	AlarmName []string `json:"alarmName,omitempty"`
	ServiceName []string	`json:"serviceName,omitempty"`
	FaultyResource []string `json:"faultyResource,omitempty"`
	Severity []string `json:"severity,omitempty"`
	MaxReturn int `json:"maxReturn,omitempty"`
	EndTime int `json:"endTime,omitempty"`
	StartTime int `json:"startTime,omitempty"`
	SearchHistory bool `json:"searchHistory,omitempty"`
	TimeZone string `json:"timeZone,omitempty"`
	PrettyPrint bool `json:"pp,omitempty"`
	OutputFormat string `json:"outputFormat,omitempty"`
}

type BsfAlarm struct {
	AlarmName string `json:"alarmName"`
	ServiceName string	`json:"serviceName"`
	FaultyResource string `json:"faultyResource,omitempty"`
	Severity string `json:"severity"`
	Description string `json:"description,omitempty"`
	EventTime string `json:"eventTime"`
	AdditionalInformation []byte `json:"additionalInformation,omitempty"`
	Expires int `json:"expires,omitempty"`
	Vendor int `json:"vendor,omitempty"`
	Code int `json:"code,omitempty"`
	Category string `json:"category,omitempty"`
	ProbableCause string `json:"probableCause,omitempty"`
	History AlarmHistory `json:"history,omitempty"`
}

type AlarmHistory struct {
	EventTime string `json:"eventTime"`
	Description string `json:"description,omitempty"`
	AdditionalInformation []byte `json:"additionalInformation,omitempty"`
	Severity string `json:"severity,omitempty"`
}

// BsfConfProvider is the CM interface from BSF perspective, i.e. providing all config data fucntions needed by BSF
type BsfAlarmCollector interface {
	Get(newQueryParams *BsfAlarmQueryParameters) (*[]BsfAlarm, error)
	GetAll() (*[]BsfAlarm, error)
}


var ExampleQueryParameters BsfAlarmQueryParameters = BsfAlarmQueryParameters {
	AlarmName: []string{"alarm"},
	ServiceName: []string{ExampleServiceName, "service2"},
	FaultyResource: nil,
	Severity: nil,
	MaxReturn: 0,
	StartTime: 0,
	SearchHistory: false,
	TimeZone: "Europe/Stockholm",
	PrettyPrint: false,
	OutputFormat: "",
}

var EmptyAlarmList []BsfAlarm = []BsfAlarm{}
