# 2020-10-21 Alina Behrens, Maximilian Pohl, Jessica Klein, Alexander Langer
from PySide2.QtCore import Qt
from PySide2.QtWidgets import QVBoxLayout, QWidget, QHBoxLayout


class LogViewWindow(QWidget):
    def __init__(self, log_view):
        # create Window
        QWidget.__init__(self)
        self.main_layout = QVBoxLayout()
        self.button_box = QHBoxLayout()
        self.setWindowTitle(log_view.name)

        self.text_edit = log_view.text_edit
        self.text_edit.set_resizeable(False)
        self.text_edit.setVerticalScrollBarPolicy(Qt.ScrollBarAsNeeded)

        # changes the size of the new window to value,
        # that fits in the screen, but not waste space.
        log_view_height = self.text_edit.height()
        if log_view_height < 900:
            self.resize(1000, log_view_height + 50)
        else:
            self.resize(1000, 1000)
        self.text_edit.setMinimumHeight(50)

        # to ensure search works the Widgets have to be
        # moved to self (window).
        self.search_line = log_view.search_line
        self.backward_search_button = log_view.backward_search_button
        self.forward_search_button = log_view.forward_search_button
        self.forward_search_button = log_view.forward_search_button
        self.check_case_sen = log_view.check_case_sen
        self.check_reg_ex = log_view.check_reg_ex

        self.button_box.addWidget(self.search_line)
        self.button_box.addWidget(self.backward_search_button)
        self.button_box.addWidget(self.forward_search_button)
        self.button_box.addWidget(self.check_case_sen)
        self.button_box.addWidget(self.check_reg_ex)
        self.main_layout.addWidget(self.text_edit)
        self.main_layout.addLayout(self.button_box)
        self.setLayout(self.main_layout)
