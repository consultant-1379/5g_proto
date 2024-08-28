import os
# this file contains the shared resources among all scripts.


# when this flag is enabled CAI simulator reply the request in according the policies configured.
# For performance-test it's used testMode = False
global policyMode   
policyMode = False

print ("*** POLICY MODE {}  ***".format(str(policyMode)))

# ----------------------------------- ENV VARIABLE FOR CAI ---------------------------------------------------------

# it checks if the env variable CAI_PATH_BASE is set. If not, it set a default value.
dict_env = os.environ.get('CAI_PATH_BASE')
if dict_env is None:
    cai_path_base = '/application-info-collector/api/v1'
    print("CAI_PATH_BASE is not set! Default value is the following: " + str(cai_path_base) + "\n")
else:
    cai_path_base = str(os.getenv('CAI_PATH_BASE'))
    print("CAI_PATH_BASE value is the following: " + str(cai_path_base) + "\n")
    
# Application Report Schema name
applicationReportSchema = 'ApplicationReportSchema.json'
# ------------------------------------------------------------------------------------------------------------------
