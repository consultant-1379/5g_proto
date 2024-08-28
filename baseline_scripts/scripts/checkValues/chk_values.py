#!/usr/bin/env python

__author__ = "ekoteva"

import sys
import argparse
import os.path
import yaml
import logging
import getpass
import urllib2
from urllib2 import HTTPError
import base64
import re
from contextlib import closing
import tarfile
import collections
from abc import ABCMeta, abstractmethod
import json

sys.path.insert(0, "..")
# noinspection PyUnresolvedReferences
from common_functions import read_yaml

# set cd to parent
os.chdir(os.path.abspath(os.path.join(os.getcwd(), os.pardir)))


# abstract-blueprint for builders
class AppParameter:
    __metaclass__ = ABCMeta

    @abstractmethod
    def set_name(self, name):
        """Set parameter name"""
        pass

    @abstractmethod
    def set_is_present(self, is_present):
        """sets value for is present"""
        pass

    @abstractmethod
    def set_value(self, value):
        """sets value for value"""
        pass

    @abstractmethod
    def set_parameter(self, parameter):
        """sets value for parameter"""
        pass

    @abstractmethod
    def set_parameter_type(self, p_type):
        """Set parameter type(instantiation or upgrade)"""
        pass


# interface-blueprint for VNFD,Chart Parameters
class IParameters:
    __metaclass__ = ABCMeta

    @abstractmethod
    def exists(self):
        """"To check if certain attribute exists"""

    @abstractmethod
    def get_name(self, name):
        """Get selected value"""
        pass

    @abstractmethod
    def get_value(self, value):
        """Returns parameter"""
        pass

    @abstractmethod
    def get_parameters(self):
        """Returns parameters"""
        pass

    @abstractmethod
    def get_parameter(self, parameter):
        """Returns parameter"""
        pass

    @abstractmethod
    def get_parameter_type(self):
        """Return parameter type(instantiation/upgrade)"""
        pass


# ready
class VnfParameterBuilder(AppParameter):

    @staticmethod
    def __name__():
        return "VnfParameterBuilder"

    def __init__(self):
        self.VNFDParameters = VNFDParameters()

    def set_name(self, name):
        self.VNFDParameters.name = name

    def set_is_present(self, is_present):
        if is_present == "TRUE":
            self.VNFDParameters.is_present = True
        else:
            self.VNFDParameters.is_present = False

    def set_value(self, value):
        self.VNFDParameters.value = value
        return self

    def set_parameter(self, parameter):
        self.VNFDParameters.parameter = parameter
        return self

    def set_parameter_type(self, p_type):
        self.VNFDParameters.p_type = p_type
        return self

    def get_result(self):
        return self.VNFDParameters


# ready
class VNFDParameters(IParameters):
    """Product"""

    @staticmethod
    def __name__():
        return "VNFDParameters"

    def __init__(self):
        self.name = ""
        self.is_present = ""
        self.value = ""
        self.parameter = ""
        self.parameter_type = ""

    def exists(self):
        return self.is_present

    def get_name(self, name):
        if self.name is not None:
            return self.name
        return None

    def get_parameters(self):
        """Not sure if this should be implemented"""
        pass

    def get_parameter(self, parameter):
        if self.parameter is not None:
            return self.parameter
        return None

    def get_value(self, value):
        if self.value is not None:
            return self.value
        return None

    def get_parameter_type(self):
        return self.parameter_type

    # for testing purposes
    def __str__(self):
        return "Name = {0}, is_present= {1},value = {2}, parameter = {3}, parameter_type = {4} ".format(self.name,
                                                                                                        self.is_present,
                                                                                                        self.value,
                                                                                                        self.parameter,
                                                                                                        self.parameter_type)


# ready
class ChartParameterBuilder(AppParameter):

    @staticmethod
    def __name__():
        return "ChartParameterBuilder"

    def __init__(self):
        self.ChartParameters = ChartParameters()

    def set_name(self, name):
        self.ChartParameters.name = name

    def set_is_present(self, is_present):
        if is_present == "TRUE":
            self.ChartParameters.is_present = True
        else:
            self.ChartParameters.is_present = False

    def set_value(self, value):
        self.ChartParameters.value = value
        return self

    def set_parameter(self, parameter):
        self.ChartParameters.parameter = parameter
        return self

    def set_parameter_type(self, p_type):
        self.ChartParameters.p_type = p_type
        return self

    def get_result(self):
        return self.ChartParameters


# ready
class ChartParameters(IParameters):
    """Product"""

    @staticmethod
    def __name__():
        return "ChartParameters"

    parameters = []

    def __init__(self):
        self.name = ""
        self.is_present = ""
        self.value = ""
        self.parameter = ""
        self.parameter_type = ""

    def exists(self):
        return self.is_present

    def get_name(self, name):
        if self.name is not None:
            return self.name
        return None

    def get_parameters(self):
        """This will get deleted """
        pass

    def get_parameter(self, parameter):
        if self.parameter is not None:
            return self.parameter
        return None

    def get_value(self, value):
        if self.value is not None:
            return self.value
        return None

    def get_parameter_type(self):
        return self.parameter_type

    # for testing purposes
    def __str__(self):
        return "Name = {0}, is_present= {1},value = {2}, parameter = {3}, parameter_type = {4}".format(self.name,
                                                                                                       self.is_present,
                                                                                                       self.value,
                                                                                                       self.parameter,
                                                                                                       self.parameter_type)


# abstract-blueprint class for AppDependency Builder
class AppDependency:
    __metaclass__ = ABCMeta

    @abstractmethod
    def __init__(self):
        """Init method"""
        pass

    def get(self):
        """Placeholder"""
        pass

    @abstractmethod
    def set_name(self, name):
        """Returns name"""
        pass

    @abstractmethod
    def set_repository(self, repository):
        """Returns repository"""
        pass

    @abstractmethod
    def set_version(self, version):
        """Returns version"""
        pass


# Interface-blueprint class for AppDependencies
class IAppDependencies:
    __metaclass__ = ABCMeta

    @abstractmethod
    def is_present(self, name):
        """Returns true if name exists"""
        pass

    @abstractmethod
    def get_repository(self, repository):
        """Returns repository"""
        pass

    @abstractmethod
    def get_version(self, version):
        """Returns version"""
        pass

    @abstractmethod
    def get_dependency(self, dependency):
        """Returns AppDependency"""
        pass


