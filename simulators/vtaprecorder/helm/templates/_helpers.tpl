{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-vtaprecorder.ipfamilies" -}}
{{- if .Values.service.internalIPFamily }}
ipFamilies:
    {{- range .Values.service.internalIPFamily }}
        - {{ . }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-vtaprecorder.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-vtaprecorder.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
        {{- if .Values.global.pullSecret -}}
            {{- $pullSecret = .Values.global.pullSecret -}}
        {{- end -}}
{{- else if .Values.imageCredentials.registry.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.registry.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create vtaprecorder container image registry url
*/}}
{{- define "eric-vtaprecorder.registryUrl" -}}
{{- $url := "" -}}
{{- if .Values.global -}}
    {{- if .Values.global.registry -}}
        {{- if .Values.global.registry.url -}}
            {{- $url = .Values.global.registry.url -}}
        {{- end -}}
    {{- end -}}
{{- else  if .Values.imageCredentials.registry.url -}}
    {{- $url = .Values.imageCredentials.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{ define "eric-vtaprecorder.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}


{{- define "eric-vtaprecorder.labels" -}}
{{- include "eric-vtaprecorder.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}