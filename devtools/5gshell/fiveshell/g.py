# Global Variables

globalDict = {}  # all variables, user defined and from k8s
config = {}  # contents of the config file (usually config.yaml)
app = None  # the QApplicationObject
main_windows = []  # a list of all open windows  # the current active window can be accessed with app.activeWindow()
preference_window = None
kubernetes_management_window = None
actions = {}  # dict of all actions
action_creator = None

# {"config": {"watcher": obj, "paths": ["path1, path2"]}, "kube_config": {"watcher": obj, "paths": ["path1"]}}
file_watchers = {}  # a dictionary of all file watcher objects with the associated paths
user_config_filename = ""
example_user_config_filename = ""
