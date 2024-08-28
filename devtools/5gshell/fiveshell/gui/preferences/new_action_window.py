from PySide2.QtGui import QIcon, QDesktopServices
from PySide2.QtWidgets import QDialog, QLabel, QLineEdit, QHBoxLayout, QVBoxLayout, QFormLayout, QPushButton, \
    QDialogButtonBox, QRadioButton, QTextEdit, QMessageBox, QCheckBox

from utils.config_utils import add_action_to_user_config


class NewActionDialog(QDialog):
    def __init__(self, user_config_filename, action_config, variables_dict, edit_mode=False):
        super(NewActionDialog, self).__init__()
        self.setWindowTitle("New Action")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        self.action_config = action_config
        self.edit_mode = edit_mode
        self.variables_dict = variables_dict

        self.line_edit_action_id = QLineEdit()
        self.line_edit_label = QLineEdit()
        self.text_edit_command = QTextEdit()
        self.line_edit_tooltip = QLineEdit()
        self.line_edit_shortcut = QLineEdit()
        self.line_edit_dir = QLineEdit()

        self.line_variable = QHBoxLayout()
        self.line_edit_var_name = QLineEdit()
        self.line_edit_var_filter = QLineEdit()
        self.line_variable.addWidget(QLabel("name:"))
        self.line_variable.addWidget(self.line_edit_var_name)
        self.line_variable.addWidget(QLabel("filter:"))
        self.line_variable.addWidget(self.line_edit_var_filter)
        self.button_add_variable = QPushButton("Add variable")
        self.additional_variables = []
        self.button_add_variable.clicked.connect(self.add_additional_variable)

        self.line_loop = QHBoxLayout()
        self.line_edit_loop_name = QLineEdit()
        self.line_edit_loop_var = QLineEdit()
        self.line_loop.addWidget(QLabel("for "))
        self.line_loop.addWidget(self.line_edit_loop_name)
        self.line_loop.addWidget(QLabel(" in "))
        self.line_loop.addWidget(self.line_edit_loop_var)

        self.radio_button_linux = QRadioButton("Linux")
        self.radio_button_python = QRadioButton("Python")
        self.radio_box_type = QHBoxLayout()
        self.radio_box_type.addWidget(self.radio_button_linux)
        self.radio_box_type.addWidget(self.radio_button_python)

        self.checkbox_no_log_view = QCheckBox("Do not create a new log view when executing this command")

        self.layout = QVBoxLayout()
        self.form_layout = QFormLayout()
        label_action_id = QLabel("Action ID \U000024D8")
        label_action_id.setToolTip("The unique key to define the action in the config:\n"
                                   "a short pregnant description of the action, words separated with _ .")
        self.form_layout.addRow(label_action_id, self.line_edit_action_id)
        label_label = QLabel("Label \U000024D8")
        label_label.setToolTip("The name of the action. \n"
                               "Unicode symbols can be added with \\U and 8 digits. e.g.'\\U00001234'")
        self.form_layout.addRow(label_label, self.line_edit_label)
        label_tooltip = QLabel("Tooltip \U000024D8")
        label_tooltip.setToolTip("A description of the action.")
        self.form_layout.addRow(label_tooltip, self.line_edit_tooltip)
        self.form_layout.addRow(QLabel("Type"), self.radio_box_type)
        label_command = QLabel("Command \U000024D8")
        label_command.setToolTip("A one line Python or Linux command.")
        self.form_layout.addRow(label_command, self.text_edit_command)
        label_shortcut = QLabel("Shortcut (optional) \U000024D8")
        label_shortcut.setToolTip("e.g. Ctrl+T")
        self.form_layout.addRow(label_shortcut, self.line_edit_shortcut)
        label_variables = QLabel("Variables (optional) \U000024D8")
        label_variables.setToolTip("Variables that you can use in the command section with the given name."
                                   "Check out jmespath for a documentation of the filters.")
        self.form_layout.addRow(label_variables, self.line_variable)
        self.form_layout.addRow(QLabel(""), self.button_add_variable)
        label_loop = QLabel("Loop (optional) \U000024D8")
        label_loop.setToolTip("Specifies an iterator variable that iterates through the given variable.")
        self.form_layout.addRow(label_loop, self.line_loop)
        label_dir = QLabel("Dir (optional) \U000024D8")
        label_dir.setToolTip("The directory where the command is executed.")
        self.form_layout.addRow(label_dir, self.line_edit_dir)
        self.checkbox_no_log_view.setToolTip("Normally, when executing commands, a log view window pops up. "
                                             "\nIf you want your command being executed silently, tick this option.")
        self.form_layout.addRow(QLabel(""), self.checkbox_no_log_view)

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

        self.action_id = None

    def on_ok_button_clicked(self, user_config_filename):
        if self.line_edit_action_id.text() and self.line_edit_label.text() and self.line_edit_tooltip.text() and \
                self.text_edit_command.toPlainText() and \
                (self.radio_button_linux.isChecked or self.radio_button_python.isChecked()):
            self.action_id = "{}_custom".format(self.line_edit_action_id.text())
            try:
                action_dict = self.create_action_dict()
            except:
                return
            success = add_action_to_user_config(user_config_filename, action_dict, edit_mode=self.edit_mode)
            if success:
                if "variables" in action_dict[self.action_id]:
                    for variable_dict in action_dict[self.action_id]['variables']:
                        self.variables_dict[variable_dict["name"]] = self.action_id
                if "loop" in action_dict[self.action_id]:
                    self.variables_dict[action_dict[self.action_id]["loop"]["name"]] = self.action_id
                self.action_config.update(action_dict)
                self.accept()
        else:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("You did not fill all non optional fields. "
                        "\nPlease check your input and try again.")
            msg.exec_()

    def create_action_dict(self):
        try:
            label = self.line_edit_label.text().encode().decode('unicode-escape')
        except:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("Your unicode in the label seems not to be correct. \n"
                        "Please make sure that it exists and starts with \\U followed by exactly 8 digits.")
            msg.exec_()
            raise Exception("inputs not entered correctly")
        action_dict = {self.action_id: {
            "cmd": self.text_edit_command.toPlainText(),
            "label": label,
            "tooltip": self.line_edit_tooltip.text(),
            "type": "linux" if self.radio_button_linux.isChecked() else "python"
        }}
        if self.checkbox_no_log_view.isChecked():
            action_dict[self.action_id]["type"] += " no log"
        if self.line_edit_shortcut.text():
            action_dict[self.action_id]["shortcut"] = self.line_edit_shortcut.text()
        if self.line_edit_dir.text():
            action_dict[self.action_id]["dir"] = self.line_edit_dir.text()
        if self.line_edit_var_name.text() and self.line_edit_var_filter.text():
            if self.check_if_variable_exists(self.line_edit_var_name.text()):
                raise Exception("inputs not entered correctly")
            action_dict[self.action_id]["variables"] = [
                {"filter": self.line_edit_var_filter.text(), "name": self.line_edit_var_name.text()}
            ]
        for additional_var in self.additional_variables:
            if additional_var[0].text() and additional_var[1].text():
                if self.check_if_variable_exists(additional_var[0].text()):
                    raise Exception("inputs not entered correctly")
                action_dict[self.action_id]["variables"].append(
                    {"filter": additional_var[1].text(), "name": additional_var[0].text()}
                )
        if self.line_edit_loop_name.text() and self.line_edit_loop_var.text():
            if self.check_if_variable_exists(self.line_edit_loop_name.text()):
                raise Exception("inputs not entered correctly")
            action_dict[self.action_id]["loop"] = {"name": self.line_edit_loop_name.text(),
                                                   "variable": self.line_edit_loop_var.text()}
        return action_dict

    def check_if_variable_exists(self, var_name):
        if var_name in self.variables_dict.keys():
            if self.variables_dict[var_name] != self.action_id:
                msg = QMessageBox()
                msg.setWindowTitle("Attention")
                msg.setIcon(QMessageBox.Warning)
                msg.setText(f"The variable '{var_name}' is already assigned in an other "
                            "context. \nPlease choose an other name.")
                msg.setStandardButtons(QMessageBox.Ok)
                msg.exec_()
                return True
        return False

    def add_additional_variable(self):
        additional_variable_line = QHBoxLayout()
        delete_button = QPushButton("\U0000274C")
        delete_button.setMaximumWidth(25)

        line_edit_name = QLineEdit()
        line_edit_filter = QLineEdit()

        additional_variable_line.addWidget(QLabel("name:"))
        additional_variable_line.addWidget(line_edit_name)
        additional_variable_line.addWidget(QLabel("filter:"))
        additional_variable_line.addWidget(line_edit_filter)

        delete_button.setProperty('row', additional_variable_line)
        delete_button.setProperty('line_edits', (line_edit_name, line_edit_filter))

        delete_button.clicked.connect(self.remove_additional_variable_line)

        self.additional_variables.append((line_edit_name, line_edit_filter))
        self.form_layout.insertRow(
            self.form_layout.getItemPosition(self.form_layout.indexOf(self.button_add_variable))[0],  # row number
            delete_button,
            additional_variable_line
        )

    def remove_additional_variable_line(self):
        self.additional_variables.remove(self.sender().property('line_edits'))
        self.form_layout.removeRow(self.sender().property('row'))
