#!/usr/bin/env python3

import sys
import yaml
import os




#fossFilesDir="../fossFiles"

fossFilesDir=sys.argv[1]



for fossFile in os.listdir(fossFilesDir):
    with open(fossFilesDir + "/" + fossFile, 'r') as fossFileID:
        fossFileObj = yaml.safe_load(fossFileID)  # READ EACH FOSS FILE INTO A YAML OBJECT
    fossFileID.close()

    for tag in fossFileObj['FOSS']:
        try:
        #EXCLUDING MAVEN DEPENDENCY TAG FROM THE SEARCH
            if (not str(fossFileObj['FOSS'][tag])) or (str(fossFileObj['FOSS'][tag]) == "None"):
                print("")
                print("The tag :",tag," is empty in file:"+fossFile)


        except IOError as e:
            print("." % e);
