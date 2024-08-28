# Values.yaml and VNF Descriptor Check Scripts

Tiny set of commands to check consistency of all relevant `values.yaml` files, the `sc_vnf_descriptor.yaml` file and the customer values file `eric-sc-values.yaml` against the parameter documentation shipped with our CPI: `1_19059-csh109710_1uen.xlsx`.

## When do I want to run one of the commands?
In case you need to know if all CPI documented parameters and their default values (1_19059-csh109710_1uen.xlsx) are correctly reflected in all relevant `values.yaml` files, the `sc_vnf_descriptor.yaml` file or, most importantly, in the customer values file `eric-sc-values.yaml`! Note that all checks are done in either direction, that is there is no single source of truth presumed. 


## Available Commands
For ease of use there are two commands tailored for the standard use cases:
- `checkCustomerValues` Check consistency between the Excel workbook `1_19059-csh109710_1uen.xlsx` and the customer values file `eric-sc-values.yaml`. The command reports all parameters claimed to be mandatory in the workbook but do not exist in the customer values file (ERROR) and, vise versa, parameters present in the customer values file that are not documented in the workbook (ERROR) or not marked mandatory in the workbook (WARNING). Besides, default values are compared for equality for parameters that are present on both sides (WARNING).
-  `checkVnfDescriptor` Check consistency between the Excel workbook `1_19059-csh109710_1uen.xlsx` and the VNF descriptor `sc_vnf_descriptor.yaml`. Same behavior as for `checkCustomerValues` but for the VNF descriptor `sc_vnf_descriptor.yaml`.
-  `checkAllCharts` Check consistency between the Excel workbook `1_19059-csh109710_1uen.xlsx` and all release relevant `values.yaml` files.

Besides the convenience commands `checkCustomerValues`, `checkVnfDescriptor`, and `checkAllCharts` the following command allows the explicit definition of all input files and other details.

- `checkParams` Same as above allowing customizing which `values.yaml` files to consider and where to find them, where to find the parameter Excel workbook and which sheets of the workbook to be checked plus more details (see Usage `checkParams` below).


## Requirements

Python versions 3.x.

Certain Python libs might need to be loaded in your environment through the `pip` command:
- `pandas` Excel parsing
- `yaml` YAML parsing
- `argparse` CLI parameter handling


## Convenience Commands `checkCustomerValues`, `checkVnfDescriptor`, and `checkAllCharts`

All input files are ==relative to this directory== such that you don't have to care about details other than change dir to `script/sc_param_check` prior to invoking the commands. All three commands `checkCustomerValues`, `checkVnfDescriptor`, and `checkAllCharts` can be run without CLI parameters except for the CPI Excel workbook `1_19059-csh109710_1uen.xlsx` that shall be verified.

For convenience an example workbook (`1_19059-csh109710_1uen_EXAMPLE.xlsx`) is located in the same directory to perform example runs.


### Usage

```
./checkCustomerValues WORKBOOK
```

```
./checkVnfDescriptor WORKBOOK
```

```
./checkAllCharts WORKBOOK
```

### Example Calls
```
./checkCustomerValues 1_19059-csh109710_1uen_EXAMPLE.xlsx
```

```
./checkVnfDescriptor 1_19059-csh109710_1uen_EXAMPLE.xlsx
```

```
./checkAllCharts 1_19059-csh109710_1uen_EXAMPLE.xlsx
```

### What pre-defined input do the convenience commands check?

- Common path to all charts: `../../esc`
- Customer values file `release_artifacts/eric-sc-values.yaml` (`checkCustomerValues` only)
- Charts (`checkVnfDescriptor` and `checkAllCharts` only):
    - `helm/eric-sc-umbrella/Chart.yaml` (treated as umbrella chart = no prefixes in Excel sheets)
    - eric-sc-nlf: `nlf/helm/Chart.yaml`
    - eric-scp: `scp/helm/Chart.yaml`
    - eric-sc-rlf: `rlf/helm/Chart.yaml`
    - eric-sc-slf: `slf/helm/Chart.yaml`
    - eric-bsf: `bsf/helm/Chart.yaml`
    - eric-bsf-diameter: `bsf/bsfdiameter/helm/Chart.yaml`
    - eric-sepp: `sepp/helm/Chart.yaml`
    - eric-sc-monitor: `monitor/helm/Chart.yaml`
