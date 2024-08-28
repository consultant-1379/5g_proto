{{- define "eric-sc-slf.recording-rules" }}
groups:
- name: slf_metrics
  interval: 15s
  rules:
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
{{- end }}
