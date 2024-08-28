#!/bin/bash
#
# Exclude members not used by the SC itself from mapping to their generated Java class during the JSON processing.
# This to lower the probability of faults due to invalid JSON input from external nodes (e.g. the NRF).
#
# Synopsis: ignore_unused_members.sh <path-to-5g_proto>

echo "$0: starting ..."

#set -e # Any subsequent commands which fail will cause the shell script to exit immediately

pushd "${1:-.}/cnal/ts2java"

# Using absolute paths everywhere.
#
OUT_SRC_DIR="tmp/tmp/src/main/java/com/ericsson/cnal/openapi/r17"

# Exclude members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters.
# This is done first by excluding all members and their setters and then by including those setters that should be used for deserialization.
#
echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfdiscovery/NFProfile.java"
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setAusfInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setAusfInfoList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setCapacity\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setChfInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setChfInfoList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setFqdn\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setInterPlmnFqdn\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv4Addresses\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv6Addresses\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setLoad\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setLoadTimeStamp\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setLocality\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNefInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfInstanceName\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfInstanceId\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfServices\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfServiceList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfSetIdList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfStatus\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfType\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setPcfInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setPcfInfoList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setPriority\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setRecoveryTime\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setScpDomains\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setScpInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setUdmInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setUdmInfoList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setUdrInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setUdrInfoList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setUdsfInfo\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFProfile.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setUdsfInfoList\()/\1\2/g'

echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfdiscovery/NFService.java"
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setCapacity\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setFqdn\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setInterPlmnFqdn\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpEndPoints\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setLoad\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setLoadTimeStamp\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfServiceSetIdList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfServiceStatus\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setPriority\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setScheme\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setServiceInstanceId\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "NFService.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setServiceName\()/\1\2/g'

echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfdiscovery/SearchResult.java"
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfInstances\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setNfInstanceList\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setSearchId\()/\1\2/g'
find "$OUT_SRC_DIR/ts29510/nnrf/nfdiscovery" -name "SearchResult.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setValidityPeriod\()/\1\2/g'

#echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfmanagement/ChfInfo.java"
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ChfInfo.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ChfInfo.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ChfInfo.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ChfInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setGpsiRangeList\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ChfInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setSupiRangeList\()/\1\2/g'

#echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfmanagement/IpEndPoint.java"
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "IpEndPoint.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "IpEndPoint.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "IpEndPoint.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "IpEndPoint.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv4Address\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "IpEndPoint.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv6Address\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "IpEndPoint.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setPort\()/\1\2/g'

#echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfmanagement/PcfInfo.java"
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "PcfInfo.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "PcfInfo.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "PcfInfo.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "PcfInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setRxDiamHost\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "PcfInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setRxDiamRealm\()/\1\2/g'

#echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfmanagement/ScpInfo.java"
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv4Addresses\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv6Addresses\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setScpPorts\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setServedNfSetIdList\()/\1\2/g'

#echo "$0: Excluding members not used by NF discovery from deserialization by adding @JsonIgnore to them and their setters in $OUT_SRC_DIR/ts29510/nnrf/nfmanagement/ScpDomainInfo.java"
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^import com.fasterxml.jackson.annotation.JsonInclude;/import com.fasterxml.jackson.annotation.JsonIgnore;\nimport com.fasterxml.jackson.annotation.JsonInclude;/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^([ \t]+)(private .+;)/\1@JsonIgnore \2/g'    # Add @JsonIgnore to all members
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^([ \t]+)(.+void set.+\()/\1@JsonIgnore \2/g' # Add @JsonIgnore to all setters
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpv4Addresses\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setIpEndPoints\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setScpFqdn\()/\1\2/g'
#find "$OUT_SRC_DIR/ts29510/nnrf/nfmanagement" -name "ScpDomainInfo.java" | xargs sed -i -r 's/^([ \t]+)@JsonIgnore (.+setScpPorts\()/\1\2/g'

popd

echo "$0: finished."

