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
 * Created on: Jun 10, 2018
 *     Author: eedsvs
 */
// First DSC Evo prototpye showing HELM deployment of some Kubernetes application
package adp


import (
 	"ericsson.com/5g/bsf/cm"
 	"ericsson.com/5g/bsf/ah"
 	)

type SerivceFactory struct {}

func (a SerivceFactory) GetBsfConfProvider() (cm.BsfConfProvider, error) {
	return cm.NewBsfConfProviderCmImpl()
}


func (a SerivceFactory) GetBsfAlarmCollector() (ah.BsfAlarmCollector, error) {
	return ah.NewBsfAlarmCollectorAhImpl()
} 