class AppDependencyBuilder(AppDependency):
    @staticmethod
    def __name__():
        return "AppDependencyBuilder"

    def __init__(self):
        self.AppDependencies = AppDependencies()

    def set_name(self, name):
        self.AppDependencies.name = name
        return self

    def set_repository(self, repository):
        self.AppDependencies.repository = repository
        return self

    def set_version(self, version):
        self.AppDependencies.version = version
        return self

    def get_result(self):
        return self.AppDependencies


class AppDependencies(IAppDependencies):
    """Product"""

    @staticmethod
    def __name__():
        return "AppDependencies"

    def __init__(self):
        self.name = ""
        self.repository = ""
        self.version = ""
        self.dependency = ""

    def is_present(self, name):
        return name is not None

    def get_repository(self, repository):
        return self.repository

    def get_version(self, version):
        if self.version is not None:
            return self.version

    def get_dependency(self, dependency):
        if self.dependency is not None:
            return self.dependency

    def __str__(self):
        return "Name: {0},Repository {1},Version: {2}".format(self.name, self.repository, self.version)


class ExporterFactory:

    @staticmethod
    def get_umbrella_chart_param():
        return ChartParameterBuilder().set_name("hello").set_repository("repo").set_version("123.1").get_result()

    @staticmethod
    def get_object_of_type(param_type):
        parameter_types = {
            "UmbrellaChart": ChartParameterBuilder(),
            "SubChart": ChartParameterBuilder(),
            "ExposedChart": ChartParameterBuilder(),
            "InstantiateVnf": VnfParameterBuilder(),
            "UpgradeVnf": VnfParameterBuilder(),
            "Dependencies": AppDependencyBuilder(), }

        if param_type in parameter_types:
            return parameter_types[param_type]
        return None

    ##similar to above, but this is called by certain classes
    # call this with  a class named an appropriate name to get the according builder
    # e.g. UmbrellaChart().get_object() returns ChartParameterBuilder object
    @staticmethod
    def get_obj(obj_type):
        obj_type = obj_type.__name__()

        parameter_types = {
            "UmbrellaChart": ChartParameterBuilder(),
            "SubChart": ChartParameterBuilder(),
            "InstantiateVnf": VnfParameterBuilder(),
            "UpgradeVnf": VnfParameterBuilder(),
            "Dependencies": AppDependencyBuilder(),
            "ExposedChart": AppDependencyBuilder(), }
        if obj_type in parameter_types:
            return parameter_types[obj_type]
        return None


class IAllParameters:
    __metaclass__ = ABCMeta

    @abstractmethod
    def __name__(self):
        """Return class name"""
        pass

    @abstractmethod
    def get_object(self):
        """Calls factory method to return the appropriate builder """
        pass


class UmbrellaChart(IAllParameters):

    def __name__(self):
        return "UmbrellaChart"

    def get_object(self):
        return ExporterFactory().get_obj(self)


class SubChart(IAllParameters):

    def __name__(self):
        return "SubChart"

    def get_object(self):
        return ExporterFactory().get_obj(self)


class ExposedChart(IAllParameters):

    def __name__(self):
        return "ExposedChart"

    def get_object(self):
        return ExporterFactory().get_obj(self)


class InstantiateVnf(IAllParameters):

    def __name__(self):
        return "InstantiateVnf"

    def get_object(self):
        return ExporterFactory().get_obj(self)


class UpgradeVnf(IAllParameters):

    def __name__(self):
        return "UpgradeVnf"

    def get_object(self):
        return ExporterFactory().get_obj(self)


class Dependencies(IAllParameters):

    def __name__(self):
        return "Dependencies"

    def get_object(self):
        return ExporterFactory().get_obj(self)


class ScriptFiles:
    __instance = None

    def instantiate(self):
        self.user = getpass.getuser()
        self.exec_path = os.path.dirname(os.path.abspath(__file__))
        # self.git_path= git.Repo(self.exec_path,search_parent_directories = True).git.rev_parse('--show-toplevel')

        self.git_path = os.path.abspath(
            os.path.join(
                os.path.join(
                    os.path.dirname(
                        os.path.abspath(__file__)
                    ),
                    os.pardir
                ),
                os.pardir
            )
        )
        # check if all these are filled properly
        self.app_dependencies = []
        self.exposed_parameters = []
        self.vnfd_parameters = []

        # Directories
        self.defaultUmbrellaChartDir = self.git_path + "/esc/helm/eric-sc-umbrella"
        self.defaultCustomerChartDir = self.git_path + "/esc/release_artifacts"
        self.chk_values_properties_file = "%s/chk_values_properties.yaml" % self.exec_path
        self.credentials_path = "/home/{}/.docker".format(self.user)
        # script Paths
        # make dictionary
        self.script_paths = {
            "requirements_yaml_file": "%s/requirements.yaml" % self.defaultUmbrellaChartDir,
            "values_yaml_file": "%s/values.yaml" % self.defaultUmbrellaChartDir,
            "sc_values_yaml_file": "%s/eric-sc-values.yaml" % self.defaultCustomerChartDir,
            "vnfd_yaml_file": "%s/sc_vnf_descriptor.yaml" % self.defaultCustomerChartDir,
            "chk_values_properties_file": "%s/chk_values_properties.yaml" % self.exec_path}

    def check_and_return_file(self, file_name):
        if file_name in self.script_paths:
            file_path = self.script_paths.get(file_name)
            check_files(file_path)
            log.debug(file_name + " file to be used: " + file_path)
            file = read_yaml(file_path)
            if file is None:
                print("File " + file_name + " not found!")
                exit(1)
            else:
                return file
        else:
            print("File not in dictionary!")
            return None

    @staticmethod
    def get_instance():
        if ScriptFiles.__instance is None:
            ScriptFiles()
        return ScriptFiles.__instance

    def __init__(self):

        if ScriptFiles.__instance is not None:
            print("ScriptFiles has already been instanced!")
        else:
            self.git_path = None
            self.exec_path = None
            self.chk_values_properties_file = None
            self.defaultCustomerChartDir = None
            self.defaultUmbrellaChartDir = None
            self.app_dependencies = []
            self.exposed_parameters = []
            self.vnfd_parameters = []
            self.script_paths = {}
            self.credentials_path = ""
            self.username = ""
            self.password = ""
            self.properties = ""

            self.instantiate()

            ScriptFiles.__instance = self

    @staticmethod
    def return_data():
        return "Name: {0}, Age: {1}".format(ScriptFiles.__instance.name, ScriptFiles.__instance.age)

    # fetches username and password from ~/.docker/config file
    def get_credentials(self):
        file_path = "%s/config.json" % self.credentials_path
        try:
            with open(file_path, 'r') as f:
                credentials_json = json.load(f)

            credentials = base64.b64decode(credentials_json['auths']['armdocker.rnd.ericsson.se']['auth'])

            username = (credentials.split(":"))[0]
            password = (credentials.split(":"))[1]

        # intentional broad exception to catch any possible errors that might occur
        except:
            print("credentials not found in " + self.credentials_path + "/config.json path!")
            username = raw_input("Username: ")
            password = getpass()

        return username, password

    def get_all(self):
        variables = [attr for attr in dir(self) if not callable(getattr(self, attr)) and not attr.startswith("__")]
        return variables


