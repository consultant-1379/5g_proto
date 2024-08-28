# 2019-08-27 Jessica Klein, Alexander Langer

from PySide2.QtWidgets import QWidget
from gui import flow_layout


class Buttons(QWidget):
    """Collection of buttons in a grid-layout"""
    def __init__(self, buttons):
        """Buttons/Widgets can be added in the constructor or later with
        the addButton() function"""
        super().__init__()
        self.layout = flow_layout.FlowLayout(self)
        self.layout.setMargin(0)
        self.buttons = buttons
        for button in self.buttons:
            self.layout.addWidget(button)

    def add_button(self, button):
        """Add a single button"""
        self.buttons.append(button)
        self.layout.addWidget(button)

    def add_combo_box(self, combobox):
        """Add a single combobox"""
        self.buttons.append(combobox)
        self.layout.addWidget(combobox)

    def add_radio_button_group_box(self, radiobutton_box):
        """Add a group box of radio buttons"""
        self.buttons.append(radiobutton_box)
        self.layout.addWidget(radiobutton_box)

    def add_check_box(self, checkbox):
        """Add a single checkbox"""
        self.buttons.append(checkbox)
        self.layout.addWidget(checkbox)

    def add_line_edit(self, lineedit):
        """Add a single line_edit"""
        self.buttons.append(lineedit)
        self.layout.addWidget(lineedit)

    def add_log_view(self, logview):
        """Add a single logview"""
        self.buttons.append(logview)
        self.layout.addWidget(logview)
