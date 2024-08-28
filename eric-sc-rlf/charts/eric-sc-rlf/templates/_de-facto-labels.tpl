{{- define "eric-sc-rlf.de-facto-labels" }}
app.kubernetes.io/name: {{ .Chart.Name | quote }}
app.kubernetes.io/version: {{ template "eric-sc-rlf.version" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end}}
