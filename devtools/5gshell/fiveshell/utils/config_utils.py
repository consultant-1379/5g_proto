import getpass
import os
import sys
from pathlib import Path
from shutil import copyfile

import kubernetes as kubernetes
from PySide2.QtGui import QIcon
from PySide2.QtWidgets import QInputDialog, QSizePolicy, QTextEdit, QApplication

from commands.envoy import *
from commands.monitor import setup_monitor_port_forwarding
from utils.port_forwarding_utils import *
from utils.startup_dialog_utils import *


# ------------------------------------------------------------------------
# Config-File, Kubernetes, Variables


def get_k8s_data_for_namespace(namespace):
    """Read Pods, Services etc from Kubernetes and return a dict
    with the data for a given namespace.
    Structure:
      - pods:
        - labels: list of labels
        - node_name:
        - host_ip: an IP-address
        - phase: "Running", "Terminating",...
    """
    # This has the API token and the cluster name:
    kubernetes.config.load_kube_config()
    v1 = kubernetes.client.CoreV1Api()
    # POD data:
    k8s_data = {'pod': []}
    ret = v1.list_namespaced_pod(namespace=namespace, watch=False)
    for p in ret.items:
        pod = {'name': p.metadata.name,
               'labels': p.metadata.labels,
               'node_name': p.spec.node_name,
               'host_ip': p.status.host_ip,
               'pod_ip': p.status.pod_ip,
               'phase': p.status.phase}
        k8s_data['pod'].append(pod)
    # SERVICE data:
    k8s_data['service'] = []
    ret = v1.list_namespaced_service(namespace=namespace, watch=False)
    for s in ret.items:
        svc = {'name': s.metadata.name,
               'labels': s.metadata.labels,
               'cluster_ip': s.spec.cluster_ip,
               'ports': []}
        for prt in s.spec.ports or []:
            port = {'name': prt.name,
                    'node_port': prt.node_port,
                    'port': prt.port,
                    'target_port': prt.target_port}
            svc['ports'].append(port)
        k8s_data['service'].append(svc)
    # NODE data:
    k8s_data['node'] = []
    ret = v1.list_node(watch=False)
    for n in ret.items:
        node = {'name': n.metadata.name,
                'labels': n.metadata.labels,
                'addresses': []}
        for add in n.status.addresses:
            address = {'address': add.address,
                       'type': add.type}
            node['addresses'].append(address)
        node['capacity'] = n.status.capacity
        k8s_data['node'].append(node)
    return k8s_data


def merge_dicts(a, b, path=None):
    """merges dict b into a. https://stackoverflow.com/questions/7204805
    The dict in "a" is being modified."""
    if path is None:
        path = []
    for key in b:
        if key in a:
            if isinstance(a[key], dict) and isinstance(b[key], dict):
                merge_dicts(a[key], b[key], path + [str(key)])
            elif a[key] == b[key]:
                pass  # same leaf value
            elif isinstance(a[key], list) and isinstance(b[key], list):
                a[key] = a[key] + b[key]  # append arrays
            else:
                a[key] = b[key]
        else:
            a[key] = b[key]
    return a


def merge_main_menus(public_config):
    menus = {}
    for element in public_config["main_window"]["elements"]:  # element {menu: ..., actions: []}
        if element["menu"] not in menus:
            menus[element["menu"]] = element
        else:
            for action in element["actions"]:
                menus[element["menu"]]["actions"].append(action)

    res = []
    for menu in menus:
        res.append(menus[menu])

    public_config["main_window"]["elements"] = res


def read_yaml_file(filename):
    """Return the contents of the config file that is in YAML format."""
    try:
        with open(os.path.expanduser(filename), 'r', encoding='utf-8') as fd:
            contents = yaml.safe_load(fd)
    except FileNotFoundError:
        if "fiveshellrc" in filename:
            changed = create_user_config_if_wanted()
            if not changed:
                sys.exit(1)
            else:
                return read_yaml_file(filename)  # try to read the .fiveshellrc again if it was changed
        else:
            print(f"### Error: Cannot open config file {filename}")
    else:
        return contents


