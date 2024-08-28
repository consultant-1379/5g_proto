/* Copyright 2021 Ericsson GmbH */

{{/* vim: set filetype=mustache: */}}

{{/*
Sanitized application name from Chart.yaml.
*/}}
{{- define "eric-dsc-load.name" -}}
    {{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Sanitized application name from Chart.yaml.
*/}}
{{- define "eric-dsc-load.version" -}}
    {{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Pod-specific environment parameters.
*/}}
{{- define "eric-dsc-load.pod.envs" -}}
- name: NAMESPACE
  valueFrom:
    fieldRef:
      fieldPath: metadata.namespace
- name: POD_IP
  valueFrom:
    fieldRef:
      fieldPath: status.podIP
- name: POD_IPS
  valueFrom:
    fieldRef:
      fieldPath: status.podIPs      
- name: POD_NAME
  valueFrom:
    fieldRef:
      fieldPath: metadata.name
{{- end -}}

{{/*
Dsc-load configuration environment parameters.
*/}}
{{- define "eric-dsc-load.configuration.envs" }}
- name: DUALSTACK
  value: {{ .Values.configuration.dualstack | default false | quote }}
- name: SINGLE_PEER_TEMPLATE
  value: {{ .Values.configuration.singlePeer | default false | quote }}
- name: IP_VERSION
  value: {{ index .Values.configuration "ip-version" | default 4 | quote }}
- name: TLS
  value: {{ .Values.configuration.tls.enabled | default false | quote }}
    {{- if (index .Values.configuration "af-diameter-realm") }}
- name: AF_DIAMETER_REALM
  value: {{ index .Values.configuration "af-diameter-realm" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "pcf-diameter-host") }}
- name: PCF_DIAMETER_HOST
  value: {{ index .Values.configuration "pcf-diameter-host" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "pcf-diameter-realm") }}
- name: PCF_DIAMETER_REALM
  value: {{ index .Values.configuration "pcf-diameter-realm" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "diameter-service-ip") }}
- name: DIAMETER_SERVICE_IP
  value: {{ index .Values.configuration "diameter-service-ip" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "diameter-service-tls-port") }}
- name: DIAMETER_SERVICE_TLS_PORT
  value: {{ index .Values.configuration "diameter-service-tls-port" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "diameter-service-port") }}
- name: DIAMETER_SERVICE_PORT
  value: {{ index .Values.configuration "diameter-service-port" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "diameter-service-name") }}
- name: DIAMETER_SERVICE_NAME
  value: {{ index .Values.configuration "diameter-service-name" | quote }}
    {{- end -}}
    {{- if (index .Values.configuration "diameter-tps") }}
- name: DIAMETER_TPS
  value: {{ index .Values.configuration "diameter-tps" | quote }}
    {{- end -}}
{{- end -}}

{{/*
TLS-specific environment parameters.
*/}}
{{- define "eric-dsc-load.tls.envs" -}}
    {{- if .Values.configuration.tls.enabled }}
- name: DSC_SSL_PRIVATE_KEY_FILE
  value: "/root/certificates/key.pem"
- name: DSC_SSL_CERTIFICATE_FILE
  value: "/root/certificates/certificate.pem"
- name: DSC_SSL_TRUSTED_CERTIFICATE_DIR
  value: "/opt/dsc-load/rootCA"
- name: DSC_SSL_PRIVATE_KEY_PASSWORD
  value: "rootroot"
    {{- end -}}
{{- end -}}

{{/*
TLS related volume mounts.
*/}}
{{- define "eric-dsc-load.tls.volumeMounts" -}}
    {{- if .Values.configuration.tls.enabled }}
volumeMounts:
- name: secrets
  mountPath: /root/certificates
  readOnly: true
- name: trustca
  mountPath: /root/certificates/trustCA
  readOnly: true
    {{- end -}}
{{- end -}}

{{/*
TLS related volumes
*/}}
{{- define "eric-dsc-load.tls.volumes" -}}
    {{- if .Values.configuration.tls.enabled }}
volumes:
- name: secrets
  secret:
    secretName: {{ .Values.certificates.certificatesSecret | quote }}
    optional: true
    items:
      - key: {{ .Values.certificates.certificate | quote }}
        path: certificate.pem
      - key: {{ .Values.certificates.key | quote }}
        path: key.pem
- name: trustca
  secret:
    optional: true
    secretName: {{ .Values.certificateAuthorities.certificatesSecret | quote }}
    {{- end -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-dscload.pullSecrets" -}}
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
Create eric-dscload container image registry url
*/}}
{{- define "eric-dscload.registryUrl" -}}
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