- Sheets (of given Excel workbook):
    - Main
    - SC Common
    - SCP
    - SEPP
    - BSF
    - Generic

- VNF descriptor: `../../esc/release_artifacts/sc_vnf_descriptor.yaml` (`checkVnfDescriptor` only)

## Common Errors and Warnings

Note that `>` and `<` indicate the direction of prameter mismatches simlar to the UNIX `diff` command. In that sense YAML files (`value.yaml` files or VNF descriptor) are "on the left" while the CPI Excel workbook is "on the right": Thus a `>` indicates a potential missing item in the sheet, `<` a potential missing item in YAML:

#### ERROR: Sheet parameter missing in values.yaml files or VNF descriptor
```
<   ERROR: Sheet param global.nodeSelector (Main/No.4) missing in any given YAML (best prefix match 'global.' found in eric-sc-umbrella)!
...
<   ERROR Mandatory global.eircsson.licensing.licenseDomains in sheet Generic/No.122 not present in VNF descriptor.
```

#### WARNING: Default value mismatch between sheet and values.yaml file
```
< WARNING: Sheet param global.externalIPv4.enabled (Main/No.5) claims 'default: true' not complying to 'false' in eric-sc-umbrella!
```

#### WARNING Syntax error in sheet's default value
Warning to indicate that a check for equality cannot be performed.

Note that the below error example is apparently a common problem in our sheet, which stems from the fact that indentations like the below are forbidden in YAML. The error can be corrected by indenting the mapping values one character to the left, that is `operator`, `effect` and `tolerationSeconds` should align with `key` in the example below.

```
< WARNING: Cannot check default value of param eric-scp.spec.manager.tolerations! Syntax error found in sheet SCP/No.5/Col."Value":
tolerations:
- key: node.kubernetes.io/not-ready
   operator: Exists
   effect: NoExecute
   tolerationSeconds: 0
- key: node.kubernetes.io/unreachable
   operator: Exists
   effect: NoExecute
   tolerationSeconds: 0
-------------------------
Error was:
mapping values are not allowed here
  in "<unicode string>", line 3, column 12:
       operator: Exists
               ^
-------------------------
```

#### ERROR: YAML parameter missing in sheet
```
>   ERROR: YAML param eric-data-wide-column-database-cd.cassandra.yaml.num_tokens missing in workbook/sheets!
```

#### WARNING: VNF parameter not marked as mandatory in sheet
```
> WARNING eric-scp.service.worker.externalIPv4.enabled not marked as mandatory in sheet SCP/No.19.
```

## Explicit Command `checkParam`

In contrast to `checkCustomerValues`, `checkVnfDescriptor`, and `checkAllCharts` `checkParam` allows to define all input to be checked explicitly.

### Usage
```
$ ./checkParams --help
python3 scParamCheck.py --help
usage: scParamCheck.py [-h] [-c {sheets,yamls,vnfd,all}] [--skip] [--debug] [-v VNF_DESCRIPTOR] WORKBOOK SHEETS CHARTS_PATH CHARTS

Compares the day-0 parameters documented in the SC day-0 param Excel workbook against values.yaml files and the VNF descriptor.

positional arguments:
  WORKBOOK              Name of input Excel workbook.
  SHEETS                Comma separated list of workbook sheets to check.
  CHARTS_PATH           Common path to all input Chart.yaml files.
  CHARTS                Comma separated list of Chart.yaml files starting with the umbrella chart.

options:
  -h, --help            show this help message and exit
  -c {sheets,yamls,vnfd,all}, --check {sheets,yamls,vnfd,all}
                        Use to run selected checks. Default: all
  --skip                Skip checking correctness of default values (Columns "Values" in workbook)
  --debug               Debug mode
  -v VNF_DESCRIPTOR, --vnf-descriptor VNF_DESCRIPTOR
                        Path to VNF descriptor. If present check mandatory workbook parameters.
```

