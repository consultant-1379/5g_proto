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
    "log"
    "net/http"
    "io/ioutil"
    "time"
)

func main() {
    log.Println("helloADP running api server in production mode")

    url := "http://log:5080/log/api/v1/health"

    for {
        log.Println("Check log serivce's health...")
        log.Println("    esp, err := http.Get(\"%s\")", url)

        resp, err := http.Get(url)
        if err != nil {
            log.Println("    fetch: %v\n", err)
        } else {
            log.Println("    b, err := ioutil.ReadAll(resp.Body)")
            b, err := ioutil.ReadAll(resp.Body)
            log.Println("    fresp.Body.Close()")
            resp.Body.Close()

            if err != nil {
                log.Println("    fetch: reading %s: %v\n", url, err)
            }
            log.Println("%s\n", string(b))
        }

        log.Println("Check log serivce's health... DONE.")

        log.Println("Next poll in 2s...")
        time.Sleep(2000 * time.Millisecond);
    }
}