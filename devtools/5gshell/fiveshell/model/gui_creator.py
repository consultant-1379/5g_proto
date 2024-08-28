# 2020-10-20 Alina Behrens
from gui.about_window import AboutWindow
from gui.kubernetes_mangement_window import KubernetesManagementWindow
from gui.main_window import MainWindow
from gui.preferences.preferences_window import PreferencesWindow
from model.connector import connect_all
from utils.gui_utils import *
from utils.config_utils import *


# this function is necessary to create more than one fiveshell window
def create_fiveshell_window():
    """Creates a new fiveshell window. """
    button_box_configs = g.config["button_boxes"] if "button_boxes" in g.config else []
    current_button_box = button_box_configs[0]["name"]
    main_window_config = g.config["main_window"]
    buttons_config = g.config["buttons"]
    available_kube_contexts, index_current_context = get_list_of_available_kube_contexts()
    main_window_width, main_window_height, buttonbox_width, main_window_pos = get_configured_window_dimensions()
    window = MainWindow(main_window_config, button_box_configs, current_button_box,
                        buttons_config, main_window_width, buttonbox_width,
                        available_kube_contexts, index_current_context)
    if not g.main_windows:  # if its the first window
        window.resize(main_window_width, main_window_height)
        window.move(main_window_pos)
    else:
        width = g.app.activeWindow().width()  # get the dimensions of the active window
        height = g.app.activeWindow().height()
        pos = g.app.activeWindow().pos()
        pos += QPoint(20, 20)  # move the new window a bit to the right
        window.resize(width, height)
        window.move(pos)

    g.main_windows.append(window)

    connect_all()
    window.show()

    return "finished"


def open_about_window():
    about = AboutWindow()
    about.exec_()
    about.show()


def open_fiveshell_documentation():
    QDesktopServices.openUrl("https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/FiveShell")


def open_preferences():
    if not g.preference_window:  # if no preferences window is open
        main_window_width, main_window_height, buttonbox_width, main_window_pos = get_configured_window_dimensions()
        button_box_configs = g.config["button_boxes"] if "button_boxes" in g.config else []
        variables_dict = get_used_variables_dict(g.config)
        preferences = PreferencesWindow(g.config["actions"], button_box_configs, g.config["buttons"],
                                        g.user_config_filename,
                                        2 / 3 * main_window_width, 2 / 3 * main_window_height, g.actions,
                                        variables_dict)
        preferences.move(main_window_pos)
        g.preference_window = preferences  # so its not removed by the garbage collector
        preferences.show()
    else:
        g.preference_window.activateWindow()  # brings the open preferences window to the front


def open_kubernetes_manager():
    if not g.kubernetes_management_window:  # if no preferences window is open
        main_window_width, main_window_height, buttonbox_width, main_window_pos = get_configured_window_dimensions()
        kube_manager = KubernetesManagementWindow(1 / 2 * main_window_width, 1 / 2 * main_window_height)
        kube_manager.move(main_window_pos)
        g.kubernetes_management_window = kube_manager  # so its not removed by the garbage collector
        kube_manager.show()
    else:
        g.kubernetes_management_window.activateWindow()  # brings the open preferences window to the front
