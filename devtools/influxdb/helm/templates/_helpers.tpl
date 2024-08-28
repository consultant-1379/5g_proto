{{- define "eric-influxdb.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{- define "eric-influxdb.tls" -}}
{{- if .Values.service.tls.enabled | quote -}}
{{- .Values.service.tls.enabled -}}
{{- else -}}
{{- if .Values.global -}}
{{- if .Values.global.tls -}}
{{- if .Values.global.tls.enabled | quote -}}
{{- .Values.global.tls.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-influxdb.cr" -}}
{{- if .Values.service -}}
{{- if .Values.service.tls -}}
{{- if .Values.service.tls.useCr -}}
{{- if .Values.service.tls.useCr.enabled | quote -}}
{{- .Values.service.tls.useCr.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}