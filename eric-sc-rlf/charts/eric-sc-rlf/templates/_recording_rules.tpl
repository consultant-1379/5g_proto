{{- define "eric-sc-rlf.recording-rules" }}
groups:
- name: rlf_metrics
  rules:
  - record: rlf_cpu
    expr: avg(rate(container_cpu_usage_seconds_total{container='eric-sc-rlf',namespace='{{ .Release.Namespace }}'}[3m]))*1000

  - record: rlf_cpu_load
    expr: avg(job:container_cpu_usage_100{container='eric-sc-rlf',pod=~'eric-sc-rlf-.+'})

  - record: rlf_memory
    expr: avg(container_memory_working_set_bytes{container='eric-sc-rlf',namespace='{{ .Release.Namespace }}'})/1024/1024

  - record: rlf_memory_usage
    expr: avg(job:container_memory_usage_100{container='eric-sc-rlf',pod=~'eric-sc-rlf-.+'})

{{- end }}
