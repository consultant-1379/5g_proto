#!/usr/bin/env python
import yaml
import g  # Global variables
import os
import tempfile
import xml.dom.minidom as minidom


def check_rpc_reply(reply_text):
    """returns the 'rpc-reply' part of the netconf response. Used to
    filter the 'ok' or error when uploading file"""

    # Don't try to deode error messages, just print them:
    if len(reply_text) < 100:
        return reply_text

    reply_text = reply_text.split('hello')
    reply_text = reply_text[reply_text.__len__() - 1]

    # cut off ']]>]]>' from the netconf protocol
    reply_text = reply_text[7:reply_text.__len__() - 6]
    print(reply_text)

    dom = minidom.parseString(reply_text)
    rpc = dom.getElementsByTagName('rpc-reply')
    output = rpc[0].firstChild.toprettyxml(indent='    ')
    print(output)
    return output


# Public
def upload_via_netconf_with_port_change(config_file_location, node_ip, yp_port, worker_port, worker_port_tls, nrf_ip,
                                        verbose, user="user1", passwd="rootroot"):
    """Modify a Netconf-config file to updated the ports
    and then load the configuration into the node.
    This is very simplistic by replacing the first
    occurence of a string instead of properly parsing
    the XML inside the Netconf."""
    output = f"Uploading configuration file '{config_file_location}'..\n"

    # Copy the content of the sample_config.netconf into a string
    try:
        with open(config_file_location, 'r') as sampleconfig:
            config = sampleconfig.read()
    except Exception:
        return f"File not found: {config_file_location}"

    # Non-TLS port:
    startindex = config.find('<port')
    endindex = config.find('\n', startindex)

    # replace the example value with the new port
    if startindex >= 0:
        config = config[0:startindex:] + '<port>' + \
                 worker_port + '</port>' + config[endindex::]

    # TLS port:
    startindex = config.find('<tls-port')
    endindex = config.find('\n', startindex)

    # replace the example value with the new port
    if startindex >= 0:
        config = config[0:startindex:] + '<tls-port>' + \
                 worker_port_tls + '</tls-port>' + config[endindex::]

    # NRFSIM address:
    if "." in nrf_ip:  # Must be an IPv4 address
        startindex = config.find('<ipv4-address')
        endindex = config.find('\n', startindex)

        # replace the example value with the new port
        if startindex >= 0:
            config = config[0:startindex:] + '<ipv4-address>' + \
                     nrf_ip + '</ipv4-address>' + config[endindex::]
    else:  # IPv6
        startindex = config.find('<ipv6-address')
        endindex = config.find('\n', startindex)

        # replace the example value with the new port
        if startindex >= 0:
            config = config[0:startindex:] + '<ipv6-address>' + \
                     nrf_ip + '</ipv6-address>' + config[endindex::]

    # creates new temporary file with modified configuration
    temp = tempfile.mkstemp()
    temp_path = temp[1]
    print(config)
    with open(temp_path, 'r+') as temp:
        temp.write(config)

    output_upload = upload_via_netconf(temp_path, node_ip, yp_port, True, user, passwd)

    # deletes the tempfile
    os.remove(temp_path)

    if verbose == 'verbose':
        output += output_upload
        print(config)
        print(f"temp_path: {temp_path}")
        print(f"output: {output}")
        print(f"node_ip: {node_ip}")
        print(f"yp_port: {yp_port}")
        print(f"worker_port: {worker_port}")
        print(f"worker_port_tls: {worker_port_tls}")
    else:
        output += check_rpc_reply(output_upload)

    return output


def upload_via_netconf(file_location, node_ip, yp_port, verbose, user="user1", passwd="rootroot"):
    """uplads the file to the server using sshpass and returns the response"""

    command = "cat " + file_location + f" | {g.globalDict['sshpass']} -p " + passwd + " ssh -x -p " + \
              yp_port + " -o 'UserKnownHostsFile=/dev/null' -o 'StrictHostKeyChecking=no' "+ user +"@" + \
              node_ip + " -s netconf"
    print(command)
    output = os.popen(command).read()
    if verbose == 'compact':
        output = check_rpc_reply(output)
    return output


def view_netconf(file_location, yp_port, node_ip, element='data'):
    """displays the netconf, by uploading the request file, returning the 
    request and extract the 'element' part"""
    # Get the whole configuration by uploading the request
    large_netconf = upload_via_netconf(file_location, yp_port, node_ip, True)
    # Extract the rpc-reply:
    startindex = large_netconf.find('<rpc-reply')
    endindex = large_netconf.find('/rpc-reply>', startindex)
    netconf = large_netconf[startindex:endindex] + "/rpc-reply>"

    # Generate an xml object
    dom = minidom.parseString(netconf)
    data = dom.getElementsByTagName(element)
    if data.length > 0:
        printout = data[0].toprettyxml(indent='    ')
    else:
        printout = "(no configuration loaded)"

    # and extract the 'scp-funktion' part
    # scp = dom.getElementsByTagName('scp-function')
    # if scp.length > 0:
    #    printout = scp[0].toprettyxml(indent='    ')
    # else:
    #    printout = "(no configuration loaded)"
    # backup = dom.getElementsByTagName('backup-manager')
    # if backup.length > 0:
    #    printout += backup[0].toprettyxml(indent='    ')
    # keystore = dom.getElementsByTagName('keystore')
    # if keystore.length > 0:
    #    printout += keystore[0].toprettyxml(indent='    ')
    # nacm = dom.getElementsByTagName('nacm')
    # if nacm.length > 0:
    #    printout += nacm[0].toprettyxml(indent='    ')

    return printout


def read_config_file(filename):
    """Return the contents of the config file as dict"""
    with open(filename, 'r', encoding='utf-8') as fd:
        contents = yaml.safe_load(fd)
    return contents
