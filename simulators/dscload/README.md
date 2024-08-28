# Requirement

To run diameter traffic using the dsc-load tool, you must have a
BSF deployment. However, there is no restriction in the deployment
order, since you can deploy the dsc-load tool first.

# Configuration

NOTE: It is not necessary to configure dsc-load. Dsc-load can be
deployed as is with only few exceptions. In case ipv6 or TLS support
is required, some configuration is needed.

Each deployed dsc-load Pod is automatically configured based on the
provided helm parameters during deployment and parameters that are
either collected by the Kubernetes API or the running Pod. The helm
parameters can be set either in the values.yaml file inside the helm
directory or by passing them as cli arguments during helm install.

Dsc-load related configuration exists under the "configuration"
namespace inside the values.yaml file. Additionally, they number of
dsc-load replicas and the assigned resources can be configured.

In case dsc-load tool is deployed in IPv6 cluster then the following
helm parameter must be set:

configuration.ip-version = 6

In case TLS support is needed then the following helm parameters
must be set:

configuration.tls.enabled = true

!important: TLS is not currently supported.

Among other configuration options, the number of diameter TPS can be
set with the following helm parameter:

configuration.diameter-tps = 100

# Installation

To install the dsc-load tool execute the following target:

'make deploy'
