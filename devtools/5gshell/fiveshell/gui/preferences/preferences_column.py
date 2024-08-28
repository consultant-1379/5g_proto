from PySide2 import QtCore
from PySide2.QtCore import Qt, Signal
from PySide2.QtWidgets import QPushButton, QVBoxLayout, QHBoxLayout, QWidget, QInputDialog, QLineEdit, QLabel, \
    QComboBox, QAbstractItemView, QMessageBox

from gui.preferences.config_structures import Button, ButtonInBox
from gui.preferences.list_widget import ButtonListWidget
from gui.preferences.new_button_window import NewButtonDialog
from gui.preferences.new_simple_button_window import SimpleButtonDialog
from gui.preferences.tree_widget import ButtonBoxTreeWidget


class ButtonBoxPreferences(QWidget):
    def __init__(self, column_name):
        super().__init__()

        self.tree_widget = ButtonBoxTreeWidget()
        self.tree_widget.setHeaderLabel(column_name)

        self.rename_tab_button = QPushButton("Rename Tab")
        self.add_group_button = QPushButton("Add Group")
        self.add_group_button.clicked.connect(self.open_group_input_dialog)
        self.remove_button = QPushButton("Remove Selected")
        self.remove_button.clicked.connect(lambda: self.remove_current_selected())
        self.clone_to_label = QLabel("Clone selection to: ")
        self.tabs_drop_down = QComboBox()
        self.clone_button = QPushButton("Clone")

        self.layout = QVBoxLayout(self)
        self.tree_widget_box = QHBoxLayout()  # layout of the text edit
        self.tree_widget_box.addWidget(self.tree_widget)
        self.button_box = QHBoxLayout()
        self.button_box.addWidget(self.rename_tab_button)
        self.button_box.addWidget(self.add_group_button)
        self.button_box.addWidget(self.remove_button)
        self.clone_box = QHBoxLayout()
        self.clone_box.addWidget(self.clone_to_label)
        self.clone_box.addWidget(self.tabs_drop_down)
        self.clone_box.addWidget(self.clone_button)
        self.layout.addLayout(self.tree_widget_box)
        self.layout.addLayout(self.button_box)
        self.layout.addLayout(self.clone_box)

    def add_group(self, group_name):
        return self.tree_widget.add_group(group_name)

    def remove_current_selected(self):
        self.tree_widget.remove_current_selected()

    def get_config_dict(self):
        res = []
        for group in self.tree_widget.group_objects:
            res_group = {}  # {group: group name, elements:[id1, id2]}, ...
            group_name = group.text(0)
            res_group["group"] = group_name
            res_group["elements"] = []
            for i in range(0, group.childCount()):
                cur_child = group.child(i)
                button_obj = cur_child.data(1, QtCore.Qt.UserRole)
                res_group["elements"].append(button_obj.button_id)
            res.append(res_group)
        return res

    def open_group_input_dialog(self):
        text, ok = QInputDialog().getText(self, "New Group", "Enter group name:", QLineEdit.Normal)
        if ok and text:
            self.add_group(text)

    def update_changed_existing_buttons(self, preferences_obj):
        for group in self.tree_widget.group_objects:
            for i in range(0, group.childCount()):
                cur_child = group.child(i)
                if cur_child.data(1, QtCore.Qt.UserRole).get_button_id() == preferences_obj.get_button_id():
                    cur_child.setData(1, QtCore.Qt.UserRole, preferences_obj)
                    cur_child.setText(0, preferences_obj.get_name())


