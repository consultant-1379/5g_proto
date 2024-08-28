#!/bin/bash

UMBRELLA_DIRECTORY=eric-sc-umbrella
UMBRELLA_DIRECTORY_BACKUP=backup-umbrella
CHART_DIRECTORY=eric-sc-umbrella/charts
UMBRELLA_NAME=$2


#List with all target services
services=("eric-sec-certm"
          "eric-data-distributed-coordinator-ed"
          "eric-data-coordinator-zk"
          "eric-odca-diagnostic-data-collector"
          "eric-sec-key-management"
          "eric-sec-ldap-server"
          "eric-sec-sip-tls"
          "eric-cnom-server"
          "eric-probe-virtual-tap-broker")

function lines_affected (){  
   local file=$1
   awk '
      /secret/{
         inside=1
         next
      }
      /configMap/{
         inside=0
         next
      }
      /^[[:blank:]]*defaultMode:[[:blank:]]*0..0[[:blank:]]*$/{
         if(inside){
            printf("%d ", NR)
            inside=0
         }
      }
   ' $file
}

function lines_to_delete () {
   local lines=$1
   lines=${lines// /d;} 
   echo ${lines::-1} 
}

function adapt_general_case () {
   local file=$1
   local lines=$(lines_affected $file)
   [ -z "$lines" ] && return
   local cmd=$(lines_to_delete "$lines")
   echo -e "\tRemoving defaultMode in $file"
   sed -i "$cmd" $file
}

function adapt_charts () {
   for file in `grep -Ri defaultMode $CHART_DIRECTORY/* | cut -d: -f1 | sort -u`; do
      for service in ${services[@]}; do
         if [[ $file == *${service}* ]]; then
            echo "Checking defaultMode value in $file"
            adapt_general_case $file
         fi
      done
   done
}

#set -e

cd $1

echo "backup umbrella directory"

mv  $UMBRELLA_DIRECTORY $UMBRELLA_DIRECTORY_BACKUP


echo "unpack umbrella"

tar xzf $UMBRELLA_NAME

pwd

ls -lrt $CHART_DIRECTORY



echo "checking permissions before"
grep -Ri "defaultMode: [0-9]" * | grep -v '#\+[[:space:]]\+defaultMode' | awk '{print $2  $3}' | sort -u



echo "Adapting adp-common, please be sure to run the script on the top-level"
echo "of an ADP Common uncompressed tgz"
echo "------------------------"
adapt_charts
echo "------------------------"
echo "Removing repository url from requirements.yaml file"
ls -ltrh $UMBRELLA_DIRECTORY/requirements.yaml
sed -i '/repository/d' $UMBRELLA_DIRECTORY/requirements.yaml
echo "checking permissions after"
grep -Ri "defaultMode: [0-9]" * | grep -v '#\+[[:space:]]\+defaultMode' | awk '{print $2  $3}' | sort -u




echo "backup of umbrella"
mv $UMBRELLA_NAME backup-um.tgz

echo "repack umbrella"
tar czf $UMBRELLA_NAME $UMBRELLA_DIRECTORY/*

echo "restore umbrella directory"

rm -rf $UMBRELLA_DIRECTORY
cp -r $UMBRELLA_DIRECTORY_BACKUP $UMBRELLA_DIRECTORY

