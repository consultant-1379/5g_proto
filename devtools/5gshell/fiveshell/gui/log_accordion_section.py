# 2020-10-21 Alina Behrens, Maximilian Pohl, Jessica Klein, Alexander Langer

from PySide2.QtCore import Slot
from PySide2.QtWidgets import QWidget, QVBoxLayout, QPushButton, QHBoxLayout


from gui.log_icon_toggle_label import LogIconToggleLabel
from gui.log_view import LogView


class LogAccordionSection(QWidget):
    """A section in the accordion, consisting of the clickable label
    to show/hide the group, the new window and delete button and the log view"""

    def __init__(self, group_name):
        super().__init__()
        self.section_name = group_name
        self.layout = QVBoxLayout(self)  # overall layout of the section

        self.new_window_button = QPushButton('New Window')
        self.delete_button = QPushButton('Delete')
        # self.new_window_button.setFixedWidth(120)
        # self.delete_button.setFixedWidth(80)

        self.button_layout = QHBoxLayout()  # layout for the two buttons next to the toggle label
        self.button_layout.addWidget(self.new_window_button)
        self.button_layout.addWidget(self.delete_button)

        self.group_label = LogIconToggleLabel("icons/triangle_right.png", "icons/triangle_up.png",
                                              self.section_name, self.button_layout)

        self.log_view = LogView(self.section_name)  # log view including line edit, buttons

        self.new_window_button.setProperty("log_view", self.log_view)  # add the associated log_view to the button
        # to be able to create a separate window when pressing the button

        self.layout.addWidget(self.log_view)
        self.layout.addWidget(self.group_label)
        self.layout.setMargin(0)
        self.group_label.clicked.connect(self.on_click)

    @Slot()
    def on_click(self):
        if self.group_label.isOpen:
            self.log_view.setVisible(True)
        else:
            self.log_view.setVisible(False)
