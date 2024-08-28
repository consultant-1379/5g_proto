#!/usr/bin/python3
# Pipe json-formatted logs into this script to get all fields in JSON format
# (the "message" contains a couple of fields that should really be JSON)
# eedala 2020-02-14

import json
import sys
import pprint
import re

# Read from stdin unbuffered:
# https://stackoverflow.com/questions/3670323/setting-smaller-buffer-size-for-sys-stdin
while True:
    line = sys.stdin.readline()
    if not line: break # EOF

    try:
        fields = json.loads(line)
        msg = str.split(fields['message'], '||')
        if len(msg) > 1:
            fields['severity'] = fields['severity'].upper()
            fields['process'] = msg[0]
            fields['thread'] = msg[1]
            fields['file'] = msg[2]
            fields['line'] = int(msg[3])
            fields['method'] = msg[4]
            fields['message'] = msg[5]
            # Print JSON-ified message
            sys.stdout.write(json.dumps(fields) + "\n")
        else:
            # Message isn't JSON-log from Java code (= no "||" in the message)
            sys.stdout.write(json.dumps(fields) + "\n")
    except:
        # JSON could not be decoded -> print line as it is:
        sys.stdout.write(f"{line}\n")
