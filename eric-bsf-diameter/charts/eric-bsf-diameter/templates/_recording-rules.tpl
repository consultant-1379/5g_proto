# START. DND-60324
{{- define "eric-bsf-diameter.recording-rules" }}
# START. DND-60324
groups:
- name: bsfdiameter_traffic
  interval: 15s
  rules:

  # Number of transactions per second (TPS) for Diameter traffic
  - record: job:bsf_diameter_tps
    expr: sum(rate(Diameter_TransTerm_Ordered{dsl_class='DiaApp'}[60s])) or vector(0)

  # Success rate of Diameter traffic
  - record: job:bsf_diameter_success_rate
    expr: ((sum(rate(Diameter_TransTerm_Succeeded{dsl_class='DiaApp'}[60s]))/(sum(rate(Diameter_TransTerm_Ordered{dsl_class='DiaApp'}[60s]))>0)) or (0*sum(rate(Diameter_TransTerm_Ordered{dsl_class='DiaApp'}[60s])))) * 100

  # Success rate of Diameter traffic for CNOM
  - record: job:bsf_diameter_success_rate_cnom
    expr: (sum(rate(Diameter_TransTerm_Succeeded{dsl_class='DiaApp'}[60s]))/sum(rate(Diameter_TransTerm_Ordered{dsl_class='DiaApp'}[60s]))) * 100

- name: bsfdiameter_cpu_usage
  interval: 15s
  rules:

  # CPU usage (in millicores) of BSF Diameter
  - record: job:bsf_diameter_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='bsfdiameter',namespace='{{ .Release.Namespace }}'}[3m]))*1000 or vector(0)

  # CPU usage (in millicores) of STM Diameter
  - record: job:stm_diameter_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='diameter',namespace='{{ .Release.Namespace }}'}[3m]))*1000 or vector(0)

  # CPU usage (in millicores) of BSF Diameter Proxy gRPC
  - record: job:bsf_diameter_grpc_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='diameterproxygrpc',namespace='{{ .Release.Namespace }}'}[3m]))*1000 or vector(0)

  # CPU usage (in millicores) of BSF DSL Load
  - record: job:bsf_dsl_avg_cpu_load_millicores
    expr: avg(rate(container_cpu_usage_seconds_total{container='dsl',namespace='{{ .Release.Namespace }}'}[3m]))*1000 or vector(0)

  # CPU usage (in percentage) of BSF DSL
  - record: job:avg_bsf_dsl_load_percentage
    expr: avg(bsf_dsl_load) or vector(0)

  # CPU usage (in percentage) of BSF Diameter FE
  - record: job:avg_bsf_fe_diameter_load_percentage
    expr: avg(bsf_fe_diameter_load) or vector(0)

  # CPU usage (in percentage) of BSF Diameter
  - record: job:avg_bsf_diameter_load_percentage
    expr: avg(bsf_diameter_load) or vector(0)

  # CPU usage (in percentage) of BSF Diameter Proxy gRPC
  - record: job:avg_bsf_diameter_grpc_load_percentage
    expr: avg(bsf_proxy_grpc_diameter_load) or vector(0)

- name: bsfdiameter_memory_usage
  interval: 15s
  rules:

  # Memory usage (in MBytes) of BSF Diameter
  - record: job:bsf_diameter_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='bsfdiameter',namespace='{{ .Release.Namespace }}'})/1024/1024 or vector(0)

  # Memory usage (in MBytes) of STM Diameter
  - record: job:stm_diameter_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='diameter',namespace='{{ .Release.Namespace }}'})/1024/1024 or vector(0)

  # Memory usage (in MBytes) of BSF DSL
  - record: job:bsf_dsl_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='dsl',namespace='{{ .Release.Namespace }}'})/1024/1024 or vector(0)

  # Memory usage (in MBytes) of BSF Diameter Proxy gRPC
  - record: job:bsf_diameter_grpc_avg_memory_mb
    expr: avg(container_memory_working_set_bytes{container='diameterproxygrpc',namespace='{{ .Release.Namespace }}'})/1024/1024 or vector(0)

