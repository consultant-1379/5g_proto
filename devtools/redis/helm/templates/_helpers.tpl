{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-seppsim.ipfamilies" -}}
{{- if .Values.service.internalIPFamily }}
ipFamilies:
    {{- range .Values.service.internalIPFamily }}
        - {{ . }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-redis.pullSecrets" -}}
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
Create seppsim container image registry url
*/}}
{{- define "eric-redis.registryUrl" -}}
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