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
 * Created on: Jun 19, 2018
 *     Author: eedsvs
 */
// Tools
package tools

 import (
    "strings"
    "runtime"
    "fmt"
 )

func ProgLocation() string {
    pc := make([]uintptr, 15)
    n := runtime.Callers(2, pc)
    frames := runtime.CallersFrames(pc[:n])
    frame, _ := frames.Next()

    var trimFilenameAt int = strings.Index(frame.File, "ericsson.com")
    if trimFilenameAt < 0 {
    	trimFilenameAt = 0
    }

    var trimFuncnameAt int = strings.LastIndex(frame.Function, "/") + 1
    if trimFuncnameAt > len(frame.Function) {
    	trimFuncnameAt = len(frame.Function)
    }

    return fmt.Sprintf("%s:%d %s", frame.File[trimFilenameAt:], frame.Line, frame.Function[trimFuncnameAt:])
}

