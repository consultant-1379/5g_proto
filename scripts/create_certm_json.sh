#!/bin/bash -x

# folders' names
certsFolder=".certificates"
extSecretsFolder="scripts/external_secrets/certs_day0"
bobOutputFolder=".bob"

# The following variable regard the services' folder name in the above <certsFolder> folder. 
# The names below should be the same in the following areas:
# 1) "private static final String NAME <FOLDER_NAME>" of the corresponding supreme class
# 2) generate (install) <FOLDER_NAME> -d ${CERTS_OUT_DIR}/supreme.yaml -l debug in the esc-ruleset
rootca="rootca"
wcdbcdInternodeExt="cassandra-internode-external"
cqlClientExt="cql-client-external"
cqlServerExt="cql-server-external"
cmypNetconfTls="yang-provider"
syslog="transformer"

# json files' names
certmJson="eric-sec-certm-deployment-configuration.json" # do not change this name; it's defined in the Marketplace of CERTM

# certificate and key files' names
certPemFile="cert.pem"
keyPemFile="key.pem"
p12File="container.p12"
p12Base64File="container-base64.p12"

# The function checks if a given file exists in a given directory and it's not empty
fileChecker() 
{
    local serviceDir=$1
    local file=$2
    if [ ! -f "${serviceDir}/${file}" ]; then
        echo "There is no '${file}' file in '${serviceDir}' directory"
        exit 1
    else 
        if [ ! -s "$serviceDir/$file" ]; then 
            echo "File '${serviceDir}/${file}' is empty"
            exit 1
        fi
    fi
}

# The function checks whether files of a given certificate folder exist and are non-empty
folderChecker() 
{
    local certFolder="${certsFolder}/${1}"
    if [ ! -d "${certFolder}/" ]; then
        echo "There is no '${certFolder}' folder in '${certsFolder}/' directory"
        exit 1
    fi 
    fileChecker ${certFolder} ${certPemFile}
    fileChecker ${certFolder} ${keyPemFile}
    fileChecker ${certFolder} ${p12File}
    fileChecker ${certFolder} ${p12Base64File}
}

# The functions verifies the ca certificates' format and adapts it to accord with the CERTM's format
# For more info, refer to the following section of the CERTM ADP service:
# "Helm Chart Installation of Certificate Management Service" -> "Initial Configuration" 
formatCaCert() 
{
    local certFolder=$1
    folderChecker "${certFolder}"
    cert=($(cat ${certsFolder}/${certFolder}/${certPemFile}))
    cert_len=${#cert[@]}

    firstSubString="${cert[0]} ${cert[1]}"
    if [ "${firstSubString}" != "-----BEGIN CERTIFICATE-----" ]; then
        echo "${certFolder} certificate must start with '-----BEGIN CERTIFICATE-----'"
        exit 1
    fi

    lastSubString="${cert[$(($cert_len-2))]} ${cert[$(($cert_len-1))]}"
    if [ "${lastSubString}" != "-----END CERTIFICATE-----" ]; then
        echo "${certFolder} certificate must end with '-----END CERTIFICATE-----'"
        exit 1
    fi

    middleSubString=""
    for (( i=2; i<$cert_len-2; i++ )); do
        middleSubString+=${cert[i]}
    done
    if [ ${#middleSubString[@]} -eq 0 ]; then
        echo "The ${certFolder} certificate is empty"
    fi

    echo "${firstSubString}\n${middleSubString}\n${lastSubString}"
}

checkCertFiles()
{
    case $binary in
        1)
            echo "Check syslog files"
            folderChecker ${syslogExternal}
            ;;
        2)
            echo "Check geored files"
            folderChecker ${wcdbcdInternodeExt}
            folderChecker ${cqlClientExt}
            folderChecker ${cqlServerExt}
            ;;
        3)
            echo "Check geored and syslog files"
            folderChecker ${wcdbcdInternodeExt}
            folderChecker ${cqlClientExt}
            folderChecker ${cqlServerExt}
            folderChecker ${syslogExternal}
            ;;
        4)
            echo "Check netconf files"
            folderChecker ${cmypNetconfTls}
            ;;
        5)
            echo "Check netconf and syslog files"
            folderChecker ${cmypNetconfTls}
            folderChecker ${syslog}
            ;;
        6)
            echo "Check netconf and geored files"
            folderChecker ${wcdbcdInternodeExt}
            folderChecker ${cqlClientExt}
            folderChecker ${cqlServerExt}
            folderChecker ${cmypNetconfTls}
            ;;
        7)
            echo "Check all files"
            folderChecker ${wcdbcdInternodeExt}
            folderChecker ${cqlClientExt}
            folderChecker ${cqlServerExt}
            folderChecker ${cmypNetconfTls}
            folderChecker ${syslogExternal}
            ;;
        :)
            echo "No files to check"
            exit 0
            ;;
    esac
}