############--End Class declaration--###############

class bcolors:
    HEADER = '\033[95m'
    OKBLUE = '\033[94m'
    UPDATE = '\033[92m'
    WARNING = '\033[93m'
    FAIL = '\033[91m'
    ENDC = '\033[0m'


# print colored('hello', 'red'), colored('world', 'green')
##### INITIALIZATIONS #####


script_files = ScriptFiles.get_instance()

log = logging.getLogger(__name__)
logging.basicConfig(format="%(asctime)s %(name)s %(levelname)s %(message)s", level=logging.INFO)
exec_path = os.path.dirname(os.path.abspath(__file__))


def parse_arguments():
    parser = argparse.ArgumentParser()
    parser.add_argument(
        '-l', '--logLevel',
        help="""set the logging level
        """,
        default="info",
        required=False
    )
    parser.add_argument(
        '-u', '--umbrellaChartDir',
        help="""Full path to the application chart directory
        """,
        required=False
    )
    parser.add_argument(
        '-c', '--customerChartDir',
        help="""Full path to the application chart directory
        """,
        required=False
    )
    parser.add_argument(
        '--username',
        help="""path to the username
        """,
        required=False
    )
    parser.add_argument(
        '--password',
        help="""path to the password
        """,
        required=False
    )
    return parser.parse_args()


def set_logging_level(level):
    switcher = {
        "debug": logging.DEBUG,
        "info": logging.INFO,
        "warning": logging.WARNING,
        "error": logging.ERROR,
        "critical": logging.CRITICAL
    }
    log.setLevel(switcher.get(level, logging.INFO))


def check_files(file):
    if os.path.isfile(file):
        log.debug(file + " file exists.")
    else:
        log.error(file + " file does not exist.")
        exit(1)


def simple_remove_comments(orig_file_name, new_file_name):
    """This is the function that removes all comments with '# ' from yaml files."""
    log.debug("Remove '# ' from file '" + orig_file_name + "' -> " + new_file_name)
    try:
        with open(orig_file_name, 'r') as infile, open(new_file_name, 'w') as outfile:
            for line in infile:
                if (":" in line) or ("- " in line):
                    outfile.write(line.replace('# ', ''))
                else:
                    outfile.write(line)

    except IOError as e:
        log.error(
            "Unexpected exception while handling files:\n" + orig_file_name + "\n" + new_file_name + "\nException:\n" + str(
                e))


def remove_comments(orig_file_name, new_file_name):
    """This is the function that removes all comments with from sub-chart values.yaml files."""
    log.debug("Remove comments and align values files '" + orig_file_name + "' -> " + new_file_name)
    try:
        with open(orig_file_name, 'r') as infile, open(new_file_name, 'w') as outfile:
            previous_white_spaces = 0
            list_indicator = False
            for line in infile:
                # print("pws: " + str(previous_white_spaces))
                tmp_line = line
                # print("pli: " + tmp_line)

                white_spaces = len(tmp_line) - len(tmp_line.lstrip(' '))
                modulus_white_spaces = white_spaces % 2
                div_white_spaces = white_spaces / 2
                if modulus_white_spaces != 0:
                    white_spaces = int(div_white_spaces) + 1
                # print("cws: " + str(white_spaces))

                if ":" in tmp_line and re.match(r"^ {0,}#{1,} {1}\S", tmp_line) and len(
                        tmp_line.split(':')) == 2 and "NOTE" not in tmp_line:
                    # outfile.write(line.replace('# ', ''))
                    # tmp_line=line.replace('# ', '')

                    # white_spaces = len(tmp_line) - len(tmp_line.lstrip(' '))
                    # modulus_white_spaces = white_spaces%2
                    # div_white_spaces = white_spaces/2
                    # if modulus_white_spaces!=0:
                    #    white_spaces = int(div_white_spaces)+1
                    # print("cws: " + str(white_spaces))

                    tmp_line = re.sub(r"\s{1,}#{1,}.*", "", re.split("\s{0,}#\s", tmp_line)[1])

                    if re.match(r"^ {0,}- {1}\"{0,}\S", tmp_line):
                        list_indicator = True
                        list_white_spaces = white_spaces
                        # print("list indicator ACTIVATED, white-spaces: " + str(list_white_spaces))

                    # if re.match(r"\s{1,}\S{1,}:{1} {1}\S", tmp_line):
                    if re.match(r"\s{1,}\S{1,}:{1} {1}\S", tmp_line) or list_indicator:
                        # print("'" + tmp_line + "'")
                        # if re.match(r"^ {0,}- {1}\"{0,}\S", tmp_line):
                        #    list_indicator = True
                        #    list_white_spaces = white_spaces
                        #    print("list indicator ACTIVATED, white-spaces: " + str(list_white_spaces))

                        if (white_spaces > previous_white_spaces):  # and ((white_spaces-previous_white_spaces)!=2):
                            if re.match(r"^ {0,}- {1}\S", tmp_line):
                                list_indicator = True
                                list_white_spaces = white_spaces
                                # print("list indicator ACTIVATED, white-spaces: " + str(list_white_spaces))
                                previous_white_spaces = previous_white_spaces + 2
                            elif list_indicator:
                                previous_white_spaces = list_white_spaces + 2
                            else:
                                previous_white_spaces = previous_white_spaces + 2
                        else:
                            if previous_white_spaces > white_spaces:
                                # print("list indicator DEACTIVATED")
                                list_indicator = False
                            previous_white_spaces = white_spaces

                        tmp_line = (" " * previous_white_spaces) + re.sub(r"^ {0,}", "", tmp_line)
                    # print("nli: " + tmp_line)
                else:
                    # outfile.write(line)
                    tmp_line = line
                    previous_white_spaces = white_spaces

                if (tmp_line[0].isupper() == False) and not re.match(r"^ {0,}#{1,}.*", tmp_line):
                    outfile.write(tmp_line)

    except IOError as e:
        log.error(
            "Unexpected exception while handling files:\n" + orig_file_name + "\n" + new_file_name + "\nException:\n" + str(
                e))


