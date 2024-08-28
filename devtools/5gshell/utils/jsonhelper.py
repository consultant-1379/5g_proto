import logging
import json
import sys
from utils import printer


class JsonHelper:

    def __init__(self, filename):
        self.filename = filename
        self.fullpath = ""

    def open_file(self):
        try:
            with open(self.filename, 'r') as json_file:
                self.json_data = json.load(json_file)
                return True
        except FileNotFoundError:
            printer.error("Wrong file or file path")
            return False

    def replace_port(self, port_to_replace):
        addresses = self.json_data["data"][
                        "ericsson-scp:scp-function"][
                        "service-address"]
        for item in addresses:
            logging.debug("Port found in json file: %s", item["port"])
            item["port"] = int(port_to_replace)

    def replace_tls(self, tls_to_replace):
        svc_instances = self.json_data["data"]["ericsson-scp:scp-function"][
                                    "service-address"]

        for svc_inst in svc_instances:
            svc_inst["tls"] = tls_to_replace

    def get_tls(self):
        return self.json_data["data"]["ericsson-scp:scp-function"][
                            "service-address"][0]["tls"]

    def write_file(self):
        with open(self.filename, 'w') as file:
            json.dump(self.json_data, file, indent=2)

    def get_json_string(self):
        with open(self.filename, 'r') as json_file:
            return json_file.read()

    def remove_name(self):
        if "name" in self.json_data:
            del self.json_data["name"]

    def add_name(self, name):
        self.json_data["name"] = name

