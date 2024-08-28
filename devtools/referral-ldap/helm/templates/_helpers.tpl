{{- define "eric-referral-ldap.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{- define "eric-referral-ldap.tls" -}}
{{- if .Values.tls.enabled | quote -}}
{{- .Values.tls.enabled -}}
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

{{- define "eric-referral-ldap.cr" -}}
{{- if .Values.tls -}}
{{- if .Values.tls.useCr -}}
{{- if .Values.tls.useCr.enabled | quote -}}
{{- .Values.tls.useCr.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}