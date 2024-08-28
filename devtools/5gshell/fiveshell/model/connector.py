# 2020-10-14 Alina Behrens
from PySide2.QtWidgets import QPushButton

import g


def connect_all():
    """connects all signals and slots"""
    for window in g.main_windows:
        # connect all action_executors to the slots new_section, add_output, end_cmd
        for executor in g.action_creator.executors:
            if executor not in window.main_widget.log_area.connected_to:
                executor.signal_new_log_window.connect(window.main_widget.log_area.new_section)
                executor.signal_add_output.connect(window.main_widget.log_area.add_output)
                executor.signal_end_cmd.connect(window.main_widget.log_area.end_cmd)
                window.main_widget.log_area.connected_to.append(executor)

        # connect all buttons of the window to its actions
        for button_box in window.main_widget.button_box_tabs.tabs:
            for section in button_box.accordion.sections:
                for button in section.buttons.buttons:
                    if type(button) is QPushButton:
                        if button.property('connected') is None:
                            for action in button.property("action_ids"):
                                button.clicked.connect(g.actions[action].trigger)
                            button.setProperty('connected', True)
