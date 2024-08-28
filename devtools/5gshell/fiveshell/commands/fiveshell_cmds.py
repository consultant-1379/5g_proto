from PySide2.QtWidgets import QApplication
import g


# connected to action quit_fiveshell
def quit_fiveshell(signum, frame):
    """End this program. Do all cleanup, then exit."""
    print("Exiting...")
    QApplication.quit()


def clear_output():  # connected to action clear_output
    g.app.activeWindow().main_widget.log_area.clear_output()


def close_one_window():  # connected to action close_one_window
    g.app.activeWindow().close()
