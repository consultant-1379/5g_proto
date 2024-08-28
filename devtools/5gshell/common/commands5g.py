import logging
from curl import request as req
from curl import response
from utils import printer
import os
import sys
from utils import globaldict as gd
from shell import shellutils
import click as c
from utils.jsonhelper import JsonHelper
from utils.config5g import Config5g
from utils import kubehelper
from common.cmdresult import CmdResult, RequestResult


@c.command()
@c.option('--notify', '-n', is_flag=True, required=False,
          help="If set, a notifyUri will be included to the request "
               "with default client name 'hellokube'")
@c.option('--client', '-c', type=str, default="hellokube", required=False,
          help="The client name to be included in the notifyUri.")
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def cc_create(notify, client, tls, verbose):
    """
    Sends a CREATE converged charging reference request to SCP,
    either with Notify URI included or not. TLS can also be 
    enabled or disabled.
    """

    if tls and not notify:
        response = req.cc_create_request_tls(gd.dict5[gd.envoy_port],
                                             gd.dict5[gd.kubeproxy_ip])

    elif tls and notify:
        response = req.cc_create_request_notify_tls(gd.dict5[gd.envoy_port],
                                                    gd.dict5[gd.kubeproxy_ip],
                                                    client)
    elif notify and not tls:
        response = req.cc_create_request_notify(gd.dict5[gd.envoy_port],
                                                gd.dict5[gd.kubeproxy_ip],
                                                client)
    else:
        response = req.cc_create_request(gd.dict5[gd.envoy_port],
                                         gd.dict5[gd.kubeproxy_ip])

    cmd_result = RequestResult("CC CREATE", response,
                               response.resultCode, "201",
                               verbose)
    if notify:
        notifyUri = "http://" + client + ".5g-bsf-" + os.environ['USER'] + \
                    ".svc.cluster.local"
        printer.important("NotifyUri: " + notifyUri)

    cmd_result.print(withName=True)

    if cmd_result.isCorrect():
        gd.dict5[gd.last_ref] = response.getChargingReference()
        printer.important("Charging reference: " + response.location)


@c.command()
@c.argument('reference', required=0)
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def cc_release(reference, tls, verbose):
    """
    Sends a RELEASE converged charging reference request to SCP.
    If no charging reference is provided into
    the command, then the last one observed is used.
    TLS can also be enabled or disabled.
    """

    if reference:
        ref = reference
    elif gd.dict5[gd.last_ref]:
        ref = gd.dict5[gd.last_ref]
    else:
        printer.justprint("Please provide a charging reference")
        return

    if tls:
        response = req.cc_release_request_tls(gd.dict5[gd.envoy_port],
                                              gd.dict5[gd.kubeproxy_ip],
                                              ref)
    else:
        response = req.cc_release_request(gd.dict5[gd.envoy_port],
                                          gd.dict5[gd.kubeproxy_ip],
                                          ref)

    cmd_result = RequestResult("CC RELEASE", response,
                               response.resultCode, "204",
                               verbose)  

    cmd_result.print(withName=True)

    if cmd_result.isCorrect():
        gd.dict5[gd.last_ref] = None


@c.command()
@c.argument('reference', required=0)
@c.option('--notify', '-n', is_flag=True, required=False,
          help="If set, a notifyUri will be included to the request "
               "with default client name 'hellokube'")