### Example Call

Note that blanks need to be quoted twice, that is a single blank ` ` needs to be written as `\\\ ` as in `SC\\\ Common` below constituting the sheet name "SC Common" of `1_19059-csh109710_1uen_EXAMPLE.xlsx`:

```
$ ./checkParams 1_19059-csh109710_1uen_EXAMPLE.xlsx --check vnfd                    \
    --vnf-descriptor ../../esc/release_artifacts/sc_vnf_descriptor.yaml             \
    Main,SC\\\ Common,SCP,SEPP,BSF,Generic                                          \
    ../../esc                                                                       \
    helm/eric-sc-umbrella/Chart.yaml,nlf/helm/Chart.yaml,scp/helm/Chart.yaml,rlf/helm/Chart.yaml,slf/helm/Chart.yaml,bsf/helm/Chart.yaml,bsf/bsfdiameter/helm/Chart.yaml,sepp/helm/Chart.yaml,monitor/helm/Chart.yaml
```

## Structure
- `1_19059-csh109710_1uen_EXAMPLE.xlsx` - Example SC 1.12 parameter Excel workbook added for convenience and quick checks.
- `README.md` - This readme.
- `checkAllCharts` - Command as described above (implemented as softlink to `scParamCheck.sh`).
- `checkCustomerValues` - Command as described above (implemented as softlink to `scParamCheck.sh`).
- `checkParams` - Command as described above (implemented as softlink to `scParamCheck.sh`).
- `checkVnfDescriptor` - Command as described above (implemented as softlink to `scParamCheck.sh`).
- `customerValueCheck/` - Directory comprising dummy `Chart.yaml` and softlink to `eric-sc-values.yaml`. Hack to convince current version of `scParamCheck.py`, which insists on the existence of a `Chart.yaml`, to perform a check on customer values.
- `scParamCheck.py` - The actual implementing Python script.
- `scParamCheck.sh` - BASH script wrapper of `scParamCheck.py`.


## Misc
If you call the convenience bash wrapper `scParamCheck.sh` explicitly you'll get a quick help as well. 

```
$ ./scParamCheck.sh
usage: checkParams [-h] ...
usage: checkCustomerValues WORKBOOK
usage: checkAllCharts WORKBOOK
usage: checkVnfDescriptor WORKBOOK

scParamCheck.sh is not supposed to be called directly but rather through softlinks:
  checkParams           Calls scParamCheck.py without any tailored parameters. -h for help.
  checkCustomerValues   Compares given Excel workbook against eric-sc-values.yaml.
  checkAllCharts        Compares given Excel workbook against all SC chars in ../../esc.
  checkVnfDescriptor    Compares given Excel workbook against VNF descriptor ../../esc/release_artifacts/sc_vnf_descriptor.yaml.

  Call above commands with -h or --help for more details.
```

