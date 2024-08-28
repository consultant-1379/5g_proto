import json
import random

from PySide2.QtGui import QIcon, QPixmap, QDesktopServices
from PySide2.QtWidgets import QDialog, QLineEdit, QPushButton, QHBoxLayout, QVBoxLayout, QFormLayout, QLabel, \
    QDialogButtonBox, QColorDialog, QRadioButton, QCheckBox, QMessageBox

from utils.config_utils import add_action_to_user_config, add_button_to_user_config


class SimpleButtonDialog(QDialog):
    def __init__(self, user_config_filename, action_config):
        super(SimpleButtonDialog, self).__init__()
        self.setWindowTitle("New Button")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        self.action_config = action_config
        self.action_list = list(action_config.keys())

        self.line_edit_label = QLineEdit()
        self.line_edit_tooltip = QLineEdit()
        self.line_edit_color = QLineEdit()
        self.line_edit_command = QLineEdit()

        self.radio_button_linux = QRadioButton("Linux")
        self.radio_button_python = QRadioButton("Python")
        self.radio_box_type = QHBoxLayout()
        self.radio_box_type.addWidget(self.radio_button_linux)
        self.radio_box_type.addWidget(self.radio_button_python)

        self.button_find_unicode = QPushButton("Find unicode symbol")
        self.button_find_unicode.clicked.connect(lambda: QDesktopServices.openUrl("https://unicode-table.com/en/sets/"))
        self.label_pair = QHBoxLayout()
        self.label_pair.addWidget(self.line_edit_label)
        self.label_pair.addWidget(self.button_find_unicode)

        self.color_pick_button = QPushButton()
        self.color_pick_button.setIcon(QIcon(QPixmap("icons/color_picker.png")))
        self.color_pick_button.setMaximumWidth(25)
        self.color_pick_button.setToolTip("Pick a color for your button")
        self.color_pair = QHBoxLayout()
        self.color_pair.addWidget(self.line_edit_color)
        self.color_pair.addWidget(self.color_pick_button)

        self.checkbox_no_log_view = QCheckBox("Do not create a new log view when executing this command")

        self.button_advanced = QPushButton("Advanced...")

        self.color_pick_button.clicked.connect(self.open_color_pick_dialog)

        self.layout = QVBoxLayout()
        self.form_layout = QFormLayout()
        label_label = QLabel("Label \U000024D8")
        label_label.setToolTip("The name that is displayed on your button. \n"
                               "Unicode symbols can be added with \\U and 8 digits. e.g.'\\U00001234'")
        self.form_layout.addRow(label_label, self.label_pair)
        label_tooltip = QLabel("Tooltip \U000024D8")
        label_tooltip.setToolTip("A description of your button.")
        self.form_layout.addRow(label_tooltip, self.line_edit_tooltip)
        self.form_layout.addRow(QLabel("Type"), self.radio_box_type)
        label_command = QLabel("Command \U000024D8")
        label_command.setToolTip("A one line Python or Linux command.")
        self.form_layout.addRow(label_command, self.line_edit_command)
        label_color = QLabel("Color (optional) \U000024D8")
        label_color.setToolTip("The color of your button as hex code (without #). \nDefault: 000000 (black)")
        self.form_layout.addRow(label_color, self.color_pair)
        self.checkbox_no_log_view.setToolTip("Normally, when executing commands, a log view window pops up. "
                                             "\nIf you want your command being executed silently, tick this option.")
        self.form_layout.addRow(QLabel(""), self.checkbox_no_log_view)
        self.form_layout.addRow(QLabel(""), self.button_advanced)

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

    def on_ok_button_clicked(self, user_config_filename):
        if self.line_edit_label.text() and self.line_edit_tooltip.text() and \
                self.line_edit_command.text() and \
                (self.radio_button_linux.isChecked or self.radio_button_python.isChecked()):
            try:
                self.button_id = "{}_{}_custom".format(
                    self.line_edit_label.text().encode().decode('unicode-escape').encode("ascii", "ignore").decode()
                        .replace(" ", "_").lower(),
                    random.randint(0, 10000))
            except:
                msg = QMessageBox()
                msg.setWindowTitle("ERROR")
                msg.setIcon(QMessageBox.Critical)
                msg.setText("Your unicode in the label seems not to be correct. \n"
                            "Please make sure that it exists and starts with \\U followed by exactly 8 digits.")
                msg.exec_()

            action_dict = self.create_action_dict()
            if action_dict:
                success_action = add_action_to_user_config(user_config_filename, action_dict)
                if success_action:
                    self.action_config.update(action_dict)
                    button_dict = self.create_button_dict()
                    success_button = add_button_to_user_config(user_config_filename, button_dict)

                    if success_button:
                        self.accept()
        else:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("You did not fill all non optional fields. "
                        "\nPlease check your input and try again.")
            msg.exec_()

    def create_button_dict(self):
        button_dict = {
            self.button_id: {
                "action_ids": [
                    self.button_id
                ]
            }
        }
        if self.line_edit_color.text():
            button_dict[self.button_id]["color"] = self.line_edit_color.text()
        button_dict[self.button_id]["tags"] = []
        if "Custom" not in button_dict[self.button_id]["tags"]:
            button_dict[self.button_id]["tags"].insert(0, "Custom")
        self.button_dict = button_dict[self.button_id]
        return button_dict

    def create_action_dict(self):
        label = self.line_edit_label.text().encode().decode('unicode-escape')
        action_dict = {self.button_id: {
            "cmd": self.line_edit_command.text(),
            "label": label,
            "tooltip": self.line_edit_tooltip.text(),
            "type": "linux" if self.radio_button_linux.isChecked() else "python"
        }}
        if self.checkbox_no_log_view.isChecked():
            action_dict[self.button_id]["type"] += " no log"
        return action_dict

    def open_color_pick_dialog(self):
        standard_colors = json.load(open("icons/colors.json"))
        color_dialog = QColorDialog()
        color_dialog.setOption(QColorDialog.DontUseNativeDialog)
        for i in range(len(standard_colors)):
            color_dialog.setStandardColor(i, standard_colors[i])
        res = color_dialog.exec_()
        if res:
            self.line_edit_color.setText(color_dialog.selectedColor().name().replace("#", ""))
