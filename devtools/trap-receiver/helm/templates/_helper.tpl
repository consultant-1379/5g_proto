{{/* vim: set filetype=mustache: */}}

{{/* SNMPv2 alarm provider configuration */}}
{{- define "eric-snmp-trap-receiver.snmpv2-config" -}}
{{- printf "{ \"trapTargets\": [{ \"address\": \"%s\", \"community\": \"%s\" }], \"heartbeatInterval\": %v }" .Values.service.name .Values.trapCredentials.snmpv2.communityName .Values.providerConfiguration.heartbeatInterval | b64enc -}}
{{- end -}}

{{/* SNMPv3 alarm provider configuration */}}
{{- define "eric-snmp-trap-receiver.snmpv3-config" -}}
{{- if eq .Values.trapCredentials.snmpv3.securityLevel "noAuthNoPriv" }}
  {{- printf "{ \"trapTargets\": [{ \"address\": \"%s\", \"user\": \"%s\", \"securityLevel\": \"%s\" }], \"agentEngineId\": \"%v\", \"heartbeatInterval\": %v }" .Values.service.name .Values.trapCredentials.snmpv3.user .Values.trapCredentials.snmpv3.securityLevel .Values.trapCredentials.snmpv3.engineId .Values.providerConfiguration.heartbeatInterval | b64enc -}}
{{- end }}
{{- if eq .Values.trapCredentials.snmpv3.securityLevel "authNoPriv" }}
  {{- printf "{ \"trapTargets\": [{ \"address\": \"%s\", \"user\": \"%s\", \"securityLevel\": \"%s\", \"authPassphrase\": \"%s\", \"authProtocol\": \"%s\" }], \"agentEngineId\": \"%v\", \"heartbeatInterval\": %v }" .Values.service.name .Values.trapCredentials.snmpv3.user .Values.trapCredentials.snmpv3.securityLevel .Values.trapCredentials.snmpv3.authPassphrase .Values.trapCredentials.snmpv3.authProtocol .Values.trapCredentials.snmpv3.engineId .Values.providerConfiguration.heartbeatInterval | b64enc -}}
{{- end }}
{{- if eq .Values.trapCredentials.snmpv3.securityLevel "authPriv" }}
  {{- printf "{ \"trapTargets\": [{ \"address\": \"%s\", \"user\": \"%s\", \"securityLevel\": \"%s\", \"authPassphrase\": \"%s\", \"authProtocol\": \"%s\", \"privPassphrase\": \"%s\", \"privProtocol\": \"%s\" }], \"agentEngineId\": \"%v\", \"heartbeatInterval\": %v }" .Values.service.name .Values.trapCredentials.snmpv3.user .Values.trapCredentials.snmpv3.securityLevel .Values.trapCredentials.snmpv3.authPassphrase .Values.trapCredentials.snmpv3.authProtocol .Values.trapCredentials.snmpv3.privPassphrase .Values.trapCredentials.snmpv3.privProtocol .Values.trapCredentials.snmpv3.engineId .Values.providerConfiguration.heartbeatInterval | b64enc -}}
{{- end }}
{{- end -}}
