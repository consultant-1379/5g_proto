from PySide2.QtCore import Qt
from PySide2.QtGui import QDesktopServices
from PySide2.QtWidgets import QMessageBox, QFileDialog, QDialog, QLineEdit, QPushButton, QHBoxLayout, QDialogButtonBox, \
    QLabel, QVBoxLayout


def open_import_example_dialog():
    """
    opens a message dialog to ask the user if his config file should be expanded by the example config

    :returns: true if the user chooses yes else false
    """
    message_box = QMessageBox()
    message_box.setWindowTitle("Welcome to fiveshell")
    message_box.setText("Your config file seems not to fulfill all prerequisites for the new fiveshell version.")
    message_box.setInformativeText("Do you want to copy an example configuration to your ~/.fiveshellrc? ")
    message_box.setDetailedText("Your old config file will be copied to ~/.fiveshellrc.bak, so it will not be lost. \n"
                                "If you have your own custom configuration in your ~/.fiveshellrc and you want to "
                                "keep it, please contact Alina Behrens or Alexander Langer.")
    message_box.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
    message_box.setDefaultButton(QMessageBox.Yes)
    res = message_box.exec_()
    return res == QMessageBox.Yes


def open_create_fiveshellrc_dialog():
    """
    Opens a message dialog to ask the user he wants to have an .fiveshellrc file automatically created.
    If the user chooses yes he will be asked to provide the path to his 5g_proto folder

    :returns: false if the user does not want to have the file automatically created or cancels the process, or the path
    as string if the user presses OK
    """
    message_box = QMessageBox()
    message_box.setWindowTitle("Welcome to fiveshell")
    message_box.setText("It seems like you have no config file yet.")
    message_box.setInformativeText("Do you want to have a config file ~/.fiveshellrc automatically created?")
    message_box.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
    message_box.setDefaultButton(QMessageBox.Yes)
    res = message_box.exec_()

    if res == QMessageBox.No:
        return False

    dialog = QDialog()  # will contain a text label, a line edit, a button to choose a path and a ok and cancel button
    dialog.setModal(True)
    text_label = QLabel()
    text_label.setText("Please select the path to your 5g_proto folder:")
    line_edit = QLineEdit()
    choose_file_button = QPushButton("...")
    choose_file_button.clicked.connect(lambda: open_choose_directory_dialog(line_edit))
    ok_cancel_button_box = QDialogButtonBox(QDialogButtonBox.Ok | QDialogButtonBox.Cancel | QDialogButtonBox.Help)
    ok_cancel_button_box.accepted.connect(dialog.accept)
    ok_cancel_button_box.rejected.connect(dialog.reject)
    ok_cancel_button_box.helpRequested.connect(
        lambda: QDesktopServices.openUrl(
            "https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/FiveShell#GettingStarted"
        )
    )
    dialog_layout = QVBoxLayout()
    text_layout = QHBoxLayout()
    text_layout.addWidget(text_label)
    directory_layout = QHBoxLayout()
    directory_layout.addWidget(line_edit)
    directory_layout.addWidget(choose_file_button)
    standard_button_layout = QHBoxLayout()
    standard_button_layout.addWidget(ok_cancel_button_box)
    dialog_layout.addLayout(text_layout)
    dialog_layout.addLayout(directory_layout)
    dialog_layout.addLayout(standard_button_layout)
    dialog.setLayout(dialog_layout)
    res = dialog.exec_()  # returns 1 if the user presses Ok else 0
    if res:
        path = line_edit.text()
        return path
    return False


def open_choose_directory_dialog(line_edit):
    # The native dialog is not used here because it prints the warning:
    # Gtk-Message: hh:mm:ss.sss: GtkDialog mapped without a transient parent. This is discouraged
    dir_path = QFileDialog.getExistingDirectory(None, "Select the path to your 5g_proto folder", "/",
                                                QFileDialog.DontUseNativeDialog)
    line_edit.setText(dir_path)


