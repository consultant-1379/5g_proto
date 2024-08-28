{{- define "eric-sc-bsf.recording-rules" }}
groups:
- name: bsf_vertx
  interval: 15s
  rules:

  - record: bsf_http_server_response_time_seconds
    expr: sum by (nf,nf_instance,method) (rate(vertx_http_server_response_time_seconds_sum{nf="bsf",route="/nbsf-management/v1>/pcfBindings"}[45s]))/ sum by (nf,nf_instance,method)  (rate(vertx_http_server_response_time_seconds_count{nf="bsf",route="/nbsf-management/v1>/pcfBindings"}[45s])) >=0
    labels:
      service: "nbsf-management"

  - record: bsf_http_server_response_time_seconds_max
    expr:  max by (nf,nf_instance,method) (vertx_http_server_response_time_seconds_max{nf="bsf",route="/nbsf-management/v1>/pcfBindings"})
    labels:
      service: "nbsf-management"

  - record: bsf_http_server_active_connections
    expr: sum(vertx_http_server_active_connections{nf="bsf",nf_instance!="",local_type="external"}) by (nf,nf_instance,app)

- name: bsf_traffic
  interval: 15s
  rules:

  # Number of transactions per second (TPS) for HTTP traffic
  - record: job:bsf_http_tps
    expr: sum(rate(bsf_in_requests_total{nf='bsf'}[60s])) or vector(0)

  # Success rate of HTTP traffic
  - record: job:bsf_http_success_rate
    expr: ((sum(rate(bsf_out_answers_total{nf='bsf',status=~'2.*'}[60s]))/(sum(rate(bsf_in_requests_total{nf='bsf'}[60s]))>0)) or (0*sum(rate(bsf_in_requests_total{nf='bsf'}[60s])))) * 100

  # Success rate of HTTP traffic for CNOM
  - record: job:bsf_http_success_rate_cnom
    expr: (sum(rate(bsf_out_answers_total{nf='bsf',status=~'2.*'}[60s]))/sum(rate(bsf_in_requests_total{nf='bsf'}[60s]))) * 100

- name: bsf_cpu_usage
  interval: 15s
  rules:

  # CPU usage (in millicores) of BSF Manager
  - record: job:bsf_manager_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-bsf-manager',namespace='{{ .Release.Namespace }}'}[3m]))*1000 or vector(0)

  # CPU usage (in millicores) of BSF Worker
  - record: job:bsf_worker_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-bsf-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000 or vector(0)

  # CPU usage (in millicores) of WCDB
  - record: job:wcdb_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='cassandra',namespace='{{ .Release.Namespace }}',pod=~'{{ template "eric-sc-bsf.wcdbcd.hostname" . }}-{{ template "eric-sc-bsf.wcdbcd.datacenter" . }}-.*'}[3m]))*1000 or vector(0)

  # Total CPU usage (in percentage) of BSF
  - record: job:avg_bsf_load_percentage
    expr: avg(bsf_load) or vector(0)

  # CPU usage (in percentage) of BSF Worker
  - record: job:avg_bsf_worker_load_percentage
    expr: avg(bsf_worker_load) or vector(0)

  # CPU usage (in percentage) of WCDB
  - record: job:avg_bsf_cassandra_load_percentage
    expr: avg(bsf_cassandra_load) or vector(0)

- name: bsf_memory_usage
  interval: 15s
  rules:

  # Memory usage (in MBytes) of BSF Manager
  - record: job:bsf_manager_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='eric-bsf-manager',namespace='{{ .Release.Namespace }}'})/1024/1024 or vector(0)

  # Memory usage (in MBytes) of BSF Worker
  - record: job:bsf_worker_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='eric-bsf-worker',namespace='{{ .Release.Namespace }}'})/1024/1024 or vector(0)

  # Memory usage (in MBytes) of WCDB
  - record: job:wcdb_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='cassandra',namespace='{{ .Release.Namespace }}',pod=~'{{ template "eric-sc-bsf.wcdbcd.hostname" . }}-{{ template "eric-sc-bsf.wcdbcd.datacenter" . }}-.*'})/1024/1024 or vector(0)

