# 2020-10-21 Alina Behrens, Maximilian Pohl, Jessica Klein, Alexander Langer
from PySide2.QtWidgets import QWidget, QVBoxLayout, QSpacerItem, QSizePolicy

from gui.log_accordion_section import LogAccordionSection


class LogAccordion(QWidget):
    """An accordion widget, consisting of sections that have a clickable
    label to open/close the section, and a number of widgets inside each
    section"""

    def __init__(self, *args):
        super().__init__()
        self.sections = []  # list of accordion sections
        self.layout = QVBoxLayout(self)
        self.spacerWidget = None

    def add_section(self, section_name):
        """Create a new section with the given name, add the section
        to the accordion, and return the new section"""
        if self.spacerWidget:
            self.layout.removeItem(self.spacerWidget)
        section = LogAccordionSection(section_name)
        self.layout.addWidget(section)
        self.sections.append(section)
        self.spacerWidget = QSpacerItem(0, 0, QSizePolicy.Minimum, QSizePolicy.Expanding)
        self.layout.addItem(self.spacerWidget)
        return section

    def delete_section(self, section_name):
        """deletes the section with the given section name"""
        for section in self.sections:
            if section.section_name == section_name:
                del_section = section
                del_section.setParent(None)
        # remove the section from the list
        self.sections = [section for section in self.sections if not section.section_name == section_name]