def flatten(d, parent_key='', sep='_'):  # returns a copy of the array collapsed into one dimension #
    items = []
    for k, v in d.items():
        new_key = parent_key + sep + k if parent_key else k
        if isinstance(v, dict) and len(v) == 0:
            v = "\'{" + "}\'"
        if isinstance(v, collections.MutableMapping):
            items.extend(flatten(v, new_key, sep=sep).items())
        else:
            items.append((new_key, v))
    return dict(items)


def update(results, parameters):
    for item in results:
        log.debug(bcolors.UPDATE + "Updating" + bcolors.ENDC + " chart-values-exist for: " + str(item))
        log.debug("Value of new parameter: " + str(results[item]))
        parameter = {}
        parameters[str(item)] = parameter
        parameter["chart-values-exist"] = "TRUE"
        parameter["chart-value"] = str(results[item])
        parameter["eric-sc-umbrella-values-exist"] = "FALSE"
        parameter["eric-sc-umbrella-value"] = "n/a"
        parameter["eric-sc-values-exist"] = "FALSE"
        parameter["eric-sc-value"] = "n/a"
        parameter["vnfd-inst-exist"] = "FALSE"
        parameter["vnfd-inst-value"] = "n/a"
        parameter["vnfd-upgr-exist"] = "FALSE"
        parameter["vnfd-upgr-value"] = "n/a"
    return parameters


def update_umbrella(results, parameters):
    
    sf = ScriptFiles.get_instance()
    properties = sf.check_and_return_file("chk_values_properties_file")

    for item in results:
        
        # adps check added here
        if "definitions" in str(item): 
            continue
        
        if str(item) in parameters:

            if not adps_exists(item, properties):
                log.debug(bcolors.UPDATE + "Already found parameter - " + bcolors.ENDC + "Updating eric-sc-umbrella-values-exist for: " + str(item))
                log.debug("Value of new parameter: " + str(results[item]))

            parameter = parameters[str(item)]
            if (parameter["eric-sc-umbrella-values-exist"] == "TRUE"):
                if not adps_exists(item, properties):
                    log.warning(bcolors.WARNING + "Parameter " + str(item) + " has already value in eric-sc-umbrella-values that will be replaced!" + bcolors.ENDC)   
            parameter["eric-sc-umbrella-values-exist"] = "TRUE"
            parameter["eric-sc-umbrella-value"] = str(results[item])
        else:
            if not adps_exists(item, properties):
                log.warning("New parameter found in eric-sc-umbrella-values: " + bcolors.WARNING + str(item) + ":" + str(results[item]) + bcolors.ENDC + " but not present in any sub-chart")   
                log.debug("Value of new parameter: " + str(results[item]))
            parameter = {}
            parameters[str(item)] = parameter
            parameter["chart-values-exist"] = "FALSE"
            parameter["chart-value"] = "n/a"
            parameter["eric-sc-umbrella-values-exist"] = "TRUE"
            parameter["eric-sc-umbrella-value"] = str(results[item])
            parameter["eric-sc-values-exist"] = "FALSE"
            parameter["eric-sc-value"] = "n/a"
            parameter["vnfd-inst-exist"] = "FALSE"
            parameter["vnfd-inst-value"] = "n/a"
            parameter["vnfd-upgr-exist"] = "FALSE"
            parameter["vnfd-upgr-value"] = "n/a"
            if not adps_exists(item, properties):
                log.warning("chart-values-exist: " + parameters[str(item)]["chart-values-exist"])
                log.warning("eric-sc-umbrella-values-exist: " + parameters[str(item)]["eric-sc-umbrella-values-exist"])
                log.warning("eric-sc-values-exist: " + parameters[str(item)]["eric-sc-values-exist"])
                log.warning("vnfd-inst-exist: " + parameters[str(item)]["vnfd-inst-exist"])
                log.warning("vnfd-upgr-exist: " + parameters[str(item)]["vnfd-upgr-exist"])
    return parameters


def create_results_csv(filename, parameters):
    if os.path.exists(filename):
        os.remove(filename)

    file = open(filename, "w+")
    log.info("Exporting results to file: " + os.path.abspath(filename))
    file.write(
        "parameter, chart-values-exist, chart-value, eric-sc-umbrella-values-exist, eric-sc-umbrella-value, eric-sc-values-exist, eric-sc-value, vnfd-inst-exist, vnfd-inst-value, vnfd-upgr-exist, vnfd-upgr-value" + "\n")

    for p_key, p_values in parameters.iteritems():
        file.write(p_key + "," + parameters[p_key]["chart-values-exist"] + "," + "\"" + parameters[p_key][
            "chart-value"] + "\"," +
                   parameters[p_key]["eric-sc-umbrella-values-exist"] + "," + "\"" + parameters[p_key][
                       "eric-sc-umbrella-value"] + "\"," +
                   parameters[p_key]["eric-sc-values-exist"] + "," + "\"" + parameters[p_key]["eric-sc-value"] + "\"," +
                   parameters[p_key]["vnfd-inst-exist"] + "," + "\"" + parameters[p_key]["vnfd-inst-value"] + "\"," +
                   parameters[p_key]["vnfd-upgr-exist"] + "," + "\"" + parameters[p_key]["vnfd-upgr-value"] + "\n")

    file.close()


