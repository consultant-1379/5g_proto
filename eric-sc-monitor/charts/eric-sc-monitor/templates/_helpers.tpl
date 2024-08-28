{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}
{{- define "eric-sc-monitor.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sc-monitor.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Create monitor container image registry url
*/}}
{{- define "eric-sc-monitor.monitor.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.monitor.registry -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.monitor -}}
        {{- if .Values.imageCredentials.monitor.registry -}}
            {{- if .Values.imageCredentials.monitor.registry.url -}}
                {{- $url = .Values.imageCredentials.monitor.registry.url -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- else if .Values.global -}}
    {{- if .Values.global.registry -}}
        {{- if .Values.global.registry.url -}}
            {{- $url = .Values.global.registry.url -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-sc-monitor.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
    {{- if .Values.global.pullSecret -}}
        {{- $pullSecret = .Values.global.pullSecret -}}
    {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.pullSecret -}}
        {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
    {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull policy monitor container
*/}}
{{- define "eric-sc-monitor.monitor.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((((.Values.imageCredentials).monitor).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.imageCredentials.monitor.registry.imagePullPolicy -}}
{{- else if (((.Values.global).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create nodeSelector
*/}}
{{- define "eric-sc-monitor.nodeSelector" -}}
{{- $nodeSelector := dict -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
        {{- $nodeSelector = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector }}
    {{- range $key, $localValue := .Values.nodeSelector -}}
        {{- if hasKey $nodeSelector $key -}}
            {{- $globalValue := index $nodeSelector $key -}}
            {{- if ne $globalValue $localValue -}}
                {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- $nodeSelector = merge $nodeSelector .Values.nodeSelector -}}
{{- end -}}
{{- if $nodeSelector -}}
    {{- toYaml $nodeSelector | indent 8 | trim -}}
{{- end -}}
{{- end -}}


{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-sc-monitor.ipfamilies" -}}
{{- if .Release.IsInstall }}
{{- if .Values.global }}
  {{- if .Values.global.internalIPFamily }}
ipFamilies: [{{ .Values.global.internalIPFamily | quote }}]
  {{- end }}
{{- end }}
{{- end }}
{{- end }}


{{/*
Create TLS enabled.
Default: true
*/}}
{{- define "eric-sc-monitor.tls.enabled" -}}
{{- $tlsEnabled := "true" -}}
{{- if .Values.global -}}
   {{- if .Values.global.security -}}
       {{- if .Values.global.security.tls -}}
           {{- $tlsEnabled = .Values.global.security.tls.enabled -}}
       {{- end -}}
   {{- end -}}
{{- end -}}
{{- print $tlsEnabled -}}
{{- end -}}

{{/*
Define seccompprofile for the entire sc-monitor pod
*/}}
{{- define "eric-sc-monitor.podSeccompProfile" -}}
{{- if and .Values.seccompProfile .Values.seccompProfile.type }}
seccompProfile:
  type: {{ .Values.seccompProfile.type }}
{{- if eq .Values.seccompProfile.type "Localhost" }}
  localhostProfile: {{ .Values.seccompProfile.localhostProfile }}
{{- end }}
{{- end }}
{{- end -}}

{{/*
Define seccompProfile for sc-monitor container
*/}}
{{- define "eric-sc-monitor.monitor.seccompProfile" -}}
{{- if .Values.seccompProfile -}}
{{- if and .Values.seccompProfile.monitor .Values.seccompProfile.monitor.type }}
seccompProfile:
  type: {{ .Values.seccompProfile.monitor.type }}
{{- if eq .Values.seccompProfile.monitor.type "Localhost" }}
  localhostProfile: {{ .Values.seccompProfile.monitor.localhostProfile }}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-sc-monitor.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ .Chart.AppVersion | quote }}
{{- end}}

{{ define "eric-sc-monitor.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}

{{- define "eric-sc-monitor.labels" -}}
{{- include "eric-sc-monitor.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

{{- define "eric-sc-monitor.monitor.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.monitor.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.monitor -}}
        {{- if .Values.imageCredentials.monitor.repoPath }}
            {{- $repoPath = .Values.imageCredentials.monitor.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sc-monitor.monitor.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.monitor.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-monitor.monitor.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.monitor.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-sc-monitor.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
Return the log streaming type, default is indirect
*/}}
{{- define "eric-sc-monitor.streamingMethod" -}}
{{ $streamingMethod := "indirect"}}
{{- if (.Values.log).streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
{{- else if ((.Values.global).log).streamingMethod -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}  
{{- end -}}
{{- if not (has $streamingMethod (list "indirect" "direct" "dual")) -}}
    {{- fail "Incorrect value for streamingMethod in either global or local-sc-monitor values.yaml. Possible values: indirect, direct or dual" -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define LOGBACK file to be used, note: returns logback xml file
*/}}
{{- define "eric-sc-monitor.logbackFileName" -}}
{{- $streamingMethod := include "eric-sc-monitor.streamingMethod" . -}}
{{- $fileName := "logbackInDirect.xml" -}}
{{- if eq "direct" $streamingMethod -}}
    {{- $fileName = "logbackDirect.xml" -}}
{{- else if eq "dual" $streamingMethod -}}
    {{- $fileName = "logback.xml" -}} 
{{- end -}}
{{- print $fileName -}}
{{- end -}}

{{/*
Determines logshipper sidecar deployment, true if log streamingMethod is "direct" or "dual" 
*/}}
{{- define "eric-sc-monitor.logshipper-enabled" -}}
{{- $streamingMethod := include "eric-sc-monitor.streamingMethod" . -}}
{{- $enabled := "false" -}}
{{- if or (eq $streamingMethod "direct") (eq $streamingMethod "dual") -}}
    {{- $enabled = "true" -}}
{{- end -}}
{{- print $enabled -}}
{{- end -}}

{{/*
Generate labels helper function
*/}}
{{- define "eric-sc-monitor.generate-peer-labels" -}}
{{- $peers := index . "peers" -}}
{{- $peerLabels := dict }}
{{- range $_, $peer := $peers }}
    {{- $_ := set $peerLabels ((list $peer "access") | join "-") "true" -}}
{{- end }}
{{- toYaml $peerLabels }}
{{- end -}}

{{- define "eric-sc-monitor.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-sc-monitor.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-sc-monitor.labels" . | fromYaml -}}
{{- include "eric-sc-monitor.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies
*/}}
{{- define "eric-sc-monitor.peer.labels" -}}
{{- $peers := list }}
{{- if eq (include "eric-sc-monitor.logshipper-enabled" . ) "true" }}
    {{- $peers = append $peers .Values.logShipper.output.logTransformer.host }}
{{- end }}
{{- $peers = append $peers .Values.sc.bsf.manager.hostname }}
{{- $peers = append $peers .Values.sc.bsf.worker.hostname }}
{{- $peers = append $peers .Values.sc.scp.manager.hostname }}
{{- $peers = append $peers .Values.sc.sepp.manager.hostname }}
{{- $peers = append $peers .Values.sc.rlf.hostname }}
{{- $peers = append $peers .Values.sc.slf.hostname }}
{{- template "eric-sc-monitor.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}