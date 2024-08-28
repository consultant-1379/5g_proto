# 2020-10-14 Alina Behrens
import sys
import time

from PySide2.QtCore import QProcess, Slot

from model.action_executor import ActionExecutor


class LinuxExecutor(ActionExecutor):
    """LinuxExecutor is a subclass of the abstract class ActionExecutor that implements the method exec_command
    for a linux command"""
    def __init__(self, action_id, action_name, command, variables, directory):
        """
        Creates a new LinuxExecutor object to execute the linux command specified in the associated action.

        :param action_id: the unique id of the action
        :param action_name: the name of the action
        :param command: the linux command that will be executed
        :param variables: the variables the linux command needs
        :param directory: the directory in which the command should be executed
        """
        super().__init__()

        self.action_id = action_id
        self.action_name = action_name

        self.command = command
        self.variables = variables
        self.directory = directory
        self.process = None

    def exec_cmd(self):
        """Called to execute a linux action.
        Executes the linux command and sends the result as Signals to the log view.
        A log view with the current time must have been already created before calling this method."""
        self.prepare_per_command_variables(self.variables)
        try:
            command = self.interpolate_variables(self.command)
        except:
            text = f"FiveShell Error, action execution resulted in exception: {sys.exc_info()}"
            self.signal_add_output.emit(text, self.time)  # sends the output to the log view with the given time
            self.signal_end_cmd.emit(self.time)  # sends the information that the output is finished
            self.is_working = False
            return  # doesn't make sense to continue here

        output = "\u25B9 " + command + "\n"
        self.process = QProcess()
        if self.directory:
            try:
                self.directory = self.interpolate_variables(self.directory)
            except:
                text = f"FiveShell Error, cannot find directory: {sys.exc_info()}"
                self.signal_add_output.emit(text, self.time)  # sends the output to the log view with the given time
                self.signal_end_cmd.emit(self.time)  # sends the information that the output is finished
                self.is_working = False
                return  # doesn't make sense to continue here
            output += f"\u25B9 cd {self.directory} + \n"
            self.process.setWorkingDirectory(self.directory)

        print(f"Button clicked for command: {command}")

        # sends the output including action name and commands to the log view with the given time
        self.signal_add_output.emit(output, self.time)

        self.process.finished.connect(self.on_cmd_finished)
        self.process.readyReadStandardOutput.connect(self.on_cmd_output)
        self.process.readyReadStandardError.connect(self.on_cmd_error_output)
        self.process.start(command)

    @Slot(int)
    def on_cmd_finished(self, exit_code):
        """Called when a Unix command finished. Sends all output to the associated log view."""
        print(f"Exit code: {exit_code}")
        printout = str(self.process.readAllStandardOutput(), "utf-8")
        if len(printout) > 0:
            self.signal_add_output.emit(printout + "\n", self.time)
        print(printout)
        printout = str(self.process.readAllStandardError(), "utf-8")
        if len(printout) > 0:
            self.signal_add_output.emit(printout + "\n", self.time)
        self.process.close()
        self.signal_end_cmd.emit(self.time)  # sends the output to the log view
        self.is_working = False

    @Slot()
    def on_cmd_output(self):
        """Called when the Unix process produced text on stdout. Sends all output to the associated log view."""
        printout = str(self.process.readAllStandardOutput(), "utf-8")
        if len(printout) > 0:
            self.signal_add_output.emit(printout + "\n", self.time)
        print(printout)

    @Slot()
    def on_cmd_error_output(self):
        """Called when the Unix process produced text on stderr. Sends all output to the associated log view."""
        printout = str(self.process.readAllStandardError(), "utf-8")
        if len(printout) > 0:
            self.signal_add_output.emit(printout + "\n", self.time)
        print(printout)