def check_for_fails(parameters, status_code):
    sf = ScriptFiles.get_instance()

    # loop through yaml file and skip any blacklisted adp services
    properties = sf.check_and_return_file("chk_values_properties_file")

    parameters["eric-data-wide-column-database-cd.dataCenters"]["eric-sc-values-exist"] = \
        parameters["eric-data-wide-column-database-cd.dataCenters"]["vnfd-upgr-exist"]

    parameters["eric-stm-diameter.service.diameter.ports.tcp"]["eric-sc-values-exist"] = \
        parameters["eric-stm-diameter.service.diameter.ports.tcp"]["vnfd-upgr-exist"]

    parameters["eric-data-wide-column-database-cd.georeplication.certificates.trustedCertificateListName"]["eric-sc-values-exist"] = \
        parameters["eric-data-wide-column-database-cd.georeplication.certificates.trustedCertificateListName"]["vnfd-upgr-exist"]
            
    parameters["eric-data-wide-column-database-cd.georeplication.certificates.asymmetricKeyCertificateName"]["eric-sc-values-exist"] = \
        parameters["eric-data-wide-column-database-cd.georeplication.certificates.asymmetricKeyCertificateName"]["vnfd-upgr-exist"]
            

    for p_key, p_values in parameters.iteritems():
        
        # check vnf upgrade and instantiation
        if parameters[p_key]["vnfd-inst-exist"] != parameters[p_key]["vnfd-upgr-exist"]:
            status_code = 1
            if not adps_exists(p_key, properties):
                log.error("Parameter " + bcolors.FAIL + p_key + bcolors.ENDC + " should be the same in vnfd-inst-exist and in vnfd-upgr-exist")
                log.error("chart-values-exist: " + parameters[p_key]["chart-values-exist"])
                log.error("chart-value: " + parameters[p_key]["chart-value"])
                log.error("eric-sc-umbrella-values-exist: " + parameters[p_key]["eric-sc-umbrella-values-exist"])
                log.error("eric-sc-umbrella-value: " + parameters[p_key]["eric-sc-umbrella-value"])
                log.error("eric-sc-values-exist: " + parameters[p_key]["eric-sc-values-exist"])
                log.error("eric-sc-value: " + parameters[p_key]["eric-sc-value"])
                log.error("vnfd-inst-exist: " + parameters[p_key]["vnfd-inst-exist"])
                log.error("vnfd-inst-value: " + parameters[p_key]["vnfd-inst-value"])
                log.error("vnfd-upgr-exist: " + parameters[p_key]["vnfd-upgr-exist"])
                log.error("vnfd-upgr-value: " + parameters[p_key]["vnfd-upgr-value"])
            
        # check vnfd upgrade  / instatiation  / eric-sc-umbrellla
        if ( parameters[p_key]["eric-sc-values-exist"] != (parameters[p_key]["vnfd-inst-exist"] or parameters[p_key]["vnfd-upgr-exist"]) ):
            status_code = 1
            if not adps_exists(p_key, properties):
                log.error("Parameter '" + bcolors.FAIL + p_key + bcolors.ENDC + "' should exist both in eric-sc-values and vnfd parameters")
                log.error("chart-values-exist: " + parameters[p_key]["chart-values-exist"])
                log.error("chart-value: " + parameters[p_key]["chart-value"])
                log.error("eric-sc-umbrella-values-exist: " + parameters[p_key]["eric-sc-umbrella-values-exist"])
                log.error("eric-sc-umbrella-value: " + parameters[p_key]["eric-sc-umbrella-value"])
                log.error("eric-sc-values-exist: " + parameters[p_key]["eric-sc-values-exist"])
                log.error("eric-sc-value: " + parameters[p_key]["eric-sc-value"])
                log.error("vnfd-inst-exist: " + parameters[p_key]["vnfd-inst-exist"])
                log.error("vnfd-inst-value: " + parameters[p_key]["vnfd-inst-value"])
                log.error("vnfd-upgr-exist: " + parameters[p_key]["vnfd-upgr-exist"])
                log.error("vnfd-upgr-value: " + parameters[p_key]["vnfd-upgr-value"])
            

        ## check values between VNFD vs eric-sc-values

        # check if default sub-chart value modified in our umbrella chart
        if (((parameters[p_key]["eric-sc-umbrella-values-exist"]) == parameters[p_key]["chart-values-exist"] == "TRUE") 
            and (parameters[p_key]["chart-value"] != parameters[p_key]["eric-sc-umbrella-value"])):
            # log.warning("Parameter "+ bcolors.WARNING + p_key + bcolors.ENDC  +": default sub-chart value modified in our umbrella chart")
            if status_code != 1:
                status_code = 2

        # check eric-sc-values / chart-values / eric-sc-umbrella
        if ( (parameters[p_key]["eric-sc-values-exist"] == "TRUE") and 
            ((parameters[p_key]["chart-values-exist"]) == (parameters[p_key]["eric-sc-umbrella-values-exist"]) == "FALSE") ):
            # log.warning("Parameter "+ bcolors.WARNING + p_key + bcolors.ENDC  + " should exist in eric-sc-values and in sc-umbrella-values or chart-values")
            if status_code != 1:
                status_code = 2

        # check if a subchart's parameter has different value between the eric-sc-values and eric-sc-umbrella
        # check is happened only if the subchart's parameter exists both in eric-sc-values/eric-sc-umbrella
        if ((parameters[p_key]["eric-sc-values-exist"] == "TRUE") and (parameters[p_key]["eric-sc-umbrella-values-exist"] == "TRUE")):
                
            if parameters[p_key]["eric-sc-value"] != parameters[p_key]["eric-sc-umbrella-value"]:
                if status_code != 1:
                    status_code = 2
                if not adps_exists(p_key, properties):
                    log.error("Parameter " + bcolors.FAIL + p_key + bcolors.ENDC + " should have with the same default values between the eric-sc-values/eric-sc-umbrella")
                    log.error("chart-values-exist: " + parameters[p_key]["chart-values-exist"])
                    log.error("chart-value: " + parameters[p_key]["chart-value"])
                    log.error("eric-sc-umbrella-values-exist: " + parameters[p_key]["eric-sc-umbrella-values-exist"])
                    log.error("eric-sc-umbrella-value: " + parameters[p_key]["eric-sc-umbrella-value"])
                    log.error("eric-sc-values-exist: " + parameters[p_key]["eric-sc-values-exist"])
                    log.error("eric-sc-value: " + parameters[p_key]["eric-sc-value"])
                    log.error("vnfd-inst-exist: " + parameters[p_key]["vnfd-inst-exist"])
                    log.error("vnfd-inst-value: " + parameters[p_key]["vnfd-inst-value"])
                    log.error("vnfd-upgr-exist: " + parameters[p_key]["vnfd-upgr-exist"])
                    log.error("vnfd-upgr-value: " + parameters[p_key]["vnfd-upgr-value"])
                
    return status_code


def evaluate_status_code(status_code):
    if status_code == 1:
        log.error(
            "Exiting with error - check for vnf upgrade and instantiation misalignment or missing exposed parameter from sc-values or vnf-descriptor")
        sys.exit(1)
    elif status_code == 2:
        log.warning(
            "Exiting with warning - check for not extracted-missing parameters from sub-charts or different value in sc-umbrella-values and chart-values")
        sys.exit(2)
        # sys.exit(0)
    else:
        log.debug("Exiting successfully...")
        sys.exit(0)


# read lines mush faster
def _count_generator(reader):
    b = reader(1024 * 1024)
    while b:
        yield b
        b = reader(1024 * 1024)