@c.option('--client', '-c', type=str, default="hellokube", required=False,
          help="The client name to be included in the notifyUri.")
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def cc_update(reference, notify, client, tls, verbose):
    """
    Sends an UPDATE converged charging reference request to SCP, either
    with Notify URI included or not.
    If no charging reference is provided into
    the command, then the last one observed is used.
    TLS can also be enabled or disabled.
    """

    if reference:
        ref = reference
    elif gd.dict5[gd.last_ref]:
        ref = gd.dict5[gd.last_ref]
    else:
        printer.justprint("Please provide a charging reference")
        return

    if notify and not tls:
        response = req.cc_update_request_notify(gd.dict5[gd.envoy_port],
                                                gd.dict5[gd.kubeproxy_ip],
                                                ref,
                                                client)
    elif tls and not notify:
        response = req.cc_update_request_tls(gd.dict5[gd.envoy_port],
                                             gd.dict5[gd.kubeproxy_ip],
                                             ref)
    elif tls and notify:
        response = req.cc_update_request_notify_tls(gd.dict5[gd.envoy_port],
                                                    gd.dict5[gd.kubeproxy_ip],
                                                    ref,
                                                    client)
    else:
        response = req.cc_update_request(gd.dict5[gd.envoy_port],
                                         gd.dict5[gd.kubeproxy_ip],
                                         ref)

    cmd_result = RequestResult("CC UPDATE", response,
                               response.resultCode, "200",
                               verbose)

    cmd_result.print(withName=True)


@c.command()
@c.option('--notify', '-n', is_flag=True, required=False,
          help="If set, a notifyUri will be included to the request "
               "with default client name 'hellokube'")
@c.option('--client', '-c', type=str, default="hellokube", required=False,
          help="The client name to be included in the notifyUri.")
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def slc_subscribe(notify, client, tls, verbose):
    """
    Sends a spending limit control SUBSCRIBE request to SCP,
    either with Notify URI included or not.
    TLS can also be enabled or disabled.
    """

    if notify and not tls:
        response = req.slc_subscribe_request_notify(gd.dict5[gd.envoy_port],
                                                    gd.dict5[gd.kubeproxy_ip],
                                                    client)
    elif tls and notify:
        response = req.slc_subscribe_request_notify_tls(gd.dict5[gd.envoy_port],
                                                        gd.dict5[gd.kubeproxy_ip],
                                                        client)  
    elif tls and not notify:
        response = req.slc_subscribe_request_tls(gd.dict5[gd.envoy_port],
                                                 gd.dict5[gd.kubeproxy_ip])
    else:
        response = req.slc_subscribe_request(gd.dict5[gd.envoy_port],
                                             gd.dict5[gd.kubeproxy_ip])

    cmd_result = RequestResult("SLC SUBSCRIBE", response,
                               response.resultCode, "201",
                               verbose)

    cmd_result.print(withName=True)

    if notify:
        printer.important("http://" + client + ".5g-bsf-" +
                          os.environ['USER'] +
                          ".svc.cluster.local")

    if cmd_result.isCorrect():
        gd.dict5[gd.last_ref] = response.getChargingReference()
        printer.important("Charging Reference: " + response.location)


@c.command()
@c.argument('reference', required=0)
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def slc_unsubscribe(reference, tls, verbose):
    """
    Sends a spending limit control UNSUBSCRIBE request to SCP.
    If no charging reference is provided into
    the command, then the last one observed is used.
    TLS can also be enabled or disabled.
    """

    if reference:
        ref = reference
    elif gd.dict5[gd.last_ref]:
        ref = gd.dict5[gd.last_ref]
    else:
        printer.justprint("Please provide a charging reference")
        return

    if tls:
        response = req.slc_unsubscribe_request_tls(gd.dict5[gd.envoy_port],
                                                   gd.dict5[gd.kubeproxy_ip],
                                                   ref)
    else:
        response = req.slc_unsubscribe_request(gd.dict5[gd.envoy_port],
                                               gd.dict5[gd.kubeproxy_ip],
                                               ref)
                                               
    cmd_result = RequestResult("SLC UNSUBSCRIBE", response,
                               response.resultCode, "204",
                               verbose)

    cmd_result.print(withName=True)

    if cmd_result.isCorrect():
        gd.dict5[gd.last_ref] = None


@c.command()
@c.argument('reference', required=0)
@c.option('--notify', '-n', is_flag=True, required=False,
          help="If set, a notifyUri will be included to the request"
               "with default client name 'hellokube'")
