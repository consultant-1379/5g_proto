# 2020-10-21 Alina Behrens, Maximilian Pohl, Jessica Klein, Alexander Langer

from PySide2.QtCore import Slot, QRegularExpression
from PySide2.QtGui import QTextDocument
from PySide2.QtWidgets import QPushButton, QLineEdit, QCheckBox, QWidget, QVBoxLayout, QHBoxLayout

from gui.growing_text_view import GrowingTextEdit


class LogView(QWidget):
    """ Represents a log view section with the text and the search functionality.
    This includes the text edit, the line edit, the forward and backward search buttons
    and the case sensitive and regex checkboxes"""

    def __init__(self, name):
        super().__init__()
        # printed text
        self.text_edit = GrowingTextEdit()
        # self.text_edit.setReadOnly(True)
        self.text_edit.setMaximumBlockCount(20000)
        self.text_edit.ensureCursorVisible()

        self.name = name  # Necessary when outsourcing the window

        # create the necessary Widgets for searching
        self.forward_search_button = QPushButton("Search \u2193")  # arrow down
        self.backward_search_button = QPushButton("Search \u2191")  # arrow up

        # create the line edit to search for strings
        self.search_line = QLineEdit()
        # creates the checkboxes
        self.check_case_sen = QCheckBox('Case sensitive')
        self.check_reg_ex = QCheckBox('RegEx')

        # connect the buttons to the methods
        self.forward_search_button.clicked.connect(self.forward_search)
        self.backward_search_button.clicked.connect(self.backward_search)
        self.search_line.returnPressed.connect(self.backward_search)

        # create the layout
        self.layout = QVBoxLayout(self)  # overall layout of the log view
        self.layout.setMargin(0)
        self.log_box = QHBoxLayout()  # layout of the text edit
        self.log_box.addWidget(self.text_edit)
        self.buttonBox = QHBoxLayout()  # layout of the line edit, buttons, checkboxes
        self.buttonBox.addWidget(self.search_line)
        self.buttonBox.addWidget(self.backward_search_button)
        self.buttonBox.addWidget(self.forward_search_button)
        self.buttonBox.addWidget(self.check_case_sen)
        self.buttonBox.addWidget(self.check_reg_ex)
        self.layout.addLayout(self.log_box)
        self.layout.addLayout(self.buttonBox)

    def set_text(self, text):
        """Adds the given text to the end of the text edit"""
        self.text_edit.insertPlainText(str(text))

    @Slot()
    def forward_search(self):
        """looks for the search expression from the current cursor forwards"""
        if self.check_reg_ex.isChecked():
            self.forward_search_regex()
        else:
            self.unicode_forward_search()

    def unicode_forward_search(self):
        search_expression = self.search_line.text()
        flag = QTextDocument.FindCaseSensitively if self.check_case_sen.isChecked() else QTextDocument.FindFlag(0)
        if self.text_edit.find(search_expression, flag):
            print('found')
            self.text_edit.ensureCursorVisible()
        else:
            print('reached end of the text')

    @Slot()
    def backward_search(self):
        """looks for the search expression from the current cursor backwards"""
        if self.check_reg_ex.isChecked():
            self.backward_search_regex()
        else:
            self.unicode_backward_search()

    def unicode_backward_search(self):
        search_expression = self.search_line.text()
        flag = QTextDocument.FindBackward
        flag |= QTextDocument.FindCaseSensitively if self.check_case_sen.isChecked() else QTextDocument.FindFlag(0)
        if self.text_edit.find(search_expression, flag):
            print('found')
            self.text_edit.ensureCursorVisible()
        else:
            print('reached end of the text')

    def forward_search_regex(self):
        search_expression = self.search_line.text()
        regex = QRegularExpression()
        regex.setPattern(search_expression)
        if regex.isValid():
            flag = QTextDocument.FindCaseSensitively if self.check_case_sen.isChecked() else QTextDocument.FindFlag(0)
            if self.text_edit.find(regex, flag):
                print('found')
                self.text_edit.ensureCursorVisible()
            else:
                print('reached end of the text')
        else:
            print('wrong regular expression')

    def backward_search_regex(self):
        search_expression = self.search_line.text()
        regex = QRegularExpression()
        regex.setPattern(search_expression)
        if regex.isValid():
            flag = QTextDocument.FindBackward
            flag |= QTextDocument.FindCaseSensitively if self.check_case_sen.isChecked() else QTextDocument.FindFlag(0)
            if self.text_edit.find(regex, flag):
                print('found')
                self.text_edit.ensureCursorVisible()
            else:
                print('reached end of the text')
        else:
            print('wrong regular expression')
