# 2020-10-21 Alina Behrens
from PySide2.QtWidgets import QWidget, QVBoxLayout, QSplitter

from gui.button_box_tabs import ButtonBoxTabs
from gui.log_area import LogArea


class CentralWindow(QWidget):
    def __init__(self, button_box_configs, current_button_box, buttons_config, main_window_width,
                 buttonbox_width):
        """
        Creates an object of the class CentralWindow that represents the center of the fiveshell window including the
        button box and the log area (central widget of the main window). (Main menu items are not included)

        :param button_box_configs: the part of the config file specifying the buttons and sections on the left
        :param current_button_box: the name of the currently selected button box
        :param buttons_config: the part of the config file specifying all available buttons
        :param main_window_width: the width of the window
        :param buttonbox_width: the width of the button box (left side of the fiveshell window)
        """
        super().__init__()

        self.button_box_tabs = ButtonBoxTabs(button_box_configs, current_button_box, buttons_config)
        self.log_area = LogArea()

        # Layout:
        self.main_layout = QVBoxLayout()
        self.splitter = QSplitter()
        self.splitter.addWidget(self.button_box_tabs)
        self.splitter.addWidget(self.log_area.scroll_area)
        self.splitter.setSizes([buttonbox_width, main_window_width - buttonbox_width])

        self.main_layout.addWidget(self.splitter)

        self.setLayout(self.main_layout)