def read_config_files(configfilename1, configfilename2):
    """Read both the config files (user and global) and store it in
    g.globalDict. This contains variables and button definitions."""

    # Read fist the global config file (= comes with fiveshell),
    # and then the userspecific config file
    g.config = read_yaml_file(configfilename1)
    user_config = read_yaml_file(configfilename2)
    changed = check_user_config(user_config)  # check if the .fiveshellrc has the correct format
    if changed:
        user_config = read_yaml_file(configfilename2)
    merge_dicts(g.config, user_config)

    merge_main_menus(g.config)

    # A global dict with variables that can
    # be used by commands. This eliminates the need to query (and wait)
    # for K8s for port numbers or pod names with every command/function
    g.globalDict = g.config['variables']
    g.globalDict['user'] = getpass.getuser()
    g.globalDict['home'] = str(Path.home())
    g.globalDict['configFileName1'] = configfilename1
    g.globalDict['configFileName2'] = configfilename2


def read_k8s_cluster_namespace():
    """Read the Kubernetes config to get cluster and namespace
    and store in global variables"""
    print("Reading cluster and namespace (at start and on change)")
    context = subprocess.run("kubectl config current-context", shell=True,
                             stdout=subprocess.PIPE).stdout.decode("ascii").rstrip()
    if context == "":
        print("Error: No ~/.kube directory or no file ~/.kube/config found. Please check.")
        msg = QMessageBox()
        msg.setWindowTitle("ERROR")
        msg.setIcon(QMessageBox.Critical)
        msg.setText("No ~/.kube directory or no file ~/.kube/config found. "
                    "\nYou have to add a context to your ~/.kube/config.")
        msg.exec_()
        added_context_name = open_add_context_gui_and_add_context()
        if added_context_name:
            read_k8s_cluster_namespace()
        else:
            sys.exit(1)
    context_data = subprocess.run(f"kubectl config get-contexts {context}", shell=True,
                                  stdout=subprocess.PIPE).stdout.decode(
        "ascii").splitlines()
    context_data_fields = context_data[1].split()
    if len(context_data_fields) < 5:
        print("\n\nError: It is required to have a 'namespace' entry in your ~/.kube/config file,")
        print("       in a 'context' section.")
        print("Please add one. After that not only Fiveshell is happy, but you can use 'kubectl' and 'helm'")
        print("without the -n option. Please see this webpage for more information:")
        print("https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/Easy+switching+between"
              "+Kubernetes+Clusters+and+Namespaces")
        print("For example:")
        print("[...]")
        print("contexts:")
        print("- context:")
        print("    cluster: hahn011")
        print("    namespace: 5g-bsf-eedala")
        print("    user: q-ty65spgd")
        print("[...]")
        sys.exit(1)
    cluster = context_data_fields[2]
    namespace = context_data_fields[4]
    print(f"K8s context: {context}, cluster: {cluster}, namespace: {namespace}")
    g.globalDict['cluster'] = context_data_fields[2]
    g.globalDict['namespace'] = context_data_fields[4]


def open_add_context_gui_and_add_context():
    new_context_dialog = QDialog()
    new_context_dialog.setModal(True)
    new_context_dialog.setWindowTitle("Add new context")
    new_context_dialog.setWindowIcon(QIcon("icons/5G-logo_300px.png"))

    info_label = QLabel("Follow the steps to add a new context. <br>"
                        "The namespace will be automatically set to the current used namespace. <br>"
                        "<ol style='margin: 0; padding: 0;'>"
                        "<li> Go to the page linked "
                        "<a href=\"https://mocha.rnd.gic.ericsson.se\">here</a>"
                        " to see all available contexts.</li>"
                        "<li> Select the context you want to add. </li>"
                        "<li> Select 'Kubeconfig File' on the upper right hand side. </li>"
                        "<li> Copy the file to your clipboard. </li>"
                        "</ol>"
                        "<br>Then paste the text into the text edit and press OK.")
    info_label.setSizePolicy(QSizePolicy.Maximum, QSizePolicy.Maximum)
    info_label.setTextFormat(Qt.RichText)
    info_label.setTextInteractionFlags(Qt.TextBrowserInteraction)
    info_label.setOpenExternalLinks(True)

    text_edit = QTextEdit()
    paste_button = QPushButton("Paste")
    paste_button.clicked.connect(lambda: text_edit.setText(QApplication.clipboard().text()))
    ok_cancel_button_box = QDialogButtonBox(QDialogButtonBox.Ok | QDialogButtonBox.Cancel | QDialogButtonBox.Help)
    ok_cancel_button_box.accepted.connect(new_context_dialog.accept)
    ok_cancel_button_box.rejected.connect(new_context_dialog.reject)
    ok_cancel_button_box.helpRequested.connect(
        lambda: QDesktopServices.openUrl(
            "https://wcdma-confluence.rnd.ki.sw.ericsson.se/display/DSCNode/FiveShell#K8sManager"
        )
    )

    layout = QVBoxLayout()
    layout.addWidget(info_label)
    layout.addWidget(text_edit)
    layout.addWidget(paste_button)
    standard_button_layout = QHBoxLayout()
    standard_button_layout.addWidget(ok_cancel_button_box)
    layout.addLayout(standard_button_layout)
    new_context_dialog.setLayout(layout)

    res = new_context_dialog.exec_()
    if res:
        if text_edit.toPlainText():
            context_name = add_context_cluster_user_from_config_to_kube_config(text_edit.toPlainText())
            return context_name
    return None