# results = sc_values.yaml
def Check_if_exposed_params_exists_on_any_chart(results, parameters):
    
    # import blacklist yaml file
    sf = ScriptFiles.get_instance()
    # loop through yaml file and skip any blacklisted adp services
    properties = sf.check_and_return_file("chk_values_properties_file")

    counter = 0
    for item in results: 
        
        # adps check added here
        if "definitions" in str(item):
            continue
        
        if str(item) in parameters:
            counter +=1
            exposed_parameter_builder = ExporterFactory().get_object_of_type("ExposedChart")
            exposed_parameter_builder.set_name(str(item))
            exposed_parameter_builder.set_parameter(parameters[str(item)])
            parameter = parameters[str(item)]

            exposed_parameter_builder.set_is_present(parameter["eric-sc-values-exist"])
            exposed_parameter_builder.set_parameter_type("exposed parameters")
            exposed_parameter = exposed_parameter_builder.get_result()
            script_files.exposed_parameters.append(exposed_parameter)

            if not adps_exists(str(item), properties):
                log.debug(bcolors.UPDATE + "Updating" + bcolors.ENDC + " eric-sc-values-exist for: " + str(item))
                log.debug("Value of new parameter: " + str(results[item]))

            if exposed_parameter.exists() == "TRUE":
                counter +=1
                if not adps_exists(str(item), properties):
                    log.warning(bcolors.WARNING + "Parameter " + str(item) + " has already value in eric-sc-value that will be replaced!" + bcolors.ENDC)
            
            parameter["eric-sc-values-exist"] = "TRUE"
            parameter["eric-sc-value"] = str(results[item])
        else:
            if not adps_exists(str(item), properties):
                log.warning(bcolors.WARNING + "Parameter '" + str(item) + "' with value '" + str(results[item]) + "' exposed but not present in any chart." + bcolors.ENDC)
                
            parameter = {}
            parameters[str(item)] = parameter
            parameter["chart-values-exist"] = "FALSE"
            parameter["chart-value"] = "n/a"
            parameter["eric-sc-umbrella-values-exist"] = "FALSE"
            parameter["eric-sc-umbrella-value"] = "n/a"
            parameter["eric-sc-values-exist"] = "TRUE"
            parameter["eric-sc-value"] = str(results[item])
            parameter["vnfd-inst-exist"] = "FALSE"
            parameter["vnfd-inst-value"] = "n/a"
            parameter["vnfd-upgr-exist"] = "FALSE"
            parameter["vnfd-upgr-value"] = "n/a"
            if not adps_exists(str(item), properties):
                log.error("chart-values-exist: " + parameters[str(item)]["chart-values-exist"])
                log.error("eric-sc-umbrella-values-exist: " + parameters[str(item)]["eric-sc-umbrella-values-exist"])
                log.error("eric-sc-values-exist: " + parameters[str(item)]["eric-sc-values-exist"])
                log.error("vnfd-inst-exist: " + parameters[str(item)]["vnfd-inst-exist"])
                log.error("vnfd-upgr-exist: " + parameters[str(item)]["vnfd-upgr-exist"])
    
    print(counter)


def Check_VNFD_parameters(vnfd_values, parameters, parameter_type):
        
    vnfd_inst_count = 0

    if parameter_type == "instantiation":
        vnf_parameter_builder = ExporterFactory().get_object_of_type("InstantiateVnf")

        vnf_parameter_builder.set_value(
            vnfd_values["data_types"]["Ericsson.SC.datatypes.nfv.InstantiateVnfOperationAdditionalParameters"][
                "properties"])
        vnf_parameter_builder.set_parameter_type("instantiation")

        vnfd_values_short = \
            vnfd_values["data_types"]["Ericsson.SC.datatypes.nfv.InstantiateVnfOperationAdditionalParameters"][
                "properties"]
        vnfd_exist = "vnfd-inst-exist"
        vnfd_value = "vnfd-inst-value"
    elif parameter_type == "upgrade":
        vnf_parameter_builder = ExporterFactory().get_object_of_type("UpgradeVnf")

        vnf_parameter_builder.set_value(
            vnfd_values["data_types"]["Ericsson.SC.datatypes.nfv.ChangePackageVnfOperationAdditionalParameters"][
                "properties"])
        vnf_parameter_builder.set_parameter_type("upgrade")

        vnfd_values_short = \
            vnfd_values["data_types"]["Ericsson.SC.datatypes.nfv.ChangePackageVnfOperationAdditionalParameters"][
                "properties"]
        vnfd_exist = "vnfd-upgr-exist"
        vnfd_value = "vnfd-upgr-value"

    # import blacklist yaml file
    sf = ScriptFiles.get_instance()
    # loop through yaml file and skip any blacklisted adp services
    properties = sf.check_and_return_file("chk_values_properties_file")

    for vnfd_param_name in vnfd_values_short:
        
        if not adps_exists(vnfd_param_name, properties):
            log.debug("vnfd parameter: " + vnfd_param_name)

        if "metadata" in vnfd_values_short[vnfd_param_name]:
            vnfd_inst_count +=1
            if "chart_param" in vnfd_values_short[vnfd_param_name]["metadata"]:
                if not adps_exists(vnfd_param_name, properties):
                    log.debug("vnfd parameter: " + vnfd_param_name)

                vnf_parameter_builder.set_parameter(vnfd_values_short[vnfd_param_name]["metadata"]["chart_param"])

                chartparam = vnfd_values_short[vnfd_param_name]["metadata"]["chart_param"]

                if not adps_exists(str(chartparam), properties):
                    log.debug("vnfd chart parameter name: " + str(chartparam))

                if "default" in vnfd_values_short[vnfd_param_name].keys():
                    chartparamvalue = vnfd_values_short[vnfd_param_name]["default"]
                else:
                    chartparamvalue = None
                if not adps_exists(str(chartparam), properties):
                    log.debug("vnfd chart parameter value: " + str(chartparamvalue))
                parameter = {}

                if str(chartparam) in parameters:
                    vnf_parameter_builder.set_value(parameters[str(chartparam)])

                    parameter = parameters[str(chartparam)]
                    vnf_parameter_builder.set_is_present(parameter[vnfd_exist])
                    vnf_parameter = vnf_parameter_builder.get_result()
                    script_files.vnfd_parameters.append(vnf_parameter)

                    if parameter[vnfd_exist] == "TRUE":
                        if not adps_exists(str(chartparam), properties):
                            log.debug(bcolors.WARNING + "Parameter " + str(chartparam) + " is already true for " + vnfd_exist + "!" + bcolors.ENDC)      
                    parameter[vnfd_exist] = "TRUE"
                    parameter[vnfd_value] = str(chartparamvalue)
                else:
                    if not adps_exists(str(chartparam), properties):
                        log.error("VNFD " + parameter_type + " parameter '" + bcolors.FAIL + str(chartparam) + bcolors.ENDC + "' not present in chart or exposed parameters.")   
                    parameter["chart-values-exist"] = "FALSE"
                    parameter["chart-value"] = "n/a"
                    parameter["eric-sc-umbrella-values-exist"] = "FALSE"
                    parameter["eric-sc-umbrella-value"] = "n/a"
                    parameter["eric-sc-values-exist"] = "FALSE"
                    parameter["eric-sc-value"] = "n/a"
                    # they are the same up until here
                    # from here we need to branch
                    if parameter_type == "instantiation":
                        parameter["vnfd-inst-exist"] = "TRUE"
                        parameter["vnfd-inst-value"] = str(chartparamvalue)
                        parameter["vnfd-upgr-exist"] = "FALSE"
                        parameter["vnfd-upgr-value"] = "n/a"
                        if not adps_exists(str(chartparam), properties):
                            log.error("chart-values-exist: " + parameter["chart-values-exist"])
                            log.error("eric-sc-umbrella-values-exist: " + parameter["eric-sc-umbrella-values-exist"])
                            log.error("eric-sc-values-exist: " + parameter["eric-sc-values-exist"])
                            log.error("vnfd-inst-exist: " + parameter["vnfd-inst-exist"])
                            log.error("vnfd-upgr-exist: " + parameter["vnfd-upgr-exist"])
                    elif parameter_type == "upgrade":
                        parameter["vnfd-inst-exist"] = "FALSE"
                        parameter["vnfd-inst-value"] = "n/a"
                        parameter["vnfd-upgr-exist"] = "TRUE"
                        parameter["vnfd-upgr-value"] = str(chartparamvalue)
                        if not adps_exists(str(chartparam), properties):
                            log.error("chart-values-exist: " + parameters[str(chartparamvalue)]["chart-values-exist"])
                            log.error("eric-sc-umbrella-values-exist: " + parameters[str(chartparamvalue)]["eric-sc-umbrella-values-exist"])
                            log.error("eric-sc-values-exist: " + parameters[str(chartparamvalue)]["eric-sc-values-exist"])
                            log.error("vnfd-inst-exist: " + parameters[str(chartparamvalue)]["vnfd-inst-exist"])
                            log.error("vnfd-upgr-exist: " + parameters[str(chartparamvalue)]["vnfd-upgr-exist"])

                    parameters[str(chartparam)] = parameter
        else:
            if not adps_exists(vnfd_param_name, properties):
                log.debug(bcolors.WARNING + "No metadata.chart_param identified for " + vnfd_param_name + bcolors.ENDC)
    
    print('Vnfd count:',vnfd_inst_count)


