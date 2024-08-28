class PreferenceObject:
    def __init__(self):
        pass

    def get_name(self):
        pass


class Button(PreferenceObject):
    def __init__(self, button_id, button_dict, action_config):
        super().__init__()

        self.button_id = button_id
        self.button_dict = button_dict
        self.action_config = action_config

        self.action_ids = button_dict["action_ids"]
        self.label = button_dict["label"] if "label" in button_dict else None
        self.color = button_dict["color"] if "color" in button_dict else None
        self.tooltip = button_dict["tooltip"] if "tooltip" in button_dict else None
        self.inputs = button_dict["inputs"] if "inputs" in button_dict else None
        self.tags = button_dict["tags"] if "tags" in button_dict else None

    def set_action_id(self, action_id):
        self.action_ids = action_id
        self.button_dict["action_id"] = action_id

    def set_color(self, color):
        self.color = color
        self.button_dict["color"] = self.color

    def set_label(self, label):
        self.label = label
        self.button_dict["label"] = self.label

    def get_button_id(self):
        if self.button_id:
            return self.button_id
        else:
            return self.action_ids[0]

    def get_name(self):
        if self.label:
            return self.label
        else:
            return self.action_config[self.action_ids[0]]["label"]

    def get_tooltip(self):
        if self.tooltip:
            return self.tooltip
        else:
            return self.action_config[self.action_ids[0]]["tooltip"]

    def get_tags_as_string(self):
        if self.tags is None:
            return ""
        return "[" + "] [".join(self.tags) + "]"

    def to_button_in_box(self):
        return ButtonInBox(self.button_id, self.button_dict, self.action_config)


class ButtonInBox(PreferenceObject):
    def __init__(self, button_id, button_dict, action_config):
        super().__init__()

        self.button_id = button_id
        self.label = button_dict["label"] if "label" in button_dict \
            else action_config[button_dict["action_ids"][0]]["label"]
        self.tooltip = button_dict["tooltip"] if "tooltip" in button_dict \
            else action_config[button_dict["action_ids"][0]]["tooltip"]

    def set_label(self, label):
        self.label = label

    def get_button_id(self):
        return self.button_id

    def get_name(self):
        return self.label

    def get_tooltip(self):
        return self.tooltip


class Action(PreferenceObject):
    def __init__(self, action_id, action_dict):  # {"cmd": "","type": "python", "label": "","tooltip": ""}
        super().__init__()

        self.action_dict = action_dict
        self.action_id = action_id

        self.action_name = action_dict["label"]
        self.action_type = action_dict["type"]
        self.command = action_dict["cmd"]
        self.tooltip = action_dict["tooltip"]
        self.exception = action_dict["exception"] if "exception" in action_dict else None

        # list !!
        self.variables = action_dict["variables"] if "variables" in action_dict else None

        # dict !!
        self.loop = action_dict["loop"] if "loop" in action_dict else None

        self.directory = action_dict["dir"] if "dir" in action_dict else None

        self.shortcut = action_dict["shortcut"] if "shortcut" in action_dict else None

    def get_name(self):
        return self.action_name

    def get_type(self):
        return "Linux" if "linux" in self.action_type else "Python"

    def is_no_log(self):
        return True if "no log" in self.action_type else False
