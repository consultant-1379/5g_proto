# 2020-11-25 Alina Behrens
import sys
import time

from PySide2.QtCore import QProcess

from model.linux_executor import LinuxExecutor


class LinuxExecutorNoLog(LinuxExecutor):
    """LinuxExecutor is a subclass of the abstract class ActionExecutor that implements the method exec_command
    for a linux command"""
    def __init__(self, action_id, action_name, command, variables, directory):
        """
        Creates a new LinuxExecutor object to execute the linux command specified in the associated action.
        It does not create a new log view window.

        :param action_id: the unique id of the action
        :param action_name: the name of the action
        :param command: the linux command that will be executed
        :param variables: the variables the linux command needs
        :param directory: the directory in which the command should be executed
        """
        super().__init__(action_id, action_name, command, variables, directory)

        self.action_id = action_id
        self.action_name = action_name

        # the current time will be the unique id of the log view that will be created and filled
        self.time = time.time()

        self.command = command
        self.variables = variables
        self.directory = directory
        self.process = None

    def exec_command(self):
        """This method executes the linux command without sending anything to a log view.
        It overwrites the exec_command method of LinuxExecutor"""
        self.prepare_per_command_variables(self.variables)
        try:
            command = self.interpolate_variables(self.command)
        except:
            text = f"FiveShell Error, action execution resulted in exception: {sys.exc_info()}"
            print(text)
            return  # doesn't make sense to continue here

        self.process = QProcess()
        if self.directory:
            try:
                self.directory = self.interpolate_variables(self.directory)
            except:
                text = f"FiveShell Error, cannot find directory: {sys.exc_info()}"
                print(text)
                return  # doesn't make sense to continue here
            self.process.setWorkingDirectory(self.directory)

        print(f"Button clicked for command: {command}")

        self.process.start(command)
        self.is_working = False

    def new_log_view(self):
        """
        overwrites the new_log_view method of ActionExecutor so that no new log view is created
        """
        pass
