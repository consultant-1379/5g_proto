import json

from PySide2.QtCore import Qt
from PySide2.QtGui import QIcon, QPixmap, QDesktopServices
from PySide2.QtWidgets import QDialog, QLabel, QLineEdit, QComboBox, QHBoxLayout, QVBoxLayout, QFormLayout, \
    QPushButton, QDialogButtonBox, QMessageBox, QColorDialog, QTextEdit, QRadioButton, QCheckBox

from gui.preferences.config_structures import Action
from gui.preferences.new_action_window import NewActionDialog
from utils.config_utils import add_button_to_user_config


class NewButtonDialog(QDialog):
    def __init__(self, user_config_filename, action_config, variables_dict, edit_mode=False):
        super(NewButtonDialog, self).__init__()
        self.setWindowTitle("New Button")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        self.action_config = action_config
        self.action_list = list(action_config.keys())
        self.edit_mode = edit_mode
        self.variables_dict = variables_dict

        self.line_edit_button_id = QLineEdit()
        self.combo_box_action_id = QComboBox()
        self.line_edit_label = QLineEdit()
        self.line_edit_tooltip = QLineEdit()
        self.line_edit_color = QLineEdit()
        self.line_edit_tags = QLineEdit()

        self.add_action_button = QPushButton("Add another action")
        self.new_action_button = QPushButton("+")
        self.new_action_button.setMaximumWidth(25)
        self.new_action_button.setToolTip("Create a new action")
        self.edit_action_button = QPushButton()
        self.edit_action_button.setIcon(QIcon(QPixmap("icons/edit.png")))
        self.edit_action_button.setMaximumWidth(25)
        self.edit_action_button.setToolTip("Edit this action")

        self.action_id_line = QHBoxLayout()
        self.action_id_line.addWidget(self.combo_box_action_id)
        self.action_id_line.addWidget(self.new_action_button)
        self.action_id_line.addWidget(self.edit_action_button)

        self.color_pick_button = QPushButton()
        self.color_pick_button.setIcon(QIcon(QPixmap("icons/color_picker.png")))
        self.color_pick_button.setMaximumWidth(25)
        self.color_pick_button.setToolTip("Pick a color for your button")
        self.color_pair = QHBoxLayout()
        self.color_pair.addWidget(self.line_edit_color)
        self.color_pair.addWidget(self.color_pick_button)

        self.button_find_unicode = QPushButton("Find unicode symbol")
        self.button_find_unicode.clicked.connect(lambda: QDesktopServices.openUrl("https://unicode-table.com/en/sets/"))
        self.label_pair = QHBoxLayout()
        self.label_pair.addWidget(self.line_edit_label)
        self.label_pair.addWidget(self.button_find_unicode)

        self.input_line = QHBoxLayout()
        self.combo_box_input_types = QComboBox()
        self.combo_box_input_types.addItems(["", "radiobutton", "combobox", "checkbox", "line_edit"])
        self.combo_box_input_types.setProperty('row', self.input_line)
        self.line_edit_input_store_var = QLineEdit()
        self.input_line.addWidget(QLabel("type:"))
        self.input_line.addWidget(self.combo_box_input_types)
        self.input_line.addWidget(QLabel("store variable:"))
        self.input_line.addWidget(self.line_edit_input_store_var)
        self.combo_box_input_types.currentTextChanged.connect(self.on_input_type_changed)
        self.all_inputs = [{"type": self.combo_box_input_types, "store_var": self.line_edit_input_store_var}]

        self.button_add_input = QPushButton("Add another input")
        self.button_add_input.clicked.connect(self.add_additional_input)

        self.new_action_button.clicked.connect(
            lambda: self.open_new_action_dialog(user_config_filename, self.combo_box_action_id))
        self.color_pick_button.clicked.connect(self.open_color_pick_dialog)
        self.add_action_button.clicked.connect(lambda: self.add_additional_action(user_config_filename))
        self.edit_action_button.clicked.connect(lambda: self.show_action(self.combo_box_action_id.currentText(),
                                                                         user_config_filename))
        self.combo_box_action_id.setEditable(True)
        self.fill_action_combo_box(self.combo_box_action_id)

        self.additional_action_list = []

        self.layout = QVBoxLayout()
        self.form_layout = QFormLayout()
        label_button_id = QLabel("Button ID (optional) \U000024D8")
        label_button_id.setToolTip("The unique key to define the button in the config: "
                                   "\na short pregnant description of the button, words separated with _ . "
                                   "\nDefault: id of the first action")
        self.form_layout.addRow(label_button_id, self.line_edit_button_id)
        label_action_id = QLabel("Action IDs \U000024D8")
        label_action_id.setToolTip("Ids of the actions that will be executed when pressing the button.")
        self.form_layout.addRow(label_action_id, self.action_id_line)
        self.form_layout.addRow(QLabel(""), self.add_action_button)
        label_label = QLabel("Label (optional) \U000024D8")
        label_label.setToolTip("The name that is displayed on your button. \n"
                               "Unicode symbols can be added with \\U and 8 digits. e.g.'\\U00001234'"
                               " \nDefault: the name of the first action")
        self.form_layout.addRow(label_label, self.label_pair)
        label_tooltip = QLabel("Tooltip (optional) \U000024D8")
        label_tooltip.setToolTip("A description of your button. \nDefault: the tooltip of the first action")
        self.form_layout.addRow(label_tooltip, self.line_edit_tooltip)
        label_color = QLabel("Color (optional) \U000024D8")
        label_color.setToolTip("The color of your button as hex code (without #). \nDefault: 000000 (black)")
        self.form_layout.addRow(label_color, self.color_pair)
        label_tags = QLabel("Tags (optional) \U000024D8")
        label_tags.setToolTip("Keywords that help you find your buttons in the Preferences. Seperated by ', '."
                              "\nDefault: Custom")
        self.form_layout.addRow(label_tags, self.line_edit_tags)
        label_inputs = QLabel("Input fields (optional) \U000024D8")
        label_inputs.setToolTip("")  # TODO
        self.form_layout.addRow(label_inputs, self.input_line)
        self.form_layout.addRow(QLabel(""), self.button_add_input)

        self.ok_cancel_button_box = QDialogButtonBox(
            QDialogButtonBox.Ok | QDialogButtonBox.Cancel | QDialogButtonBox.Help)
        self.ok_cancel_button_box.accepted.connect(lambda: self.on_ok_button_clicked(user_config_filename))
        self.ok_cancel_button_box.rejected.connect(self.reject)
        self.ok_cancel_button_box.helpRequested.connect(
            lambda: QDesktopServices.openUrl(
                "https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/FiveShell#Preferences"
            )
        )

        self.layout.addLayout(self.form_layout)
        self.layout.addWidget(self.ok_cancel_button_box)

        self.setLayout(self.layout)

        self.button_id = None
        self.button_dict = None

    def open_new_action_dialog(self, user_config_filename, combo_box):
        action_dialog = NewActionDialog(user_config_filename, self.action_config, self.variables_dict)
        res = action_dialog.exec_()
        if res:
            combo_box.insertItem(0, action_dialog.action_id)
            combo_box.setCurrentIndex(0)
            self.action_list.append(action_dialog.action_id)

    def fill_action_combo_box(self, combo_box):
        action_ids = sorted(self.action_list)
        for action_id in action_ids:
            combo_box.addItem(action_id)

    def on_ok_button_clicked(self, user_config_filename):
        if self.combo_box_action_id.currentText():
            button_dict = self.create_button_dict()
            if button_dict:
                success = add_button_to_user_config(user_config_filename, button_dict, edit_mode=self.edit_mode)
                if success:
                    if "inputs" in button_dict[self.button_id]:
                        for input_dict in button_dict[self.button_id]["inputs"]:
                            self.variables_dict[input_dict["store_var"]] = self.button_id
                    self.accept()
        else:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("You did not fill all non optional fields. "
                        "\nPlease check your input and try again.")
            msg.exec_()

    def create_button_dict(self):
        self.button_id = f"{self.line_edit_button_id.text()}_custom" if self.line_edit_button_id.text() \
            else self.combo_box_action_id.currentText()
        self.button_id += "_custom" if not self.button_id.endswith("_custom") else ""
        button_dict = {
            self.button_id: {
                "action_ids": [
                    self.combo_box_action_id.currentText()
                ]
            }
        }
        for combo_box in self.additional_action_list:
            button_dict[self.button_id]["action_ids"].append(combo_box.currentText())
        if self.line_edit_label.text():
            try:
                button_dict[self.button_id]["label"] = self.line_edit_label.text().encode().decode('unicode-escape')
            except:
                msg = QMessageBox()
                msg.setWindowTitle("ERROR")
                msg.setIcon(QMessageBox.Critical)
                msg.setText("Your unicode in the label seems not to be correct. \n"
                            "Please make sure that it exists and starts with \\U followed by exactly 8 digits.")
                msg.exec_()
                return None
        if self.line_edit_tooltip.text():
            button_dict[self.button_id]["tooltip"] = self.line_edit_tooltip.text()
        if self.line_edit_color.text():
            button_dict[self.button_id]["color"] = self.line_edit_color.text()
        button_dict[self.button_id]["tags"] = []
        if self.line_edit_tags.text():
            button_dict[self.button_id]["tags"].extend(list(self.line_edit_tags.text().split(", ")))
        if "Custom" not in button_dict[self.button_id]["tags"]:
            button_dict[self.button_id]["tags"].insert(0, "Custom")
        try:
            input_list = self.__create_input_list()
            if input_list:
                button_dict[self.button_id]["inputs"] = input_list
        except:
            return None
        self.button_dict = button_dict[self.button_id]
        return button_dict

    def __create_input_list(self):
        input_list = []
        something_missing = False
        for input_dict in self.all_inputs:
            new_dict = None
            input_type = input_dict["type"].currentText()
            store_var = input_dict["store_var"].text()
            if store_var in self.variables_dict.keys():
                if self.variables_dict[store_var] != self.button_id:
                    msg = QMessageBox()
                    msg.setWindowTitle("Attention")
                    msg.setIcon(QMessageBox.Warning)
                    msg.setText(f"The store variable '{store_var}' is already assigned in an other context. \n"
                                "Please choose an other name.")
                    msg.setStandardButtons(QMessageBox.Ok)
                    msg.exec_()
                    raise Exception("inputs not entered correctly")
            if not input_type:
                continue
            if not store_var:
                something_missing = True
            elif input_type == "line_edit":
                label = input_dict["label"].text()
                if not label:
                    something_missing = True
                new_dict = {"type": input_type, "store_var": store_var, "label": label}
            elif input_type == "combobox":
                labels = input_dict["labels"].text().split(", ") if input_dict["labels"].text() else None
                values = input_dict["values"].text().split(", ") if input_dict["values"].text() else None
                if labels:
                    new_dict = {"type": input_type, "store_var": store_var, "labels": labels}
                    if values and len(labels) != len(values):
                        msg = QMessageBox()
                        msg.setWindowTitle("Attention")
                        msg.setIcon(QMessageBox.Warning)
                        msg.setText("The labels and values list don't have the same length. \n"
                                    "Make sure you have the same amount of labels as related values and all values "
                                    "are separated with ', '.")
                        msg.setStandardButtons(QMessageBox.Ok)
                        msg.exec_()
                        raise Exception("inputs not entered correctly")
                    if values:
                        new_dict["values"] = values
                else:
                    something_missing = True
            elif input_type == "radiobutton":
                labels = input_dict["labels"].text().split(", ") if input_dict["labels"].text() else None
                values = input_dict["values"].text().split(", ") if input_dict["values"].text() else None
                default = input_dict["default"].text()
                if labels:
                    new_dict = {"type": input_type, "store_var": store_var, "labels": labels}
                    if values and len(labels) != len(values):
                        msg = QMessageBox()
                        msg.setWindowTitle("Attention")
                        msg.setIcon(QMessageBox.Warning)
                        msg.setText("The labels and values list don't have the same length. \n"
                                    "Make sure you have the same amount of labels as related values and all values "
                                    "are separated with ', '.")
                        msg.setStandardButtons(QMessageBox.Ok)
                        msg.exec_()
                        raise Exception("inputs not entered correctly")
                    if values:
                        new_dict["values"] = values
                    if default and default in labels:
                        new_dict["default"] = default
                else:
                    something_missing = True
            elif input_type == "checkbox":
                label = input_dict["label"].text()
                values = input_dict["values"].text().split(", ") if input_dict["values"].text() else None
                default = input_dict["default"].text()
                if label:
                    new_dict = {"type": input_type, "store_var": store_var, "label": label}
                    if values and len(values) != 2:
                        msg = QMessageBox()
                        msg.setWindowTitle("Attention")
                        msg.setIcon(QMessageBox.Warning)
                        msg.setText("The values list does not have exactly two values. \n"
                                    "Make sure the values are separated with ', '.")
                        msg.setStandardButtons(QMessageBox.Ok)
                        msg.exec_()
                        raise Exception("inputs not entered correctly")
                    if values:
                        new_dict["values"] = values
                    if default and default in ["True", "False"]:
                        new_dict["default"] = eval(default)  # returns a boolean
                else:
                    something_missing = True
            if something_missing:
                msg = QMessageBox()
                msg.setWindowTitle("Attention")
                msg.setIcon(QMessageBox.Warning)
                msg.setText("There are some values missing in the input section. \n"
                            "Please make sure that you filled in all fields. \n"
                            "If you don't want any inputs, make sure you delete them or select no input type.")
                msg.setStandardButtons(QMessageBox.Ok)
                msg.exec_()
                raise Exception("inputs not entered correctly")
            if new_dict:
                input_list.append(new_dict)
        if len(input_list) > 0:
            return input_list
        else:
            return None

    def add_additional_action(self, user_config_filename):
        additional_action_line = QHBoxLayout()
        delete_button = QPushButton("\U0000274C")
        delete_button.setMaximumWidth(25)

        combo_box = QComboBox()
        combo_box.setEditable(True)
        self.fill_action_combo_box(combo_box)
        new_action_button = QPushButton("+")
        new_action_button.setMaximumWidth(25)
        new_action_button.setToolTip("Create a new action")

        edit_action_button = QPushButton()
        edit_action_button.setIcon(QIcon(QPixmap("icons/edit.png")))
        edit_action_button.setMaximumWidth(25)
        edit_action_button.setToolTip("Edit this action")

        additional_action_line.addWidget(delete_button)
        additional_action_line.addWidget(combo_box)
        additional_action_line.addWidget(new_action_button)
        additional_action_line.addWidget(edit_action_button)

        delete_button.setProperty('row', additional_action_line)
        delete_button.setProperty('combo_box', combo_box)
        combo_box.setProperty('row', additional_action_line)
        delete_button.clicked.connect(self.remove_additional_action_line)
        new_action_button.clicked.connect(lambda: self.open_new_action_dialog(user_config_filename, combo_box))
        edit_action_button.clicked.connect(lambda: self.show_action(combo_box.currentText(),
                                                                    user_config_filename))

        self.additional_action_list.append(combo_box)
        self.form_layout.insertRow(
            self.form_layout.getItemPosition(self.form_layout.indexOf(self.add_action_button))[0],  # row number
            QLabel(""),
            additional_action_line
        )

    def remove_additional_action_line(self):
        self.additional_action_list.remove(self.sender().property('combo_box'))
        self.form_layout.removeRow(self.sender().property('row'))

    def add_additional_input(self):
        input_line = QHBoxLayout()
        delete_button = QPushButton("\U0000274C")
        delete_button.setMaximumWidth(25)
        combo_box_input_types = QComboBox()
        combo_box_input_types.addItems(["", "radiobutton", "combobox", "checkbox", "line_edit"])
        combo_box_input_types.setProperty('row', input_line)
        line_edit_input_store_var = QLineEdit()
        input_line.addWidget(delete_button)
        input_line.addWidget(QLabel("type:"))
        input_line.addWidget(combo_box_input_types)
        input_line.addWidget(QLabel("store variable:"))
        input_line.addWidget(line_edit_input_store_var)

        delete_button.setProperty('row', input_line)
        delete_button.setProperty('combo_box', combo_box_input_types)
        delete_button.clicked.connect(self.remove_additional_input)

        self.all_inputs.append({"type": combo_box_input_types, "store_var": line_edit_input_store_var})
        combo_box_input_types.currentTextChanged.connect(self.on_input_type_changed)

        self.form_layout.insertRow(
            self.form_layout.getItemPosition(self.form_layout.indexOf(self.button_add_input))[0],  # row number
            QLabel(""),
            input_line
        )

    def remove_additional_input(self):
        dict_entry = next(item for item in self.all_inputs if item["type"] == self.sender().property('combo_box'))
        index_next_row = self.form_layout.getItemPosition(
            self.form_layout.indexOf(self.sender().property('combo_box').property('row')))[0]
        self.all_inputs.remove(dict_entry)
        self.form_layout.removeRow(self.sender().property('row'))
        next_row = self.form_layout.itemAt(index_next_row, QFormLayout.FieldRole)
        if isinstance(next_row, QHBoxLayout):
            if next_row.property('details'):
                self.form_layout.removeRow(index_next_row)

    def on_input_type_changed(self, text):
        combo_box = self.sender()
        dict_entry = next(item for item in self.all_inputs if item["type"] == combo_box)

        index_next_row = self.form_layout.getItemPosition(self.form_layout.indexOf(combo_box.property('row')))[0] + 1
        next_row = self.form_layout.itemAt(index_next_row, QFormLayout.FieldRole)
        if isinstance(next_row, QHBoxLayout):
            if next_row.property('details'):
                self.form_layout.removeRow(index_next_row)
                store_var = dict_entry["store_var"]
                dict_entry.clear()
                dict_entry["type"] = combo_box
                dict_entry["store_var"] = store_var

        line = QHBoxLayout()

        if text == "":
            return
        elif text == "line_edit":
            line_edit_for_line_edit_label = QLineEdit()
            line.addWidget(QLabel("label"))
            line.addWidget(line_edit_for_line_edit_label)
            dict_entry["label"] = line_edit_for_line_edit_label
        elif text == "combobox":
            line_edit_combobox_labels = QLineEdit()
            line_edit_combobox_values = QLineEdit()
            line.addWidget(QLabel("labels"))
            line.addWidget(line_edit_combobox_labels)
            line.addWidget(QLabel("values"))
            line.addWidget(line_edit_combobox_values)
            dict_entry["labels"] = line_edit_combobox_labels
            dict_entry["values"] = line_edit_combobox_values
        elif text == "radiobutton":
            line_edit_radiobutton_labels = QLineEdit()
            line_edit_radiobutton_default = QLineEdit()
            line_edit_radiobutton_values = QLineEdit()
            line.addWidget(QLabel("labels"))
            line.addWidget(line_edit_radiobutton_labels)
            line.addWidget(QLabel("default"))
            line.addWidget(line_edit_radiobutton_default)
            line.addWidget(QLabel("values"))
            line.addWidget(line_edit_radiobutton_values)
            dict_entry["labels"] = line_edit_radiobutton_labels
            dict_entry["default"] = line_edit_radiobutton_default
            dict_entry["values"] = line_edit_radiobutton_values
        elif text == "checkbox":
            line_edit_checkbox_label = QLineEdit()
            line_edit_checkbox_default = QLineEdit()
            line_edit_checkbox_value = QLineEdit()
            line.addWidget(QLabel("label"))
            line.addWidget(line_edit_checkbox_label)
            line.addWidget(QLabel("default"))
            line.addWidget(line_edit_checkbox_default)
            line.addWidget(QLabel("values"))
            line.addWidget(line_edit_checkbox_value)
            dict_entry["label"] = line_edit_checkbox_label
            dict_entry["default"] = line_edit_checkbox_default
            dict_entry["values"] = line_edit_checkbox_value

        line.setProperty('details', True)
        self.form_layout.insertRow(index_next_row, QLabel(""), line)

    def open_color_pick_dialog(self):
        standard_colors = json.load(open("icons/colors.json"))
        color_dialog = QColorDialog()
        color_dialog.setOption(QColorDialog.DontUseNativeDialog)
        for i in range(len(standard_colors)):
            color_dialog.setStandardColor(i, standard_colors[i])
        res = color_dialog.exec_()
        if res:
            self.line_edit_color.setText(color_dialog.selectedColor().name().replace("#", ""))

    def show_action(self, action_id, user_config_filename):
        dialog = NewActionDialog(user_config_filename, self.action_config, self.variables_dict, edit_mode=True)
        action = Action(action_id, self.action_config[action_id])
        dialog.line_edit_action_id.setText(action.action_id.replace("_custom", ""))
        dialog.line_edit_label.setText(
            action.action_name.encode('unicode_escape').decode('ascii') if action.action_name else None
        )
        dialog.line_edit_tooltip.setText(action.tooltip)
        dialog.radio_button_python.setChecked("Python" == action.get_type())
        dialog.radio_button_linux.setChecked("Linux" == action.get_type())
        dialog.text_edit_command.setText(action.command)
        dialog.line_edit_shortcut.setText(action.shortcut)
        if action.variables:
            dialog.line_edit_var_name.setText(action.variables[0]["name"])
            dialog.line_edit_var_filter.setText(action.variables[0]["filter"])
            for i in range(1, len(action.variables)):
                dialog.add_additional_variable()
                dialog.additional_variables[-1][0].setText(action.variables[i]["name"])
                dialog.additional_variables[-1][1].setText(action.variables[i]["filter"])
        dialog.line_edit_loop_name.setText(action.loop["name"] if action.loop else None)
        dialog.line_edit_loop_var.setText(action.loop["variable"] if action.loop else None)
        dialog.line_edit_dir.setText(action.directory)
        if not action_id.endswith("_custom"):
            dialog.setWindowTitle("Action - Read only")
            dialog.setWindowFlags(Qt.Window)  # makes the dialog go to the background if another window is selected
            for child in dialog.children():
                if isinstance(child, QLineEdit) or isinstance(child, QTextEdit):
                    child.setReadOnly(True)
                if isinstance(child, QPushButton):
                    child.setEnabled(False)
                if isinstance(child, QRadioButton):
                    child.setEnabled(False)
                if isinstance(child, QCheckBox):
                    child.setEnabled(False)
            dialog.ok_cancel_button_box.setVisible(False)
            button_clone = QPushButton("Clone to customize")
            button_clone.clicked.connect(lambda: self.show_cloned_action(action_id, user_config_filename))
            dialog.layout.addWidget(button_clone)
            dialog.show()
        else:
            dialog.setWindowTitle("Edit action")
            dialog.line_edit_action_id.setReadOnly(True)
            res = dialog.exec_()
            if res:
                self.action_list.append(action.action_id) if action.action_id not in self.action_list else None

    def show_cloned_action(self, action_id, user_config_filename):
        if f"{action_id}_custom" in self.action_config:
            msg = QMessageBox()
            msg.setWindowTitle("Attention")
            msg.setIcon(QMessageBox.Warning)
            msg.setText("You have already cloned this action. \n"
                        f"Edit {action_id}_custom or create a new action. \n"
                        f"Do you want to open and edit {action_id}_custom?")
            msg.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
            ok = msg.exec_()
            if ok == QMessageBox.Yes:
                self.show_action(
                    f"{action_id}_custom",
                    user_config_filename
                )
        else:
            self.action_config.update({f"{action_id}_custom": self.action_config[action_id]})
            self.show_action(
                f"{action_id}_custom",
                user_config_filename
            )
