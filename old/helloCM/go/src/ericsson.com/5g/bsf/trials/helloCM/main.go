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
package main

import (
 	"flag"
    "ericsson.com/5g/bsf/adp"
    "ericsson.com/5g/bsf/cm"
    "ericsson.com/5g/bsf/ah"
    "log"
    "time"
    "os"
)

func main() {
    log.Println("helloCM started....")

	runInteractive := flag.Bool("interactive", false, "run in interactive mode")
	flag.Parse()

	if *runInteractive {
		// just print the BSF config schema and say bye bye
	    log.Printf("JSON schema:\n%s\n", string(*cm.AsJsonSchema()))
	    return
	}

    var sf adp.SerivceFactory
    cp, err := sf.GetBsfConfProvider()
   	if (err != nil) {
		log.Fatalf("Couldn't get BSF configuration provider: %v\n", err)
	}

	go ahPolling()

    for {
    	log.Println("bsfConf.GetMainConf()...")
    	conf, err := cp.Get()

		if err != nil {
			log.Fatalf("Couldn't get configuration: %v\n", err)
		} else {
	    	log.Printf("Got %v.\n", conf)

	    	// update config
	    	conf.Meta.Touch++

	    	err = cp.Put(conf)
	    	if err != nil {
	    		log.Fatalf("Couldn't udpate configuration: %v\n", err)	
	    	}
		}

    	log.Printf("next poll in 15s...\n")
    	time.Sleep(15000 * time.Millisecond)
    }

    log.Println("helloCM stopped.")

	os.Exit(0)
}

func ahPolling() {
    var sf adp.SerivceFactory

    ac, err := sf.GetBsfAlarmCollector() 

    if (err != nil) {
        log.Fatalf("Couldn't get BSF alarm collector: %v\n", err)
    }

    for {
        log.Printf("test go routine execution. Next poll in 10s...\n")
        time.Sleep(10000 * time.Millisecond)
        log.Println("bsfAlarm.GetAll()...")
        alarmList, err2 := ac.GetAll()

        if err2 != nil {
            log.Fatalf("Couldn't get configuration: %v\n", err)
        } else {
            log.Printf("Got %v.\n", alarmList)
        }

        log.Println("bsfAlarm.Get()...")
        alarmList, err3 := ac.Get(&ah.ExampleQueryParameters)

        if err3 != nil {
            log.Fatalf("Couldn't get configuration: %v\n", err)
        } else {
            log.Printf("Got %v.\n", alarmList)
        }
    }
}

