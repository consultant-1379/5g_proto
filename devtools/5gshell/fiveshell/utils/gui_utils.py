from PySide2.QtCore import QPoint
from PySide2.QtGui import QPixmap

import g


def make_icon_pixmap(filename):
    """Helper function to create suitable QPixmap from an image whose
    filename is given"""
    pix = QPixmap()
    pix.load(filename)
    pix.setDevicePixelRatio(40)
    return pix


def get_configured_window_dimensions():
    """Read the user-configured window dimensions (width,
        height, and size of the buttonbox) from the config
        file. Use sensible defaults (= must fit in full-HD)
        and make sure that it's not too small"""
    mainwindow_width = g.globalDict.get("mainwindow_width", 1200)
    mainwindow_height = g.globalDict.get("mainwindow_height", 1000)
    buttonbox_width = g.globalDict.get("buttonbox_width", 470)
    mainwindow_x_pos = g.globalDict.get("mainwindow_x_pos", 20)
    mainwindow_y_pos = g.globalDict.get("mainwindow_y_pos", 20)
    mainwindow_width = max(mainwindow_width, 400)
    mainwindow_height = max(mainwindow_height, 400)
    buttonbox_width = min(buttonbox_width, mainwindow_width - 100)
    return mainwindow_width, mainwindow_height, buttonbox_width, QPoint(mainwindow_x_pos, mainwindow_y_pos)
