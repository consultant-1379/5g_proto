from PySide2 import QtCore
from PySide2.QtGui import QCloseEvent, QIcon, QKeySequence, QDesktopServices
from PySide2.QtWidgets import QWidget, QVBoxLayout, QPushButton, QMessageBox, QHBoxLayout, QShortcut, QLabel, \
    QSizePolicy, QTextEdit, QSpacerItem

import g
from gui.preferences.config_structures import Button, ButtonInBox
from gui.preferences.list_widget import ButtonListWidget
from gui.preferences.preferences_button_box_tabs import ButtonBoxPreferenceTabs
from gui.preferences.preferences_column import ButtonPreferences
from gui.preferences.tree_widget import ButtonBoxTreeWidget
from utils.config_utils import update_button_boxes_in_user_config, get_current_button_box_configs


class PreferencesWindow(QWidget):
    def __init__(self, action_config, button_box_configs, buttons_config, user_config_filename, window_width,
                 window_height, actions, variables_dict):
        super().__init__()  # TODO think about what happens if the config changes
        self.is_modified = False

        self.setWindowTitle("Preferences")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        self.user_config_filename = user_config_filename

        self.action_config = action_config
        self.button_box_configs = button_box_configs
        self.buttons_config = buttons_config
        self.actions = actions

        self.enable_shortcuts()

        self.info_label = QLabel("You can configure your buttons and groups by dragging the buttons from the right "
                                 "to the groups on the left.")
        self.info_label.setSizePolicy(QSizePolicy.Maximum, QSizePolicy.Maximum)
        self.info_label.setStyleSheet("font-style: italic")

        self.button_box_preference_tabs = ButtonBoxPreferenceTabs(self.button_box_configs, "Main Button Box")
        self.button_box_preference_tabs.current_button_box.tree_widget.currentItemChanged.connect(
            self.update_tooltip_label
        )

        self.button_overview = ButtonPreferences(user_config_filename, action_config, variables_dict)
        self.button_overview.list_widget.currentItemChanged.connect(self.update_tooltip_label)

        self.tooltip_label = QTextEdit()
        self.tooltip_label.setStyleSheet("background-color: transparent;")
        self.tooltip_label.setFixedHeight(75)
        self.tooltip_label.setReadOnly(True)

        self.help_button = QPushButton("Help")
        self.help_button.clicked.connect(
            lambda: QDesktopServices.openUrl(
                "https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/FiveShell#Preferences"
            )
        )

        self.apply_button = QPushButton("Apply")
        self.apply_button.clicked.connect(self.update_config)

        self.apply_close_button = QPushButton("Apply and Close")
        self.apply_close_button.clicked.connect(self.update_and_close)

        self.close_button = QPushButton("Close without Saving")
        self.close_button.clicked.connect(self.close)

        self.layout = QVBoxLayout()
        self.box_layout = QHBoxLayout()
        self.button_overview.setContentsMargins(0, 21, 0, 0)
        self.box_layout.addWidget(self.button_box_preference_tabs)
        self.box_layout.addWidget(self.button_overview)
        self.layout.addWidget(self.info_label)
        self.layout.addLayout(self.box_layout)
        self.layout.addWidget(self.tooltip_label)
        self.button_layout = QHBoxLayout()
        self.button_layout.addWidget(self.help_button)
        self.button_layout.addItem(QSpacerItem(0, 0, QSizePolicy.MinimumExpanding))
        self.button_layout.addWidget(self.close_button)
        self.button_layout.addWidget(self.apply_button)
        self.button_layout.addWidget(self.apply_close_button)
        self.layout.addLayout(self.button_layout)
        self.setLayout(self.layout)
        self.add_buttons_from_config()
        self.import_current_config()

        self.resize(window_width, window_height)

        # this needs to be at the end
        self.button_box_preference_tabs.current_button_box.tree_widget.signal_modified.connect(self.modified)
        self.button_box_preference_tabs.signal_modified.connect(self.modified)
        self.button_overview.existing_button_changed.connect(
            self.update_changed_existing_buttons
        )
        self.button_box_preference_tabs.current_button_box.tree_widget.collapseAll()

    def add_buttons_from_config(self):
        for element in self.buttons_config:
            self.button_overview.add_item(Button(element, self.buttons_config[element], self.action_config))

    def update_config(self):
        button_box_configs_list = []
        for tab_name in self.button_box_preference_tabs.tab_names:
            button_box_configs_list.append(
                {"name": tab_name,
                 "button_box": self.button_box_preference_tabs.tabs[tab_name].get_config_dict()}
            )

        update_button_boxes_in_user_config(
            self.user_config_filename,
            button_box_configs_list
        )

    def update_changed_existing_buttons(self, button_in_box_obj):
        for tab_name in self.button_box_preference_tabs.tab_names:
            tab = self.button_box_preference_tabs.tabs[tab_name]
            tab.update_changed_existing_buttons(button_in_box_obj)

    def update_and_close(self):
        self.update_config()
        self.close()

    def import_current_config(self):
        button_box_configs = get_current_button_box_configs(self.user_config_filename)
        for button_box_config in button_box_configs:
            for group in button_box_config["button_box"]:
                button_box_overview = self.button_box_preference_tabs.tabs[button_box_config["name"]]
                group_obj = button_box_overview.add_group(group["group"])
                for element in group["elements"]:
                    button = ButtonInBox(element, self.buttons_config[element], self.action_config)
                    button_box_overview.tree_widget.add_item_to_group(group_obj, button)

    def closeEvent(self, event: QCloseEvent):
        # if the close event is emitted by the x on the upper right corner the sender is None
        if not self.sender() and self.is_modified:
            selected_option = self.open_confirm_dialog()
            if selected_option == QMessageBox.Discard:
                event.accept()
                g.preference_window = None
            elif selected_option == QMessageBox.Cancel:
                event.ignore()
            elif selected_option == QMessageBox.Save:
                self.update_config()
                event.accept()
                g.preference_window = None
        else:
            event.accept()
            g.preference_window = None

    @staticmethod
    def open_confirm_dialog():
        dialog = QMessageBox()
        dialog.setWindowTitle(" ")
        dialog.setText("The Preferences have been modified.")
        dialog.setInformativeText("What do you want to do?")
        dialog.setStandardButtons(QMessageBox.Discard | QMessageBox.Cancel | QMessageBox.Save)
        dialog.setDefaultButton(QMessageBox.Save)
        selected_option = dialog.exec_()
        return selected_option

    def modified(self):
        self.is_modified = True
        for tab_name in self.button_box_preference_tabs.tab_names:
            self.button_box_preference_tabs.fill_tab_drop_down(tab_name)

    def update_tooltip_label(self):
        if isinstance(self.sender(), ButtonListWidget):
            self.tooltip_label.setText(self.sender().currentItem().data(QtCore.Qt.UserRole).get_tooltip())
        elif isinstance(self.sender(), ButtonBoxTreeWidget):
            if self.sender().currentItem().data(1, QtCore.Qt.UserRole):
                self.tooltip_label.setText(self.sender().currentItem().data(1, QtCore.Qt.UserRole).get_tooltip())
            else:
                self.tooltip_label.setText("")

    def enable_shortcuts(self):
        self.create_shortcut("close_one_window")  # FIXME might be better without hardcoding
        self.create_shortcut("quit_fiveshell")

    def create_shortcut(self, action_id):
        key_close_window = self.action_config[action_id][
            "shortcut"] if action_id in self.action_config and "shortcut" in self.action_config[
            action_id] else None
        if key_close_window:
            shortcut = QShortcut(QKeySequence(key_close_window), self)
            shortcut.activated.connect(g.actions[action_id].trigger)
