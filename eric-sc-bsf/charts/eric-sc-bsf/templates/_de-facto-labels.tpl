{{- define "eric-sc-bsf.de-facto-labels" }}
app.kubernetes.io/name: {{ .Chart.Name | quote }}
app.kubernetes.io/version: {{ template "eric-sc-bsf.version" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end}}