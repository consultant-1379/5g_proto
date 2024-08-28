# 2019-08-27 Jessica Klein, Alexander Langer
from PySide2.QtCore import Slot
from PySide2.QtWidgets import QWidget, QVBoxLayout

from gui.buttons import Buttons
from gui.icon_toggle_label import IconToggleLabel


class AccordionSection(QWidget):
    """A section in the accordion, consisting of the clickable label
    to show/hide the group, and a set of buttons inside the group"""

    def __init__(self, group_name):
        super().__init__()
        self.layout = QVBoxLayout(self)
        self.groupLabel = IconToggleLabel("icons/triangle_right.png", "icons/triangle_down.png", group_name)
        self.buttons = Buttons([])
        self.layout.addWidget(self.groupLabel)
        self.layout.addWidget(self.buttons)
        self.layout.setMargin(0)
        self.groupLabel.clicked.connect(self.on_click)

    def add_button(self, button):
        """Add a single button to the section"""
        self.buttons.add_button(button)

    def add_combo_box(self, combobox):
        """Add a single combobox to the section"""
        self.buttons.add_combo_box(combobox)

    def add_radio_button_group_box(self, radiobutton_box):
        """Add a group box with radio buttons to the section"""
        self.buttons.add_radio_button_group_box(radiobutton_box)

    def add_check_box(self, checkbox):
        """Add a single checkbox to the section"""
        self.buttons.add_line_edit(checkbox)

    def add_line_edit(self, line_edit):
        """Add a single line edit to the section"""
        self.buttons.add_check_box(line_edit)

    def add_log_view(self, log_view):
        """Add a single log view to the section"""
        self.buttons.add_log_view(log_view)

    @Slot()
    def on_click(self):
        if self.groupLabel.isOpen:
            self.buttons.setVisible(True)
        else:
            self.buttons.setVisible(False)
