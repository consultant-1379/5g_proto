# ===================================================================
# COPYRIGHT ERICSSON GMBH 2019
# 
# The copyright to the computer program(s) herein is the property
# of Ericsson GmbH, Germany.
# 
# The program(s) may be used and/or copied only with the written
# permission of Ericsson GmbH in accordance with
# the terms and conditions stipulated in the agreement/contract
# under which the program(s) have been supplied.
# 
# Created on: Nov 25, 2019
#     Author: eedstl
# ===================================================================
#
# Convert JSON formatted log-lines to a format that is easier to read
# for human beings.
#
# For example:
#
# {"version":"0.3.0","timestamp":"2019-11-22T14:12:28.718Z","service_id":"eric-scp-manager","severity":"info","message":"scpmgr[1]||main||c.e.esc.scp.manager.ScpManager||132||main||Starting SCP manager, version: {\"commit\":{\"id\":\"19ab462b673a09235be1ae66b94eb0c01448f6b0\"},\"build\":{\"time\":\"2019-11-22 14:09:45+00:00\"}}"}
#
# is converted to:
#
# 2019-11-22T14:12:28.718Z | INFO  | scpmgr[1] |                 main | c.e.esc.scp.manager.ScpManager |  132 |                           main | Starting SCP manager, version: {"commit":{"id":"19ab462b673a09235be1ae66b94eb0c01448f6b0"},"build":{"time":"2019-11-22 14:09:45+00:00"}}
#
# ===================================================================

def trim_right(width):
    tostring
    |
    if width > length
    then
        . + (width - length) * " "
    else
        .[0:width]
    end
;

def trim_left(width):
    tostring
    |
    if width > length
    then
        (width - length) * " " + .
    else
        .[length - width:length]
    end
;

def process:
    . as $line
    |
    try
    (
        fromjson
        |
        [
            (
                .timestamp
            ),
            (
                .severity | ascii_upcase | trim_right(5)
            ),
            (
                .message
                |
                split("||") as $a
                |
                if $a | length == 1
                then
                    [
                        (
                            $a[0]
                        )
                    ]
                else
                    [
                        (
                            $a[0],
                            (
                                $a[1] | trim_left(20)
                            ),
                            (
                                $a[2] | trim_left(30)
                            ),
                            (
                                $a[3] | trim_left(4)
                            ),
                            (
                                $a[4] | trim_left(30)
                            ),
                            $a[5]
                        )
                    ]
                end
                |
                join(" | ")
            )
        ]
        |
        join(" | ")
    )
    catch
        $line
;

process