def add_context_cluster_user_from_config_to_kube_config(config):
    if not os.path.exists(str(Path.home()) + "/.kube/"):
        os.mkdir(str(Path.home()) + "/.kube/")
    with open(str(Path.home()) + "/.kube/temp", 'w') as temp_file:
        temp_file.write(config)
    namespace = get_current_namespace()
    if not namespace:
        text, ok = QInputDialog.getText(None, "Add namespace", "There is no namespace available. \n"
                                                               "Please type in your namespace.", QLineEdit.Normal)
        if ok and text:
            namespace = text
        else:
            return None
    # TODO here might be some error handling missing
    command = f"export KUBECONFIG=~/.kube/temp && kubectl config set-context --current --namespace={namespace}"
    subprocess.run(command, shell=True, stdout=subprocess.PIPE).stdout.decode("ascii")
    subprocess.run("export KUBECONFIG=~/.kube/temp:~/.kube/config && "
                   "kubectl config view --flatten > ~/.kube/config_new", shell=True,
                   stdout=subprocess.PIPE).stdout.decode("ascii")
    with open(str(Path.home()) + "/.kube/config_new", "r") as f:
        with open(str(Path.home()) + "/.kube/config", "w") as f1:
            for line in f:
                f1.write(line)
    os.remove(str(Path.home()) + "/.kube/temp")
    os.remove(str(Path.home()) + "/.kube/config_new")
    current_context = subprocess.run("kubectl config current-context", shell=True,
                                     stdout=subprocess.PIPE).stdout.decode("ascii").rstrip()
    return current_context


def get_list_of_available_kube_contexts():
    contexts = subprocess.run("kubectl config get-contexts --no-headers=true", shell=True,
                              stdout=subprocess.PIPE).stdout.decode("ascii").splitlines()

    i = 0
    current_context_index = -1
    context_list = []
    for context in contexts:
        context_fields = context.split()
        if context_fields[0] == '*':
            current_context_index = i
            context_list.append(context_fields[1])
        else:
            context_list.append(context_fields[0])

        i += 1
    return context_list, current_context_index


def get_current_namespace():
    contexts = subprocess.run("kubectl config get-contexts --no-headers=true", shell=True,
                              stdout=subprocess.PIPE).stdout.decode("ascii").splitlines()

    namespace = None
    for context in contexts:
        context_fields = context.split()
        if context_fields[0] == '*':
            namespace = context_fields[4]

    return namespace


def read_k8s_data():
    print("Reading data from Kubernetes...\n", end='')
    g.globalDict['k8s'] = get_k8s_data_for_namespace(g.globalDict['namespace'])


def update_dynamic_variable_values():
    """For each dynamic variable, extract the data from the K8s data
    with the supplied filter into a "normal" variable."""

    if 'dynamic' in g.config['variables']:
        for variable in g.config['variables']['dynamic']:
            if 'filter' in variable:
                var_filter = variable['filter']
                var_value = jmespath.search(var_filter, g.globalDict)
                g.globalDict[variable['name']] = var_value
            else:
                print(f"#### Error: dynamic variable {variable['name']} " +
                      "has no filter.  Please check your config file.")