## Example Runs
```
$ ./checkCustomerValues 1_19059-csh109710_1uen_EXAMPLE.xlsx
python3 scParamCheck.py 1_19059-csh109710_1uen_EXAMPLE.xlsx --check yamls Main,SC\ Common,SCP,SEPP,BSF,Generic customerValueCheck/ Chart.yaml
Sheets:
[1] 1_19059-csh109710_1uen_EXAMPLE.xlsx:Main
[2] 1_19059-csh109710_1uen_EXAMPLE.xlsx:SC Common
[3] 1_19059-csh109710_1uen_EXAMPLE.xlsx:SCP
[4] 1_19059-csh109710_1uen_EXAMPLE.xlsx:SEPP
[5] 1_19059-csh109710_1uen_EXAMPLE.xlsx:BSF
[6] 1_19059-csh109710_1uen_EXAMPLE.xlsx:Generic

Charts:
[1] eric-sc-custom-values:customerValueCheck/values.yaml.
Umbrella chart: "eric-sc-custom-values"

>   ERROR: YAML param definitions.VIP_OAM missing in workbook/sheets!
>   ERROR: YAML param definitions.VIP_SIG_SCP missing in workbook/sheets!
>   ERROR: YAML param definitions.VIP_SIG2_SCP missing in workbook/sheets!
>   ERROR: YAML param definitions.VIP_SIG_SEPP missing in workbook/sheets!
>   ERROR: YAML param definitions.VIP_SIG2_SEPP missing in workbook/sheets!
>   ERROR: YAML param definitions.VIP_SIG_BSF missing in workbook/sheets!
>   ERROR: YAML param definitions.VIP_SIG_Diameter missing in workbook/sheets!
>   ERROR: YAML param definitions.oam_storage_class missing in workbook/sheets!
>   ERROR: YAML param definitions.shared_vip_oam_label missing in workbook/sheets!
>   ERROR: YAML param global.ipFamilyPolicy missing in workbook/sheets!
>   ERROR: YAML param global.ericsson.bsfdiameter.enabled missing in workbook/sheets!
>   ERROR: YAML param global.ericsson.nlf.enabled missing in workbook/sheets!
>   ERROR: YAML param global.ericsson.licensing.licenseDomains missing in workbook/sheets!
>   ERROR: YAML param eric-scp.service.worker.annotations.loadBalancerIPs missing in workbook/sheets!
>   ERROR: YAML param eric-scp.service.worker.multiVpn.annotations.loadBalancerIPs missing in workbook/sheets!
>   ERROR: YAML param eric-scp.spec.worker.send_goaway_for_premature_rst_streams missing in workbook/sheets!
>   ERROR: YAML param eric-scp.spec.worker.premature_reset_total_stream_count missing in workbook/sheets!
>   ERROR: YAML param eric-scp.spec.worker.premature_reset_min_stream_lifetime_seconds missing in workbook/sheets!
>   ERROR: YAML param eric-scp.spec.worker.max_requests_per_io_cycle missing in workbook/sheets!
>   ERROR: YAML param eric-bsf.service.worker.annotations.loadBalancerIPs missing in workbook/sheets!
>   ERROR: YAML param eric-sepp.service.worker.annotations.loadBalancerIPs missing in workbook/sheets!
>   ERROR: YAML param eric-sepp.service.worker.multiVpn.annotations.loadBalancerIPs missing in workbook/sheets!
>   ERROR: YAML param eric-sepp.spec.worker.send_goaway_for_premature_rst_streams missing in workbook/sheets!
>   ERROR: YAML param eric-sepp.spec.worker.premature_reset_total_stream_count missing in workbook/sheets!
>   ERROR: YAML param eric-sepp.spec.worker.premature_reset_min_stream_lifetime_seconds missing in workbook/sheets!
>   ERROR: YAML param eric-sepp.spec.worker.max_requests_per_io_cycle missing in workbook/sheets!
>   ERROR: YAML param eric-tm-ingress-controller-cr.service.loadBalancerIP missing in workbook/sheets!
>   ERROR: YAML param eric-data-object-storage-mn.persistence.persistentVolumeClaim.storageClassName missing in workbook/sheets!
>   ERROR: YAML param eric-data-sftp-server.service.annotations.sharedVIPLabel missing in workbook/sheets!
29 errors/0 warnings.
```