getGeneratedCertificates()
{
    scTrustedDefaultCas=$(formatCaCert "$rootca")
    case $binary in
        1)
            echo "Get syslog files";
            syslogDefaultKeyCert=$(cat ${certsFolder}/${syslog}/${p12Base64File});
            ;;
        2)
            echo "Get geored files"
            internodeExternalKeyCert=$(cat ${certsFolder}/${wcdbcdInternodeExt}/${p12Base64File});
            cqlClientExternalKeyCert=$(cat ${certsFolder}/${cqlClientExt}/${p12Base64File});
            cqlServerExternalKeyCert=$(cat ${certsFolder}/${cqlServerExt}/${p12Base64File});
            ;;
        3)
            echo "Get geored and syslog files";
            internodeExternalKeyCert=$(cat ${certsFolder}/${wcdbcdInternodeExt}/${p12Base64File});
            cqlClientExternalKeyCert=$(cat ${certsFolder}/${cqlClientExt}/${p12Base64File});
            cqlServerExternalKeyCert=$(cat ${certsFolder}/${cqlServerExt}/${p12Base64File});
            syslogDefaultKeyCert=$(cat ${certsFolder}/${syslog}/${p12Base64File});
            ;;
        4)
            echo "Get netconf files";
            netconfDefaultKeyCert=$(cat ${certsFolder}/${cmypNetconfTls}/${p12Base64File});
            ;;
        5)
            echo "Get netconf and syslog files";
            netconfDefaultKeyCert=$(cat ${certsFolder}/${cmypNetconfTls}/${p12Base64File});
            syslogDefaultKeyCert=$(cat ${certsFolder}/${syslog}/${p12Base64File});
            ;;
        6)
            echo "Get netconf and geored files";
            internodeExternalKeyCert=$(cat ${certsFolder}/${wcdbcdInternodeExt}/${p12Base64File});
            cqlClientExternalKeyCert=$(cat ${certsFolder}/${cqlClientExt}/${p12Base64File});
            cqlServerExternalKeyCert=$(cat ${certsFolder}/${cqlServerExt}/${p12Base64File});
            netconfDefaultKeyCert=$(cat ${certsFolder}/${cmypNetconfTls}/${p12Base64File});
            ;;
        7)
            echo "Get all files";
            internodeExternalKeyCert=$(cat ${certsFolder}/${wcdbcdInternodeExt}/${p12Base64File});
            cqlClientExternalKeyCert=$(cat ${certsFolder}/${cqlClientExt}/${p12Base64File});
            cqlServerExternalKeyCert=$(cat ${certsFolder}/${cqlServerExt}/${p12Base64File});
            netconfDefaultKeyCert=$(cat ${certsFolder}/${cmypNetconfTls}/${p12Base64File});
            syslogDefaultKeyCert=$(cat ${certsFolder}/${syslog}/${p12Base64File});
            ;;
        :)
            echo "No files to get"
            exit 0
            ;;
    esac
}

# Function to convert boolean to integer
bool_to_int() {
    if $1; then echo 1; else echo 0; fi
}

