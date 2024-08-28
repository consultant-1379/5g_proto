{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}
{{- define "eric-sc-rlf.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sc-rlf.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Define TLS, note: returns boolean as string
*/}}
{{- define "eric-sc-rlf.tls" -}}
{{- $rlftls := true -}}
{{- if .Values.global -}}
    {{- if .Values.global.security -}}
        {{- if .Values.global.security.tls -}}
            {{- if hasKey .Values.global.security.tls "enabled" -}}
                {{- $rlftls = .Values.global.security.tls.enabled -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $rlftls -}}
{{- end -}}

{{/*
Define dcedsc TLS for rlf worker, note: returns boolean as string
*/}}
{{- define "eric-sc-rlf.dcedsc.tls" -}}
{{- $dcedsctls := true -}}
{{- if ((((((.Values.service).rlf).client).interfaces).dcedsc).tls).enabled -}}
    {{- $dcedsctls = .Values.service.rlf.client.interfaces.dcedsc.tls.enabled -}}
{{- else if (((.Values.global).security).tls).enabled -}}
    {{- $dcedsctls = .Values.global.security.tls.enabled -}}
{{- end -}}
{{- $dcedsctls -}}
{{- end -}}

{{/*
Create rlf container image registry url
*/}}
{{- define "eric-sc-rlf.rlf.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.rlf.registry -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.rlf -}}
        {{- if .Values.imageCredentials.rlf.registry -}}
            {{- if .Values.imageCredentials.rlf.registry.url -}}
                {{- $url = .Values.imageCredentials.rlf.registry.url -}}
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
Create image pull policy rlf container
*/}}
{{- define "eric-sc-rlf.rlf.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((((.Values.imageCredentials).rlf).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.imageCredentials.rlf.registry.imagePullPolicy -}}
{{- else if (((.Values.global).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-sc-rlf.pullSecrets" -}}
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
Create nodeSelector
*/}}
{{- define "eric-sc-rlf.nodeSelector" -}}
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

{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-sc-rlf.ipfamilies" -}}
{{- if .Release.IsInstall }}
{{- if .Values.global }}
  {{- if .Values.global.internalIPFamily }}
ipFamilies: [{{ .Values.global.internalIPFamily | quote }}]
  {{- end }}
{{- end }}
{{- end }}
{{- end }}


{{- define "eric-sc-rlf.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ .Chart.AppVersion | quote }}
{{- end}}

{{ define "eric-sc-rlf.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}

{{- define "eric-sc-rlf.labels" -}}
{{- include "eric-sc-rlf.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

{{/*
Generate labels helper function
*/}}
{{- define "eric-sc-rlf.generate-peer-labels" -}}
{{- $peers := index . "peers" -}}
{{- $peerLabels := dict }}
{{- range $_, $peer := $peers }}
    {{- $_ := set $peerLabels ((list $peer "access") | join "-") "true" -}}
{{- end }}
{{- toYaml $peerLabels }}
{{- end -}}

{{- define "eric-sc-rlf.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-sc-rlf.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-sc-rlf.labels" . | fromYaml -}}
{{- include "eric-sc-rlf.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies
*/}}
{{- define "eric-sc-rlf.peer.labels" -}}
{{- $peers := list }}
{{- $peers = append $peers .Values.sc.common.monitor.hostname }}
{{- $peers = append $peers .Values.adp.cm.mediator.hostname }}
{{- $peers = append $peers .Values.sc.common.etcd.hostname }}
{{- $peers = append $peers .Values.adp.log.transformer.hostname }}
{{- template "eric-sc-rlf.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}

{{- define "eric-sc-rlf.rlf.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.rlf.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.rlf -}}
        {{- if .Values.imageCredentials.rlf.repoPath }}
            {{- $repoPath = .Values.imageCredentials.rlf.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sc-rlf.rlf.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.rlf.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-rlf.rlf.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.rlf.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-sc-rlf.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
Return the log streaming type, default is indirect
*/}}
{{- define "eric-rlf.streamingMethod" -}}
{{ $streamingMethod := "indirect"}}
{{- if (.Values.log).streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
{{- else if ((.Values.global).log).streamingMethod -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
{{- end -}}
{{- if not (has $streamingMethod (list "indirect" "direct" "dual")) -}}
    {{- fail "Incorrect value for streamingMethod in either global or local-RLF values.yaml. Possible values: indirect, direct or dual" -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define LOGBACK file to be used, note: returns logback xml file
*/}}
{{- define "eric-rlf.logbackFileName" -}}
{{- $streamingMethod := include "eric-rlf.streamingMethod" . -}}
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
{{- define "eric-rlf.logshipper-enabled" -}}
{{- $streamingMethod := include "eric-rlf.streamingMethod" . -}}
{{- $enabled := "false" -}}
{{- if or (eq $streamingMethod "direct") (eq $streamingMethod "dual") -}}
    {{- $enabled = "true" -}}
{{- end -}}
{{- print $enabled -}}
{{- end -}}