```
$ ./checkVnfDescriptor /mnt/c/Users/eedsvs/Documents/scParamChecker_NEW/1_19059-csh109710_1uen.xlsx
python3 scParamCheck.py /mnt/c/Users/eedsvs/Documents/scParamChecker_NEW/1_19059-csh109710_1uen.xlsx --check vnfd --vnf-descriptor ../../esc/release_artifacts/sc_vnf_descriptor.yaml Main,SC\ Common,SCP,SEPP,BSF,Generic ../../esc helm/eric-sc-umbrella/Chart.yaml,nlf/helm/Chart.yaml,scp/helm/Chart.yaml,rlf/helm/Chart.yaml,slf/helm/Chart.yaml,bsf/helm/Chart.yaml,bsf/bsfdiameter/helm/Chart.yaml,sepp/helm/Chart.yaml,monitor/helm/Chart.yaml
Sheets:
[1] 1_19059-csh109710_1uen_EXAMPLE.xlsx:Main
[2] 1_19059-csh109710_1uen_EXAMPLE.xlsx:SC Common
[3] 1_19059-csh109710_1uen_EXAMPLE.xlsx:SCP
[4] 1_19059-csh109710_1uen_EXAMPLE.xlsx:SEPP
[5] 1_19059-csh109710_1uen_EXAMPLE.xlsx:BSF
[6] 1_19059-csh109710_1uen_EXAMPLE.xlsx:Generic

Charts:
[1] eric-sc-umbrella:../../esc/helm/eric-sc-umbrella/values.yaml.
[2] eric-sc-nlf:../../esc/nlf/helm/values.yaml.
[3] eric-scp:../../esc/scp/helm/values.yaml.
[4] eric-sc-rlf:../../esc/rlf/helm/values.yaml.
[5] eric-sc-slf:../../esc/slf/helm/values.yaml.
[6] eric-bsf:../../esc/bsf/helm/values.yaml.
[7] eric-bsf-diameter:../../esc/bsf/bsfdiameter/helm/values.yaml.
[8] eric-sepp:../../esc/sepp/helm/values.yaml.
[9] eric-sc-monitor:../../esc/monitor/helm/values.yaml.
Umbrella chart: "eric-sc-umbrella"

>   ERROR global.licensing.sites not present in workbook.
> WARNING global.externalIPv4.enabled not marked as mandatory in sheet Main/No.5.
> WARNING global.externalIPv6.enabled not marked as mandatory in sheet Main/No.6.
>   ERROR global.ericsson.nlf.enabled not present in workbook.
>   ERROR global.ericsson.licensing.licenseDomains not present in workbook.
>   ERROR eric-scp.service.worker.annotations.loadBalancerIPs not present in workbook.
> WARNING eric-scp.service.worker.externalIPv4.enabled not marked as mandatory in sheet SCP/No.19.
> WARNING eric-scp.service.worker.externalIPv6.enabled not marked as mandatory in sheet SCP/No.21.
>   ERROR eric-scp.service.worker.multiVpn.annotations.loadBalancerIPs not present in workbook.
> WARNING eric-scp.service.worker.multiVpn.externalIPv4.enabled not marked as mandatory in sheet SCP/No.37.
> WARNING eric-scp.service.worker.multiVpn.externalIPv6.enabled not marked as mandatory in sheet SCP/No.39.
>   ERROR eric-bsf.service.worker.annotations.loadBalancerIPs not present in workbook.
> WARNING eric-bsf.service.worker.externalIPv4.enabled not marked as mandatory in sheet BSF/No.32.
> WARNING eric-bsf.service.worker.externalIPv6.enabled not marked as mandatory in sheet BSF/No.34.
>   ERROR eric-bsf.cassandra.contact_point not present in workbook.
>   ERROR eric-bsf.cassandra.datacenter not present in workbook.
>   ERROR eric-bsf-diameter.cassandra.contact_point not present in workbook.
>   ERROR eric-sepp.service.worker.annotations.loadBalancerIPs not present in workbook.
> WARNING eric-sepp.service.worker.externalIPv4.enabled not marked as mandatory in sheet SEPP/No.15.
> WARNING eric-sepp.service.worker.externalIPv6.enabled not marked as mandatory in sheet SEPP/No.17.
>   ERROR eric-sepp.service.worker.multiVpn.annotations.loadBalancerIPs not present in workbook.
> WARNING eric-sepp.service.worker.multiVpn.externalIPv4.enabled not marked as mandatory in sheet SEPP/No.24.
> WARNING eric-sepp.service.worker.multiVpn.externalIPv6.enabled not marked as mandatory in sheet SEPP/No.26.
>   ERROR eric-tm-ingress-controller-cr.service.loadBalancerIP not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv4.enabled not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv4.loadBalancerIP not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv4.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv4.annotations.addressPoolName not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv6.enabled not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv6.loadBalancerIP not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv6.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-tm-ingress-controller-cr.service.externalIPv6.annotations.addressPoolName not present in workbook.
>   ERROR eric-cm-yang-provider.service.cmNbiPorts.dscp not present in workbook.
>   ERROR eric-cm-yang-provider.service.certManagement.sshKeys.enabled not present in workbook.
>   ERROR eric-cm-yang-provider.service.sshHostKeys.name not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv4.enabled not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv4.annotations.addressPoolName not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv4.loadBalancerIP not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv4.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv6.enabled not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv6.annotations.addressPoolName not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv6.loadBalancerIP not present in workbook.
>   ERROR eric-cm-yang-provider.service.externalIPv6.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-data-wide-column-database-cd.cassandra.remoteSeedNodes not present in workbook.
>   ERROR eric-data-wide-column-database-cd.dataCenters not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv4.enabled not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv4.loadBalancerIP not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv4.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv4.annotations.addressPoolName not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv6.enabled not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv6.loadBalancerIP not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv6.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-fh-snmp-alarm-provider.service.externalIPv6.annotations.addressPoolName not present in workbook.
> WARNING eric-fh-snmp-alarm-provider.sourceIdentifierType not marked as mandatory in sheet Generic/No.21.
> WARNING eric-fh-snmp-alarm-provider.sourceIdentifier not marked as mandatory in sheet Generic/No.22.
>   ERROR eric-pm-bulk-reporter.service.security.keyManagement.enabled not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.security.certificateManagement.enabled not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.sshHostKeys.name not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv4.enabled not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv4.loadBalancerIP not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv4.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv4.annotations.addressPoolName not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv6.enabled not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv6.loadBalancerIP not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv6.annotations.sharedVIPLabel not present in workbook.
>   ERROR eric-pm-bulk-reporter.service.externalIPv6.annotations.addressPoolName not present in workbook.
>   ERROR eric-data-sftp-server.service.certmHostKey.enabled not present in workbook.
>   ERROR eric-data-sftp-server.service.sshHostKeys.name not present in workbook.
>   ERROR eric-si-application-sys-info-handler.applicationInfoService.port not present in workbook.
<   ERROR Mandatory eric-bsf.cassandra.contact_point  in sheet BSF/No.6 not present in VNF descriptor.
<   ERROR Mandatory eric-bsf.cassandra.datacenter  in sheet BSF/No.7 not present in VNF descriptor.
<   ERROR Mandatory eric-bsf-diameter.cassandra.contact_point  in sheet BSF/No.63 not present in VNF descriptor.
<   ERROR Mandatory eric-data-wide-column-database-cd.resources.cassandra.network.useIPv6 in sheet BSF/No.106 not present in VNF descriptor.
<   ERROR Mandatory eric-data-wide-column-database-cd.cassandra.remoteSeedNodes  in sheet BSF/No.135 not present in VNF descriptor.
<   ERROR Mandatory eric-data-wide-column-database-cd.dataCenters[0].name (8) in sheet BSF/No.136 not present in VNF descriptor.
<   ERROR Mandatory eric-data-wide-column-database-cd.dataCenters[0].service.externalIP.annotations.addressPoolName in sheet BSF/No.137 not present in VNF descriptor.
<   ERROR Mandatory global.eircsson.licensing.licenseDomains in sheet Generic/No.122 not present in VNF descriptor.
<   ERROR Mandatory eric-tm-ingress-controller-cr.sevice.loadBalancerIP in sheet Generic/No.227 not present in VNF descriptor.
<   ERROR Mandatory eric-si-application-sys-info-handler.applicationinfoservice.port in sheet Generic/No.248 not present in VNF descriptor.
65 errors/14 warnings.
```
