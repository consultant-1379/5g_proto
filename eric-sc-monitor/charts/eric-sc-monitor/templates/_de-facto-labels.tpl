{{- define "eric-sc-monitor.de-facto-labels" }}
app.kubernetes.io/name: {{ template "eric-sc-monitor.name" . }}
app.kubernetes.io/version: {{ template "eric-sc-monitor.version" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end}}