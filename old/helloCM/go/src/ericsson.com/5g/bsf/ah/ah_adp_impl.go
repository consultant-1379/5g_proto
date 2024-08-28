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

import (
 	"fmt"
    "log"
    "encoding/json"
    "net/http"
    "ericsson.com/tools/rest"
    "strings"
    "ericsson.com/tools"
)

const (
	ahAdpImpBaseUrl = "http://eric-fh-alarm-handler:5005/ah/api/v0.2"
)

// The bsfAlarmCollectorAhImpl "class", implements ah.BsfAlarmCollector
type bsfAlarmCollectorAhImpl struct {
//	Data 	*BsfAlarmQueryParameters 	`json:"data"`
	QueryMap *map[string][]string 
	AlarmList *[]BsfAlarm
}

// "constructor"
func NewBsfAlarmCollectorAhImpl() (*bsfAlarmCollectorAhImpl, error) {
	b := bsfAlarmCollectorAhImpl{&map[string][]string{},&EmptyAlarmList}

	return &b, nil
}

func (b bsfAlarmCollectorAhImpl) prepareQueryParamMap(newQueryParams *BsfAlarmQueryParameters) (error) {
	var objmap map[string]*json.RawMessage
	
	paramJson, _:= json.Marshal(newQueryParams)
	log.Printf("These are the query parameters in JSON format:\n %s\n", string(paramJson))
	err := json.Unmarshal(paramJson, &objmap)

	if err!=nil {
		log.Fatalf("%s: %v\n", tools.ProgLocation(), err)
		return err
	}

	log.Printf("This is the query parameter map:\n")
	queryMap := make(map[string][]string)
	for i, j := range objmap {
		s, err := json.Marshal(j)
    		if err != nil {
   		     	log.Fatalf("%s: %v\n", tools.ProgLocation(), err)
				return err
	        }
		trimmed := strings.Trim(string(s), "[]")
		queryMap[i]=strings.Split(trimmed, ",")
		log.Printf("%s: ", i)
		for index, value := range queryMap[i]{
			queryMap[i][index]=strings.Trim(value, "\"")
			log.Printf("%s ", queryMap[i][index])
		}
		log.Printf("\n")
	}

	*b.QueryMap = queryMap

	return nil
}


func (b bsfAlarmCollectorAhImpl) GetAll() (*[]BsfAlarm, error) {

	*b.QueryMap = map[string][]string{}
	err := b.updateAlarmList()
	if err != nil {
		return nil, nil
	}

	return b.AlarmList, nil
}

func (b bsfAlarmCollectorAhImpl) Get(newQueryParams *BsfAlarmQueryParameters) (*[]BsfAlarm, error) {
	
	err1 := b.prepareQueryParamMap(newQueryParams)
	if err1 != nil {
		return nil, nil
	}

	err2 := b.updateAlarmList()
	if err2 != nil {
		return nil, nil
	}

	return b.AlarmList, nil
}


func (b bsfAlarmCollectorAhImpl) updateAlarmList() error {

	_, body, err := rest.Action{
		ahAdpImpBaseUrl,
		"reading alarm list", http.MethodGet, "alarms", "", b.QueryMap,
		nil,
		[]int{http.StatusOK},
		[]int{http.StatusBadRequest}}.Do()
	if err != nil {
		return err
	}

	var updatedAlarmList []BsfAlarm
	err = json.Unmarshal(*body, &updatedAlarmList)
	if err != nil {
    	return fmt.Errorf("unmarshaling response %v", err)
	}

	*b.AlarmList = updatedAlarmList

	if len(*b.AlarmList)==0 {
		log.Printf("Alarm list came back empty")
	}

	return nil
}