# 2020-10-29 Alina Behrens
import PySide2
from PySide2 import QtCore, QtGui
from PySide2.QtCore import Signal
from PySide2.QtWidgets import QTreeWidget, QTreeWidgetItem, QAbstractItemView

from gui.preferences.config_structures import ButtonInBox
from gui.preferences.list_widget import ButtonListWidget


class TreeWidget(QTreeWidget):
    signal_modified = Signal()

    def __init__(self):
        """
        creates a tree widget that has two levels:
        the items on the first level are called groups and they only have a name
        the items on the second level are called items and they have a name and a PreferenceObject as Data

        the items of the first level are saved in a list
        """
        super().__init__()
        self.group_objects = []

    def add_group(self, group_name):
        """
        adds a group with the given name on the top level of the tree widget and adds it to the group_objects list
        :param group_name:
        :return:
        """
        self.signal_modified.emit()
        group = QTreeWidgetItem([group_name])
        self.addTopLevelItem(group)
        self.group_objects.append(group)
        return group

    def __add_item_to_group_at_index(self, index, group_obj, preference_obj):
        """
        creates an item, sets its data to the given preferences object and adds to the given group object at the given
        index
        """
        self.signal_modified.emit()
        item = QTreeWidgetItem([preference_obj.get_name()])
        item.setData(1, QtCore.Qt.UserRole, preference_obj)
        group_obj.insertChild(index, item)
        self.setExpanded(self.indexFromItem(group_obj, 0), True)
        self.clearSelection()
        self.setItemSelected(item, True)

    def add_item_to_group(self, group_obj, preference_obj):
        """adds a new item with the given PreferenceObject to the end of the given group"""
        index = group_obj.childCount()
        self.__add_item_to_group_at_index(index, group_obj, preference_obj)

    def insert_item_below_selected_item(self, dest_item, preference_obj):
        """adds a new item with the given PreferenceObject below the given item"""
        dest_group = dest_item.parent()
        index = dest_group.indexOfChild(dest_item) + 1
        self.__add_item_to_group_at_index(index, dest_group, preference_obj)

    def move_item_to_group(self, dest_group, move_item):
        self.signal_modified.emit()
        src_group = move_item.parent()
        src_group.removeChild(move_item)
        index = dest_group.childCount()  # the order is important!
        dest_group.insertChild(index, move_item)
        self.setExpanded(self.indexFromItem(dest_group, 0), True)
        self.clearSelection()
        self.setItemSelected(move_item, True)

    def move_item_beneath_given_item(self, dest_item, move_item):
        if dest_item != move_item:
            self.signal_modified.emit()
            src_group = move_item.parent()
            src_group.removeChild(move_item)

            dest_group = dest_item.parent()
            index = dest_group.indexOfChild(dest_item) + 1
            dest_group.insertChild(index, move_item)
            self.clearSelection()
            self.setItemSelected(move_item, True)

    def move_group_beneath_given_group(self, dest_group, move_group):
        if dest_group != move_group:
            self.signal_modified.emit()
            self.takeTopLevelItem(self.indexOfTopLevelItem(move_group))
            index = self.indexOfTopLevelItem(dest_group)
            self.insertTopLevelItem(index + 1, move_group)
            self.group_objects.remove(move_group)
            self.group_objects.insert(self.group_objects.index(dest_group) + 1, move_group)
            self.clearSelection()
            self.setItemSelected(move_group, True)

    def move_group_beneath_given_item(self, dest_item, move_group):
        group = dest_item.parent()
        self.move_group_beneath_given_group(group, move_group)

    def move_group_to_bottom(self, move_group):
        index = self.topLevelItemCount() - 1
        dest_group = self.topLevelItem(index)
        self.move_group_beneath_given_group(dest_group, move_group)

    def remove_current_selected(self):
        self.signal_modified.emit()
        root = self.invisibleRootItem()
        for item in self.selectedItems():
            if item in self.group_objects:
                self.group_objects.remove(item)
            (item.parent() or root).removeChild(item)

    def dropEvent(self, event: PySide2.QtGui.QDropEvent):
        pass


class ButtonBoxTreeWidget(TreeWidget):
    def __init__(self):
        super().__init__()
        self.setDragDropMode(QAbstractItemView.DragDrop)

    def add_item_to_group(self, group_obj, preference_obj):
        if isinstance(preference_obj, ButtonInBox):
            super(ButtonBoxTreeWidget, self).add_item_to_group(group_obj, preference_obj)

    def dropEvent(self, event: PySide2.QtGui.QDropEvent):
        if event.source() == self:
            self.drop_from_inside(event)
        elif isinstance(event.source(), ButtonListWidget):
            self.drop_from_outside(event)
        else:
            event.ignore()

    def drop_from_outside(self, event):
        destination = self.itemAt(event.pos())
        if destination is not None:  # buttons always need to be in a group
            selected_items = event.source().selectedItems()
            if destination.parent() is None:  # destination is a group
                for item in selected_items:
                    self.add_item_to_group(destination, item.data(QtCore.Qt.UserRole).to_button_in_box())
            elif destination.parent().parent() is None:  # destination is an item
                for item in selected_items:
                    self.insert_item_below_selected_item(destination, item.data(QtCore.Qt.UserRole).to_button_in_box())
            event.accept()

        else:
            event.ignore()

    def drop_from_inside(self, event):
        destination = self.itemAt(event.pos())
        selected_item = event.source().selectedItems()[0]
        if selected_item.parent() is not None:  # selected item is a button
            if destination is not None:  # buttons always need to be in a group
                if destination.parent() is None:  # destination is a group
                    self.move_item_to_group(destination, selected_item)
                elif destination.parent().parent() is None:  # destination is an item
                    self.move_item_beneath_given_item(destination, selected_item)
                event.accept()
        elif selected_item.parent() is None:  # selected item is a group
            if destination is not None:
                if destination.parent() is None:  # destination is a group
                    self.move_group_beneath_given_group(destination, selected_item)
                elif destination.parent().parent() is None:  # destination is an item
                    self.move_group_beneath_given_item(destination, selected_item)
            else:
                self.move_group_to_bottom(selected_item)
