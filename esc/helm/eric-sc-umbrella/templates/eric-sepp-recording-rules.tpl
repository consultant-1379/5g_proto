{{- define "eric-sc.sepp-recording-rules" }}
groups:
- name: sepp_metrics
  interval: 15s
  rules:
  - record: sepp_ingress_mps
    expr: sum(rate(envoy_downstream_rq_total{nf='sepp',roaming_partner=''}[45s]))
  - record: sepp_ingress_success
    expr: (sum(rate(envoy_downstream_rq_xx{group='ingress',envoy_response_code_class='2',nf='sepp',roaming_partner=''}[45s]))/sum(rate(envoy_downstream_rq_total{nf='sepp',roaming_partner=''}[45s]))) * 100
  - record: sepp_ingress_success_pmbr
    expr: (sum(rate(envoy_downstream_rq_xx{group='ingress',envoy_response_code_class='2',nf='sepp',roaming_partner=''}[45s]))/sum(rate(envoy_downstream_rq_total{nf='sepp',roaming_partner=''}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_egress_mps
    expr: sum(rate(envoy_upstream_rq_total{nf='sepp'}[45s]))
  - record: sepp_egress_pool_mps
    expr: sum(rate(envoy_upstream_rq_total{nf='sepp'}[45s])) by (pool_name)
  - record: sepp_egress_success
    expr: (sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='2',nf='sepp'}[45s]))/sum(rate(envoy_upstream_rq_total{nf='sepp'}[45s]))) * 100
  - record: sepp_egress_success_pmbr
    expr: (sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='2',nf='sepp'}[45s]))/sum(rate(envoy_upstream_rq_total{nf='sepp'}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_egress_pool_nf_mps
    expr: sum(rate(envoy_upstream_rq_total_per_nf{nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_class2
    expr: sum(rate(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="2",nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_class4
    expr: sum(rate(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="4",nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_class5
    expr: sum(rate(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="5",nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_timeout
    expr: sum(rate(envoy_upstream_rq_timeout_per_nf{nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_class2_sum
    expr: sum(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="2",nf='sepp'}) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_class5_sum
    expr: sum(envoy_upstream_rq_xx_per_nf{envoy_response_code_class="5",nf='sepp'}) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_timeout_sum
    expr: sum(envoy_upstream_rq_timeout_per_nf{nf='sepp'}) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_pending_failure_eject_sum
    expr: sum(envoy_upstream_rq_pending_failure_eject_per_nf{nf='sepp'}) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_rx_reset
    expr: sum(rate(envoy_upstream_rq_rx_reset_per_nf{nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_tx_reset
    expr: sum(rate(envoy_upstream_rq_tx_reset_per_nf{nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_rq_after_reselect
    expr: sum(rate(envoy_upstream_rq_after_reselect_per_nf{nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_rq_after_retry
    expr: sum(rate(envoy_upstream_rq_after_retry_per_nf{nf='sepp'}[45s])) by (nf_instance_id, pool_name)
  - record: sepp_egress_pool_nf_success
    expr: (sepp_egress_pool_nf_class2/sepp_egress_pool_nf_mps) * 100

  - record: sepp_egress_pool_class4
    expr: sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='4',nf='sepp'}[45s])) by (pool_name)
  - record: sepp_egress_pool_class5
    expr: sum(rate(envoy_upstream_rq_xx{envoy_response_code_class='5',nf='sepp'}[45s])) by (pool_name)
  - record: sepp_ingress_rp_mps
    expr: sum(rate(envoy_downstream_rq_total_per_roaming_partner{nf='sepp'}[45s])) by (roaming_partner)
  - record: sepp_ingress_rp_class4
    expr: sum(rate(envoy_downstream_rq_xx_per_roaming_partner{nf='sepp',envoy_response_code_class='4'}[45s])) by (roaming_partner)
  - record: sepp_ingress_rp_class5
    expr: sum(rate(envoy_downstream_rq_xx_per_roaming_partner{nf='sepp',envoy_response_code_class='5'}[45s])) by (roaming_partner)
  - record: sepp_nrf_discovery_mps
    expr: sum(rate(nrf_out_requests_total{nf='sepp', service='nnrf-disc', }[45s]))
  - record: sepp_nrf_discovery_success
    expr: (sum(rate(nrf_in_answers_total{nf='sepp',status=~'2.*', service='nnrf-disc'}[45s]))/sum(rate(nrf_out_requests_total{nf='sepp',service='nnrf-disc'}[45s]))) * 100
  - record: sepp_nrf_discovery_success_pmbr
    expr: (sum by (nf)(rate(nrf_in_answers_total{nf='sepp',status=~'2.*', service='nnrf-disc'}[45s]))/sum by (nf)(rate(nrf_out_requests_total{nf='sepp',service='nnrf-disc'}[45s]))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_ip_address_hiding_discovery
    expr: sum(rate(envoy_ip_address_hiding_applied_success{nf='sepp',type='nf_discovery'}[45s])) by (roaming_partner)
  - record: sepp_ip_address_hiding_discovery_fqdn_missing
    expr: sum(rate(envoy_ip_address_hiding_fqdn_missing{nf='sepp',type='nf_discovery'}[45s])) by (roaming_partner)
  - record: sepp_ip_address_hiding_notify
    expr: sum(rate(envoy_ip_address_hiding_applied_success{nf='sepp',type='nf_status_notify'}[45s])) by (roaming_partner)
  - record: sepp_ip_address_hiding_notify_fqdn_missing
    expr: sum(rate(envoy_ip_address_hiding_fqdn_missing{nf='sepp',type='nf_status_notify'}[45s])) by (roaming_partner)
  - record: sepp_ip_address_hiding_config_errors
    expr: sum(rate(envoy_ip_address_hiding_configuration_error{nf='sepp',type='nf_status_notify'}[45s])) by (roaming_partner)

  - record: sepp_fqdn_mapping_req_map_success
    expr: rate(envoy_th_fqdn_mapping_req_map_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_resp_map_success
    expr: rate(envoy_th_fqdn_mapping_resp_map_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_req_demap_success
    expr: rate(envoy_th_fqdn_mapping_req_demap_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_resp_demap_success
    expr: rate(envoy_th_fqdn_mapping_resp_demap_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_req_demap_failure
    expr: rate(envoy_th_fqdn_mapping_req_demap_failure_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_resp_demap_failure
    expr: rate(envoy_th_fqdn_mapping_resp_demap_failure_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_req_map_failure
    expr: rate(envoy_th_fqdn_mapping_req_map_failure_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_resp_map_failure
    expr: rate(envoy_th_fqdn_mapping_resp_map_failure_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_req_forwarded_unmodified
    expr: rate(envoy_th_fqdn_mapping_req_forwarded_unmodified_total{nf='sepp'}[45s])
  - record: sepp_fqdn_mapping_resp_forwarded_unmodified
    expr: rate(envoy_th_fqdn_mapping_resp_forwarded_unmodified_total{nf='sepp'}[45s])

  - record: sepp_fqdn_mapping_map_success
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_mapping_resp_map_success|sepp_fqdn_mapping_req_map_success"})
  - record: sepp_fqdn_mapping_demap_success
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_mapping_req_demap_success|sepp_fqdn_mapping_resp_demap_success"})
  - record: sepp_fqdn_mapping_failure
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_mapping_req_demap_failure|sepp_fqdn_mapping_resp_demap_failure|sepp_fqdn_mapping_resp_map_failure|sepp_fqdn_mapping_req_map_failure"})
  - record: sepp_fqdn_mapping_unmodified
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_mapping_req_forwarded_unmodified|sepp_fqdn_mapping_resp_forwarded_unmodified"})

  - record: sepp_fqdn_scrambling_req_scramble_success
    expr: rate(envoy_th_fqdn_scrambling_req_scramble_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_descramble_success
    expr: rate(envoy_th_fqdn_scrambling_req_descramble_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_scramble_success
    expr: rate(envoy_th_fqdn_scrambling_resp_scramble_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_descramble_success
    expr: rate(envoy_th_fqdn_scrambling_resp_descramble_success_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_scramble_invalid_fqdn
    expr: rate(envoy_th_fqdn_scrambling_req_scramble_invalid_fqdn_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_descramble_invalid_fqdn
    expr: rate(envoy_th_fqdn_scrambling_req_descramble_invalid_fqdn_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_scramble_invalid_fqdn
    expr: rate(envoy_th_fqdn_scrambling_resp_scramble_invalid_fqdn_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_descramble_invalid_fqdn
    expr: rate(envoy_th_fqdn_scrambling_resp_descramble_invalid_fqdn_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_scramble_encryption_id_not_found
    expr: rate(envoy_th_fqdn_scrambling_req_scramble_encryption_id_not_found_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_descramble_encryption_id_not_found
    expr: rate(envoy_th_fqdn_scrambling_req_descramble_encryption_id_not_found_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_scramble_encryption_id_not_found
    expr: rate(envoy_th_fqdn_scrambling_resp_scramble_encryption_id_not_found_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_descramble_encryption_id_not_found
    expr: rate(envoy_th_fqdn_scrambling_resp_descramble_encryption_id_not_found_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_scramble_incorrect_encryption_id
    expr: rate(envoy_th_fqdn_scrambling_req_scramble_incorrect_encryption_id_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_descramble_incorrect_encryption_id
    expr: rate(envoy_th_fqdn_scrambling_req_descramble_incorrect_encryption_id_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_scramble_incorrect_encryption_id
    expr: rate(envoy_th_fqdn_scrambling_resp_scramble_incorrect_encryption_id_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_descramble_incorrect_encryption_id
    expr: rate(envoy_th_fqdn_scrambling_resp_descramble_incorrect_encryption_id_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_forwarded_unmodified_fqdn
    expr: rate(envoy_th_fqdn_scrambling_req_forwarded_unmodified_fqdn_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_forwarded_unmodified_fqdn
    expr: rate(envoy_th_fqdn_scrambling_resp_forwarded_unmodified_fqdn_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_req_forwarded_unmodified_ip
    expr: rate(envoy_th_fqdn_scrambling_req_forwarded_unmodified_ip_total{nf='sepp'}[45s])
  - record: sepp_fqdn_scrambling_resp_forwarded_unmodified_ip
    expr: rate(envoy_th_fqdn_scrambling_resp_forwarded_unmodified_ip_total{nf='sepp'}[45s])

  - record: sepp_fqdn_scrambling_scramble_success
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_scrambling_req_scramble_success|sepp_fqdn_scrambling_resp_scramble_success"})
  - record: sepp_fqdn_scrambling_descramble_success
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_scrambling_req_descramble_success|sepp_fqdn_scrambling_resp_descramble_success"})
  - record: sepp_fqdn_scrambling_failure
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_scrambling_req_scramble_invalid_fqdn|sepp_fqdn_scrambling_req_descramble_invalid_fqdn|sepp_fqdn_scrambling_resp_scramble_invalid_fqdn|sepp_fqdn_scrambling_resp_descramble_invalid_fqdn|sepp_fqdn_scrambling_req_scramble_encryption_id_not_found|sepp_fqdn_scrambling_req_descramble_encryption_id_not_found|sepp_fqdn_scrambling_resp_scramble_encryption_id_not_found|sepp_fqdn_scrambling_resp_descramble_encryption_id_not_found|sepp_fqdn_scrambling_req_scramble_incorrect_encryption_id|sepp_fqdn_scrambling_req_descramble_incorrect_encryption_id|sepp_fqdn_scrambling_resp_scramble_incorrect_encryption_id|sepp_fqdn_scrambling_resp_descramble_incorrect_encryption_id"})
  - record: sepp_fqdn_scrambling_unmodified
    expr: sum by (roaming_partner) ({__name__=~"sepp_fqdn_scrambling_req_forwarded_unmodified_fqdn|sepp_fqdn_scrambling_resp_forwarded_unmodified_fqdn|sepp_fqdn_scrambling_req_forwarded_unmodified_ip|sepp_fqdn_scrambling_resp_forwarded_unmodified_ip"})

  - record: sepp_manager_cpu
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-sepp-manager',namespace='{{ .Release.Namespace }}'}[3m]))*1000
  - record: sepp_worker_cpu_avg
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-sepp-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000
  - record: sepp_worker_cpu_max
    expr: max(rate(container_cpu_usage_seconds_total{container='eric-sepp-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000
  - record: sepp_worker_cpu_min
    expr: min(rate(container_cpu_usage_seconds_total{container='eric-sepp-worker',namespace='{{ .Release.Namespace }}'}[3m]))*1000
  - record: sepp_manager_memory
    expr: avg(container_memory_working_set_bytes{container='eric-sepp-manager',namespace='{{ .Release.Namespace }}'})/1024/1024
  - record: sepp_worker_memory_avg
    expr: avg(container_memory_working_set_bytes{container='eric-sepp-worker',namespace='{{ .Release.Namespace }}'})/1024/1024
  - record: sepp_worker_memory_max
    expr: max(container_memory_working_set_bytes{container='eric-sepp-worker',namespace='{{ .Release.Namespace }}'})/1024/1024
  - record: sepp_worker_memory_min
    expr: min(container_memory_working_set_bytes{container='eric-sepp-worker',namespace='{{ .Release.Namespace }}'})/1024/1024
  - record: sepp_worker_pods
    expr: sum (up{job="eric-sepp-worker-metrics-non-tls"})
  - record: sepp_load_avg
    expr: avg(sepp_load)
  - record: rlf_memory
    expr: avg(container_memory_working_set_bytes{container='eric-sc-rlf',namespace='{{ .Release.Namespace }}'})/1024/1024
  - record: rlf_cpu
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-sc-rlf',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: sepp_egress_rq_message_body_size
    expr:  rate(envoy_upstream_rq_body_size_sum{nf='sepp',group='egress'}[60s]) / rate(envoy_upstream_rq_body_size_count{nf='sepp',group='egress'}[60s])

  - record: sepp_egress_rq_message_headers_size
    expr:  rate(envoy_upstream_rq_headers_size_sum{nf='sepp',group='egress'}[60s]) / rate(envoy_upstream_rq_headers_size_count{nf='sepp',group='egress'}[60s])

  - record: sepp_egress_rs_message_body_size
    expr:  rate(envoy_upstream_rs_body_size_sum{nf='sepp',group='egress'}[60s]) / rate(envoy_upstream_rs_body_size_count{nf='sepp',group='egress'}[60s])

  - record: sepp_egress_rs_message_headers_size
    expr: rate(envoy_upstream_rs_headers_size_sum{nf='sepp',group='egress'}[60s]) / rate(envoy_upstream_rs_headers_size_count{nf='sepp',group='egress'}[60s])

  - record: sepp_egress_rq_message_body_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rq_body_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: sepp_egress_rq_message_headers_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rq_headers_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: sepp_egress_rs_message_body_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rs_body_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: sepp_egress_rs_message_headers_size_percentile
    expr: histogram_quantile(0.99, sum (rate(envoy_upstream_rs_headers_size_bucket[1m])) by (le, pool_name, instance, nf_instance, app, nf, group))

  - record: sepp_downstream_cx_active
    expr: sum(envoy_downstream_cx_active{app='eric-sepp-worker'}) without (sepp_worker, instance, kubernetes_pod_name)
  - record: sepp_downstream_cx_http1_active
    expr: sum(envoy_downstream_cx_http1_active{app='eric-sepp-worker'}) without (sepp_worker, instance, kubernetes_pod_name)
  - record: sepp_downstream_cx_http2_active
    expr: sum(envoy_downstream_cx_http2_active{app='eric-sepp-worker'}) without (sepp_worker, instance, kubernetes_pod_name)
  - record: sepp_upstream_cx_active
    expr: sum(envoy_upstream_cx_active{app='eric-sepp-worker'}) without (sepp_worker, instance, kubernetes_pod_name)
  - record: sepp_ingress_global_rate_limit_accepted
    expr: sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]))
  - record: sepp_ingress_global_rate_limit_dropped
    expr: sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]))
  - record: sepp_ingress_global_rate_limit_rejected
    expr: sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]))
  - record: sepp_ingress_global_rate_limit_accepted_per_network
    expr: sum by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s]))
  - record: sepp_ingress_global_rate_limit_dropped_per_network
    expr: sum by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s]))
  - record: sepp_ingress_global_rate_limit_rejected_per_network
    expr: sum by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s]))
  - record: sepp_ingress_global_rate_limit_accepted_per_roaming_partner
    expr: sum(rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) by (roaming_partner)
  - record: sepp_ingress_global_rate_limit_dropped_per_roaming_partner
    expr: sum(rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s])) by (roaming_partner)
  - record: sepp_ingress_global_rate_limit_rejected_per_roaming_partner
    expr: sum(rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) by (roaming_partner)
  - record: sepp_ingress_global_rate_limit_acceptance_ratio
    expr: (sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]) ) )) * 100
  - record: sepp_ingress_global_rate_limit_acceptance_ratio_pmbr
    expr: (sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]) ) )) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_ingress_global_rate_limit_drop_ratio
    expr: (sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]) ) )) * 100
  - record: sepp_ingress_global_rate_limit_drop_ratio_pmbr
    expr: (sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]) ) )) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_ingress_global_rate_limit_rejection_ratio
    expr: (sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]) ) )) * 100
  - record: sepp_ingress_global_rate_limit_rejection_ratio_pmbr
    expr: (sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s])) /(sum(rate(envoy_global_rate_limit_accepted{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_rejected{nf='sepp',group='ingress'}[45s]) ) + sum(rate(envoy_global_rate_limit_dropped{nf='sepp',group='ingress'}[45s]) ) )) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_ingress_global_rate_limit_acceptance_ratio_per_network
    expr: (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_acceptance_ratio_per_network_pmbr
    expr: (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp',network=" "}),0)

  - record: sepp_ingress_global_rate_limit_drop_ratio_per_network
    expr: (avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_drop_ratio_per_network_pmbr
    expr: (avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp',network=" "}),0)

  - record: sepp_ingress_global_rate_limit_rejection_ratio_per_network
    expr: (avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_rejection_ratio_per_network_pmbr
    expr: (avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) / (avg by (network) (rate(envoy_global_rate_limit_accepted_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_rejected_per_network{nf='sepp',group='ingress'}[45s])) + avg by (network) (rate(envoy_global_rate_limit_dropped_per_network{nf='sepp',group='ingress'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp',network=" "}),0)

  - record: sepp_ingress_global_rate_limit_acceptance_ratio_per_roaming_partner
    expr: (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) / (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_acceptance_ratio_per_roaming_partner_pmbr
    expr: topk(5, (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) / (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s]))))) * 100

  - record: sepp_ingress_global_rate_limit_drop_ratio_per_roaming_partner
    expr: (avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s])) / (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_drop_ratio_per_roaming_partner_pmbr
    expr: topk(5, (avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s])) / (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s]))))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp',roaming_partner=" "}),0)

  - record: sepp_ingress_global_rate_limit_rejection_ratio_per_roaming_partner
    expr: (avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) / (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_rejection_ratio_per_roaming_partner_pmbr
    expr: topk(5, (avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) / (avg by (roaming_partner) (rate(envoy_global_rate_limit_accepted_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_rejected_per_roaming_partner{nf='sepp',group='ingress'}[45s])) + avg by (roaming_partner) (rate(envoy_global_rate_limit_dropped_per_roaming_partner{nf='sepp',group='ingress'}[45s]))))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp',roaming_partner=" "}),0)

  - record: sepp_ingress_global_rate_limit_sent_internal
    expr: (sum(rate(envoy_cluster_upstream_rq_total{nf='sepp',envoy_cluster_name='global_rate_limit'}[45s])))
  - record: sepp_ingress_global_rate_limit_success_ratio_internal
    expr: ((sum(rate(envoy_cluster_upstream_rq{nf='sepp',envoy_response_code=~'2.*|429',envoy_cluster_name='global_rate_limit'}[45s])) or vector(0))/(sum(rate(envoy_cluster_upstream_rq_total{nf='sepp',envoy_cluster_name='global_rate_limit'}[45s])))) * 100
  - record: sepp_ingress_global_rate_limit_success_ratio_internal_pmbr
    expr: ((sum(rate(envoy_cluster_upstream_rq{nf='sepp',envoy_response_code=~'2.*|429',envoy_cluster_name='global_rate_limit'}[45s])) or vector(0))/(sum(rate(envoy_cluster_upstream_rq_total{nf='sepp',envoy_cluster_name='global_rate_limit'}[45s])))) * 100 > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_licensed_traffic
    expr: sum(rate(envoy_upstream_rq_total{nf='sepp'}[60s])*2) + sum(rate(envoy_downstream_rq_total{nf='sepp'}[60s])*2)
  - record: sepp_request_overall_time
    expr: rate(envoy_downstream_rq_time_sum{nf='sepp'}[45s])/rate(envoy_downstream_rq_time_count{nf='sepp'}[45s])
  - record: sepp_request_process_time
    expr: rate(envoy_downstream_rq_time_sum{nf='sepp'}[45s])/rate(envoy_downstream_rq_time_count{nf='sepp'}[45s]) - on (instance)(avg by(instance)(rate(envoy_upstream_rq_time_sum{nf='sepp'}[45s]))/avg by(instance)(rate(envoy_upstream_rq_time_count{nf='sepp'}[45s])))
  - record: sepp_request_process_time_pmbr
    expr: rate(envoy_downstream_rq_time_sum{nf='sepp'}[45s])/rate(envoy_downstream_rq_time_count{nf='sepp'}[45s]) - on (instance)(avg by(instance)(rate(envoy_upstream_rq_time_sum{nf='sepp'}[45s]))/avg by(instance)(rate(envoy_upstream_rq_time_count{nf='sepp'}[45s]))) > 0 or clamp_max(absent(notExists{nf='sepp'}),0)

  - record: sepp_tapcollector_in_tap_frames_rate
    expr: rate(tapcollector_in_tap_frames_total{nf='sepp'}[90s])
  - record: sepp_tapcollector_in_tap_frames_total_rate
    expr: sum(sepp_tapcollector_in_tap_frames_rate)
  - record: sepp_tapcollector_out_tap_frames_success_rate
    expr: rate(tapcollector_out_tap_frames_success_total{nf='sepp'}[90s])
  - record: sepp_tapcollector_out_tap_frames_sent_errors_rate
    expr: rate(tapcollector_out_tap_frames_sent_errors_total{nf='sepp'}[90s])
  - record: sepp_tapcollector_out_tap_frames_dropped_rate
    expr: rate(tapcollector_out_tap_frames_dropped_total{nf='sepp'}[90s])
  - record: sepp_tapcollector_out_client_errors_rate
    expr: rate(tapcollector_out_client_errors{nf='sepp'}[90s])


  - record: sepp_tapcollector_sent_frames_total_rate
    expr: sum({__name__=~"sepp_tapcollector_out_tap_frames_success_rate|sepp_tapcollector_out_tap_frames_sent_errors_rate|sepp_tapcollector_out_tap_frames_dropped_rate|sepp_tapcollector_out_client_errors_rate"})

  - record: sepp_tapcollector_recv_frames_failure_ratio
    expr: (sum(rate(tapcollector_in_server_error_total{nf='sepp'}[90s]) or on() vector(0) )/ sepp_tapcollector_in_tap_frames_total_rate) * 100
  - record: sepp_tapcollector_sent_frames_failure_ratio
    expr: (sum({__name__=~"sepp_tapcollector_out_tap_frames_sent_errors_rate|sepp_tapcollector_out_tap_frames_dropped_rate|sepp_tapcollector_out_client_errors_rate"})/sepp_tapcollector_sent_frames_total_rate) * 100
  - record: sepp_tapcollector_out_tap_frames_dropped_ratio
    expr: ((sum(sepp_tapcollector_out_tap_frames_dropped_rate) / sepp_tapcollector_in_tap_frames_total_rate) * 100 > 0) or vector(0)

  - record: sepp_envoy_vtap_segments_dropped_rate
    expr: rate(envoy_segments_dropped{nf='sepp'}[90s])
  - record: sepp_envoy_vtap_segments_size_too_big_rate
    expr: rate(envoy_segments_size_too_big{nf='sepp'}[90s])
  - record: sepp_envoy_vtap_segments_size_too_big_rate
    expr: rate(envoy_segments_size_too_big{nf='sepp'}[90s])
  - record: sepp_envoy_vtap_segments_size_too_big_rate
    expr: rate(envoy_segments_size_too_big{nf='sepp'}[90s])
  - record: sepp_envoy_vtap_segments_tapped_rate
    expr: rate(envoy_segments_tapped{nf='sepp'}[90s])
  - record: sepp_envoy_ingress_vtap_segments_tapped_rate
    expr: sum(rate(envoy_segments_tapped{nf='sepp',group='ingress-vtap'}[90s]))
  - record: sepp_envoy_egress_vtap_segments_tapped_rate
    expr: sum(rate(envoy_segments_tapped{nf='sepp',group='egress-vtap'}[90s]))

  - record: sepp_envoy_segment_tapped_failure_ratio
    expr: (sum({__name__=~"sepp_envoy_vtap_segments_dropped_rate|sepp_envoy_vtap_segments_size_too_big_rate"})/sum({__name__=~"sepp_envoy_vtap_segments_tapped_rate|sepp_envoy_vtap_segments_dropped_rate|sepp_envoy_vtap_segments_size_too_big_rate"})) * 100
  - record: sepp_envoy_segments_dropped_ratio
    expr: (sepp_envoy_segment_tapped_failure_ratio > 0) or vector(0)
{{- end }}
