# 2020-09-04 Alina Behrens
from PySide2.QtCore import QEvent, Qt
from PySide2.QtGui import QIcon, QCloseEvent
from PySide2.QtWidgets import QMainWindow, QMenuBar, QGroupBox, QRadioButton, QHBoxLayout, QComboBox, QLineEdit, \
    QCheckBox, QApplication

import g
from gui.central_window import CentralWindow


class MainWindow(QMainWindow):
    def __init__(self, main_window_config, button_box_configs, current_button_box, buttons_config,
                 main_window_width, buttonbox_width, available_kube_contexts, index_current_context, parent=None):
        """
        Creates an object of the class MainWindow that represents the whole fiveshell window.

        :param user_config_filename: the name of the user config file (.fiveshellrc)
        :param main_window_config: the part of the config file specifying the main window buttons (f.ex. Help -> About)
        :param button_box_configs: the part of the config file specifying the buttons and sections on left
        :param current_button_box: the name of the currently selected button box
        :param buttons_config: the part of the config file specifying all available buttons
        :param main_window_width: the width of the window
        :param buttonbox_width: the width of the button box (left side of the fiveshell window)
        :param available_kube_contexts: a list of available kube contexts
        :param index_current_context: the index of the current selected kube config
        :param parent:
        """
        super(MainWindow, self).__init__(parent)
        self.main_widget = CentralWindow(button_box_configs, current_button_box, buttons_config, main_window_width,
                                         buttonbox_width)
        self.setCentralWidget(self.main_widget)
        self.setWindowTitle("FiveShell")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        # if the current tab changes the variables will be updated
        self.main_widget.button_box_tabs.currentChanged.connect(self.update_variables)

        self.statusBar()  # Will show everything with (QAction).setStatusTip("I'm the status")
        self.context_drop_down = None
        self.create_drop_down_list(available_kube_contexts, index_current_context)
        self.statusBar().addWidget(self.context_drop_down)
        self.radio_button_group_box = self.create_radio_buttons()
        self.statusBar().addWidget(self.radio_button_group_box)

        self.update_menu_bar(main_window_config)

    def changeEvent(self, event: QEvent):  # if the current selected window changes the variables will be updated
        if event.type() == QEvent.ActivationChange and self.isActiveWindow():
            self.update_variables()
        super(MainWindow, self).changeEvent(event)

    def closeEvent(self, event: QCloseEvent):
        if len(g.main_windows) == 1:
            QApplication.quit()
        event.accept()
        g.main_windows.remove(self)

    def update_variables(self):
        """
        updates all variables in the globalDict according to the current state of all input widgets of this window
        """
        # the context variable is not changed
        # the label will be updated to the current context
        if self.context_drop_down.currentText() != g.globalDict["kube_context"]:
            self.context_drop_down.setCurrentText(g.globalDict["kube_context"])

        # update variable compact verbose
        if self.radio_button_group_box.children()[1].isChecked():
            if self.radio_button_group_box.children()[1].text() != g.globalDict["verbose"]:
                g.globalDict["verbose"] = self.radio_button_group_box.children()[1].text()
        elif self.radio_button_group_box.children()[2].isChecked():
            if self.radio_button_group_box.children()[2].text() != g.globalDict["verbose"]:
                g.globalDict["verbose"] = self.radio_button_group_box.children()[2].text()

        # while the button box is updated due to a config changed it would throw an error
        if not self.main_widget.button_box_tabs.current_button_box:
            return

        # update all input variables
        for section in self.main_widget.button_box_tabs.current_button_box.accordion.sections:
            for button in section.buttons.buttons:
                if type(button) is QLineEdit:
                    if button.property("store_var") in g.globalDict:
                        if button.text() != '' and g.globalDict[button.property("store_var")] != button.text():
                            g.globalDict[button.property("store_var")] = button.text()
                if type(button) is QCheckBox:
                    if button.isChecked():
                        if button.property("store_var") in g.globalDict:
                            if button.property('values'):
                                value = button.property('values')[0]
                            else:
                                value = True
                            if g.globalDict[button.property("store_var")] != value:
                                g.globalDict[button.property("store_var")] = value

                if type(button) is QComboBox:
                    if button.property("store_var") in g.globalDict:
                        if button.property('values'):
                            value = button.property('values')[button.currentIndex()]
                        else:
                            value = button.currentText()
                        if g.globalDict[button.property("store_var")] != value:
                            g.globalDict[button.property("store_var")] = value

                if type(button) is QGroupBox:
                    store_var = button.children()[1].property("store_var")
                    if store_var in g.globalDict:
                        if button.children()[1].isChecked():
                            if button.children()[1].text() != g.globalDict[store_var]:
                                g.globalDict[store_var] = button.children()[1].text()
                        elif button.children()[2].isChecked():
                            if button.children()[2].text() != g.globalDict[store_var]:
                                g.globalDict[store_var] = button.children()[2].text()

    def update_menu_bar(self, main_window_config):
        """updates the menu bar on config changed"""
        main_menu = QMenuBar()

        for element in main_window_config["elements"]:  # for each menu item in the config
            menu_name = element['menu']  # create a new menu
            menu = main_menu.addMenu(menu_name)  # add this menu
            for action in element['actions']:  # for each action
                action = g.actions[action["action_id"]]  # find the action object
                menu.addAction(action)  # add this action to the menu
        self.setMenuBar(main_menu)

    def create_drop_down_list(self, available_contexts, index_current_context):
        self.context_drop_down = QComboBox()
        self.context_drop_down.addItems(available_contexts)
        self.context_drop_down.setCurrentIndex(index_current_context)
        g.globalDict["kube_context"] = self.context_drop_down.itemText(index_current_context)
        self.context_drop_down.addItem("Manage k8s contexts...")
        self.context_drop_down.currentIndexChanged.connect(self.drop_down_list_selection_changed)

    def update_drop_down_list(self, available_contexts, index_current_context):
        self.statusBar().removeWidget(self.context_drop_down)
        self.context_drop_down = QComboBox()
        self.context_drop_down.addItems(available_contexts)
        self.context_drop_down.setCurrentIndex(index_current_context)
        g.globalDict["kube_context"] = self.context_drop_down.itemText(index_current_context)
        self.context_drop_down.addItem("Manage k8s contexts...")
        self.context_drop_down.currentIndexChanged.connect(self.drop_down_list_selection_changed)
        self.statusBar().insertWidget(0, self.context_drop_down)

    def drop_down_list_selection_changed(self):
        if self.sender().sender():
            if self.sender().currentText() == "Manage k8s contexts...":
                g.actions["show_kubernetes_manager"].trigger()
                self.sender().setCurrentText(g.globalDict["kube_context"])
            elif g.globalDict["kube_context"] != self.sender().currentText():
                g.globalDict["last_working_kube_context"] = g.globalDict["kube_context"]
                g.globalDict["kube_context"] = self.sender().currentText()
                g.actions["change_kube_context"].trigger()

    def create_radio_buttons(self):
        """
        Creates the two radio buttons compact and verbose at the bottom of the window.
        The selected value of the radio button is stored in g.globalDict["verbose"]
        """
        group_box = QGroupBox()

        radio_button_compact = QRadioButton("compact")
        radio_button_compact.toggled.connect(self.update_radio_buttons)
        radio_button_verbose = QRadioButton("verbose")
        radio_button_verbose.toggled.connect(self.update_radio_buttons)

        radio_button_compact.setChecked(True)  # default

        h_box = QHBoxLayout()
        h_box.addWidget(radio_button_compact)
        h_box.addWidget(radio_button_verbose)
        h_box.addStretch(1)
        h_box.setMargin(0)

        group_box.setLayout(h_box)
        # group_box.setFixedHeight(15)
        group_box.setStyleSheet("QGroupBox { border: 0px; }")

        return group_box

    def update_radio_buttons(self):
        """
        Set the global dict value to the newly selected radio button
        """
        if self.sender().isChecked():
            g.globalDict["verbose"] = self.sender().text()
