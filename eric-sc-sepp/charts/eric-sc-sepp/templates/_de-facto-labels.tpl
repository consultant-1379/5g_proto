{{- define "eric-sc-sepp.de-facto-labels" }}
app.kubernetes.io/name: {{ .Chart.Name | quote }}
app.kubernetes.io/version: {{ template "eric-sc-sepp.version" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
{{- end}}