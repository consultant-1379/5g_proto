# Add grafana helm repository
$ helm repo add grafana https://grafana.github.io/helm-charts
"grafana" has been added to your repositories

# Search repo for grafana version
$ helm search repo grafana
WARNING: Repo "stable" is corrupt or missing. Try 'helm repo update'.WARNING: Repo "local" is corrupt or missing. Try 'helm repo update'.NAME                          	CHART VERSION	APP VERSION	DESCRIPTION                                       
grafana/grafana               	6.16.14      	8.2.1      	The leading tool for querying and visualizing t...
grafana/grafana-agent-operator	0.1.1        	0.19.0     	A Helm chart for Grafana Agent Operator           
grafana/enterprise-logs       	1.2.0        	v1.1.0     	Grafana Enterprise Logs                           
grafana/enterprise-metrics    	1.5.6        	v1.5.0     	Grafana Enterprise Metrics                        
grafana/fluent-bit            	2.3.0        	v2.1.0     	Uses fluent-bit Loki go plugin for gathering lo...
grafana/loki                  	2.6.0        	v2.3.0     	Loki: like Prometheus, but for logs.              
grafana/loki-canary           	0.4.0        	2.3.0      	Helm chart for Grafana Loki Canary                
grafana/loki-distributed      	0.38.1       	2.3.0      	Helm chart for Grafana Loki in microservices mode 
grafana/loki-stack            	2.5.0        	v2.1.0     	Loki: like Prometheus, but for logs.              
grafana/promtail              	3.8.1        	2.3.0      	Promtail is an agent which ships the contents o...
grafana/tempo                 	0.7.7        	1.1.0      	Grafana Tempo Single Binary Mode                  
grafana/tempo-distributed     	0.9.16       	1.1.0      	Grafana Tempo in MicroService mode                
grafana/tempo-vulture         	0.1.0        	0.7.0      	Grafana Tempo Vulture - A tool to monitor Tempo...

# Download latest version 6.16.14
$ cd devtools/
$ cd grafana/
$ helm fetch grafana/grafana
$ ls
grafana-6.16.14.tgz

# Unpack tgz file
$ tar zxvf grafana-6.16.14.tgz
grafana/Chart.yaml
grafana/values.yaml
grafana/templates/NOTES.txt
grafana/templates/_helpers.tpl
grafana/templates/_pod.tpl
grafana/templates/clusterrole.yaml
grafana/templates/clusterrolebinding.yaml
grafana/templates/configmap-dashboard-provider.yaml
grafana/templates/configmap.yaml
grafana/templates/dashboards-json-configmap.yaml
grafana/templates/deployment.yaml
grafana/templates/headless-service.yaml
grafana/templates/hpa.yaml
grafana/templates/image-renderer-deployment.yaml
grafana/templates/image-renderer-network-policy.yaml
grafana/templates/image-renderer-service.yaml
grafana/templates/ingress.yaml
grafana/templates/poddisruptionbudget.yaml
grafana/templates/podsecuritypolicy.yaml
grafana/templates/pvc.yaml
grafana/templates/role.yaml
grafana/templates/rolebinding.yaml
grafana/templates/secret-env.yaml
grafana/templates/secret.yaml
grafana/templates/service.yaml
grafana/templates/serviceaccount.yaml
grafana/templates/servicemonitor.yaml
grafana/templates/statefulset.yaml
grafana/templates/tests/test-configmap.yaml
grafana/templates/tests/test-podsecuritypolicy.yaml
grafana/templates/tests/test-role.yaml
grafana/templates/tests/test-rolebinding.yaml
grafana/templates/tests/test-serviceaccount.yaml
grafana/templates/tests/test.yaml
grafana/.helmignore
grafana/README.md
grafana/ci/default-values.yaml
grafana/ci/with-dashboard-json-values.yaml
grafana/ci/with-dashboard-values.yaml
grafana/ci/with-image-renderer-values.yaml
grafana/dashboards/custom-dashboard.json

# Check docker images repositories used
$ cat grafana/values.yaml | grep repository
  repository: grafana/grafana
  repository: curlimages/curl
    repository: busybox
  #     url: https://example.com/repository/test.json
  #     url: https://example.com/repository/test-b64.json
    repository: quay.io/kiwigrid/k8s-sidecar
    # image-renderer Image repository
    repository: grafana/grafana-image-renderer

# Use armdockerhub.rnd.ericsson.se for all repository
--set image.repository=armdockerhub.rnd.ericsson.se/grafana/grafana
--set downloadDashboardsImage.repository=armdockerhub.rnd.ericsson.se/curlimages/curl
--set initChownData.image.repository=armdockerhub.rnd.ericsson.se/busybox
--set sidecar.image.repository=armdockerhub.rnd.ericsson.se/quay.io/kiwigrid/k8s-sidecar
--set imageRenderer.image.repository=armdockerhub.rnd.ericsson.se/grafana/grafana-image-renderer

# Set the service type to LoadBalancer and port to something crazy
--set service.type=LoadBalancer
--set service.port=9966

# Activate persistence
--set persistence.enabled=true
--set persistence.storageClassName=network-block

# Activate autoscaling
--set autoscaling.enabled=true
--set autoscaling.minReplicas=1
--set autoscaling.maxReplicas=3

# Deploy grafana
helm install grafana-${USER} --timeout 1500s --namespace 5g-bsf-${USER} grafana-6.16.14.tgz --set image.repository=armdockerhub.rnd.ericsson.se/grafana/grafana --set downloadDashboardsImage.repository=armdockerhub.rnd.ericsson.se/curlimages/curl --set initChownData.image.repository=armdockerhub.rnd.ericsson.se/busybox --set sidecar.image.repository=armdockerhub.rnd.ericsson.se/quay.io/kiwigrid/k8s-sidecar --set imageRenderer.image.repository=armdockerhub.rnd.ericsson.se/grafana/grafana-image-renderer --set service.type=LoadBalancer --set service.port=9966 --set persistence.enabled=true --set persistence.storageClassName=network-block --set autoscaling.enabled=true --set autoscaling.minReplicas=1 --set autoscaling.maxReplicas=3

# Future
# - add to ldap for authentication
# - add json file for automatic dashboards
# - automation