createExportJson() {
    output+='{
        "ca-certs": [
            '"${scTrustedDefaultCasJson}"'
        ],
        "pkcs12": ['
    case $binary in
        1)
            echo "Add syslog certificate json data"
            output+="${syslogDefaultKeyCertJson}"
            ;;
        2)
            echo "Add geored certificate json data"
            output+="${internodeExternalKeyJson},"
            output+="${cqlClientExternalKeyJson},"
            output+="${cqlServerExternalKeyJson}"
            ;;
        3)
            echo "Add geored and syslog certificate json data"
            output+="${internodeExternalKeyJson},"
            output+="${cqlClientExternalKeyJson},"
            output+="${cqlServerExternalKeyJson},"
            output+="${syslogDefaultKeyCertJson}"
            ;;
        4)
            echo "Add netconf certificate json data"
            output+="${netconfDefaultKeyCertJson}"
            ;;
        5)
            echo "Add netconf and syslog certificate json data"
            output+="${netconfDefaultKeyCertJson},"
            output+="${syslogDefaultKeyCertJson}"
            ;;
        6)
            echo "Add netconf and geored certificate json data"
            output+="${internodeExternalKeyJson},"
            output+="${cqlClientExternalKeyJson},"
            output+="${cqlServerExternalKeyJson},"
            output+="${netconfDefaultKeyCertJson}"
            ;;
        7)
            echo "Add all certificate json data"
            output+="${internodeExternalKeyJson},"
            output+="${cqlClientExternalKeyJson},"
            output+="${cqlServerExternalKeyJson},"
            output+="${netconfDefaultKeyCertJson},"
            output+="${syslogDefaultKeyCertJson}"
            ;;
        :)
            echo "No certificate json data to add"
            exit 0
            ;;
    esac
    output+="]}"
    echo ${output} > ${bobOutputFolder}/${certmJson}
}

######==============================================================######

if ! $GEORED && ! $NETCONF_TLS && ! $SYSLOG;  then
    echo "No day0 certs"
    exit 0
fi

if [ ! -d "${certsFolder}/" ]; then 
    echo "'${certsFolder}/' folder does not exist"
    exit 1
fi

if [ -f "${bobOutputFolder}/${certmJson}" ]; then 
    rm -rf ${bobOutputFolder}/${certmJson}
fi
touch ${bobOutputFolder}/${certmJson}

# Convert true/false flags to binary
binary=$(( ( $(bool_to_int "$NETCONF_TLS") << 2) + ( $(bool_to_int "$GEORED") << 1) + $(bool_to_int "$SYSLOG") ))

checkCertFiles
getGeneratedCertificates

internodeExternalKeyJson='{
    "name": "bsf-internode-external-key",
    "certificate-name": "bsf-internode-external-cert",
    "pkcs12": "'"${internodeExternalKeyCert}"'",
    "password": "rootroot"
    }'

cqlClientExternalKeyJson='{
    "name": "bsf-cql-client-external-key",
    "certificate-name": "bsf-cql-client-external-cert",
    "pkcs12": "'"${cqlClientExternalKeyCert}"'",
    "password": "rootroot"
    }'
    
cqlServerExternalKeyJson='{
    "name": "bsf-cql-server-external-key",
    "certificate-name": "bsf-cql-server-external-cert",
    "pkcs12": "'"${cqlServerExternalKeyCert}"'",
    "password": "rootroot"
    }'
    
netconfDefaultKeyCertJson='{
    "name": "netconf-default-key-cert",
    "certificate-name": "netconf-default-key-cert",
    "pkcs12": "'"${netconfDefaultKeyCert}"'",
    "password": "rootroot"
    }'
    
syslogDefaultKeyCertJson='{
    "name": "syslog-default-key-cert",
    "certificate-name": "syslog-default-key-cert",
    "pkcs12": "'"${syslogDefaultKeyCert}"'",
    "password": "rootroot"
    }'
    
scTrustedDefaultCasJson='{
    "name": "sc-trusted-default-cas",
    "pem": "'"${scTrustedDefaultCas}"'"
    }'
    
createExportJson
