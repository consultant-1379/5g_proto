from PySide2.QtWidgets import QTabWidget

from gui.button_box import ButtonBox


class ButtonBoxTabs(QTabWidget):
    def __init__(self, button_box_configs, current_button_box, buttons_config):
        super(ButtonBoxTabs, self).__init__()
        self.button_box_configs = button_box_configs
        self.current_button_box = None
        self.tabs = []

        self.create_tabs(button_box_configs, buttons_config, current_button_box)
        self.currentChanged.connect(self.update_current_button_box)

    def update_current_button_box(self):
        self.current_button_box = self.widget(self.currentIndex())

    def create_tabs(self, button_box_configs, buttons_config, current_button_box):
        while self.count():
            self.removeTab(0)
        self.button_box_configs = button_box_configs
        self.current_button_box = None
        self.tabs = []
        for button_box in self.button_box_configs:
            new_button_box = ButtonBox(button_box["button_box"], buttons_config)
            self.tabs.append(new_button_box)
            self.addTab(self.tabs[-1], button_box["name"])
            if current_button_box == button_box["name"]:
                self.current_button_box = new_button_box
                self.setCurrentIndex(len(self.tabs) - 1)
