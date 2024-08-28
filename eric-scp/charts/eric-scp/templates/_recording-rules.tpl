{{- define "eric-scp.recording-rules" }}
groups:
- name: scp_metrics
  interval: 15s
  rules:
  - record: scp_ingress_mps
    expr: sum(rate(envoy_downstream_rq_total{nf='scp'}[45s]))

  - record: scp_ingress_success
    expr:  (sum(rate(envoy_downstream_rq_xx{group='ingress',envoy_response_code_class='2',nf='scp'}[45s]))/sum(rate(envoy_downstream_rq_total{nf='scp'}[45s]))) * 100
  - record: scp_ingress_success_pmbr
    expr:  (sum(rate(envoy_downstream_rq_xx{group='ingress',envoy_response_code_class='2',nf='scp'}[45s]))/sum(rate(envoy_downstream_rq_total{nf='scp'}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_egress_mps
    expr: sum(rate(envoy_upstream_rq_total{nf='scp'}[45s]))

  - record: scp_egress_pool_mps
    expr: topk(5,sum(rate(envoy_upstream_rq_total{nf='scp'}[45s])) by (pool_name))

  - record: scp_egress_success
    expr: (sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='2',nf='scp'}[45s]))/sum(rate(envoy_upstream_rq_total{nf='scp'}[45s]))) * 100
  - record: scp_egress_success_pmbr
    expr: (sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='2',nf='scp'}[45s]))/sum(rate(envoy_upstream_rq_total{nf='scp'}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_egress_pool_class4
    expr: topk(5,sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='4',nf='scp'}[45s])) by (pool_name))

  - record: scp_egress_pool_class5
    expr: topk(5,sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='5',nf='scp'}[45s])) by (pool_name))

  - record: scp_egress_pool_nf_mps
    expr: sum(rate(envoy_upstream_rq_total_per_nf{nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_class2
    expr: sum(rate(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="2",nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_class4
    expr: sum(rate(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="4",nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_class5
    expr: sum(rate(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="5",nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_timeout
    expr: sum(rate(envoy_upstream_rq_timeout_per_nf{nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_class2_sum
    expr: sum(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="2",nf='scp'}) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_class5_sum
    expr: sum(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="5",nf='scp'}) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_timeout_sum
    expr: sum(envoy_upstream_rq_timeout_per_nf{nf='scp'}) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_pending_failure_eject_sum
    expr: sum(envoy_upstream_rq_pending_failure_eject_per_nf{nf='scp'}) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_rx_reset
    expr: sum(rate(envoy_upstream_rq_rx_reset_per_nf{nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_tx_reset
    expr: sum(rate(envoy_upstream_rq_tx_reset_per_nf{nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_rq_after_reselect
    expr: sum(rate(envoy_upstream_rq_after_reselect_per_nf{nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_rq_after_retry
    expr: sum(rate(envoy_upstream_rq_after_retry_per_nf{nf='scp'}[45s])) by (nf_instance_id, pool_name)
  - record: scp_egress_pool_nf_success
    expr: (scp_egress_pool_nf_class2/scp_egress_pool_nf_mps) * 100

  - record: scp_nrf_discovery_mps
    expr: sum(rate(nrf_out_requests_total{app='eric-scp-manager', nf='scp', service='nnrf-disc', }[45s]))

  - record: scp_nrf_discovery_success
    expr: (sum(rate(nrf_in_answers_total{app='eric-scp-manager', nf='scp',status=~'2.*', service='nnrf-disc'}[45s]))/sum(rate(nrf_out_requests_total{app='eric-scp-manager', nf='scp',service='nnrf-disc'}[45s]))) * 100
  - record: scp_nrf_discovery_success_pmbr
    expr: (sum by (nf)(rate(nrf_in_answers_total{app='eric-scp-manager', nf='scp',status=~'2.*', service='nnrf-disc'}[45s]))/sum by (nf)(rate(nrf_out_requests_total{app='eric-scp-manager', nf='scp',service='nnrf-disc'}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_manager_cpu
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-scp-manager',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: scp_worker_cpu_avg
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-scp-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: scp_worker_cpu_max
    expr: max(rate(container_cpu_usage_seconds_total{container='eric-scp-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: scp_worker_cpu_min
    expr: min(rate(container_cpu_usage_seconds_total{container='eric-scp-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: scp_manager_memory
    expr: avg(container_memory_working_set_bytes{container='eric-scp-manager',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: scp_worker_memory_avg
    expr: avg(container_memory_working_set_bytes{container='eric-scp-worker',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: scp_worker_memory_max
    expr: max(container_memory_working_set_bytes{container='eric-scp-worker',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: scp_worker_memory_min
    expr: min(container_memory_working_set_bytes{container='eric-scp-worker',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: scp_worker_pods
    expr: sum (up{job="eric-scp-worker-metrics-non-tls"})

  - record: scp_load_avg
    expr: avg(scp_load)

  - record: slf_scp_nrf_discovery_mps
    expr: sum(rate(nrf_out_requests_total{app='eric-sc-slf',nf='scp', service='nnrf-disc', }[45s]))

  - record: slf_nrf_discovery_success
    expr: (sum(rate(nrf_in_answers_total{app='eric-sc-slf',nf='scp',status=~'2.*', service='nnrf-disc'}[45s]))/sum(rate(nrf_out_requests_total{app='eric-sc-slf',nf='scp',service='nnrf-disc'}[45s]))) * 100
  - record: slf_nrf_discovery_success_pmbr
    expr: (sum(rate(nrf_in_answers_total{app='eric-sc-slf',nf='scp',status=~'2.*', service='nnrf-disc'}[45s]))/sum(rate(nrf_out_requests_total{app='eric-sc-slf',nf='scp',service='nnrf-disc'}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: slf_cpu
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-sc-slf',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: slf_memory
    expr: avg(container_memory_working_set_bytes{container='eric-sc-slf',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: scp_egress_rq_message_body_size
    expr:  rate(envoy_upstream_rq_body_size_sum{nf='scp',group='egress'}[60s]) / rate(envoy_upstream_rq_body_size_count{nf='scp',group='egress'}[60s])

  - record: scp_egress_rq_message_headers_size
    expr:  rate(envoy_upstream_rq_headers_size_sum{nf='scp',group='egress'}[60s]) / rate(envoy_upstream_rq_headers_size_count{nf='scp',group='egress'}[60s])

  - record: scp_egress_rs_message_body_size
    expr:  rate(envoy_upstream_rs_body_size_sum{nf='scp',group='egress'}[60s]) / rate(envoy_upstream_rs_body_size_count{nf='scp',group='egress'}[60s])

  - record: scp_egress_rs_message_headers_size
    expr: rate(envoy_upstream_rs_headers_size_sum{nf='scp',group='egress'}[60s]) / rate(envoy_upstream_rs_headers_size_count{nf='scp',group='egress'}[60s])

  - record: scp_egress_rq_message_body_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rq_body_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: scp_egress_rq_message_headers_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rq_headers_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: scp_egress_rs_message_body_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rs_body_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: scp_egress_rs_message_headers_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rs_headers_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: scp_downstream_cx_active
    expr: sum(envoy_downstream_cx_active{app='eric-scp-worker'}) without (scp_worker, instance, kubernetes_pod_name)
  - record: scp_downstream_cx_http1_active
    expr: sum(envoy_downstream_cx_http1_active{app='eric-scp-worker'}) without (scp_worker, instance, kubernetes_pod_name)
  - record: scp_downstream_cx_http2_active
    expr: sum(envoy_downstream_cx_http2_active{app='eric-scp-worker'}) without (scp_worker, instance, kubernetes_pod_name)
  - record: scp_upstream_cx_active
    expr: sum(envoy_upstream_cx_active{app='eric-scp-worker'}) without (scp_worker, instance, kubernetes_pod_name)

  - record: scp_ingress_global_rate_limit_accepted
    expr: sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]))
  - record: scp_ingress_global_rate_limit_dropped
    expr: sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]))
  - record: scp_ingress_global_rate_limit_rejected
    expr: sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]))
  - record: scp_ingress_global_rate_limit_accepted_per_network
    expr: sum by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s]))
  - record: scp_ingress_global_rate_limit_dropped_per_network
    expr: sum by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s]))
  - record: scp_ingress_global_rate_limit_rejected_per_network
    expr: sum by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s]))
  - record: scp_ingress_global_rate_limit_acceptance_ratio
    expr: (sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]) ) )) * 100
  - record: scp_ingress_global_rate_limit_acceptance_ratio_pmbr
    expr: (sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]) ) )) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_ingress_global_rate_limit_drop_ratio
    expr: (sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]) ) )) * 100
  - record: scp_ingress_global_rate_limit_drop_ratio_pmbr
    expr: (sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]) ) )) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_ingress_global_rate_limit_rejection_ratio
    expr: (sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]) ) )) * 100
  - record: scp_ingress_global_rate_limit_rejection_ratio_pmbr
    expr: (sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='scp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='scp',group='ingress'}[45s]) ) )) * 100 > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_ingress_global_rate_limit_acceptance_ratio_per_network
    expr: (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])))) * 100
  - record: scp_ingress_global_rate_limit_acceptance_ratio_per_network_pmbr
    expr: (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='scp',group='ingress',network=" "}),0)

  - record: scp_ingress_global_rate_limit_drop_ratio_per_network
    expr: (avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])))) * 100
  - record: scp_ingress_global_rate_limit_drop_ratio_per_network_pmbr
    expr: (avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='scp',group='ingress',network=" "}),0)

  - record: scp_ingress_global_rate_limit_rejection_ratio_per_network
    expr: (avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])))) * 100
  - record: scp_ingress_global_rate_limit_rejection_ratio_per_network_pmbr
    expr: (avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='scp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='scp',group='ingress'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='scp',group='ingress',network=" "}),0)

  - record: scp_ingress_global_rate_limit_sent_internal
    expr: (sum(rate(envoy_cluster_upstream_rq_total{nf='scp',envoy_cluster_name='global_rate_limit'}[45s])))

  - record: scp_ingress_global_rate_limit_success_ratio_internal
    expr: ((sum(rate(envoy_cluster_upstream_rq{nf='scp',envoy_response_code=~'2.*|429',envoy_cluster_name='global_rate_limit'}[45s])) or vector(0))/(sum(rate(envoy_cluster_upstream_rq_total{nf='scp',envoy_cluster_name='global_rate_limit'}[45s])))) * 100
  - record: scp_ingress_global_rate_limit_success_ratio_internal_pmbr
    expr: ((sum(rate(envoy_cluster_upstream_rq{nf='scp',envoy_response_code=~'2.*|429',envoy_cluster_name='global_rate_limit'}[45s])) or vector(0))/(sum(rate(envoy_cluster_upstream_rq_total{nf='scp',envoy_cluster_name='global_rate_limit'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='scp',envoy_cluster_name='global_rate_limit'}),0)

  - record: scp_licensed_traffic
    expr: sum(rate(envoy_upstream_rq_total{nf='scp'}[60s])*2) + sum(rate(envoy_downstream_rq_total{nf='scp'}[60s])*2)
  - record: scp_request_overall_time
    expr: rate(envoy_downstream_rq_time_sum{nf='scp'}[45s])/rate(envoy_downstream_rq_time_count{nf='scp'}[45s])

  - record: scp_request_process_time
    expr: rate(envoy_downstream_rq_time_sum{nf='scp'}[45s])/rate(envoy_downstream_rq_time_count{nf='scp'}[45s]) - on (instance)(avg by(instance)(rate(envoy_upstream_rq_time_sum{nf='scp'}[45s]))/avg by(instance)(rate(envoy_upstream_rq_time_count{nf='scp'}[45s])))
  - record: scp_request_process_time_pmbr
    expr: rate(envoy_downstream_rq_time_sum{nf='scp'}[45s])/rate(envoy_downstream_rq_time_count{nf='scp'}[45s]) - on (instance)(avg by(instance)(rate(envoy_upstream_rq_time_sum{nf='scp'}[45s]))/avg by(instance)(rate(envoy_upstream_rq_time_count{nf='scp'}[45s]))) > 0 or clamp_max(absent(notExists{nf='scp'}),0)

  - record: scp_tapcollector_in_tap_frames_total_rate
    expr: sum(rate(tapcollector_in_tap_frames_total{nf='scp'}[90s]))
  - record: scp_tapcollector_in_tap_frames_rate
    expr: rate(tapcollector_in_tap_frames_total{nf='scp'}[90s])
  - record: scp_tapcollector_sent_frames_total_rate
    expr: sum(rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s]) + (rate(tapcollector_out_tap_frames_sent_errors_total{nf='scp'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s]))) + (rate(tapcollector_out_tap_frames_dropped_total{nf='scp'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s]))) + (rate(tapcollector_out_client_errors{nf='scp'}[90s])  or (0 * rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s]))))
  - record: scp_tapcollector_sent_frames_rate
    expr: rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s]) + rate(tapcollector_out_tap_frames_sent_errors_total{nf='scp'}[90s]) + rate(tapcollector_out_tap_frames_dropped_total{nf='scp'}[90s]) + (rate(tapcollector_out_client_errors{nf='scp'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s])))
  - record: scp_tapcollector_recv_frames_failure_ratio
    expr: (sum(rate(tapcollector_in_server_error_total{nf='scp'}[90s]) or (0 * scp_tapcollector_in_tap_frames_rate))/ sum(scp_tapcollector_in_tap_frames_rate)) * 100
  - record: scp_tapcollector_sent_frames_failure_ratio
    expr: ((sum(rate(tapcollector_out_tap_frames_sent_errors_total{nf='scp'}[90s]) + rate(tapcollector_out_tap_frames_dropped_total{nf='scp'}[90s]) + (rate(tapcollector_out_client_errors{nf='scp'}[90s]) or (0 * rate(tapcollector_out_tap_frames_success_total{nf='scp'}[90s]))))) / scp_tapcollector_sent_frames_total_rate) * 100
  - record: scp_envoy_ingress_vtap_segments_tapped_rate
    expr: sum by(nf)(rate(envoy_segments_tapped{nf='scp' ,group='ingress-vtap'}[90s]))
  - record: scp_envoy_egress_vtap_segments_tapped_rate
    expr: sum by(nf)(rate(envoy_segments_tapped{nf='scp' ,group='egress-vtap'}[90s]))
  - record: scp_envoy_segment_tapped_failure_ratio
    expr: (sum((rate(envoy_segments_dropped{nf='scp'}[90s]) or (0* rate(envoy_segments_tapped{nf='scp'}[90s]))) + (rate(envoy_segments_size_too_big{nf='scp'}[90s]) or (0 * rate(envoy_segments_tapped{nf='scp'}[90s])))) / sum(rate(envoy_segments_tapped{nf='scp'}[90s]) + (rate(envoy_segments_dropped{nf='scp'}[90s]) or (0* rate(envoy_segments_tapped{nf='scp'}[90s]))) + (rate(envoy_segments_size_too_big{nf='scp'}[90s]) or (0 * rate(envoy_segments_tapped{nf='scp'}[90s]))))) * 100
  - record: scp_tapcollector_out_tap_frames_dropped_ratio
    expr: ((rate(tapcollector_out_tap_frames_dropped_total{nf='scp'}[90s]) / (rate(tapcollector_in_tap_frames_total{nf='scp'}[90s])> 0)) or (0 * rate(tapcollector_in_tap_frames_total{nf='scp'}[90s]))) * 100
  - record: scp_envoy_segments_dropped_ratio
    expr: ((((rate(envoy_segments_dropped{nf='scp'}[90s]) or (0* rate(envoy_segments_tapped{nf='scp'}[90s]))) + (rate(envoy_segments_size_too_big{nf='scp'}[90s]) or (0 * rate(envoy_segments_tapped{nf='scp'}[90s])))) / ((rate(envoy_segments_tapped{nf='scp'}[90s]) + (rate(envoy_segments_dropped{nf='scp'}[90s]) or (0* rate(envoy_segments_tapped{nf='scp'}[90s]))) + (rate(envoy_segments_size_too_big{nf='scp'}[90s]) or (0 * rate(envoy_segments_tapped{nf='scp'}[90s]))))> 0)) or (0* rate(envoy_segments_tapped{nf='scp'}[90s]))) * 100
  # new in SC 1.15
  - record: scp_tapcollector_out_tap_frames_truncated_total_rate
    expr: sum(rate(tapcollector_out_tap_frames_truncated_total{nf='scp'}[90s]))
  - record: scp_tapcollector_out_tap_frame_splits_total_rate
    expr: sum(rate(tapcollector_out_tap_frame_splits_total{nf='scp'}[90s]))
{{- end }}
