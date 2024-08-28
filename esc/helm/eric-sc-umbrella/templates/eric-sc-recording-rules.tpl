{{- define "eric-sc.recording-rules" }}
groups:
- name: sc_cpu_metrics
  rules:
  # cpu usage per pod in millicores
  - record: instance_job:pod_cpu_usage
    expr: 1000 * sum(rate(container_cpu_usage_seconds_total{namespace="{{ .Release.Namespace }}",container!=""}[1m])) by (pod)
    labels:
      type: "resource-usage"

  # cpu usage percentage per pod
  - record: job:pod_cpu_usage_100
    expr: 100 * (sum by (pod) (rate(container_cpu_usage_seconds_total{container!="",namespace="{{ .Release.Namespace }}"}[1m])) / sum by (pod) ((container_spec_cpu_quota{container!="",namespace="{{ .Release.Namespace }}"} / container_spec_cpu_period{container!="",namespace="{{ .Release.Namespace }}"})))

  # cpu usage per container, pod
  - record: instance_job:container_cpu_usage_total
    expr: sum(rate(container_cpu_usage_seconds_total{namespace="{{ .Release.Namespace }}",container!=""}[1m])) by (container, pod)
    labels:
      type: "resource-usage"

  # cpu requests per container, pod
  - record: instance_job:container_cpu_requests_total
    expr: sum(container_spec_cpu_shares{namespace="{{ .Release.Namespace }}",container!=""}/1000) by (container, pod)
    labels:
      type: "resource-usage"

  # cpu limits per container, pod
  - record: instance_job:container_cpu_limits_total
    expr: sum((container_spec_cpu_quota{namespace="{{ .Release.Namespace }}",container!=""}/container_spec_cpu_period{namespace="{{ .Release.Namespace }}",container!=""})) by (container, pod)
    labels:
      type: "resource-usage"

  # cpu usage percentage per container, pod
  - record: job:container_cpu_usage_100
    expr: 100 * (sum(rate(container_cpu_usage_seconds_total{namespace="{{ .Release.Namespace }}",container!=""}[1m])) by (container, pod) /sum((container_spec_cpu_quota{namespace="{{ .Release.Namespace }}",container!=""}/container_spec_cpu_period{namespace="{{ .Release.Namespace }}",container!=""})) by (container, pod))

- name: sc_memory_metrics
  rules:

  # memory usage per pod
  - record: instance_job:pod_memory_usage
    expr: sum(0.000001 * container_memory_working_set_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (pod)
    labels:
      unit: "Mi"
      type: "resource-usage"

  # memory usage percentage per pod
  - record: job:pod_memory_usage_100
    expr: sum without (unit) (100 * (sum(0.000001 * container_memory_working_set_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (pod) / sum(0.000001 * container_spec_memory_limit_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (pod)))

  # memory usage per container, pod
  - record: instance_job:container_memory_usage
    expr: sum(0.000001 * container_memory_working_set_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (container, pod)
    labels:
      unit: "Mi"
      type: "resource-usage"

  # memory limits per container, pod
  - record: instance_job:container_memory_limits
    expr: sum(0.000001 * container_spec_memory_limit_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (container, pod)
    labels:
      unit: "Mi"
      type: "resource-usage"

  # memory usage percentage per container, pod
  - record: job:container_memory_usage_100
    expr: 100 * (sum(0.000001 * container_memory_working_set_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (container, pod) /sum(0.000001 * container_spec_memory_limit_bytes{namespace="{{ .Release.Namespace }}",container!=""}) by (container, pod))
{{- end }}
