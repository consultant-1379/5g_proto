from PySide2 import QtCore
from PySide2.QtWidgets import QListWidget, QListWidgetItem, QAbstractItemView

from gui.preferences.config_structures import Button


class ListWidget(QListWidget):
    def __init__(self):
        super().__init__()
        self.setDragDropMode(QAbstractItemView.DragOnly)

    def add_item(self, preference_obj):
        item = QListWidgetItem(
            str("{}  {}".format(preference_obj.get_name(), preference_obj.get_tags_as_string()))
        )

        item.setData(QtCore.Qt.UserRole, preference_obj)
        self.addItem(item)

    def remove_current_selected(self):
        for item in self.selectedItems():
            self.takeItem(self.row(item))


class ButtonListWidget(ListWidget):
    def __init__(self):
        super().__init__()

    def add_item(self, preference_obj):
        if isinstance(preference_obj, Button):
            super().add_item(preference_obj)
