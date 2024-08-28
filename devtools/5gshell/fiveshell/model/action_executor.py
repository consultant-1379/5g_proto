# 2020-10-14 Alina Behrens
import time
from abc import abstractmethod, ABCMeta

import jmespath

from PySide2.QtCore import QObject, Signal
from PySide2.QtWidgets import QMessageBox

import g


class FinalMeta(type(QObject), ABCMeta):  # this class is used to avoid a meta class conflict of QObject and ABC
    # see
    # https://stackoverflow.com/questions/54635205/how-do-i-use-generic-typing-with-pyqt-subclass-without-metaclass-conflicts
    # and
    # https://stackoverflow.com/questions/46837947/how-to-create-an-abstract-base-class-in-python-which-derived-from-qobject
    pass


class ActionExecutor(QObject, metaclass=FinalMeta):
    signal_new_log_window = Signal(str, float)
    signal_add_output = Signal(str, float)  # TODO this might not stay a string
    signal_end_cmd = Signal(float)

    def __init__(self):
        """
        ActionExecutor objects can execute the command of the given (in the subclasses) action.
        """
        self.time = None
        self.is_working = False
        super().__init__()

    def exec_action(self):
        """executes the command of the action"""
        # the current time will be the unique id of the log view that will be created and filled
        self.time = time.time()
        if self.is_working:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("Sorry, this action is busy right now (possibly in a different fiveshell window).  "
                        "\nPlease try again later.")
            msg.exec_()
        else:
            self.is_working = True
            self.new_log_view()
            self.exec_cmd()  # execute the command

    def new_log_view(self):
        # send a signal, so that a new log view window is created with the given time as id
        # (self.time is created in the subclasses)
        self.signal_new_log_window.emit(self.action_name, self.time)
        output = "\u25B6 " + self.action_name + "\n"
        self.signal_add_output.emit(output, self.time)  # send the action name to this new log window

    @abstractmethod
    def exec_cmd(self):
        """Abstract method that executes a command and sends all output as Signals to the log view with the current
        timestamp.
        A log view with the current time must have been already created before calling this method."""
        pass  # implemented in the subclasses LinuxExecutor and PythonExecutor

    @staticmethod
    def prepare_per_command_variables(variables):
        """Some commands have their own dynamic variables (jmespath
        expressions) that need to be prepared just-in-time when the
        button is clicked. This function evaluates these variables and
        stores them into the global dict as a normal variable."""
        if variables:
            for variable in variables:
                var_filter = variable['filter']
                var_value = jmespath.search(var_filter, g.globalDict)
                g.globalDict[variable['name']] = var_value

    @staticmethod
    def interpolate_variables(string):
        """Given a string (usually containing a button command or variable)
        interpolate all variables inside and return the interpolated
        string."""
        old_string = ""
        while string != old_string:
            old_string = string
            string = string.format(**g.globalDict)
        return string