# Public
def reload_k8s_data():
    teardown_port_forwarding()
    read_k8s_data()
    update_dynamic_variable_values()
    setup_envoy_port_forwarding(g.globalDict['envoy_pod_names'])
    setup_monitor_port_forwarding(g.globalDict['monitor_pod_names'])
    return "Kubernetes data re-loaded."


def load_config_and_k8s_data(configfilename1, configfilename2):
    read_config_files(configfilename1, configfilename2)
    read_k8s_cluster_namespace()
    read_k8s_data()
    update_dynamic_variable_values()
    setup_envoy_port_forwarding(g.globalDict['envoy_pod_names'])
    setup_monitor_port_forwarding(g.globalDict['monitor_pod_names'])


# Public
def load_config_file():
    """Load the config file again.
    Note: the kubernetes config is not reloaded."""
    print("Reloading config-file")
    k8s = g.globalDict['k8s']  # save K8s data
    port_forwardings = g.globalDict['port-forwardings']
    port_forwarding_procs = g.globalDict['port-forwarding-procs']
    kube_context = g.globalDict['kube_context']
    verbose = g.globalDict['verbose']
    read_config_files(g.globalDict['configFileName1'],
                      g.globalDict['configFileName2'])
    g.globalDict['k8s'] = k8s  # restore K8s data
    g.globalDict['port-forwardings'] = port_forwardings
    g.globalDict['port-forwarding-procs'] = port_forwarding_procs
    g.globalDict['kube_context'] = kube_context
    g.globalDict['verbose'] = verbose
    read_k8s_cluster_namespace()
    update_dynamic_variable_values()
    return "Configuration files re-loaded."


# Public
def set_global_var(name, value):
    """Set a global variable to a new value. Good to overwrite
    something read from Kubernetes"""
    g.globalDict[name] = value
    return f"Global variable '{name}' now has the value {g.globalDict[name]}"


def update_button_boxes_in_user_config(user_config_filename, new_button_boxes_list):
    new_dict = read_yaml_file(user_config_filename)
    new_dict["button_boxes"] = new_button_boxes_list
    with open(user_config_filename, 'w') as outfile:
        yaml.dump(new_dict, outfile, default_flow_style=False)


def get_current_button_box_configs(user_config_filename):
    cur_dict = read_yaml_file(user_config_filename)
    return cur_dict["button_boxes"] if "button_boxes" in g.config else []


def add_action_to_user_config(user_config_filename, action_dict, edit_mode=False):
    new_dict = read_yaml_file(user_config_filename)
    if "actions" not in new_dict:
        new_dict["actions"] = {}
    problem = False
    ok = None
    if list(action_dict.keys())[0] in new_dict["actions"] and not edit_mode:
        msg = QMessageBox()
        msg.setWindowTitle("Attention")
        msg.setIcon(QMessageBox.Warning)
        msg.setText("The provided action id does already exist. \n"
                    "If you continue, the old action will be overwritten.")
        msg.setStandardButtons(QMessageBox.Ok | QMessageBox.Cancel)
        ok = msg.exec_()
        problem = True
    if not problem or ok == QMessageBox.Ok:
        new_dict["actions"].update(action_dict)
        with open(user_config_filename, 'w') as outfile:
            yaml.dump(new_dict, outfile, default_flow_style=False)
        return True
    else:
        return False


def add_button_to_user_config(user_config_filename, button_dict, edit_mode=False):
    new_dict = read_yaml_file(user_config_filename)
    if "buttons" not in new_dict:
        new_dict["buttons"] = {}
    problem = False
    ok = None
    if list(button_dict.keys())[0] in new_dict["buttons"] and not edit_mode:
        msg = QMessageBox()
        msg.setWindowTitle("Attention")
        msg.setIcon(QMessageBox.Warning)
        msg.setText("The provided button id does already exist. \n"
                    "If you continue, the old button will be overwritten.")
        msg.setStandardButtons(QMessageBox.Ok | QMessageBox.Cancel)
        ok = msg.exec_()
        problem = True
    if not problem or ok == QMessageBox.Ok:
        new_dict["buttons"].update(button_dict)
        with open(user_config_filename, 'w') as outfile:
            yaml.dump(new_dict, outfile, default_flow_style=False)
        return True
    else:
        return False