- name: cassandra_bsfdiameter_metrics
  interval: 15s
  rules:

  # Number of total CQL requests from BSF Diameter in requests/sec
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_cql_rate
    expr: sum(rate(cassandra_driver_cql_requests_counter{cdd_metric_source='diameter',nf='bsf'}[45s])) by (instance)

  # Number of mean latency of CQL requests from BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_mean_latency_per_instance
    expr: sum(cassandra_driver_cql_requests_mean{cdd_metric_source='diameter',nf='bsf'}) by (instance)

  # Number of total timeouts of CQL requests from BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_total_request_timeouts_per_instance
    expr: sum(job:cassandra_driver_cql_client_timeouts_counter:rate45s{cdd_metric_source='diameter',nf='bsf'}) by (instance)

  # Number of total throttling errors of CQL requests from BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_total_request_throttling_per_instance
    expr: sum(job:cassandra_driver_throttling_errors_counter:rate45s{cdd_metric_source='diameter',nf='bsf'}) by (instance)

  # Number of total connection init errors of CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_connection_init_errors
    expr: sum(rate(cassandra_driver_nodes_errors_connection_init_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total connection auth errors of CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_connection_auth_errors
    expr: sum(rate(cassandra_driver_nodes_errors_connection_auth_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total read timeout errors of CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_read_timeout_errors
    expr: sum(rate(cassandra_driver_nodes_errors_request_read_timeouts_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total write timeout errors of CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_write_timeout_errors
    expr: sum(rate(cassandra_driver_nodes_errors_request_write_timeouts_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total aborted CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_requests_aborted
    expr: sum(rate(cassandra_driver_nodes_errors_request_aborted_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total unavailable CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_requests_unavailable
    expr: sum(rate(cassandra_driver_nodes_errors_request_unavailables_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total unsent CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_requests_unsent
    expr: sum(rate(cassandra_driver_nodes_errors_request_unsent_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total other errors in CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_requests_other_error
    expr: sum(rate(cassandra_driver_nodes_errors_request_others_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Mean throttling delay of CQL requests from BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_throttling_delay_mean_latency
    expr: sum(cassandra_driver_throttling_delay_mean{cdd_metric_source='diameter',nf='bsf'}) by (instance)

  # Number of total throttling errors in CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_throttling_errors
    expr: sum(rate(cassandra_driver_throttling_errors_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Number of total timeout errors in CQL requests from BSF Diameter in errors/sec
  - record: job:bsf_diameter_timeout_errors
    expr: sum(rate(cassandra_driver_cql_client_timeouts_counter{cdd_metric_source='diameter',nf='bsf'}[45s]))

  # Max throttling queue of BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_throttling_queue_max
    expr: max(cassandra_driver_throttling_queue_size_value{cdd_metric_source='diameter',nf='bsf'}) by (instance)

  # Pool in flight for CQL requests from BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_pool_in_flight
    expr: sum(cassandra_driver_nodes_pool_in_flight_value{cdd_metric_source='diameter',nf='bsf'}) by (instance)

  # Pool open connections for CQL requests from BSF Diameter
  # {instance="<SCRAPE-TARGET-IP-PORT>"}
  - record: job:bsf_diameter_pool_open_connections
    expr: sum(cassandra_driver_nodes_pool_open_connections_value{cdd_metric_source='diameter',nf='bsf'}) by (instance)

- name: bsfdiameter_licensed_traffic
  interval: 15s
  rules:
  - record: job:bsf_diameter_ingress_req_total_or_vector_0
    expr: sum(rate(Diameter_IngressReqMsg_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_ingress_req_conn_mgmt_or_vector_0
    expr: sum(rate(Diameter_IngressReqMsgConnectionMgmt_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_ingress_req
    expr: job:bsf_diameter_ingress_req_total_or_vector_0 - job:bsf_diameter_ingress_req_conn_mgmt_or_vector_0

  - record: job:bsf_diameter_ingress_ans_total_or_vector_0
    expr: sum(rate(Diameter_IngressAnswMsg_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_ingress_ans_conn_mgmt_or_vector_0
    expr: sum(rate(Diameter_IngressAnswMsgConnectionMgmt_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_ingress_ans
    expr: job:bsf_diameter_ingress_ans_total_or_vector_0 - job:bsf_diameter_ingress_ans_conn_mgmt_or_vector_0

  - record: job:bsf_diameter_ingress_total
    expr: job:bsf_diameter_ingress_req + job:bsf_diameter_ingress_ans

  - record: job:bsf_diameter_egress_req_total_or_vector_0
    expr: sum(rate(Diameter_EgressReqMsg_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_egress_req_conn_mgmt_or_vector_0
    expr: sum(rate(Diameter_EgressReqMsgConnectionMgmt_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_egress_req
    expr: job:bsf_diameter_egress_req_total_or_vector_0 - job:bsf_diameter_egress_req_conn_mgmt_or_vector_0

  - record: job:bsf_diameter_egress_ans_total_or_vector_0
    expr: sum(rate(Diameter_EgressAnswMsg_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_egress_ans_conn_mgmt_or_vector_0
    expr: sum(rate(Diameter_EgressAnswMsgConnectionMgmt_TotalCount{dsl_class='DiaNode'}[60s])) or vector(0)

  - record: job:bsf_diameter_egress_ans 
    expr: job:bsf_diameter_egress_ans_total_or_vector_0 - job:bsf_diameter_egress_ans_conn_mgmt_or_vector_0

  - record: job:bsf_diameter_egress_total
    expr: job:bsf_diameter_egress_req + job:bsf_diameter_egress_ans

  - record: job:bsf_diameter_total
    expr: job:bsf_diameter_ingress_total + job:bsf_diameter_egress_total

{{- end }}
