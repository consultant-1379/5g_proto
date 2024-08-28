{{/* vim: set filetype=mustache: */}}

{{/*
Create unique name for PM Server cluster monitoring RBAC resources
*/}}
{{- define "eric-sc.cluster-monitoring" -}}
    {{- printf "eric-pm-server-cluster-monitoring-%s" .Release.Namespace | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create unique name for diameter RBAC resources
*/}}
{{- define "eric-sc.diameter-rbac" -}}
    {{- printf "eric-stm-diameter-rbac-%s" .Release.Namespace | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sc.version" -}}
    {{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
    Define tls client verification enabled
*/}}
{{- define "eric-sc.tlsClientValidation.enabled" -}}
{{- if index .Values "eric-sc" -}}
    {{- if index .Values "eric-sc" "routes" -}}
        {{- if index .Values "eric-sc" "routes" "nbi" -}}
            {{- if index .Values "eric-sc" "routes" "nbi" "security" -}}
                {{- if index .Values "eric-sc" "routes" "nbi" "security" "tls" -}}
                    {{- if index .Values "eric-sc" "routes" "nbi" "security" "tls" "clientValidation" -}}
                        {{- if eq (index .Values "eric-sc" "routes" "nbi" "security" "tls" "clientValidation" "enabled" | quote) "\"false\"" -}}
                            false
                        {{- else -}}
                            true
                        {{- end -}}
                    {{- else -}}
                        false
                    {{- end -}}
                {{- else -}}
                    false
                {{- end -}}
            {{- else -}}
                false
            {{- end -}}
        {{- else -}}
            false
        {{- end -}}
    {{- else -}}
        false
    {{- end -}}
{{- else -}}
    false
{{- end -}}
{{- end -}}

{{/*
    Define filecollector enabled
*/}}
{{- define "eric-sc.fileCollector.enabled" -}}
{{- if  .Values.global -}}
    {{- if  .Values.global.ericsson -}}
        {{- if  .Values.global.ericsson.sc -}}
            {{- if  .Values.global.ericsson.sc.fileCollector -}}
                {{- .Values.global.ericsson.sc.fileCollector.enabled | toString -}}
            {{- else -}}
                {{- "false" -}}
            {{- end -}}
        {{- else -}}
            {{- "false" -}}
        {{- end -}}
    {{- else -}}
        {{- "false" -}}
    {{- end -}}
{{- else -}}
    {{- "false" -}}
{{- end -}}
{{- end -}}
