{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-chfsim.ipfamilies" -}}
{{- if .Values.service.internalIPFamily }}
ipFamilies:
    {{- range .Values.service.internalIPFamily }}
        - {{ . }}
    {{- end }}
{{- end }}
{{- end }}

{{- define "eric-chfsim.ipfamily" -}}
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
{{- define "eric-chfsim.pullSecrets" -}}
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
Create chfsim container image registry url
*/}}
{{- define "eric-chfsim.registryUrl" -}}
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

{{- define "eric-chfsim-1.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-1" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-1-mnc-123-mcc-123" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-2.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-2" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-2-mnc-123-mcc-123" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-3.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-3" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-3-mnc-456-mcc-456" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}.

{{- define "eric-chfsim-4.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-4" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-4-mnc-456-mcc-456" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-5.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-5" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-5-mnc-456-mcc-456" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-6.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-6" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-6-mnc-456-mcc-456" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-7.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-7" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-7s-mnc-456-mcc-456" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-8.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-8" -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = "eric-chfsim-8-mnc-456-mcc-456" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-9.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-9" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-10.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-10" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-11.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-11" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim-12.name" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = "eric-chfsim-12" -}}
{{- end -}}
{{- print $name -}}
{{- end -}}

{{- define "eric-chfsim.secretName" -}}
{{- $name := "default" -}}
{{- if eq .Values.chfsim "chfsim" -}}
    {{- $name = .Values.certificates.secret -}}
{{- else if eq .Values.chfsim "chfsim-sepp" -}}
    {{- $name = .Values.certificates.seppSecret -}}
{{- end -}}
{{- print $name -}}
{{- end -}}