# pmbrCounters2doc

This is a small script for generating documentation from PMBR files in scope of DND-29679.
As input it takes PMBR folders with JSON documents and generates:
 - `.md` file with mapping of internal PM counters to external representation
 - `.csv` file with jobs description
 - `.csv` file with metric description

.md output file is DITA compatible.

## Requirements

Python version >=3.6

## Usage

```
python main.py --args
```
With following arguments:
- '--format', **required**, sets desired output: md, csv, all
- '--sepp', path to sepp PMBR folder
- '--scp', path to scp PMBR folder
- '--bsf', path to bsf PMBR folder
- '--rlf', path to rlf PMBR folder
- '--nlf', path to nlf PMBR folder
- '--slf', path to slf PMBR folder

Example:
```
python main.py --format all --scp ./test/scp --sepp ./test/sepp --bsf ./test/bsf --rlf
python3 ./scripts/pmbrCounters2doc/main.py --format all --scp ./esc/scp/scpmgr/src/main/resources/pmbr/configs/ --sepp ./esc/sepp/seppmgr/src/main/resources/pmbr/configs/ --bsf ./esc/bsf/bsfmgr/src/main/resources/pmbr/configs/ --rlf ./esc/rlf/src/main/resources/pmbr/configs/ --nlf ./esc/nlf/src/main/resources/pmbr/configs/ 
````

## Structure

- main.py - main fail, entry point
- MdCreator.py - creates md document
- csvJobCreator - creates csv job file
- csvMetricCreator - creates csv metric file