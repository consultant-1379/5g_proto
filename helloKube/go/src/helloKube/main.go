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
    "fmt"
    "github.com/julienschmidt/httprouter"
    "log"
    "net"
    "net/http"
    "os"
)

func indexHandler(w http.ResponseWriter, r *http.Request, _ httprouter.Params) {
    fmt.Fprintf(w, "This is helloKube running on: ")

    host, _ := os.Hostname()
    addrs, _ := net.LookupIP(host)
    first := true
    for _, addr := range addrs {
        if ipv4 := addr.To4(); ipv4 != nil {
            if first {
                first = false
            } else {
                fmt.Fprintf(w, ", ")
            }
            fmt.Fprintf(w, "", ipv4)
        }
    }
    fmt.Fprintf(w, ".\n\n")
    fmt.Fprintf(w, "Welcome to the world of Kubernetes!\n")
    fmt.Fprintf(w, "\n")
}

func main() {
    router := httprouter.New()
    router.GET("/", indexHandler)

    // print env
    env := os.Getenv("APP_ENV")
    if env == "production" {
        log.Println("helloKube running api server in production mode")
    } else {
        log.Println("helloKube api server in dev mode")
    }

    log.Fatal(http.ListenAndServe(":80", router))
}