# loops through ch_value_properties yaml file and returns true if "service" exists in blacklisted services
def adps_exists(service, properties):
    if type(service) != 'str':
        service = str(service)
    for item in properties["blacklisted-services"]:
        blacklist_service = str(item["service-name"])
        if blacklist_service.lower() in service.lower():
            return True
    return False


# return("eric-data-wide-column-database-cd" in item or "eric-stm-diameter" in item or "eric-data-key-value-database-rd-rlf" in item or "eric-scp" in item or "eric-sepp" in item or "eric-bsf" in item or "eric-sc-slf" in item)
def get_chk_values_properties(properties):
    if properties is None:
        property = properties
    return property


def main():
    """This is the main function that does all the work."""

    sf = ScriptFiles().get_instance()
    # gets username and password from ~/.docker/config file
    username, password = sf.get_credentials()

    args = parse_arguments()

    # set logging level
    set_logging_level(args.logLevel)

    # import yaml files, check if they exist

    # requirements_yaml_file = script_files.script_paths.get("requirements_yaml_file")
    # check_files(requirements_yaml_file)
    # log.debug("requirements.yaml file to be used: " + requirements_yaml_file)
    # requirements = read_yaml(requirements_yaml_file)
    # if requirements is None: exit(1)

    # instead of the above, we use the following:

    requirements = sf.check_and_return_file("requirements_yaml_file")
    values = sf.check_and_return_file("values_yaml_file")
    #maybe useless
    sc_values = sf.check_and_return_file("sc_values_yaml_file")
    vnfd_values = sf.check_and_return_file("vnfd_yaml_file")
    


    new_sc_values_yaml_file = "%s/new-eric-sc-values.yaml" % script_files.defaultCustomerChartDir
    simple_remove_comments(sf.script_paths.get("sc_values_yaml_file"), new_sc_values_yaml_file)
    check_files(new_sc_values_yaml_file)
    log.debug("new eric-sc-values.yaml file to be used: " + new_sc_values_yaml_file)

    sc_values = read_yaml(new_sc_values_yaml_file)
    if sc_values == None : exit(1)
    
    # Cleanup actions
    cleanup_file(new_sc_values_yaml_file)

    # vnfd_values = read_yaml(vnfd_yaml_file)

    parameters = {}
    print("-------------------------------------------------")
    print("Checking sub-charts included in SC umbrella chart")
    print("-------------------------------------------------")

    # base 64 encoding of username and password
    base64string = base64.b64encode('%s:%s' % (username, password))

    exporter_factory = ExporterFactory()

    for dependency in requirements["dependencies"]:
        # return app dependency builder object
        app_dependency = exporter_factory.get_object_of_type("Dependencies")

        # build obj
        app_dependency.set_name(dependency["name"])
        app_dependency.set_repository(dependency["repository"])
        app_dependency.set_version(dependency["version"])

        # return app dependency obj
        app_dependency = app_dependency.get_result()

        # append obj to array
        script_files.app_dependencies.append(app_dependency)

        sub_chart = app_dependency.name + "-" + app_dependency.version
        if "eric-sc" in app_dependency.name or "eric-bsf" in app_dependency.name or "eric-sepp" in dependency["name"] \
                or "eric-csa" in app_dependency.name or "rd-operand" in app_dependency.name:
            url = app_dependency.repository + app_dependency.name + "-" + app_dependency.version + ".tgz"
        else:
            url = app_dependency.repository + app_dependency.name + "//" + app_dependency.name + "-" + app_dependency.version + ".tgz"

        # removed zip_file as it's not used anywhere else
        zip_file_full_path = exec_path + "/" + sub_chart + ".tgz"

        values_file = sub_chart = "-values.yaml"
        values_file_full_path = exec_path + "/" + values_file

        new_values_file_full_path = exec_path + "/" + "new-" + sub_chart + "-values.yaml"
        try:
            request = urllib2.Request(url)
            log.debug("Downloading zipped file from " + url)
            request.add_header("Authorization", "Basic %s" % base64string)
            with closing(urllib2.urlopen(request)) as downloaded_file:
                log.debug("Storing zipped file to " + zip_file_full_path)
                with open(zip_file_full_path, 'w') as zipped_file:
                    zipped_file.write(downloaded_file.read())

            log.debug("Opening zipped file from " + str(zip_file_full_path))

            with closing(tarfile.open(zip_file_full_path, "r")) as tar_file:
                for member in tar_file.getmembers():
                    # is reg same as is file(returns true if object is regular file)
                    if member.isreg() and re.search(app_dependency.name + "/values.yaml", member.name):
                        # print("Select member from values file: " + values_file)
                        member.name = os.path.basename(values_file)  # remove the path by reset it
                        # print("Extracting member: " + member.name + " from path: " + exec_path)
                        tar_file.extract(member, exec_path)  # extract
            # TODO: remove comments to eliminate comparison warnings
            # ignore comments starting with "#" and do not include ":"
            # huge problems
            # remove_comments(values_file_full_path, new_values_file_full_path)
            # check_files(new_values_file_full_path)
            # log.debug("new " + app_dependency.name + "/values.yaml file to be used: " + new_values_file_full_path)
            # tmp_values = read_yaml(new_values_file_full_path)
            # print(values_file_full_path)
            tmp_values = read_yaml(values_file_full_path)  # this is read it from the charts

            if tmp_values is None:
                log.error("Unexpected error while reading subchart values.yaml file")
                exit(1)

            results = flatten(tmp_values, app_dependency.name, ".")

            parameters = update(results, parameters)

            # cleanup actions
            log.debug("Cleanup actions - Delete sub-chart helm chart and values.yaml files")

            cleanup_file(zip_file_full_path)

            cleanup_file(values_file_full_path)
            # TODO: if cleanup of "#" is possible then uncomment this part
            # if(os.path.isfile(new_values_file_full_path)):
            #    log.debug("Deleting not needed values.yaml file " + new_values_file_full_path)
            #    os.remove(new_values_file_full_path)
            #    log.debug(new_values_file_full_path + " file deleted successfully")
            # else:
            #    log.error(new_values_file_full_path + " file does not exist")

        except (ValueError, urllib2.URLError) as e:
            log.error(e)
            #log.error("Unexpected exception during the extraction of sub-chart values.yaml files\n" + e)
    
    log.error("Test")

    print("-------------------------------------")
    print("Checking SC umbrella chart parameters")
    print("-------------------------------------")

    # get all parameters from eric-sc-umbrella/values.yaml
    results = flatten(values, parent_key='', sep='.')
    parameters = update_umbrella(results, parameters)

    # gather all parameters from eric-sc-values
    parameter = {}
    
    
    print("----------------------------------------------------")
    print("Check if exposed parameters are present on any chart")
    print("----------------------------------------------------")
    results = flatten(sc_values, parent_key='', sep='.')
    
    Check_if_exposed_params_exists_on_any_chart(results, parameters)

    
    print("--------------------------------------")
    print("Checking VNFD-instantiation parameters")
    print("--------------------------------------")

    # check VNFD instatiation parameters
    Check_VNFD_parameters(vnfd_values, parameters, "instantiation")

    print("--------------------------------")
    print("Checking VNFD-upgrade parameters")
    print("--------------------------------")

    # check VNFD_upgrade_parameters
    Check_VNFD_parameters(vnfd_values, parameters, "upgrade")


    print("--------------------------------------------------------------------------------------------------------")
    print("Saving the results in csv in path: /local/$USER/5g_proto/baseline_scripts/scripts/chk_values_results.csv")
    print("--------------------------------------------------------------------------------------------------------")
    filename = "chk_values_results.csv"
    create_results_csv(filename, parameters)

    #raw_input("WAIT 6: ")
    print("----------------------------------------------")
    print("Checking for miss-alignments in the parameters")
    print("----------------------------------------------")
    status_code = 0
    status_code = check_for_fails(parameters, status_code)
    # evaluate_status_code(status_code)