@c.option('--client', '-c', type=str, default="hellokube", required=False,
          help="The client name to be included in the notifyUri.")
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def slc_modify(reference, notify, client, tls, verbose):
    """
    Sends a spending limit control MODIFY request to SCP, either
    with Notify URI included or not.
    If no charging reference is provided into
    the command, then the last one observed is used.
    TLS can also be enabled or disabled.
    """

    if reference:
        ref = reference
    elif gd.dict5[gd.last_ref]:
        ref = gd.dict5[gd.last_ref]
    else:
        printer.justprint("Please provide a charging reference")
        return

    if notify and not tls:
        response = req.slc_modify_request_notify(gd.dict5[gd.envoy_port],
                                                 gd.dict5[gd.kubeproxy_ip],
                                                 ref,
                                                 client)
    if notify and tls:
        response = req.slc_modify_request_notify_tls(gd.dict5[gd.envoy_port],
                                                     gd.dict5[gd.kubeproxy_ip],
                                                     ref,
                                                     client)
    if tls and not notify:
        response = req.slc_modify_request_tls(gd.dict5[gd.envoy_port],
                                              gd.dict5[gd.kubeproxy_ip],
                                              ref)
    else:
        response = req.slc_modify_request(gd.dict5[gd.envoy_port],
                                          gd.dict5[gd.kubeproxy_ip],
                                          ref)
    
    cmd_result = RequestResult("SLC MODIFY", response,
                               response.resultCode, "200",
                               verbose)

    cmd_result.print(withName=True)


@c.command()
@c.argument('json_file', type=c.Path(exists=True), required=1)
def config_file_set(json_file):
    """
    Sets the configuration file for the SCP.
    This path is persistent for each 5Gshell session until
    this command is issued again.
    """

    gd.dict5[gd.config_filename] = json_file
    Config5g.getInstance().set(gd.config_filename, json_file)


@c.command()
def config_file_show():
    """ Shows the defined configuration file for the SCP """
    cmd_result = CmdResult("config_file_show",
                           gd.dict5[gd.config_filename],
                           "1", "1")
    cmd_result.print()


@c.command()
@c.argument('json_file', type=c.Path(exists=True), required=0)
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def config_post(json_file, verbose):
    """
    POSTS the configuration file to the SCP manager.
    If no configuration is provided then the one set to
    config-file-set command is used.

    The envoy port is read and replaced automatically in the configuration
    every time this command is executed.
    """

    if json_file:
        jfile = json_file
    elif gd.dict5[gd.config_filename]:
        jfile = gd.dict5[gd.config_filename]
    else:
        printer.justprint("Please provide a configuration file")
        return

    printer.justprint("Using configuration: " + jfile)
    json_helper = JsonHelper(jfile)

    if not json_helper.open_file():
        return

    # replace the port
    temp_port = kubehelper.get_envoy_port()
    if not temp_port:
        printer.error("Envoy port was not found")
        return

    gd.dict5[gd.envoy_port] = temp_port

    json_helper.replace_port(gd.dict5[gd.envoy_port])
    # add name property if it doesn't exist
    json_helper.add_name(gd.dict5[gd.cmm_config_name])
    json_helper.write_file()
    response = req.post_configuration(gd.dict5[gd.kubeproxy_ip],
                                      gd.dict5[gd.cmm_port],
                                      jfile)

    cmd_result = RequestResult("config_post", response,
                               response.body, "Created configuration",
                               verbose)

    cmd_result.print()


@c.command()
@c.argument('json_file', type=c.Path(exists=True), required=0)
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def config_update(json_file, verbose):
    """
    UPDATES the configuration file of the SCP manager.
    If no configuration is provided then the one set to
    config-file-set command is used.

    The envoy port is read and replaced automatically in the configuration
    every time this command is executed.
    The name property is removed from the JSON file temporarily, for the
    request to take place successfully.
    """

    if json_file:
        jfile = json_file
    elif gd.dict5[gd.config_filename]:
        jfile = gd.dict5[gd.config_filename]
    else:
        printer.justprint("Please provide a configuration file")
        return

    printer.justprint("Using configuration: " + jfile)
    json_helper = JsonHelper(jfile)

    if not json_helper.open_file():
        return

    # replace the port
    temp_port = kubehelper.get_envoy_port()
    if not temp_port:
        printer.error("Envoy port was not found")
        return

    gd.dict5[gd.envoy_port] = temp_port

    json_helper.replace_port(gd.dict5[gd.envoy_port])
    # remove the name property
    json_helper.remove_name()
    json_helper.write_file()
    response = req.update_configuration(gd.dict5[gd.kubeproxy_ip],
                                        gd.dict5[gd.cmm_port],
                                        gd.dict5[gd.cmm_config_name],
                                        jfile)
    # add the name property again
    json_helper.add_name(gd.dict5[gd.cmm_config_name])
    json_helper.write_file()

    cmd_result = RequestResult("config_update", response,
                               response.body, "Updated configuration",
                               verbose)

    cmd_result.print()