def get_used_variables_dict(config_dict):
    """
    Creates a dictionary consisting of all variables in the config (dynamic vars, input vars, loop vars, ...)
    as keys and the according button or action ids.
    """
    variables_dict = {}
    if 'variables' in config_dict:
        for key in config_dict['variables']:
            if key == 'dynamic':
                for item in config_dict['variables'][key]:
                    variables_dict[item['name']] = None
            else:
                variables_dict[key] = None
    if 'buttons' in config_dict:
        for button_key in config_dict['buttons']:
            if 'inputs' in config_dict['buttons'][button_key]:
                for input_dict in config_dict['buttons'][button_key]['inputs']:
                    variables_dict[input_dict['store_var']] = button_key
    if 'actions' in config_dict:
        for action_key in config_dict['actions']:
            if 'loop' in config_dict['actions'][action_key]:
                variables_dict[config_dict['actions'][action_key]['loop']['name']] = action_key
            if 'variables' in config_dict['actions'][action_key]:
                for variable_dict in config_dict['actions'][action_key]['variables']:
                    variables_dict[variable_dict['name']] = action_key
    return variables_dict


def create_user_config_if_wanted():
    """
    creates a working .fiveshellrc file if the user wants it and provides the correct path

    :returns: true if .fiveshellrc was created else false
    """
    variables_path = open_create_fiveshellrc_dialog()
    # variables_path is false if the user does not want to have the config file created

    try_again = None
    if variables_path:
        if os.path.exists(variables_path) and variables_path.endswith("5g_proto"):
            import_example_config_to_user_config({"5g_proto": variables_path})
            return True
        else:
            msg = QMessageBox()
            msg.setWindowTitle("ERROR")
            msg.setIcon(QMessageBox.Critical)
            msg.setText("The provided path was not correct.\n"
                        "Either it does not exist or it does not end with 5g_proto.\n"
                        "Fiveshell does not work without a ~/.fiveshellrc config.\n"
                        "Do you want to try again?\n")
            msg.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
            try_again = msg.exec_()
    elif variables_path == "":
        msg = QMessageBox()
        msg.setWindowTitle("ERROR")
        msg.setIcon(QMessageBox.Critical)
        msg.setText("You did not provide a path.\n"
                    "Fiveshell does not work without a ~/.fiveshellrc config.\n"
                    "Do you want to try again?\n")
        msg.setStandardButtons(QMessageBox.Yes | QMessageBox.No)
        try_again = msg.exec_()
    if try_again == QMessageBox.Yes:
        return create_user_config_if_wanted()
    else:
        msg = QMessageBox()
        msg.setWindowTitle("ERROR")
        msg.setIcon(QMessageBox.Critical)
        msg.setText("Fiveshell does not work without a ~/.fiveshellrc config.\nFiveshell will be terminated.\n")
        msg.exec_()
        print("### Error: Cannot open config file ~/.fiveshellrc")
        print("This is a good start for your ~/.fiveshellrc file (adapt to your environment")
        print("regarding the location you cloned the 5g_proto repository to):")
        print()
        print("variables:")
        print("  5g_proto: \"{home}/5g_proto\"")
        print()

    return False


def check_user_config(user_config):
    """
    Checks if the user config (.fiveshellrc) exists and does not contain the key button box which is necessary
    for the new fiveshell version.
    If so, it is assumed that the user does not have a proper configuration and might want to copy the example
    config to his file.

    :returns: true if the user config file was updated else false
    """
    if len(user_config.keys()) >= 1 and "button_boxes" not in user_config.keys():
        import_wanted = open_import_example_dialog()
        if import_wanted:
            copyfile(g.user_config_filename, str(Path.home()) + "/.fiveshellrc.bak")
            variables = user_config["variables"]
            import_example_config_to_user_config(variables)
            return True
    return False


def import_example_config_to_user_config(variables):
    """
    open the user config file (.fiveshellrc) writes the given variables to it and appends the example config file
    """
    with open(g.user_config_filename, "w") as user_config_file:
        new_config_dict = {"variables": variables}
        yaml.dump(new_config_dict, user_config_file, default_flow_style=False)

    example_config = open(g.example_user_config_filename, "r")
    with open(g.user_config_filename, "a") as user_config_file:
        user_config_file.write(example_config.read())
