{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-nrfsim.ipfamilies" -}}
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
{{- define "eric-nrfsim.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}


{{- define "eric-nrfsim.ipfamily" -}}
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
{{- define "eric-nrfsim.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
        {{- if .Values.global.pullSecret -}}
            {{- $pullSecret = .Values.global.pullSecret -}}
        {{- end -}}
{{- else if .Values.imageCredentials.nrfsim.registry.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.nrfsim.registry.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create nrfsim container image registry url
*/}}
{{- define "eric-nrfsim.nrfsim.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.nrfsim.registry -}}
{{- if .Values.imageCredentials.nrfsim.registry.url -}}
    {{- $url = .Values.imageCredentials.nrfsim.registry.url -}}
{{- else if .Values.global -}}
    {{- if .Values.global.registry -}}
        {{- if .Values.global.registry.url -}}
            {{- $url = .Values.global.registry.url -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{- define "eric-nrfsim.nrfsim.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.nrfsim.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.nrfsim -}}
        {{- if .Values.imageCredentials.nrfsim.repoPath }}
            {{- $repoPath = .Values.imageCredentials.nrfsim.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-nrfsim.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productRevision | quote }}
{{- end}}

{{ define "eric-nrfsim.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}

{{- define "eric-nrfsim.labels" -}}
{{- include "eric-nrfsim.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

{{- define "eric-nrfsim.nrfsim.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.nrfsim.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-nrfsim.nrfsim.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.nrfsim.tag -}}
{{- print $image -}}
{{- end -}}
