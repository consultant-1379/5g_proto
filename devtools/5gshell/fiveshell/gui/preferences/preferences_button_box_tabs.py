from PySide2.QtCore import Signal
from PySide2.QtGui import Qt
from PySide2.QtWidgets import QTabWidget, QWidget, QInputDialog, QLineEdit, QMessageBox, QTreeWidgetItem

from gui.preferences.preferences_column import ButtonBoxPreferences


class ButtonBoxPreferenceTabs(QTabWidget):
    signal_modified = Signal()

    def __init__(self, button_box_configs, title_button_box_preferences):
        super(ButtonBoxPreferenceTabs, self).__init__()
        self.button_box_configs = button_box_configs
        self.tabs = {}
        self.tab_names = []  # to get the order later

        for button_box in self.button_box_configs:
            new_button_box_preferences = ButtonBoxPreferences(title_button_box_preferences)
            new_button_box_preferences.rename_tab_button.clicked.connect(self.rename_tab)
            new_button_box_preferences.clone_button.clicked.connect(self.clone_to_tab)
            self.tabs[button_box["name"]] = new_button_box_preferences
            self.tab_names.append(button_box["name"])
            self.addTab(self.tabs[button_box["name"]], button_box["name"])

        for tab_name in self.tab_names:
            self.fill_tab_drop_down(tab_name)

        self.addTab(QWidget(), "+")

        self.setCurrentIndex(0)
        self.current_button_box = self.widget(0)

        self.setTabsClosable(True)
        self.setMovable(True)
        self.tabBar().setTabButton(self.count() - 1, self.tabBar().RightSide, None)  # + is not closable
        self.currentChanged.connect(self.update_current_button_box)
        self.tabCloseRequested.connect(self.close_tab)
        self.tabBar().tabBarDoubleClicked.connect(self.rename_tab)
        self.tabBar().tabMoved.connect(self.moved)

    def update_current_button_box(self):
        index = self.sender().currentIndex()
        if index == self.count() - 1 and self.tabText(index) == '+':
            self.add_new_tab(index)
        elif index < self.count() - 1:
            self.current_button_box = self.widget(self.currentIndex())

    def add_new_tab(self, index):
        title, ok = QInputDialog.getText(self, "Add button box", "Type in a name for your new button box:",
                                         QLineEdit.Normal)
        if ok and title:
            new_button_box_preferences = ButtonBoxPreferences(title)
            self.insertTab(index, new_button_box_preferences, title)
            self.setCurrentIndex(index)
            self.current_button_box = self.widget(index)
            self.tabs[title] = new_button_box_preferences
            self.tab_names.append(title)
            self.signal_modified.emit()

    def close_tab(self, index):
        title = self.tabText(index)
        if self.count() > 2:
            del self.tab_names[index]
            del self.tabs[title]
            if index == self.count() - 2:
                self.setCurrentIndex(self.count() - 3)
            self.removeTab(index)
            self.signal_modified.emit()
        else:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("Sorry, you can not close the last button box. \n"
                        "Please create a new one before closing this one.")
            msg.exec_()
        self.current_button_box = self.widget(self.currentIndex())

    def rename_tab(self):
        current_title = self.tabText(self.currentIndex())
        new_title, ok = QInputDialog().getText(self, "Rename Tab", "Enter tab name:", QLineEdit.Normal, current_title)
        if ok and new_title:
            self.setTabText(self.currentIndex(), new_title)
            self.tab_names[self.currentIndex()] = new_title
            del self.tabs[current_title]
            self.tabs[new_title] = self.widget(self.currentIndex())
            self.signal_modified.emit()

    def moved(self, from_index, to_index):
        if from_index < len(self.tab_names) and to_index < len(self.tab_names):
            self.tab_names[from_index], self.tab_names[to_index] = self.tab_names[to_index], self.tab_names[from_index]
            self.signal_modified.emit()
        elif self.tabText(self.count() - 1) != '+':
            self.tabBar().moveTab(to_index, from_index)

    def clone_to_tab(self):
        dest_tab = self.current_button_box.tabs_drop_down.currentText()
        item = self.current_button_box.tree_widget.selectedItems()[0]
        if item.parent() is None:  # if item is a group
            self.signal_modified.emit()
            new_group = self.tabs[dest_tab].tree_widget.add_group(item.text(0))
            for i in range(item.childCount()):
                cur_child = item.child(i)
                self.tabs[dest_tab].tree_widget.add_item_to_group(new_group, cur_child.data(1, Qt.UserRole))
        elif item.parent() is not None:  # if item is a Button
            self.signal_modified.emit()
            self.tabs[dest_tab].tree_widget.add_item_to_group(
                self.tabs[dest_tab].tree_widget.group_objects[-1],
                item.data(1, Qt.UserRole)
            )
        self.setCurrentIndex(self.tab_names.index(dest_tab))

    def fill_tab_drop_down(self, current_name):
        for name in self.tab_names:
            if name != current_name:
                self.tabs[current_name].tabs_drop_down.addItem(name)