# Not used
def print_parameters(parameters, service):
    for k in parameters.iterkeys():
        if service in k:
            print(k + " " + parameters[k]["chart-values-exist"] + " " + parameters[k]["chart-value"] + " " +
                  parameters[k]["eric-sc-umbrella-values-exist"] + " " + parameters[k]["eric-sc-umbrella-value"]
                  + " " + parameters[k]["eric-sc-values-exist"] + " " + parameters[k]["eric-sc-value"] + " " +
                  parameters[k]["vnfd-inst-exist"] + " " + parameters[k]["vnfd-upgr-exist"]
                  )


# Not used
def print_all_parameters(parameters):
    for k in parameters.iterkeys():
        print(k + " " + parameters[k]["chart-values-exist"] + " " + parameters[k]["chart-value"] + " " + parameters[k][
            "eric-sc-umbrella-values-exist"] + " " + parameters[k]["eric-sc-umbrella-value"]
              + " " + parameters[k]["eric-sc-values-exist"] + " " + parameters[k]["eric-sc-value"] + " " +
              parameters[k]["vnfd-inst-exist"] + " " + parameters[k]["vnfd-upgr-exist"]
              )


# Not used
def chk_parameters(parameters, key):
    found_params = []
    for k, v in parameters.iteritems():
        if key in k:
            print(str(k) + " -VS- " + str(key) + " --value: " + str(v))
            found_params.append(k)

    for par in found_params:
        parameter = parameters[par]
        v = parameter["chart-value"]
        log.info("Identified parameter: '" + par + "'")
        log.info("         chart-value: '" + parameter["chart-value"] + "'")
        log.info("         chart-value: '" + parameter["eric-sc-umbrella-value"] + "'")
        log.info("         chart-value: '" + parameter["eric-sc-value"] + "'")
        log.info("         chart-value: '" + parameter["vnfd-inst-exist"] + "'")
        log.info("         chart-value: '" + parameter["vnfd-upgr-exist"] + "'")


def cleanup_file(full_path_file):
    log.debug("Cleanup actions - Delete " + full_path_file)
    if (os.path.isfile(full_path_file)):
        log.debug("Deleting not needed values.yaml file " + full_path_file)
        os.remove(full_path_file)
        log.debug(full_path_file + " file deleted successfully")
    else:
        log.error(full_path_file + " file does not exist")


if __name__ == "__main__":
    main()

