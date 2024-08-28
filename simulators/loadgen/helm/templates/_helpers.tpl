{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-loadgen.ipfamilies" -}}
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
{{- define "eric-loadgen.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}


{{- define "eric-loadgen.ipfamily" -}}
    {{- if .Values.service.internalIPFamily }}
       {{- first .Values.service.internalIPFamily -}}
    {{- else -}}
       {{- "default" -}}
    {{- end }}
{{- end }}



{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-loadgen.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
        {{- if .Values.global.pullSecret -}}
            {{- $pullSecret = .Values.global.pullSecret -}}
        {{- end -}}
{{- else if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create loadgen container image registry url
*/}}
{{- define "eric-loadgen.registryUrl" -}}
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

{{- define "eric-loadgen.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productRevision | quote }}
{{- end}}

{{ define "eric-loadgen.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}


{{- define "eric-loadgen.labels" -}}
{{- include "eric-loadgen.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}