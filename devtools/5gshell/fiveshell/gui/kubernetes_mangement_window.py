from PySide2.QtGui import QCloseEvent
from PySide2.QtWidgets import QWidget, QListWidget

from utils.config_utils import *

import g


class KubernetesManagementWindow(QWidget):
    def __init__(self, window_width, window_height):
        super().__init__()

        self.setWindowTitle("Kubernetes Manager")
        self.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

        self.info_label = QLabel("All kubernetes contexts from your ~/.kube/config are listed below. \n"
                                 "You can remove contexts (this will also remove clusters and users with the "
                                 "same name) and add new contexts. \n \n"
                                 "After adding a new context, the new context will automatically be your current "
                                 "context.")
        self.info_label.setSizePolicy(QSizePolicy.Maximum, QSizePolicy.Maximum)

        self.context_list = QListWidget()
        contexts, cur_index = get_list_of_available_kube_contexts()
        self.context_list.addItems(contexts)

        self.add_context_button = QPushButton("Add new context")
        self.add_context_button.clicked.connect(self.open_add_context_gui_add_item_to_list)

        self.remove_button = QPushButton("Remove selected context")
        self.remove_button.clicked.connect(self.remove_current_selected)

        self.layout = QVBoxLayout()
        self.layout.addWidget(self.info_label)
        self.layout.addWidget(self.context_list)
        self.layout.addWidget(self.add_context_button)
        self.layout.addWidget(self.remove_button)
        self.setLayout(self.layout)

        self.resize(window_width, window_height)

    def closeEvent(self, event: QCloseEvent):
        event.accept()
        g.kubernetes_management_window = None

    def remove_current_selected(self):
        """
        Removes the current selected context from the .kube/config file and if this succeeds removes it from the gui.
        """
        for item in self.context_list.selectedItems():
            success = self.remove_context_from_kube_config(item.text())
            if success:
                self.context_list.takeItem(self.context_list.row(item))

    def open_add_context_gui_add_item_to_list(self):
        """
        Opens a dialog to add a new context and adds the given context name to the gui.
        """
        context_name = open_add_context_gui_and_add_context()
        if context_name:
            self.context_list.addItem(context_name)

    @staticmethod
    def remove_context_from_kube_config(context_name):
        """
        Removes the context, cluster and user with the given name from the .kube/config file.
        Returns True if the removing succeeded else False.
        """
        current_context = subprocess.run("kubectl config current-context", shell=True,
                                         stdout=subprocess.PIPE).stdout.decode("ascii").rstrip()
        if context_name == current_context:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("The current context can not be deleted. "
                        "\nPlease change your context to delete this context.")
            msg.exec_()
        else:  # TODO some error handling?
            command = f"kubectl config delete-context {context_name}  && kubectl config delete-cluster {context_name}" \
                      f" && kubectl config unset users.{context_name}"
            subprocess.run(command, shell=True, stdout=subprocess.PIPE).stdout.decode("ascii")
            return True
        return False