@c.command()
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def config_delete(verbose):
    """
    DELETES the configuration from the SCP manager.
    """

    response = req.delete_configuration(gd.dict5[gd.kubeproxy_ip],
                                        gd.dict5[gd.cmm_port],
                                        gd.dict5[gd.cmm_config_name])

    cmd_result = RequestResult("config_delete", response,
                               response.body, "Deleted configuration",
                               verbose)

    cmd_result.print()


@c.command()
@c.argument('reference', required=0)
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
def chfsim_notify(reference, verbose):
    """
    Sends a notify message from chfsim to the destination
    in the notifyUri.
    If no charging reference is provided into
    the command, then the last one observed is used.
    """

    if reference:
        ref = reference
    elif gd.dict5[gd.last_ref]:
        ref = gd.dict5[gd.last_ref]
    else:
        printer.justprint("Please provide a charging reference")
        return

    # get the chfsim port for the charging reference
    occ = ref.split("-")[0]
    chfsim_port = kubehelper.get_chfsim_port(occ[len(occ)-1])

    # set envoy port in chfsim
    response = req.chfsim_set_envoy_port_request(gd.dict5[gd.kubeproxy_ip],
                                                 chfsim_port,
                                                 gd.dict5[gd.envoy_port])

    logging.debug(response)

    cmd_result = RequestResult("CHFSIM SET ENVOY PORT", response,
                               response.resultCode, "200",
                               verbose)

    cmd_result.print(withName=True)

    if not cmd_result.isCorrect():
        return

    response = req.chfsim_notify_request(gd.dict5[gd.kubeproxy_ip],
                                         chfsim_port,
                                         ref)

    cmd_result = RequestResult("CHFSIM NOTIFY", response,
                               response.resultCode, "200",
                               verbose)

    cmd_result.print(withName=True)


@c.command()
@c.option('--tls', '-t', is_flag=True, required=False,
          help="If set, the docker containter \"curl\" will be used "
          "to send a request with TLS")
@c.option('--verbose', '-v', is_flag=True, help="Will print verbose response.")
@c.pass_context
def quick_check(ctx, tls, verbose):
    """
    Performs a quick check on the traffic by sending:
    1) create - update - release charging reference request
    2) create with NotifyURI - update with NotifyURI charging reference request
    3) subscribe - modify - unsubscribe spending limit control
    4) subscribe with NotifyURI - modify with NotifyURI spending limit control

    TLS can also be enabled for the requests mentioned above. 
    """

    printer.separator()
    ctx.forward(cc_create, tls=tls)
    printer.separator()
    ctx.forward(cc_update, tls=tls)
    printer.separator()
    ctx.forward(cc_release, tls=tls)
    printer.separator()
    ctx.forward(cc_create, notify=True, tls=tls)
    printer.separator()
    ctx.forward(cc_update, notify=True, tls=tls)
    printer.separator()
    ctx.invoke(chfsim_notify)
    printer.separator()
    ctx.forward(slc_subscribe, tls=tls)
    printer.separator()
    ctx.forward(slc_modify, tls=tls)
    printer.separator()
    ctx.forward(slc_unsubscribe, tls=tls)
    printer.separator()
    ctx.forward(slc_subscribe, notify=True, tls=tls)
    printer.separator()
    ctx.forward(slc_modify, notify=True, tls=tls)
    printer.separator()
    ctx.invoke(chfsim_notify)
    printer.separator()


