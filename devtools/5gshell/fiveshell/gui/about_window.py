# 2020-12-16 Alina Behrens
import subprocess

from PySide2.QtCore import Qt
from PySide2.QtGui import QFont, QIcon
from PySide2.QtWidgets import QDialog, QVBoxLayout, QLabel


class AboutWindow(QDialog):
    def __init__(self):
        super(AboutWindow, self).__init__()
        self.setWindowTitle("About")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        self.header = QLabel()
        self.header.setStyleSheet("background-color: transparent;")
        self.header.setText("Fiveshell")
        monospaced_font = QFont("nonexistent", 15, QFont.Bold)
        monospaced_font.setStyleHint(QFont.Monospace)
        self.header.setFont(monospaced_font)
        self.header.setAlignment(Qt.AlignCenter)

        self.text = QLabel()
        self.text.setStyleSheet("background-color: transparent;")

        # get the commit hash where the fiveshell folder was last changed
        # fiveshell folder one level under gui
        commit_hash = subprocess.check_output(['git', 'rev-list', '-1', 'HEAD', '..']).decode("utf-8").strip('\n')
        commit_date = subprocess.check_output(
            ['git', 'show', '-s', '--format=%cd', "--date=format:%d.%m.%Y", commit_hash]
        ).decode("utf-8").strip('\n')
        print(commit_date)
        self.text.setText("Version {} \n {} \n \nDevelopers: \nAlina Behrens \nAlexander Langer \n"
                          "Konstantina Karponi \nJessica Klein \nMaximilian Pohl".format(commit_hash[:9], commit_date))
        monospaced_font.setPointSize(10)
        monospaced_font.setBold(False)
        self.text.setFont(monospaced_font)
        self.text.setAlignment(Qt.AlignCenter)

        self.layout = QVBoxLayout(self)
        self.layout.setMargin(0)
        self.layout.addWidget(self.header)
        self.layout.addWidget(self.text)

        self.setFixedWidth(300)
