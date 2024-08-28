# 2020-10-14 Alina Behrens
import sys
import time
import traceback

from model.action_executor import ActionExecutor

# TODO import all python functions called by the config file here
import g
# DO NOT DELETE THESE IMPORTS
from pprint import pformat
from commands.fiveshell_cmds import *
from model.gui_creator import *
from commands.envoy import *
from utils.config_utils import *
from commands.load_update_config_netconf import *


class PythonExecutor(ActionExecutor):
    """PythonExecutor is a subclass of the abstract class ActionExecutor that implements the method exec_command
    for a python command"""
    def __init__(self, action_id, action_name, command, variables, loop):
        """
        Creates a new PythonExecutor object to execute the python command specified in the associated action.

        :param action_id: the unique id of the action
        :param action_name: the name of the action
        :param command: the python command that will be executed
        :param variables: the variables the python command uses
        :param loop:
        """
        super().__init__()

        self.action_id = action_id
        self.action_name = action_name

        # the current time will be the unique id of the log view that will be created and filled
        self.time = time.time()

        self.command = command
        self.variables = variables
        self.loop = loop

    def exec_cmd(self):
        """Called to execute a python action.
        Executes the python command and sends the result as Signals to the log view.
        A log view with the current time must have been already created before calling this method."""
        text = self.exec_command_without_output()

        self.signal_add_output.emit(str(text), self.time)
        self.signal_end_cmd.emit(self.time)
        self.is_working = False

    def execute_single_cmd(self):
        """Execute one python command after expanding variables
        from the global dictionary. This function is called
        for single- and also looped-Python commands"""
        try:
            command = self.interpolate_variables(self.command)
        except:
            text = f"FiveShell Error, action execution resulted in exception: {sys.exc_info()}"
            print(text)
            return text  # doesn't make sense to continue here

        print(f"### Execute python function \u226B {command} \u226A")
        try:
            result = eval(command)
        except:
            result = f"###Error: {sys.exc_info()}"
            print(traceback.print_tb(sys.exc_info()[2]))
            print(result)
        return result

    def exec_command_without_output(self):
        """Executes the python command without sending anything to a log view"""
        self.prepare_per_command_variables(self.variables)
        text = ""
        if self.loop:
            for value in g.globalDict[self.loop["variable"]]:
                g.globalDict[self.loop["name"]] = value
                text += self.execute_single_cmd()
        else:
            text = self.execute_single_cmd()
        return text
