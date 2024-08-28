# 2020-10-14 Alina Behrens
from PySide2.QtWidgets import QAction

import g
from model.linux_executor import LinuxExecutor
from model.python_executor import PythonExecutor
from model.linux_executor_no_log import LinuxExecutorNoLog
from model.python_executor_no_log import PythonExecutorNoLog


class ActionCreator:
    """A class to create all actions from the config file"""
    def __init__(self, action_config):
        """
        Creates an ActionCreator object that can create an action dictionary
        :param action_config: the dictionary of actions in the config file
        """
        self.action_config = action_config
        self.executors = []

    def create_action_dict(self):
        """
        fills the dictionary g.actions with the key action_id and the value QAction object
        """
        for action in self.action_config:  # for each action
            action_id = action
            current_action_dict = self.action_config[action]

            action_name = current_action_dict["label"]
            status_tip = current_action_dict["tooltip"]
            command = current_action_dict["cmd"]
            cmd_type = current_action_dict["type"]

            variables = current_action_dict["variables"] if "variables" in current_action_dict else None
            loop = current_action_dict["loop"] if "loop" in current_action_dict else None
            directory = current_action_dict["dir"] if "dir" in current_action_dict else None
            shortcut = current_action_dict["shortcut"] if "shortcut" in current_action_dict else None

            executor = None
            if cmd_type == 'linux':  # if the action has a linux command create a new LinuxExecutor object
                executor = LinuxExecutor(action_id, action_name, command, variables, directory)
            elif cmd_type == 'linux no log':
                executor = LinuxExecutorNoLog(action_id, action_name, command, variables, directory)
            elif cmd_type == 'python':  # if the action has a python command create a new PythonExecutor object
                executor = PythonExecutor(action_id, action_name, command, variables, loop)
            elif cmd_type == 'python no log':
                executor = PythonExecutorNoLog(action_id, action_name, command, variables, loop)

            q_action = self.create_action(action_name, status_tip, shortcut, executor)
            g.actions[action_id] = q_action  # adds the action to the global action dictionary
            self.executors.append(executor)

    @staticmethod
    def create_action(action_name, status_tip, shortcut, executor):
        """
        creates an QAction object with the given name and status tip that calls the method exec_action from the given
        executor when the action is triggered.

        :param action_name: The name of the action
        :param status_tip: The status tip of the action
        (tooltip when hover over buttons, status when hover over menu actions)
        :param shortcut: An optional shortcut
        :param executor: an ActionExecutor object
        :return: the QAction object
        """
        action = QAction(action_name)
        action.setStatusTip(status_tip)
        if shortcut:
            action.setShortcut(shortcut)
        action.triggered.connect(executor.exec_action)
        return action

    def update_action_config(self, action_config):
        self.action_config = action_config
