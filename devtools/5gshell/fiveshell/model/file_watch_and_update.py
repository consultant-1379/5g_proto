# 2020-10-21 Alina Behrens
from PySide2.QtCore import QFileSystemWatcher

from model.connector import connect_all
from utils.config_utils import *


def update_fiveshell():
    """updates the fiveshell window when the config changes"""

    load_config_file()
    button_box_configs = g.config["button_boxes"]
    current_button_box = button_box_configs[0]["name"]
    buttons_config = g.config["buttons"]
    main_window_config = g.config["main_window"]
    action_config = g.config["actions"]

    g.actions = {}
    g.action_creator.update_action_config(action_config)
    g.action_creator.create_action_dict()

    for window in g.main_windows:
        window.update_menu_bar(main_window_config)
        window.main_widget.button_box_tabs.create_tabs(button_box_configs, buttons_config, current_button_box)

    connect_all()

    # the file watcher needs to be set again after each change, because
    # some text editors don't modify the contents of a file, but replace the original file with a new file.
    # So replacing a file will delete the old one and qt will stop watching the file.
    start_watching_config()


def update_k8s_cluster_namespace():
    try:
        read_k8s_cluster_namespace()
        reload_k8s_data()
    except Exception as e:
        message = "An error occurred while trying to connect to {}. \n" \
                  "The context will be automatically reset to {}.".format(g.globalDict["kube_context"],
                                                                          g.globalDict["last_working_kube_context"])

        g.actions["reset_kube_context"].trigger()
        g.globalDict["kube_context"] = g.globalDict["last_working_kube_context"]

        msg = QMessageBox()
        msg.setWindowTitle("ERROR")
        msg.setIcon(QMessageBox.Critical)
        msg.setText(message)
        msg.setDetailedText(str(e))

        msg.exec_()
    # update labels
    for window in g.main_windows:
        context_list, current_context_index = get_list_of_available_kube_contexts()
        window.update_drop_down_list(context_list, current_context_index)

    # the file watcher needs to be set again after each change, because
    # some text editors don't modify the contents of a file, but replace the original file with a new file.
    # So replacing a file will delete the old one and qt will stop watching the file.
    start_watching_k8s()


def start_watching_config():
    paths = g.file_watchers["config"]["paths"]
    g.file_watchers["config"]["watcher"] = QFileSystemWatcher(paths)
    g.file_watchers["config"]["watcher"].fileChanged.connect(update_fiveshell)


def start_watching_k8s():
    paths = g.file_watchers["kube_config"]["paths"]
    g.file_watchers["kube_config"]["watcher"] = QFileSystemWatcher(paths)
    g.file_watchers["kube_config"]["watcher"].fileChanged.connect(update_k8s_cluster_namespace)