class ButtonPreferences(QWidget):
    existing_button_changed = Signal(ButtonInBox)

    def __init__(self, user_config_filename, action_config, variables_dict):
        super().__init__()

        self.list_widget = ButtonListWidget()
        self.list_widget.itemDoubleClicked.connect(lambda: self.show_button(user_config_filename, action_config,
                                                                            variables_dict))
        # self.remove_button = QPushButton("Remove")
        # self.remove_button.clicked.connect(lambda: self.remove_current_selected())

        self.line_edit = QLineEdit()
        self.line_edit.returnPressed.connect(self.find_button_in_list)
        self.find_button = QPushButton("Find")
        self.find_button.clicked.connect(self.find_button_in_list)
        self.add_button = QPushButton("New Button")
        self.add_button.clicked.connect(lambda: self.open_new_simple_button_dialog(user_config_filename, action_config,
                                                                                   variables_dict))
        self.last_searched_string = ""
        self.last_found_index = 0

        self.layout = QVBoxLayout(self)
        self.list_widget_box = QHBoxLayout()  # layout of the text edit
        self.list_widget_box.addWidget(self.list_widget)
        self.find_box = QHBoxLayout()
        self.find_box.addWidget(self.line_edit)
        self.find_box.addWidget(self.find_button)
        self.find_box.addWidget(self.add_button)
        self.layout.addLayout(self.list_widget_box)
        self.layout.addLayout(self.find_box)

    def add_item(self, preference_obj):
        self.list_widget.add_item(preference_obj)

    def remove_current_selected(self):
        self.list_widget.remove_current_selected()

    def find_button_in_list(self):
        text = self.line_edit.text()
        if text != self.last_searched_string:
            self.last_found_index = 0
            self.last_searched_string = text
        found = False
        for i in range(self.last_found_index + 1, self.list_widget.count()):
            if text.lower() in self.list_widget.item(i).data(QtCore.Qt.UserRole).get_name().lower() \
                    or text.lower() in self.list_widget.item(i).data(QtCore.Qt.UserRole).get_tags_as_string().lower() \
                    or text.lower() in self.list_widget.item(i).data(QtCore.Qt.UserRole).get_tooltip().lower():
                self.list_widget.setCurrentRow(i)
                self.last_found_index = i
                found = True
                break
        if not found and self.last_found_index != 0:
            self.last_found_index = 0
            self.find_button_in_list()

    def open_new_simple_button_dialog(self, user_config_filename, action_config, variables_dict):
        dialog = SimpleButtonDialog(user_config_filename, action_config)

        dialog.button_advanced.clicked.connect(dialog.reject)

        dialog.button_advanced.clicked.connect(
            lambda: self.open_new_advanced_button_dialog(user_config_filename, action_config, variables_dict)
        )
        res = dialog.exec_()
        if res:
            self.__add_button(dialog, action_config)

    def open_new_advanced_button_dialog(self, user_config_filename, action_config, variables_dict):
        dialog = NewButtonDialog(user_config_filename, action_config, variables_dict)
        res = dialog.exec_()
        if res:
            self.__add_button(dialog, action_config)

    def show_button(self, user_config_filename, action_config, variables_dict, button_obj=None):
        dialog = NewButtonDialog(user_config_filename, action_config, variables_dict, edit_mode=True)
        if button_obj:
            data = button_obj
        else:
            data = self.list_widget.selectedItems()[0].data(QtCore.Qt.UserRole)
        dialog.line_edit_button_id.setText(data.button_id.replace("_custom", "") if data.button_id else None)
        dialog.line_edit_label.setText(data.label.encode('unicode_escape').decode('ascii') if data.label else None)
        dialog.line_edit_tooltip.setText(data.tooltip)
        color = str(data.color) if data.color else ""
        dialog.line_edit_color.setText(color)
        dialog.line_edit_tags.setText(", ".join(data.tags))

        if data.inputs:
            for i, button_input in enumerate(data.inputs):
                if len(dialog.all_inputs) <= i:
                    dialog.add_additional_input()
                input_type = button_input["type"]
                dialog.all_inputs[i]["type"].setCurrentText(input_type)
                dialog.all_inputs[i]["store_var"].setText(button_input["store_var"])
                if input_type == "line_edit":
                    dialog.all_inputs[i]["label"].setText(button_input["label"])
                elif input_type == "combobox":
                    dialog.all_inputs[i]["labels"].setText(", ".join(button_input["labels"]))
                    if "values" in button_input:
                        dialog.all_inputs[i]["values"].setText(", ".join(button_input["values"]))
                elif input_type == "radiobutton":
                    dialog.all_inputs[i]["labels"].setText(", ".join(button_input["labels"]))
                    if "default" in button_input:
                        dialog.all_inputs[i]["default"].setText(button_input["default"])
                    if "values" in button_input:
                        dialog.all_inputs[i]["values"].setText(", ".join(button_input["values"]))
                elif input_type == "checkbox":
                    dialog.all_inputs[i]["label"].setText(button_input["label"])
                    if "default" in button_input:
                        dialog.all_inputs[i]["default"].setText(str(button_input["default"]))
                    if "values" in button_input:
                        dialog.all_inputs[i]["values"].setText(", ".join(button_input["values"]))

        dialog.combo_box_action_id.setCurrentText(data.action_ids[0])
        for i in range(1, len(data.action_ids)):
            dialog.add_additional_action(user_config_filename)
            dialog.additional_action_list[-1].setCurrentText(data.action_ids[i])

        if not data.button_id.endswith("_custom"):
            dialog.setWindowTitle("Button - Read only")
            dialog.setWindowFlags(Qt.Window)  # makes the dialog go to the background if another window is selected
            for child in dialog.children():
                if isinstance(child, QLineEdit):
                    child.setReadOnly(True)
                if isinstance(child, QPushButton):
                    child.setEnabled(False)
                if isinstance(child, QComboBox):
                    child.setEnabled(False)
            button_show_action = QPushButton("\U0001F441")
            button_show_action.setFixedWidth(25)
            button_show_action.setToolTip("Show this action")
            button_show_action.clicked.connect(lambda: dialog.show_action(dialog.combo_box_action_id.currentText(),
                                                                          user_config_filename))
            for i in range(len(dialog.additional_action_list)):
                button = QPushButton("\U0001F441")
                button.setFixedWidth(25)
                button.setToolTip("Show this action")
                button.clicked.connect(lambda: dialog.show_action(dialog.additional_action_list[i].currentText(),
                                                                  user_config_filename))
                dialog.additional_action_list[i].property('row').addWidget(button)
            dialog.action_id_line.addWidget(button_show_action)
            dialog.ok_cancel_button_box.setVisible(False)
            button_clone = QPushButton("Clone to customize")
            button_clone.clicked.connect(
                lambda: self.show_cloned_button(
                    Button(data.get_button_id(), data.button_dict, action_config),
                    user_config_filename, action_config, variables_dict
                )
            )
            dialog.layout.addWidget(button_clone)
            dialog.show()
        else:
            dialog.setWindowTitle("Edit button")
            dialog.line_edit_button_id.setReadOnly(True)
            res = dialog.exec_()
            if res:
                if not button_obj:  # button does already exist
                    button = Button(dialog.button_id, dialog.button_dict, action_config)
                    self.list_widget.selectedItems()[0].setData(
                        QtCore.Qt.UserRole,
                        button
                    )
                    self.list_widget.selectedItems()[0].setText(
                        str("{}  {}".format(button.get_name(), button.get_tags_as_string()))
                    )
                    self.existing_button_changed.emit(button.to_button_in_box())
                else:
                    self.__add_button(dialog, action_config)

    def show_cloned_button(self, button_obj, user_config_filename, action_config, variables_dict):
        existing_button = self.__find_and_select_button_by_button_id(f"{button_obj.button_id}_custom")
        if existing_button:
            msg = QMessageBox()
            msg.setWindowTitle("Attention")
            msg.setIcon(QMessageBox.Warning)
            msg.setText("You have already cloned this button. \n"
                        f"Edit {button_obj.button_id}_custom or create a new button. \n"
                        f"Do you want to open and edit {button_obj.button_id}_custom?")
            msg.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
            ok = msg.exec_()
            if ok == QMessageBox.Yes:
                self.show_button(
                    user_config_filename,
                    action_config,
                    variables_dict
                )  # the button is now selected that's why the object does not need to be a param
        else:
            self.show_button(user_config_filename, action_config, variables_dict,
                             Button(f"{button_obj.get_button_id()}_custom", button_obj.button_dict, action_config))

    def __find_and_select_button_by_button_id(self, button_id):
        for i in range(self.list_widget.count()):
            if button_id == self.list_widget.item(i).data(QtCore.Qt.UserRole).get_button_id():
                self.list_widget.setItemSelected(self.list_widget.item(i), True)
                self.list_widget.scrollToItem(self.list_widget.item(i), QAbstractItemView.PositionAtTop)
                return self.list_widget.item(i).data(QtCore.Qt.UserRole)
        return None

    def __add_button(self, dialog, action_config):
        self.add_item(Button(dialog.button_id, dialog.button_dict, action_config))
        item = self.list_widget.item(self.list_widget.count() - 1)
        self.list_widget.setItemSelected(item, True)
        self.list_widget.scrollToItem(item, QAbstractItemView.PositionAtTop)
