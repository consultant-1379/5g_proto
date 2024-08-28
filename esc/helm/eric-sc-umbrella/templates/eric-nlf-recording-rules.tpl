{{- define "eric-sc.nlf-recording-rules" }}
groups:
- name: nlf_metrics
  rules:
  - record: nlf_cpu
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-sc-nlf',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: nlf_cpu_load
    expr: avg(job:container_cpu_usage_100{container='eric-sc-nlf',pod=~'eric-sc-nlf-.+'})

  - record: nlf_memory
    expr: avg(container_memory_working_set_bytes{container='eric-sc-nlf',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: nlf_memory_usage
    expr: avg(job:container_memory_usage_100{container='eric-sc-nlf',pod=~'eric-sc-nlf-.+'})

  - record: nlf_nrf_discovery_mps
    expr: sum(rate(nrf_out_requests_total{app='eric-sc-nlf', service='nnrf-disc', }[45s]))

  - record: nlf_nrf_discovery_success
    expr: (sum(rate(nrf_in_answers_total{app='eric-sc-nlf', status=~'2.*', service='nnrf-disc'}[45s]))/sum(rate(nrf_out_requests_total{app='eric-sc-nlf',service='nnrf-disc'}[45s]))) * 100

  - record: nlf_nrf_discovery_success_pmbr
    expr: (sum(rate(nrf_in_answers_total{app='eric-sc-nlf', status=~'2.*', service='nnrf-disc'}[45s]))/sum(rate(nrf_out_requests_total{app='eric-sc-nlf',service='nnrf-disc'}[45s]))) * 100 > 0 or on() vector(0)

{{- end }}
