{{- define "eric-scp.de-facto-labels" }}
app.kubernetes.io/name: {{ .Chart.Name | quote }}
app.kubernetes.io/version: {{ template "eric-scp.version" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end}}
