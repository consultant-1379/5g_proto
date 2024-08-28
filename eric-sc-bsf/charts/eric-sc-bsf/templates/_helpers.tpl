{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}
{{- define "eric-sc-bsf.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-sc-bsf.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sc-bsf.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Define Labels
*/}}
{{- define "eric-sc-bsf.labels" -}}
{{- include "eric-sc-bsf.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end }}
{{- end -}}

{{- define "eric-sc-bsf.product-info" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}" | quote }}
{{- end}}

{{/*
Common annotations added to all resources
*/}}
{{- define "eric-sc-bsf.common-annotations" }}
{{- template "eric-sc-bsf.product-info" . }}
{{- if .Values.annotations }}
{{ toYaml .Values.annotations }}
{{- end }}
{{- end }}


{{/*
Create WCDBCD hostname.
*/}}
{{- define "eric-sc-bsf.wcdbcd.hostname" -}}
{{- $wcdbcdHostName := "eric-bsf-wcdb-cd" -}}
{{- if (index .Values "eric-bsf-wcdb-cd" "nameOverride") -}}
  {{- $wcdbcdHostName = index .Values "eric-bsf-wcdb-cd" "nameOverride" -}}
{{- else if (index .Values "eric-bsf" "adp" "data" "wcdbcd" "hostname") -}}
  {{- $wcdbcdHostName = index .Values "eric-bsf" "adp" "data" "wcdbcd" "hostname" -}}
{{- end -}}
{{- toYaml $wcdbcdHostName -}}
{{- end -}}

{{/*
Create WCDBCD datacenter.
*/}}
{{- define "eric-sc-bsf.wcdbcd.datacenter" -}}
{{- $datacenter := "datacenter1" -}}
{{- if (index .Values "eric-bsf" "cassandra" "datacenter") -}}
  {{- $datacenter = index .Values "eric-bsf" "cassandra" "datacenter" -}}
{{- end -}}
{{- toYaml $datacenter -}}
{{- end -}}

{{/*
Create WCDBCD rack.
*/}}
{{- define "eric-sc-bsf.wcdbcd.rack" -}}
{{- $rack := "rack1" -}}
{{- toYaml $rack -}}
{{- end -}}
