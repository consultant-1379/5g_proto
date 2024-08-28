#!/bin/bash

VP=$(cat VERSION_PREFIX) && arr=(${VP//./ }) && Y_VERSION=${arr[1]} && Z_VERSION=${arr[2]} && R_STATE=$(echo R$(expr $Y_VERSION + 1)$(printf "\x$(printf %x $((65 + $Z_VERSION)))"));

if [[ "$RELEASE" = true ]];
then
   COMMIT_NUMBER=$(EV=$(cat $1) && arr=(${EV//-/ }) && echo ${arr[1]});
   R_STATE=$(echo $R_STATE$COMMIT_NUMBER);  
fi;
echo $R_STATE;