- name: cassandra_metrics
  interval: 15s
  rules:

  # number of request write timeouts
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.request.write-timeouts",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_request_write_timeouts_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_request_write_timeouts_counter[45s])
  - record: job:cassandra_driver_nodes_errors_request_write_timeouts_counter:rate45s
    expr: sum without (instance, cdd_node_instance, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_request_write_timeouts_counter:rate45s)

  # number of request read timeouts
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.request.read-timeouts",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_request_read_timeouts_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_request_read_timeouts_counter[45s])
  - record: job:cassandra_driver_nodes_errors_request_read_timeouts_counter:rate45s
    expr: sum without (instance, cdd_node_instance, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_request_read_timeouts_counter:rate45s)

  # number of cql client timeouts
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="cql-client-timeouts",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_cql_client_timeouts_counter:rate45s
    expr: rate(cassandra_driver_cql_client_timeouts_counter[45s])
  - record: job:cassandra_driver_cql_client_timeouts_counter:rate45s
    expr: sum without (cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_cql_client_timeouts_counter:rate45s)

  # number of request unavailable errors
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.request.unavailables",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_request_unavailables_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_request_unavailables_counter[45s])
  - record: job:cassandra_driver_nodes_errors_request_unavailables_counter:rate45s
    expr: sum without (instance, cdd_node_instance, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_request_unavailables_counter:rate45s)

  # number of request other errors for each cassandra client
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.request.others",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_request_others_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_request_others_counter[45s])
  - record: job:cassandra_driver_nodes_errors_request_others_counter:rate45s
    expr: sum without (instance, cdd_node_instance, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_request_others_counter:rate45s)

  # number of request aborted errors for each cassandra client
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.request.aborted",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_request_aborted_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_request_aborted_counter[45s])
  - record: job:cassandra_driver_nodes_errors_request_aborted_counter:rate45s
    expr: sum without (instance, cdd_node_instance, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_request_aborted_counter:rate45s)

  # number of request send unsent errors for each cassandra client
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.request.unsent",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_request_unsent_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_request_unsent_counter[45s])
  - record: job:cassandra_driver_nodes_errors_request_unsent_counter:rate45s
    expr: sum without (instance, cdd_node_instance, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_request_unsent_counter:rate45s)

  # number of connection init errors for each cassandra client-server combination
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.connection.init",
  #  cdd_metric_type="long",
  #  cdd_node_instance="<CASSANDRA-INSTANCE-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_connection_init_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_connection_init_counter[45s])
  - record: job:cassandra_driver_nodes_errors_connection_init_counter:rate45s
    expr: sum without (instance, cdd_metric_source, cdd_session, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_connection_init_counter:rate45s)

  # number of connection auth errors for each cassandra client-server combination
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="errors.connection.auth",
  #  cdd_metric_type="long",
  #  cdd_node_instance="<CASSANDRA-INSTANCE-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_nodes_errors_connection_auth_counter:rate45s
    expr: rate(cassandra_driver_nodes_errors_connection_auth_counter[45s])
  - record: job:cassandra_driver_nodes_errors_connection_auth_counter:rate45s
    expr: sum without (instance, cdd_metric_source, cdd_session, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_nodes_errors_connection_auth_counter:rate45s)
    
  # total sum of all connection init/auth attempts per cassandra instance
  # {cdd_metric_attribute="Count",
  #  cdd_metric_type="long",
  #  cdd_node_instance="<CASSANDRA-INSTANCE-IP-PORT>",
  #  nf="bsf"}
  - record: job:cassandra_driver_nodes_errors_connection_counter_sum:rate45s
    expr: sum without(job, app, cdd_metric_name, app_kubernetes_io_instance, app_kubernetes_io_name, app_kubernetes_io_version, kubernetes_namespace, kubernetes_pod_name, pod_template_hash) (job:cassandra_driver_nodes_errors_connection_init_counter:rate45s) + sum without(job, app, cdd_metric_name, app_kubernetes_io_instance, app_kubernetes_io_name, app_kubernetes_io_version, kubernetes_namespace, kubernetes_pod_name, pod_template_hash) (job:cassandra_driver_nodes_errors_connection_auth_counter:rate45s)

  # number of throttling errors per cassandra client
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="throttling-errors",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  cdd_node_instance="<CASSANDRA-INSTANCE-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_throttling_errors_counter:rate45s
    expr: rate(cassandra_driver_throttling_errors_counter[45s])
  - record: job:cassandra_driver_throttling_errors_counter:rate45s
    expr: sum without (cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_throttling_errors_counter:rate45s)

  # number of unsuccessful transactions per cassandra client
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_source="<WORKER or DIAMETER>",
  #  cdd_metric_type="long",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: job:cassandra_unsuccessful_transactions:rate45s
    expr: job:cassandra_driver_nodes_errors_request_write_timeouts_counter:rate45s +
          ignoring(cdd_metric_name, cdd_node_instance, cdd_session, cdd_metric_destination) job:cassandra_driver_nodes_errors_request_read_timeouts_counter:rate45s + 
          ignoring(cdd_metric_name, cdd_node_instance, cdd_session, cdd_metric_destination) job:cassandra_driver_nodes_errors_request_unavailables_counter:rate45s +
          ignoring(cdd_metric_name, cdd_node_instance, cdd_session, cdd_metric_destination) job:cassandra_driver_nodes_errors_request_others_counter:rate45s + 
          ignoring(cdd_metric_name, cdd_node_instance, cdd_session, cdd_metric_destination) job:cassandra_driver_nodes_errors_request_aborted_counter:rate45s + 
          ignoring(cdd_metric_name, cdd_node_instance, cdd_session, cdd_metric_destination) job:cassandra_driver_nodes_errors_request_unsent_counter:rate45s

  # total number of unsuccessful transactions 
  # {cdd_metric_attribute="Count",
  #  cdd_metric_type="long",
  #  nf="bsf"}
  - record: job:cassandra_unsuccessful_transactions_sum:rate45s
    expr: sum without (app, cdd_metric_source, app_kubernetes_io_instance, app_kubernetes_io_name, app_kubernetes_io_version, job, kubernetes_namespace, kubernetes_pod_name, pod_template_hash) (job:cassandra_unsuccessful_transactions:rate45s)

  # number of cql messages for each cassandra client-server combination
  - record: instance_job:cassandra_driver_cql_messages_counter:rate45s
    expr: rate(cassandra_driver_cql_messages_counter[45s])
  - record: job:cassandra_driver_cql_messages_counter:rate45s
    expr: sum without (instance, cdd_metric_source, cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_cql_messages_counter:rate45s)

  # total number of all cql messages 
  # {cdd_metric_attribute="Count", cdd_metric_name="cql-messages", cdd_metric_type="long", nf="bsf"}
  - record: job:cassandra_driver_cql_messages_counter_sum:rate45s
    expr: sum without (job, cdd_node_instance, cdd_metric_source, app, app_kubernetes_io_instance, app_kubernetes_io_name, app_kubernetes_io_version, kubernetes_namespace, kubernetes_pod_name, pod_template_hash) (job:cassandra_driver_cql_messages_counter:rate45s)
       
  # number of cql request for each cassandra client-server combination
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  cdd_metric_attribute="Count",
  #  cdd_metric_name="cql-messages",
  #  cdd_metric_type="long",
  #  cdd_node_instance="<CASSANDRA-INSTANCE-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: instance_job:cassandra_driver_cql_requests_counter:rate45s
    expr: rate(cassandra_driver_cql_requests_counter[45s])
  - record: job:cassandra_driver_cql_requests_counter:rate45s
    expr: sum without (cdd_session, cdd_metric_destination, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (instance_job:cassandra_driver_cql_requests_counter:rate45s)

  # total number of all cql requests
  # {cdd_metric_attribute="Count",
  #  cdd_metric_name="cql-requests",
  #  nf="bsf",
  #  pod_template_hash="<POD-HASH>"}
  - record: job:cassandra_driver_cql_requests_counter_sum:rate45s
    expr: sum without (instance, cdd_metric_source, cdd_session, app, app_kubernetes_io_instance, app_kubernetes_io_name, app_kubernetes_io_version, kubernetes_namespace, kubernetes_pod_name, pod_template_hash, job, app_kubernetes_io_managed_by, chart, eric_stm_diameter_dsl_access, helm_sh_chart) (rate(cassandra_driver_cql_requests_counter[45s]))

  # Ratio of total number of unsucessful transactions against total number of cql messages
  # {cdd_metric_attribute="Count",
  #  cdd_metric_type="long",
  #  nf="bsf"}
  - record: job:cassandra_unsuccessful_transactions_per_cassandra_total_messages:ratio_rate45s
    expr: job:cassandra_unsuccessful_transactions_sum:rate45s / ignoring(cdd_metric_name) (job:cassandra_driver_cql_messages_counter_sum:rate45s > 20) or sum without(cdd_metric_name) (0 * (job:cassandra_driver_cql_messages_counter_sum:rate45s))

  # Unsuccessful transactions per cassandra total messages
  # {cdd_metric_attribute="Count",
  #  cdd_metric_type="long",
  #  nf="bsf"}
  - record: job:cassandra_unsuccessful_transactions_per_cassandra_total_messages:ratio_rate45s_100
    expr: 100 * (job:cassandra_unsuccessful_transactions_per_cassandra_total_messages:ratio_rate45s)

  # Storage_utilization = 100 * (cassandra_storage_filesystem_bytes_total - cassandra_storage_filesystem_usable_bytes) / cassandra_storage_filesystem_bytes_total
  - record: job:cassandra_storage_utilization_total
    expr: (cassandra_storage_filesystem_bytes_total{wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'} - cassandra_storage_filesystem_usable_bytes{wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'})/(cassandra_storage_filesystem_bytes_total{wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'})
  - record: job:cassandra_storage_utilization_total_100
    expr: job:cassandra_storage_utilization_total * 100

  # Number of total CQL requests from BSF Worker in requests/sec
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_cql_rate
    expr: sum(rate(cassandra_driver_cql_requests_counter{cdd_metric_source='worker',nf='bsf'}[45s])) by (instance)

  # Number of mean latency of CQL requests from BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_mean_latency_per_instance
    expr: sum(cassandra_driver_cql_requests_mean{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Number of total timeouts of CQL requests from BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_total_request_timeouts_per_instance
    expr: sum(job:cassandra_driver_cql_client_timeouts_counter:rate45s{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Number of total throttling errors of CQL requests from BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_total_request_throttling_per_instance
    expr: sum(job:cassandra_driver_throttling_errors_counter:rate45s{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Number of total connection init errors of CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_connection_init_errors
    expr: sum(rate(cassandra_driver_nodes_errors_connection_init_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total connection auth errors of CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_connection_auth_errors
    expr: sum(rate(cassandra_driver_nodes_errors_connection_auth_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total read timeout errors of CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_read_timeout_errors
    expr: sum(rate(cassandra_driver_nodes_errors_request_read_timeouts_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total write timeout errors of CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_write_timeout_errors
    expr: sum(rate(cassandra_driver_nodes_errors_request_write_timeouts_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total aborted CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_requests_aborted
    expr: sum(rate(cassandra_driver_nodes_errors_request_aborted_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total unavailable CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_requests_unavailable
    expr: sum(rate(cassandra_driver_nodes_errors_request_unavailables_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total unsent CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_requests_unsent
    expr: sum(rate(cassandra_driver_nodes_errors_request_unsent_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total unsent CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_requests_other_error
    expr: sum(rate(cassandra_driver_nodes_errors_request_others_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Mean throttling delay of CQL requests from BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_throttling_delay_mean_latency
    expr: sum(cassandra_driver_throttling_delay_mean{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Number of total throttling errors in CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_throttling_errors
    expr: sum(rate(cassandra_driver_throttling_errors_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Number of total timeout errors in CQL requests from BSF Worker in errors/sec
  - record: job:bsf_worker_timeout_errors
    expr: sum(rate(cassandra_driver_cql_client_timeouts_counter{cdd_metric_source='worker',nf='bsf'}[45s]))

  # Max throttling queue of BSF
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:throttling_queue_max
    expr: max(cassandra_driver_throttling_queue_size_value{nf='bsf'}) by (instance)

  # Max throttling queue of BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_throttling_queue_max
    expr: max(cassandra_driver_throttling_queue_size_value{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Pool in flight for CQL requests from BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_pool_in_flight
    expr: sum(cassandra_driver_nodes_pool_in_flight_value{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Pool open connections for CQL requests from BSF Worker
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_worker_pool_open_connections
    expr: sum(cassandra_driver_nodes_pool_open_connections_value{cdd_metric_source='worker',nf='bsf'}) by (instance)

  # Storage usage of WCDB
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:file_system_storage_usage
    expr: sum(cassandra_storage_filesystem_bytes_total{wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'} - cassandra_storage_filesystem_usable_bytes{wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'}) by (instance)

  # Max WCDB storage utilization
  - record: job:max_database_storage_utilization
    expr: max(job:cassandra_storage_utilization_total_100)

  # Cassandra unavailable node (boolean)
  - record: job:cassandra_node_unavailable
    expr: (sum((cassandra_endpoint_downtime_seconds_total{endpoint_datacenter='{{ template "eric-sc-bsf.wcdbcd.datacenter" . }}',wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'} != bool 0)) by (endpoint)) >= bool ({{ index .Values "eric-bsf-wcdb-cd" "replicaCount" | int}} - 1)

  # Number of cassandra node interconnections between datacenters
  - record: job:cassandra_datacenter_unavailable
    expr: (sum(cassandra_endpoint_active{wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}',endpoint_datacenter!='{{ template "eric-sc-bsf.wcdbcd.datacenter" . }}'}) by (endpoint_datacenter)) <= bool 0

  # Number of Estimated Partitions per Table
  - record: cassandra_table_estimated_partitions_by_table
    expr: cassandra_table_estimated_partitions{keyspace='nbsf_management_keyspace',wcdbcd_deployment_name='{{ template "eric-sc-bsf.wcdbcd.hostname" . }}'} >= 0

- name: bsf_licensed_traffic
  interval: 15s
  rules:
  - record: job:bsf_in_req_total_or_vector_0
    expr: sum(rate(bsf_in_requests_total[60s])) or vector(0)

  - record: job:bsf_out_ans_total_or_vector_0
    expr: sum(rate(bsf_out_answers_total[60s])) or vector(0)

  - record: job:bsf_in_out_total
    expr: job:bsf_in_req_total_or_vector_0 + job:bsf_out_ans_total_or_vector_0

  - record: job:bsf_licensed_traffic
    expr: job:bsf_in_out_total + ((job:bsf_diameter_total > 0) or (0 * job:bsf_in_out_total))

- name: bsf_cleanup_statistics
  interval: 15s
  rules:

  # Rate of multiple bindings deleted due to resolution upon lookup
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  nf_instance="<NF-INSTANCE>",
  #  pcf_id="<PCF-ID> or unknown",
  #  pod_template_hash="<POD-HASH>",
  #  reason="multiple_bindings_found",
  #  source="resolve_upon_lookup"}
  - record: bsf_bindings_stale_deleted_total_lookup_multiple_bindings_rate45s
    expr: rate(bsf_bindings_stale_deleted_total{source='resolve_upon_lookup',reason='multiple_bindings_found'}[45s])

  # Sum of the rate of multiple bindings deleted due to resolution upon lookup per pcf_id
  # {pcf_id="<PCF-ID> or unknown"}
  - record: bsf_bindings_stale_deleted_total_lookup_multiple_bindings
    expr: sum(bsf_bindings_stale_deleted_total_lookup_multiple_bindings_rate45s) by (pcf_id)

  # Rate of stale bindings deleted due to pcf recovery time from resolution upon lookup
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  nf_instance="<NF-INSTANCE>",
  #  pcf_id="<PCF-ID>",
  #  pod_template_hash="<POD-HASH>",
  #  reason="pcf_recovery_time",
  #  source="resolve_upon_lookup"}
  - record: bsf_bindings_stale_deleted_total_lookup_recovery_time_rate45s
    expr: rate(bsf_bindings_stale_deleted_total{source='resolve_upon_lookup',reason='pcf_recovery_time'}[45s])

  # Sum of the rate of stale bindings deleted due to pcf recovery time from resolution upon lookup per pcf_id
  # {pcf_id="<PCF-ID>"}
  - record: bsf_bindings_stale_deleted_total_lookup_recovery_time
    expr: sum(bsf_bindings_stale_deleted_total_lookup_recovery_time_rate45s) by (pcf_id)

  # Rate of stale bindings deleted due to binding database scan
  # {app="eric-bsf-manager",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  leader="eric-bsf-manager-leader" or no label if leader election is disabled,
  #  nf="bsf",
  #  nf_instance="<NF-INSTANCE>",
  #  pcf_id="<PCF-ID>",
  #  pod_template_hash="<POD-HASH>",
  #  reason="pcf_recovery_time",
  #  source="binding_database_scan"}
  - record: bsf_bindings_stale_deleted_total_scan_rate45s
    expr: rate(bsf_bindings_stale_deleted_total{source='binding_database_scan',reason='pcf_recovery_time'}[45s])

  # Rate of scanned bindings
  # {app="eric-bsf-manager",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  leader="eric-bsf-manager-leader" or no label if leader election is disabled,
  #  nf="bsf",
  #  nf_instance="<NF-INSTANCE>",
  #  pcf_id="<PCF-ID> or unknown",
  #  pod_template_hash="<POD-HASH>"}
  - record: bsf_bindings_scanned_total_rate45s
    expr: rate(bsf_bindings_scanned_total[45s])

  # Rate of stale bindings detected due to binding database scan
  # {app="eric-bsf-manager",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  leader="eric-bsf-manager-leader" or no label if leader election is disabled,
  #  nf="bsf",
  #  nf_instance="<NF-INSTANCE>",
  #  pcf_id="<PCF-ID> or unknown",
  #  pod_template_hash="<POD-HASH>"}
  - record: bsf_bindings_stale_total_scan_rate45s
    expr: rate(bsf_bindings_stale_total[45s])

  # Total number of started binding database scans for the last two days
  - record: bsf_binding_database_scans_started_last_two_days
    expr: sum(bsf_binding_database_scans_started_total - on (instance) (bsf_binding_database_scans_started_total offset 2d)) or sum(bsf_binding_database_scans_started_total)

  # Total number of failed binding database scans for the last two days
  - record: bsf_binding_database_scans_failed_last_two_days_sum_by_reason
    expr: sum(bsf_binding_database_scans_failed_total - on (instance) (bsf_binding_database_scans_failed_total offset 2d)) or sum(bsf_binding_database_scans_failed_total)

  # Total number of successfully completed binding database scans for the last two days
  - record: bsf_binding_database_scans_completed_last_two_days
    expr: sum(bsf_binding_database_scans_completed_total - on (instance) (bsf_binding_database_scans_completed_total offset 2d)) or sum(bsf_binding_database_scans_completed_total)

- name: bsf_vtap
  interval: 15s
  rules:
  - record: bsf_tapcollector_in_tap_frames_total_rate
    expr: sum(rate(tapcollector_in_tap_frames_total{nf='bsf'}[90s]))
  - record: bsf_tapcollector_sent_frames_total_rate
    expr: sum(rate(tapcollector_out_tap_frames_success_total{nf='bsf'}[90s]) + (rate(tapcollector_out_tap_frames_sent_errors_total{nf='bsf'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='bsf'}[90s]))) + (rate(tapcollector_out_tap_frames_dropped_total{nf='bsf'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='bsf'}[90s])) ) + (rate(tapcollector_out_client_errors{nf='bsf'}[90s])  or (0 * rate(tapcollector_out_tap_frames_success_total{nf='bsf'}[90s]))))
  - record: bsf_tapcollector_recv_frames_failure_ratio
    expr: (sum(rate(tapcollector_in_server_error_total{nf='bsf'}[90s]) or (0 * bsf_tapcollector_in_tap_frames_total_rate))/ bsf_tapcollector_in_tap_frames_total_rate) * 100
  - record: bsf_tapcollector_sent_frames_failure_ratio
    expr: ((sum(rate(tapcollector_out_tap_frames_sent_errors_total{nf='bsf'}[90s]) + rate(tapcollector_out_tap_frames_dropped_total{nf='bsf'}[90s]) + (rate(tapcollector_out_client_errors{nf='bsf'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='bsf'}[90s]))))) / bsf_tapcollector_sent_frames_total_rate) * 100
  - record: bsf_tapcollector_out_tap_frames_dropped_ratio
    expr: ((rate(tapcollector_out_tap_frames_dropped_total{nf='bsf'}[90s]) / (rate(tapcollector_in_tap_frames_total{nf='bsf'}[90s])> 0)) or (0 * rate(tapcollector_in_tap_frames_total{nf='bsf'}[90s]))) * 100
  # new in SC 1.15
  - record: bsf_tapcollector_out_tap_frames_truncated_total_rate
    expr: sum(rate(tapcollector_out_tap_frames_truncated_total{nf='bsf'}[90s]))
  - record: bsf_tapcollector_out_tap_frame_splits_total_rate
    expr: sum(rate(tapcollector_out_tap_frame_splits_total{nf='bsf'}[90s]))
  # Rate of vtap segments that are successfully tapped from BSF-worker to the tapcollector sidecar
  - record: bsf_vtap_segments_tapped_rate
    expr: sum(rate(bsf_vtap_segments_tapped_total[90s]))

  # Ratio of vtap segments that are dropped from BSF-worker per instance
  # {app="<SERVICE-NAME>",
  #  app_kubernetes_io_instance="<HELM-RELEASE>",
  #  app_kubernetes_io_name="<CHART-NAME>",
  #  app_kubernetes_io_version="<RELEASE-VERSION>",
  #  instance="<SCRAPE-TARGET-IP-PORT>",
  #  job="<SCRAPE-JOB>",
  #  kubernetes_namespace="<NAMESPACE>",
  #  kubernetes_pod_name="<POD-NAME>",
  #  nf="bsf",
  #  nf_instance="<NF-INSTANCE>",
  #  pod_template_hash="<POD-HASH>",
  #  segment_type="<SEGMENT-TYPE>"}
  - record: bsf_vtap_segments_dropped_ratio_by_instance
    expr: ((((rate(bsf_vtap_segments_dropped_total[90s]) or (0* rate(bsf_vtap_segments_tapped_total[90s]))) + (rate(bsf_vtap_segments_dropped_size_too_big_total[90s]) or (0*rate(bsf_vtap_segments_tapped_total[90s])))) / (((rate(bsf_vtap_segments_tapped_total[90s]) or (0*rate(bsf_vtap_segments_dropped_total[90s]))) + (rate(bsf_vtap_segments_dropped_total[90s]) or (0* rate(bsf_vtap_segments_tapped_total[90s]))) + (rate(bsf_vtap_segments_dropped_size_too_big_total[90s]) or (0* rate(bsf_vtap_segments_tapped_total[90s]))))> 0)) or (0 * rate(bsf_vtap_segments_tapped_total[90s]))) * 100

  # Total ratio of vtap segments that are dropped from BSF-worker
  - record: bsf_vtap_segments_dropped_ratio
    expr: (sum((rate(bsf_vtap_segments_dropped_total[90s]) or (0* rate(bsf_vtap_segments_tapped_total[90s]))) + (rate(bsf_vtap_segments_dropped_size_too_big_total[90s]) or (0*rate(bsf_vtap_segments_tapped_total[90s]))))) / (sum((rate(bsf_vtap_segments_tapped_total[90s]) or (0*rate(bsf_vtap_segments_dropped_total[90s]))) + (rate(bsf_vtap_segments_dropped_total[90s]) or (0* rate(bsf_vtap_segments_tapped_total[90s]))) + (rate(bsf_vtap_segments_dropped_size_too_big_total[90s]) or (0* rate(bsf_vtap_segments_tapped_total[90s]))))) * 100
{{- end }}
