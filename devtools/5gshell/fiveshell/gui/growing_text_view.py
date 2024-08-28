# 2019-11-19 Maximilian Pohl, Alexander Langer, Jessica Klein
from PySide2.QtCore import QSize
from PySide2.QtGui import QFont, Qt
from PySide2.QtWidgets import QPlainTextEdit


class GrowingTextEdit(QPlainTextEdit):
    def __init__(self, *args, **kwargs):
        super(GrowingTextEdit, self).__init__(*args, **kwargs)
        self.setVerticalScrollBarPolicy(Qt.ScrollBarAlwaysOff)
        self.automaticResize = True
        self.document().contentsChanged.connect(self.size_change)

        monospaced_font = QFont("nonexistent")
        monospaced_font.setStyleHint(QFont.Monospace)
        self.setFont(monospaced_font)
        self.setLineWrapMode(QPlainTextEdit.NoWrap)

    def size_change(self):
        if self.automaticResize:
            count = 0
            lines = self.toPlainText()
            for i in range(0, len(lines)):
                if lines[i] == '\n':
                    count += 1
            self.setMinimumHeight(self.fontMetrics().height() * (count + 2) + self.fontMetrics().height())

    def set_resizeable(self, boolean):
        self.automaticResize = boolean
