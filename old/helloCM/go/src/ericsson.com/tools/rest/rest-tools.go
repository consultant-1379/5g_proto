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
 * Created on: Jun 15, 2018
 *     Author: eedsvs
 */
// Simple REST i/f
package rest

import (
 	"fmt"
 	"bytes"
    "log"
    "io/ioutil"
    "net/http"
    "net/url"
    "ericsson.com/tools"
)

type Action struct {
	// REQUEST
	// used for logging, e.g. "create schema", "add config"
	BaseUrl			string
	Action 			string
	HttpMethod		string
	// such as 'schemas' or 'configurations' (see CM Mediator marketplace)
	Cmd 	 		string
	// might be nil, command's parm such as the name of the JSON schema
	Param			string

	//might be nil, query param, mainly used to filter GET response
	QueryParam      *map[string][]string

	// might be nil, MIME 'application/json' assumed
	Body 			*[]byte

	// RESPONSE
	// Considered OK
	PostiveResps	[]int
	// Considered NOTOK
	ExpectedResps 	[]int
	// Everything that does not fall in the above is considered FATAL
}

func (a Action) Do() (response *http.Response, body *[]byte, err error) {
	postiveResp := make(map[int]bool)
	for _ , pr := range a.PostiveResps {
		postiveResp[pr] = true
	}

	expectedResp := make(map[int]bool)
	for _ , er := range a.ExpectedResps {
		expectedResp[er] = true
	}

	// bulky if statement for performance reasons (avoid multiple string copies)
	var urlString string
	if len(a.Cmd) > 0 && len(a.Param) > 0 {
		urlString = a.BaseUrl + "/" + a.Cmd + "/" + a.Param
	} else if len(a.Cmd) > 0 {
		urlString = a.BaseUrl + "/" + a.Cmd
	} else {
		urlString = a.BaseUrl
	}
	
	log.Printf("\n")
	log.Printf(" *** %s using %s \"%s\"...\n", a.Action, a.HttpMethod, urlString)

	var req *http.Request

	if a.Body != nil  {
		req, err = http.NewRequest(a.HttpMethod, urlString, bytes.NewReader(*a.Body))
		req.Header.Set("Content-Type", "application/json")
	    log.Printf("body:\n%s\n", string(*a.Body))
	} else {
		req, err = http.NewRequest(a.HttpMethod, urlString, nil)
	}

    if err != nil {
        log.Fatalf("%s: %v\n", tools.ProgLocation(), err)
        return nil, nil, err
    }

    if a.QueryParam != nil {
    	q:= url.Values{}
    	for key, value := range *a.QueryParam {
    		for _, valuePart := range value{
    			q.Add(key, valuePart)
    			log.Printf("Added query pair %s:%s\n", key,valuePart)
    		}
    	}
    	req.URL.RawQuery = q.Encode()
    	log.Printf("Final query: %s\n", q.Encode())
    }

    log.Printf("\n")
    log.Printf("Final request URL: %s\n", req.URL.String())

    // send HTTP response now
    client := http.Client{}
    resp, err := client.Do(req)

    if err != nil {
        return nil, nil, fmt.Errorf("%s %v\n", a.Action, err)
    }
    defer resp.Body.Close()

    var rbStr string

    rb, err := ioutil.ReadAll(resp.Body)
    if err != nil {
    	log.Fatalf("Error during body read: %v!\n", err)
    	return nil, nil, fmt.Errorf("FAILED %s. %s/%s (reading response) %v",
    		a.Action, a.Cmd, a.Param, err)
    }

   	rbStr = string(rb)

    log.Printf("Service response (%d):  %s\n", resp.StatusCode, rbStr)

    if postiveResp[resp.StatusCode] {
    	log.Printf("OK %s. %s/%s\n", a.Action, a.Cmd, a.Param)
    } else {
    	log.Printf("FAILED %s. %s/%s\n", a.Action, a.Cmd, a.Param)

    	if expectedResp[resp.StatusCode] {
	    	return nil, nil, fmt.Errorf("NOT OK %s. %s/%s (http resp %d):\n%s\n",
    		a.Action, a.Cmd, a.Param, resp.StatusCode, rbStr)
	    } else {
	    	return nil, nil, fmt.Errorf("FAILED %s. %s/%s (unexpected http resp %d):\n%s\n",
    		a.Action, a.Cmd, a.Param, resp.StatusCode, rbStr)
	    }
	}

    return resp, &rb, nil
}

