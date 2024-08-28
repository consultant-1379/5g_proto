# 2019-11-19 Maximilian Pohl, Alexander Langer, Jessica Klein

from PySide2.QtCore import Signal, Qt
from PySide2.QtWidgets import QWidget, QLabel, QSizePolicy, QSplitter, QHBoxLayout

from utils.gui_utils import make_icon_pixmap


class LogIconToggleLabel(QWidget):
    """An icon+label widget that, when clicked, shows "the other" icon
    and emits a signal that it was clicked."""
    clicked = Signal()

    def __init__(self, icon_closed, icon_open, title, head_button_layout):
        """Create a new JIconLabel with two images and a title. The images are
        for the open and closed state. They are toggled on each click. The
        initial state is closed."""
        super().__init__()
        self.isOpen = True
        self.icon_closed_pix = make_icon_pixmap(icon_closed)
        self.icon_open_pix = make_icon_pixmap(icon_open)
        self.icon = QLabel()
        self.icon.setPixmap(self.icon_closed_pix)
        self.icon.setAlignment(Qt.AlignCenter)
        self.icon.setSizePolicy(QSizePolicy.Maximum, QSizePolicy.Fixed)
        self.title = QLabel(title)
        self.title.setAlignment(Qt.AlignCenter)
        self.title.setAutoFillBackground(True)
        self.title.setSizePolicy(QSizePolicy.Minimum, QSizePolicy.Fixed)
        self.textStatus = QLabel()
        self.textStatus.setSizePolicy(QSizePolicy.Minimum, QSizePolicy.Minimum)
        self.splitter = QSplitter()
        self.splitter.addWidget(self.title)
        self.splitter.addWidget(self.textStatus)
        self.splitter.setSizes([99, 1])
        self.update_widget()
        self.layout = QHBoxLayout(self)
        self.layout.addWidget(self.icon)
        self.layout.addWidget(self.splitter)
        self.layout.addLayout(head_button_layout)
        self.layout.setMargin(0)

    def mousePressEvent(self, event):
        """Overloads the QWidget mousePressEvent so that we can toggle the
        image and emit a "clicked" signal"""
        self.isOpen = not self.isOpen
        self.update_widget()
        self.clicked.emit()

    def update_widget(self):
        """Set the correct icon and background color for the open/close
        state"""
        p = self.title.palette()
        if self.isOpen:
            self.icon.setPixmap(self.icon_open_pix)
            p.setColor(self.title.backgroundRole(), "#e0e0e0")
        else:
            self.icon.setPixmap(self.icon_closed_pix)
            p.setColor(self.title.backgroundRole(), "#e0e0e0")
        self.title.setPalette(p)

    def ended_printout(self):
        self.textStatus.setText("\u2713")

    def started_printout(self):
        self.textStatus.setText("\u21bb")
