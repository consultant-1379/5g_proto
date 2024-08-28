import os
import sys
import json
import jsonschema
from jsonschema import validate
import settings

# pointing to root path of simulator
sys.path.append(os.path.join(os.path.dirname(os.path.dirname(os.path.realpath(__file__))), os.path.pardir))
def get_schema():
    """This function loads the given schema available"""
    schema_path = os.environ.get('SIM_CERT_PATH', '/app')
    schema_file = schema_path + os.sep + settings.applicationReportSchema
    with open(schema_file, 'r') as file:
        schema = json.load(file)
    return schema

def validate_json(json_data):
    """REF: https://json-schema.org/ """
    # Describe what kind of json you expect.
    execute_api_schema = get_schema()
    try:
        validate(instance=json_data, schema=execute_api_schema)
    except jsonschema.exceptions.ValidationError as err:
        print(err)
        err = "Given JSON data is InValid"
        return False, err
    message = "Given JSON data is Valid"
    return True, message

# Object that defines the parameters of the reports towards CAI (POST request)
class Report_CAI_POST:
    
    def __init__(self, report):
        self.report = report
        
    def __str__(self):
        return str(self.report)
    
    def validate_report(self):
        #jsonData = json.loads(str(self.report))
        is_valid, msg = validate_json(self.report)
        print(msg)
        return is_valid