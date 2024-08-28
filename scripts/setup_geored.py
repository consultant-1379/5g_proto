from argparse import ArgumentParser, RawTextHelpFormatter
import os
import textwrap
import paramiko
from time import sleep
from jinja2.environment import Template
from kubernetes import client, config
from kubernetes.stream import stream

HOME = os.path.abspath(os.path.dirname(__file__))
PROTO = HOME.split("scripts")[0]

TERMINATOR = b"]]>]]>"

def check_args():

    file_help = "Use this script to automatically setup an already deployed 1+1 geored deployment."

    parser = ArgumentParser(
        description=
        "Use this script to automatically setup an already deployed 1+1 geored deployment.\n"
        "This setup contains:\n"
        "\t- Push default bsf config to yang provider for dc1 and dc2.\n"
        "\t- Initialize DB for dc1 and dc2.\n"
        "\t- Repair DB.\n"
        "As input both dc1 and dc2 namespaces are needed.",
        epilog=textwrap.dedent("""\
                Usage examples: 
                setup_geored -dc1 <dc1_namespace> -dc2 <dc2_namespace> --diameter <false>"""),
        formatter_class=RawTextHelpFormatter,
    )

    parser.add_argument(
        "-dc1",
        dest="dc1",
        help=file_help + "\nSet the namespace of datacenter1.",
        required=True,
    )
    parser.add_argument(
        "-dc2",
        dest="dc2",
        help=file_help + "\nSet the namespace of datacenter2.",
        required=True,
    )

    parser.add_argument(
        "--diameter",
        dest="diameter",
        help=file_help + "\nSet if diameter is enabled or not.",
        required=True,
    )

    return parser.parse_args().dc1, parser.parse_args().dc2, parser.parse_args().diameter


def push_default_config(namespace, init_db, diameter):

    # Set up kubeAPI
    config.load_kube_config(os.environ.get("KUBECONFIG"))
    contexts, _ = config.list_kube_config_contexts()

    v1 = client.CoreV1Api()
    
    # Get external IP of yang provider
    svcs = v1.list_namespaced_service(namespace)
    yang_ext_ip = None

    if "n106" in contexts[0]["name"]:
        yang_ext_ip = "214.6.255.228"
        if svcs.items:
            yang_port = (next(
            filter(lambda svc: svc.metadata.name == "eric-cm-yang-provider",
                svcs.items)).spec.ports[0].node_port)

    else:
        if svcs.items:
            yang_ext_ip = (next(
                filter(lambda svc: "yang-provider-external" in svc.metadata.name ,
                    svcs.items)).status.load_balancer.ingress[0].ip)
        yang_port=830

    if yang_ext_ip is None:
        print("Error: external IP of yang provider svc in namespace '" +
              namespace + "' not found!")
        exit(1)    

    if yang_port is None:
        print("Error: yang provider port svc in namespace '" +
              namespace + "' not found!")
        exit(1)    

    # Get default config
        
    config_path = "/esc/bsf/sample_bsf_func_and_diameter.netconf" if diameter=="True" or diameter=="true" else "/esc/bsf/sample_bsf_func_without_diameter.netconf"
    print("Using the following config: " + config_path)
    bsf_config = Template(
        open(PROTO + config_path).read())
    bsf_config = bsf_config.render()

    #  Create Ssh connection to yang service
    ssh_client = paramiko.SSHClient()
    ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    ssh_client.connect(yang_ext_ip,
                       yang_port,
                       "bsf-admin",
                       "bsfbsf",
                       look_for_keys=False,
                       allow_agent=False)
    ssh_client.get_transport().set_keepalive(600)
 
    # Setup netconf channel
    netconf_channel = ssh_client.get_transport().open_session()
    netconf_channel.invoke_subsystem("netconf")

    # Read Hello
    list(read(netconf_channel))

    # Push default config
    netconf_channel.send(bsf_config)

    # Read response
    res = list(read(netconf_channel))

    if str(res[0]).find("<ok/>") > 0:
        print("Config in namespace: '" + namespace + "' pushed successfully")
    else:
        print("Error while pushing config:")
        print(res[0])
        exit()

    netconf_channel.close

    if init_db:

        #  Read yang action netconf
        init_action = Template(
            open(PROTO + "/esc/bsf/action_bsf_init_db_geored.netconf").read())
        init_action = init_action.render()

        #  Create Ssh connection to yang service
        ssh_client = paramiko.SSHClient()
        ssh_client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
        ssh_client.connect(
            yang_ext_ip,
            yang_port,
            "bsf-admin",
            "bsfbsf",
            look_for_keys=False,
            allow_agent=False,
        )
        ssh_client.get_transport().set_keepalive(600)

        # Push yang action
        count = 0
        total_times_to_try = 10
        action_was_successfull = False
        while count < total_times_to_try and not action_was_successfull:
            # Setup netconf channel
            netconf_channel = ssh_client.get_transport().open_session()
            netconf_channel.invoke_subsystem("netconf")

            # Read Hello
            list(read(netconf_channel))

            netconf_channel.send(init_action)
            # Get response
            res = list(read(netconf_channel))
            print("try", count)
            if str(res[0]).find(">Database initialized successfully<") > 0:
                action_was_successfull = True
                print("DB initialized successfully")
            else:
                print("Error! DB initialization failed:")
                print(res[0])
                sleep(5)
            count += 1

        print("Initialize db run times: ",count)
        netconf_channel.close


def repair_db(namespace):

    # Set up kubeAPI
    config.load_kube_config(os.environ.get("KUBECONFIG"))
    v1 = client.CoreV1Api()

    # Set repair command
    cmd_to_exec = [
        "/bin/sh", "-c", "nodetool rebuild -- datacenter1"
    ]

    # Execute repair command on pod-0
    for i in range(2):
        wcdb_pod_name="eric-bsf-wcdb-cd-datacenter2-rack1-"+str(i)
        print("running rebuild on pod: "+wcdb_pod_name)
        resp = stream(
            v1.connect_get_namespaced_pod_exec,
            wcdb_pod_name,
            namespace,
            command=cmd_to_exec,
            container="cassandra",
            stderr=True,
            stdin=False,
            stdout=True,
            tty=False,
        )

        if resp == "":
            print("Rebuild on pod-0 was successfull")
        else:
            print("Rebuild on pod-0 failed with error: "+resp)


def read(chan, responses=1):
    """Read responses."""
    while responses:
        sleep(1)
        response = chan.recv(2048)
        yield response
        responses -= response.count(TERMINATOR)

########## Main ###############

# Get namespaces for dc1 and dc2
dc1, dc2, diameter = check_args()

# Push default BSF config to dc1
print("Pushing configuration to dc1")
push_default_config(dc1, False, diameter)

# Push default BSF config to dc2
print("Pushing configuration to dc2")
push_default_config(dc2, True, diameter)

# Repair DB in dc2
repair_db(dc2)
