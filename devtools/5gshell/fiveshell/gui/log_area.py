# 2020-10-21 Alina Behrens, Maximilian Pohl, Jessica Klein, Alexander Langer
from PySide2.QtCore import Slot
from PySide2.QtWidgets import QWidget, QScrollArea, QVBoxLayout

import g
from gui.log_accordion import LogAccordion
from gui.log_view_window import LogViewWindow


class LogArea(QWidget):
    def __init__(self):
        """
        Creates an object of the class LogArea that represents the right part of the fiveshell window including the
        scroll area, the log accordions and all their content.
        """
        super().__init__()
        self.log_accordion = LogAccordion()

        # Scroll area
        self.scroll_area = QScrollArea()
        self.scroll_area.setWidget(self.log_accordion)
        self.scroll_area.setWidgetResizable(True)
        self.scroll_area.verticalScrollBar().rangeChanged.connect(lambda: self.scroll_down())

        self.number_of_sections = 0
        self.log_view_windows = []  # list of all open log view windows
        self.accordion_sections = []  # list of all accordion sections
        self.active_sections = {}  # a dictionary consisting of a timestamp as key and the accordion section as value
        # this is necessary to add output until the action is finished

        self.layout = QVBoxLayout()
        self.layout.addWidget(self.scroll_area)
        self.setLayout(self.layout)

        self.connected_to = []  # list of all connected ActionExecutor objects

    @Slot()  # is connected to self.scroll_area.verticalScrollBar().rangeChanged
    def scroll_down(self):
        end = self.scroll_area.verticalScrollBar().maximum()
        self.scroll_area.verticalScrollBar().setValue(end)

    def clear_output(self):
        """delete all accordion sections from the accordion"""
        self.log_accordion = None
        self.log_accordion = LogAccordion()
        self.scroll_area.setWidget(self.log_accordion)

    @Slot()
    def new_section(self, title, time):
        if g.app.activeWindow().main_widget.log_area is not self:  # check if this area is the area of the active window
            return
        """add a section to the accordion and add it to active_sections until action is finished"""
        self.number_of_sections += 1
        section = self.log_accordion.add_section("{}: {}".format(self.number_of_sections, title))
        self.accordion_sections.append(section)
        self.active_sections[time] = section

        # open the section in a new window if the button is clicked
        section.new_window_button.clicked.connect(self.open_in_new_window)

        # delete the section if the button is clicked
        # set the property of the button to the section it is in to delete this specific section when clicked
        section.delete_button.setProperty("section", section)
        section.delete_button.clicked.connect(self.delete_section)

    @Slot()
    def add_output(self, text, time):
        """add some text to a specific active window that is chosen by the unique timestamp"""
        if time not in self.active_sections:  # check if this area is the area of the correct window
            return
        self.active_sections[time].log_view.set_text(text)

    @Slot()
    def end_cmd(self, time):
        """when the action is finished the section is removed from the active sections"""
        if time not in self.active_sections:  # check if this area is the area of the correct window
            return
        self.active_sections.pop(time)

    # @Slot() if this is a Slot it does not work anymore (I have no idea why)
    def open_in_new_window(self):  # connected to section.new_window_button.clicked
        """open a log view in a new separate window"""
        window = LogViewWindow(self.sender().property('log_view'))
        self.log_view_windows.append(window)  # to make multiple windows possible
        window.show()
        self.log_accordion.delete_section(self.sender().property('log_view').name)

    # @Slot() if this is a Slot it does not work anymore (I have no idea why)
    def delete_section(self):
        """deletes the property (the section) of the clicked button"""
        section = self.sender().property('section')
        self.log_accordion.delete_section(section.section_name)