@c.command()
def envoy_port_show():
    """
    Prints the envoy port as defined for the pod
    """
    temp_port = kubehelper.get_envoy_port()
    if not temp_port:
        printer.error("Envoy port was not found")
        return

    if gd.dict5[gd.envoy_port] != temp_port:
        printer.important("Envoy port has changed from " +
                          gd.dict5[gd.envoy_port] + " to " + temp_port)
        printer.important("Please update your configuration in SCP.")
        printer.important("New port is: ")
        gd.dict5[gd.envoy_port] = temp_port

    cmd_result = CmdResult("envoy_port_show", gd.dict5[gd.envoy_port],
                           "1", "1")

    cmd_result.print()


@c.command() 
def envoy_list():
    """
    Prints all the envoys of the cluster
    """
    result = '\n'.join(kubehelper.get_scp_workers())
    cmd_result = CmdResult("envoy_list", result,
                           "1", "1")

    cmd_result.print()


@c.command()
@c.argument('mode', required=1, type=c.Choice(['on', 'off']))
@c.option('--envoy', '-e', type=str,
          help="Envoy to be used for port-forwarding")
def envoy_port_forward(mode, envoy):
    """
    Establishes port forwarding from local port 9901,
    for the first envoy worker encountered (if no other envoy
    is specified).
    """

    if not envoy:
        envoy = kubehelper.get_first_scp_worker()

    if mode == "on" and gd.dict5[gd.port_forwarding_envoy] is None:
        result = kubehelper.envoy_port_forward(envoy)
        if result:
            printer.important("Successful port forwarding for " + envoy)
            gd.dict5[gd.port_forwarding_envoy] = envoy
        else:
            printer.error("Unsuccessful port forwading for " + envoy)
    elif mode == "on":
        printer.error("Port forwarding in use for " + gd.dict5[gd.port_forwarding_envoy])
    #elif mode == "off" and gd.dict5[gd.port_forwarding_envoy] is not None:


@c.command()
@c.argument('mode', required=1, type=c.Choice(['off', 'mutual', 'server']))
@c.option('--file', '-f', type=c.Path(exists=True),
          help="The JSON configuration file to include the value")
def tls_set(mode, file):
    """
    Sets the TLS mode and updates it in the configuration file.
    If no configuration file is provided then the one set to
    config-file-set command is used.
    """
    if file:
        jfile = file
    elif gd.dict5[gd.config_filename]:
        jfile = gd.dict5[gd.config_filename]
    else:
        printer.justprint("Please provide a configuration file")
        return

    json_helper = JsonHelper(jfile)

    if not json_helper.open_file():
        return

    gd.dict5[gd.tls] = mode
    json_helper.replace_tls(gd.dict5[gd.tls])
    json_helper.write_file()


@c.command()
@c.option('--file', '-f', type=c.Path(exists=True),
          help="The JSON configuration file")
def tls_show(file):
    """
    Prints the value of TLS as defined in configuration file.
    If no configuration file is provided then the one set to
    config-file-set command is used.
    """

    if file:
        jfile = file
    elif gd.dict5[gd.config_filename]:
        jfile = gd.dict5[gd.config_filename]
    else:
        printer.justprint("Please provide a configuration file")
        return

    json_helper = JsonHelper(jfile)
    if not json_helper.open_file():
        return

    gd.dict5[gd.tls] = json_helper.get_tls()

    cmd_result = CmdResult("envoy_list", gd.dict5[gd.tls],
                           "1", "1")

    cmd_result.print()


@c.command()
@c.argument('mode', required=1, type=c.Choice(['on', 'off']))
def debug(mode):
    """ Turns on/off the debug mode """

    root_logger = logging.getLogger()

    if mode == 'on':
        root_logger.setLevel(logging.DEBUG)
    else:
        root_logger.setLevel(logging.INFO)


@c.command()
def help():
    """ """
    shellutils.client_help()


@c.command()
def pwd():
    """ """
    os.system("pwd")


@c.command()
def exit():
    """ """
    sys.exit(0)
