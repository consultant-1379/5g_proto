from utils import kubehelper
from utils.config5g import Config5g
# Config5g should run first
# Dictionary is loading persistent values from this file at initialization
# which are needed to the other libraries
shell_conf = Config5g()
shell_conf.getInstance().prepare_env()

envoy_port = 'envoy_port'
kubeproxy_ip = 'kubeproxy_ip'
cmm_port = 'cmm_port'
cmm_config_name = 'cmm_config_name'
tls = 'tls'
last_ref = 'last_ref'
last_ref = 'last_ref'
config_filename = 'config_filename'
port_forwarding_envoy = 'port_forwarding_envoy'


dict5 = dict(envoy_port=kubehelper.get_envoy_port(),
             kubeproxy_ip=kubehelper.get_kubeproxy_ip(),
             cmm_port=kubehelper.get_cmm_port(),
             cmm_config_name=kubehelper.get_cmm_config_name(),
             tls=None,
             last_ref=None,
             config_filename=Config5g.getInstance().get(config_filename),
             port_forwarding_envoy=None)
