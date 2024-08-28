# 2020-10-21 Alina Behrens, Maximilian Pohl, Jessica Klein, Alexander Langer
from PySide2.QtCore import Qt, Slot
from PySide2.QtWidgets import QWidget, QScrollArea, QPushButton, QComboBox, QRadioButton, QLabel, QLineEdit, \
    QCheckBox, QVBoxLayout, QGroupBox, QHBoxLayout

import g
from gui.accordion import Accordion


class ButtonBox(QWidget):
    def __init__(self, button_box_config, buttons_config):
        """
        This function creates a button box that means the left side of the fiveshell gui containing
        accordion sections, buttons and a scroll bar

        :param button_box_config: the section g.config['button_boxes']['name']
        :param buttons_config: the section g.config['buttons']
        """
        super().__init__()
        self.accordion = None

        self.scroll_area = QScrollArea()
        self.scroll_area.setWidgetResizable(True)
        self.set_accordion(button_box_config, buttons_config)

        self.layout = QVBoxLayout()
        self.layout.addWidget(self.scroll_area)
        self.setLayout(self.layout)

    def update_button_box(self, button_box_config, buttons_config):
        """updates the button box on config changed"""
        self.set_accordion(button_box_config, buttons_config)

    def set_accordion(self, button_box_config, buttons_config):
        self.accordion = self.create_button_view(button_box_config, buttons_config)
        self.scroll_area.setWidget(self.accordion)

    def create_button_view(self, button_box_config, buttons_config):
        """Traverse the button-configuration and create buttons"""
        accordion = Accordion(self)
        for group in button_box_config:
            accordion_section = accordion.add_section(group["group"])
            for element in group['elements']:
                button_element = buttons_config[element]
                self.create_button(button_element, accordion_section)
        return accordion

    def create_button(self, element, accordion_section):
        """creates one button as defined in the element argument
        and add this to the given accordionSection"""
        # TODO the button just takes the label of the first action it has
        label = element['label'] if "label" in element else g.config["actions"][element["action_ids"][0]]["label"]
        button = QPushButton(label)
        if 'color' in element:
            button.setStyleSheet('QPushButton {color: #' + f"{element['color']}" + '}')

        # TODO the button just takes the label of the first action it has
        button.setToolTip(g.config["actions"][element["action_ids"][0]]["tooltip"])

        button.setProperty('action_ids', element['action_ids'])

        if "inputs" in element:
            for item in element["inputs"]:
                if item['type'] == 'combobox':
                    self.create_combobox(item, accordion_section)

                elif item['type'] == 'radiobutton':
                    self.create_radio_button(item, accordion_section)

                elif item['type'] == 'checkbox':
                    self.create_checkbox(item, accordion_section)

                elif item['type'] == 'line_edit':
                    self.create_line_edit(item, accordion_section)
        accordion_section.add_button(button)

    def create_combobox(self, cmd, toolbox_section):
        """Creates one Combobox as defined in the cmd argument
        and adds this to the given toolboxSection"""
        combobox = QComboBox()
        # adds the labels which can be selected to the combobox
        combobox.addItems(cmd['labels'])
        # creates tooltip for each entry in the combobox
        if 'tooltip' in cmd:
            for index, tooltip in enumerate(cmd['tooltip']):
                combobox.setItemData(index, cmd['tooltip'][index], Qt.ToolTipRole)

        combobox.setProperty("store_var", cmd['store_var'])

        if 'values' in cmd:
            combobox.setProperty('values', cmd['values'])

        if 'private' in cmd:
            combobox.setProperty("private", True)

        if combobox.property("values"):
            g.globalDict[cmd['store_var']] = combobox.property('values')[combobox.currentIndex()]
        else:
            g.globalDict[cmd['store_var']] = combobox.currentText()

        combobox.currentTextChanged.connect(self.update_combobox_variable)

        toolbox_section.add_combo_box(combobox)

    def create_radio_button(self, cmd, toolbox_section):
        """Creates one RadioButton as defined in the cmd argument
        and adds this to the given toolboxSection"""
        group_box = QGroupBox()
        h_box = QHBoxLayout()
        for index, text in enumerate(cmd['labels']):
            radiobutton = QRadioButton()
            radiobutton.setText(text)
            if 'values' in cmd:
                radiobutton.setProperty('value', cmd['values'][index])

            radiobutton.setProperty("store_var", cmd['store_var'])
            radiobutton.toggled.connect(self.update_radio_button_variable)

            if 'default' in cmd:
                if cmd['default'] == text:
                    radiobutton.setChecked(True)
            else:
                if cmd['labels'][0] == text:
                    radiobutton.setChecked(True)
                    if 'values' in cmd:
                        g.globalDict[cmd['store_var']] = cmd['values'][0]
                    else:
                        g.globalDict[cmd['store_var']] = cmd['labels'][0]
            h_box.addWidget(radiobutton)
        h_box.addStretch(1)
        h_box.setMargin(0)
        group_box.setLayout(h_box)
        group_box.setStyleSheet("QGroupBox { border: 0px;}")
        toolbox_section.add_radio_button_group_box(group_box)

    def create_checkbox(self, cmd, toolbox_section):
        """Creates one CheckBox as defined in the cmd argument
        and adds this to the given toolboxSection"""
        checkbox = QCheckBox()
        checkbox.setText(cmd['label'])
        checkbox.setProperty("store_var", cmd['store_var'])

        if 'values' in cmd:
            checkbox.setProperty('values', cmd['values'])
            if 'default' in cmd:
                checkbox.setChecked(cmd['default'])
                if cmd['default']:
                    g.globalDict[cmd['store_var']] = cmd['values'][0]
                else:
                    g.globalDict[cmd['store_var']] = cmd['values'][1]
            else:
                g.globalDict[cmd['store_var']] = cmd['values'][1]

        else:
            if 'default' in cmd:
                checkbox.setChecked(cmd['default'])
                g.globalDict[cmd['store_var']] = cmd['default']
            else:
                g.globalDict[cmd['store_var']] = False

        checkbox.stateChanged.connect(self.update_check_box_variable)
        toolbox_section.add_check_box(checkbox)

    def create_line_edit(self, cmd, toolbox_section):
        """Creates one LineEdit as definied in the cmd argument
        and adds this to the given toolboxSection"""
        line_edit = QLineEdit()
        g.globalDict[cmd['store_var']] = ''
        line_edit.setProperty("store_var", cmd['store_var'])
        line_edit.textChanged.connect(self.update_line_edit)
        if 'default' in cmd:
            line_edit.setText(cmd['default'])
        if 'label' in cmd:
            label = QLabel()
            label.setText(cmd['label'])
            toolbox_section.add_line_edit(label)
        toolbox_section.add_line_edit(line_edit)

    @Slot()
    def update_combobox_variable(self):
        if self.sender().property('values'):
            selected = self.sender().property('values')[
                self.sender().currentIndex()]
        else:
            selected = self.sender().currentText()
        store_var = self.sender().property("store_var")
        g.globalDict[store_var] = selected

    @Slot()
    def update_radio_button_variable(self):
        if self.sender().isChecked():
            if self.sender().property('value'):
                selected = self.sender().property('value')
            else:
                selected = self.sender().text()
            store_var = self.sender().property("store_var")
            g.globalDict[store_var] = selected

            # update all radio buttons with the same store var
            if self.accordion:  # if the accordion already exists
                for section in self.accordion.sections:
                    for group_box in section.buttons.buttons:  # that is where all buttons, radio buttons,... are stored
                        if isinstance(group_box, QGroupBox):  # radio buttons are always in QGroupBoxes
                            radio_buttons = group_box.children()
                            radio_buttons = radio_buttons[1::]  # the child[0] is the layout
                            if isinstance(radio_buttons[0], QRadioButton):
                                if radio_buttons[0].property('store_var') == self.sender().property('store_var'):
                                    for radio_button in radio_buttons:
                                        if radio_button.text() == self.sender().text() and not radio_button.isChecked():
                                            radio_button.setChecked(True)

    @Slot()
    def update_check_box_variable(self):
        state = self.sender().isChecked()
        store_var = self.sender().property("store_var")
        if self.sender().property('values'):
            values = self.sender().property('values')
            if state:
                g.globalDict[store_var] = values[0]
            else:
                g.globalDict[store_var] = values[1]
        else:
            g.globalDict[store_var] = state

    @Slot()
    def update_line_edit(self):
        text = self.sender().text()
        store_var = self.sender().property("store_var")
        g.globalDict[store_var] = text
