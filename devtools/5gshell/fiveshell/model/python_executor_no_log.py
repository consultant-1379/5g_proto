# 2020-11-25 Alina Behrens
import time

from model.python_executor import PythonExecutor


class PythonExecutorNoLog(PythonExecutor):
    """PythonExecutor is a subclass of the abstract class ActionExecutor that implements the method exec_command
    for a python command"""
    def __init__(self, action_id, action_name, command, variables, loop):
        """
        Creates a new PythonExecutorNoLog object to execute the python command specified in the associated action.
        It does not create a new log view window.

        :param action_id: the unique id of the action
        :param action_name: the name of the action
        :param command: the python command that will be executed
        :param variables: the variables the python command uses
        :param loop:
        """
        super().__init__(action_id, action_name, command, variables, loop)

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
        self.exec_command_without_output()
        self.is_working = False

    def new_log_view(self):
        """
        overwrites the new_log_view method of ActionExecutor so that no new log view is created
        """
        pass
