{{/*
Template of Log Shipper sidecar
Version: SIDECAR_VERSION
Optional:
  - customLogshipperResourceName
  - customLogshipperAnnotations
*/}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper-tls-cert-lt-client" -}}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
{{- if ne "false" (((((.Values).global).security).tls).enabled | toString) }}
apiVersion: siptls.sec.ericsson.com/v1
kind: InternalCertificate
metadata:
  name: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tls.secretname" . }}
  labels: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-labels" . | nindent 4 }}
  annotations:
  {{- $customAnn := dict -}}
  {{- if .customLogshipperAnnotations -}}
    {{- range $key, $value := .customLogshipperAnnotations -}}
      {{- $_ := set $customAnn $key $value -}}
    {{- end -}}
  {{- end -}}
  {{- $baseAnn := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-annotations" . | fromYaml -}}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-merge-annotations" (dict "location" .Template.Name "sources" (list $customAnn $baseAnn)) | trim | nindent 4 }}
spec:
  kubernetes:
    generatedSecretName: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tls.secretname" . }}
    certificateName: clicert.pem
    privateKeyName: cliprivkey.pem
  certificate:
    subject:
      cn: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}-log-shipper-sidecar
    subjectAlternativeName:
      dns:
        - "*.{{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}-log-shipper-sidecar.{{ .Release.Namespace }}.svc.{{ default "cluster.local" .Values.clusterDomain }}"
    issuer:
      reference: "{{ $default.logShipper.output.logTransformer.host }}-input-ca-cert"
    {{- if (((.Values).logShipper).internal).tlsTtl }}
    validity:
      overrideTtl: {{ .Values.logShipper.internal.tlsTtl }}
    {{- end }}
    extendedKeyUsage:
      tlsClientAuth: true
      tlsServerAuth: false
{{- end }}
{{- end -}}

{{/*
Logshipper sidecar configmap for hook
Inputs:
  - root
  - customLogshipperAnnotations
*/}}
{{- define "eric-stm-diameter-proxy-grpc.logshipper-tls-cert-lt-client-for-hooks" -}}
  {{- $copied := deepCopy .root -}}
  {{- $merged := (mergeOverwrite $copied (dict "customLogshipperAnnotations" .customLogshipperAnnotations)) -}}

  {{- $name := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname-for-hooks" .root -}}
  {{- $merged := (mergeOverwrite $merged (dict "customLogshipperResourceName" $name)) -}}
  {{- include "eric-stm-diameter-proxy-grpc.logshipper-tls-cert-lt-client" $merged }}
{{- end -}}
