#!/bin/env python3

import signal
from pprint import pprint
from packaging import version

import PySide2
from PySide2.QtCore import QTimer

from commands.fiveshell_cmds import quit_fiveshell
from model.action_creator import ActionCreator
from model.file_watch_and_update import *
from model.gui_creator import create_fiveshell_window
from utils.config_utils import *

if __name__ == "__main__":
    # CTRL-C in the shell where this program is started doesn't work
    # without this trick (stackoverflow #4938723)
    # Second part is below where we set a periodic timer to ensure
    # that Qt's event loop is triggered often enough
    if version.parse(PySide2.__version__) <= version.parse("5.15.1"):
        print("The PySide2 version is lower than 15.1.1. Please update to continue. \n"
              "pip3 install -r requirements.txt")
        raise ResourceWarning("Versions do not fulfill requirements")

    signal.signal(signal.SIGINT, quit_fiveshell)

    app = QApplication(sys.argv)
    g.app = app

    # Second part of the CTRL-C handling solution/trick:
    timer = QTimer()
    timer.start(500)
    timer.timeout.connect(lambda: None)

    public_config_filename = "config.yaml"
    user_config_filename = str(Path.home()) + "/.fiveshellrc"
    g.user_config_filename = user_config_filename
    g.example_user_config_filename = "user_config_example.yaml"
    kube_config = str(Path.home()) + "/.kube/config"
    load_config_and_k8s_data(public_config_filename, user_config_filename)

    action_config = g.config["actions"]

    g.action_creator = ActionCreator(action_config)
    g.action_creator.create_action_dict()

    create_fiveshell_window()

    # Monitor the config files and auto-reload them when changed:
    paths = [public_config_filename, user_config_filename]
    g.file_watchers["config"] = {"paths": paths}
    g.file_watchers["kube_config"] = {"paths": [kube_config]}
    start_watching_config()
    start_watching_k8s()

    pprint(g.globalDict)
    print("done")

    app.exec_()
