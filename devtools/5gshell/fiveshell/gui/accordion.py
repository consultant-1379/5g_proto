# Accordion Widget
# 2019-08-27 Jessica Klein, Alexander Langer
from PySide2.QtWidgets import QWidget, QVBoxLayout, QSpacerItem, QSizePolicy

from gui.accordion_section import AccordionSection


class Accordion(QWidget):
    """An accordion widget, consisting of sections that have a clickable
    label to open/close the section, and a number of buttons inside each
    section"""

    def __init__(self, *args):
        super().__init__()
        self.sections = []
        self.layout = QVBoxLayout(self)
        self.spacerWidget = None

    def add_section(self, section_name):
        """Create a new section with the given name, add the section
        to the accordion, and return the new section so you can add
        buttons to it"""
        if self.spacerWidget:
            self.layout.removeItem(self.spacerWidget)
        section = AccordionSection(section_name)
        self.layout.addWidget(section)
        self.sections.append(section)
        self.spacerWidget = QSpacerItem(0, 0, QSizePolicy.Minimum, QSizePolicy.Expanding)
        self.layout.addItem(self.spacerWidget)
        return section
