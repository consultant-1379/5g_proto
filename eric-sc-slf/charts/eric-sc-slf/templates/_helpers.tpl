{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}

{{- define "eric-sc-slf.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{ define "eric-slf.global" }}
  {{- $globalDefaults := dict "security" (dict "tls" (dict "enabled" true)) -}}
  {{- $globalDefaults := merge $globalDefaults (dict "timezone" "UTC") -}}
  {{- $globalDefaults := merge $globalDefaults (dict "log" (dict "outputs" (list "stdout"))) -}}
  {{ if .Values.global }}
    {{- mergeOverwrite $globalDefaults .Values.global | toJson -}}
  {{ else }}
    {{- $globalDefaults | toJson -}}
  {{ end }}
{{ end }}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sc-slf.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Define TLS, note: returns boolean as string
*/}}
{{- define "eric-sc-slf.tls" -}}
{{- $slftls := true -}}
{{- if .Values.global -}}
    {{- if .Values.global.security -}}
        {{- if .Values.global.security.tls -}}
            {{- if hasKey .Values.global.security.tls "enabled" -}}
                {{- $slftls = .Values.global.security.tls.enabled -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $slftls -}}
{{- end -}}

{{/*
Define dcedsc TLS for slf worker, note: returns boolean as string
*/}}
{{- define "eric-sc-slf.dcedsc.tls" -}}
{{- $dcedsctls := true -}}
{{- if .Values.service.slf.client.interfaces.dcedsc.tls.enabled -}}
    {{- $dcedsctls = .Values.service.slf.client.interfaces.dcedsc.tls.enabled -}}
{{- else -}}
    {{- if .Values.global -}}
        {{- if .Values.global.security -}}
            {{- if .Values.global.security.tls -}}
                {{- if hasKey .Values.global.security.tls "enabled" -}}
                    {{- $dcedsctls = .Values.global.security.tls.enabled -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $dcedsctls -}}
{{- end -}}

{{/*
Create slf container image registry url
*/}}
{{- define "eric-sc-slf.slf.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.slf.registry -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.slf -}}
        {{- if .Values.imageCredentials.slf.registry -}}
            {{- if .Values.imageCredentials.slf.registry.url -}}
                {{- $url = .Values.imageCredentials.slf.registry.url -}}
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
Create tapagent container image registry url
*/}}
{{- define "eric-sc-slf.tapagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tapagent.registry -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.tapagent -}}
        {{- if .Values.imageCredentials.tapagent.registry -}}
            {{- if .Values.imageCredentials.tapagent.registry.url -}}
                {{- $url = .Values.imageCredentials.tapagent.registry.url -}}
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
Create tlskeylogagent container image registry url
*/}}
{{- define "eric-sc-slf.tlskeylogagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tlskeylogagent.registry -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.tlskeylogagent -}}
        {{- if .Values.imageCredentials.tlskeylogagent.registry -}}
            {{- if .Values.imageCredentials.tlskeylogagent.registry.url -}}
                {{- $url = .Values.imageCredentials.tlskeylogagent.registry.url -}}
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
Create image pull policy slf container
*/}}
{{- define "eric-sc-slf.slf.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((((.Values.imageCredentials).slf).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.imageCredentials.slf.registry.imagePullPolicy -}}
{{- else if (((.Values.global).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy tapagent container
*/}}
{{- define "eric-sc-slf.tapagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((((.Values.imageCredentials).tapagent).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.imageCredentials.tapagent.registry.imagePullPolicy -}}
{{- else if (((.Values.global).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy tlskeylogagent container
*/}}
{{- define "eric-sc-slf.tlskeylogagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((((.Values.imageCredentials).tlskeylogagent).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.imageCredentials.tlskeylogagent.registry.imagePullPolicy -}}
{{- else if (((.Values.global).registry).imagePullPolicy) -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-sc-slf.pullSecrets" -}}
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
{{- define "eric-sc-slf.nodeSelector" -}}
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
    Define tapagent enabled
*/}}
{{- define "eric-sc-slf.tapagent.enabled" -}}
{{- if  .Values.tapagent -}}
    {{- if  .Values.tapagent.enabled -}}
        {{- .Values.tapagent.enabled | toString -}}
    {{- else -}}
        {{- "false" -}}
    {{- end -}}
{{- else -}}
    {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-sc-slf.ipfamilies" -}}
{{- if .Release.IsInstall }}
{{- if .Values.global }}
  {{- if .Values.global.internalIPFamily }}
ipFamilies: [{{ .Values.global.internalIPFamily | quote }}]
  {{- end }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-sc-slf.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ .Chart.AppVersion | quote }}
ericsson.com/nf-name: "SCP"
{{- end}}

{{ define "eric-sc-slf.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}

{{- define "eric-sc-slf.labels" -}}
{{- include "eric-sc-slf.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

{{/*
Generate labels helper function
*/}}
{{- define "eric-sc-slf.generate-peer-labels" -}}
{{- $peers := index . "peers" -}}
{{- $peerLabels := dict }}
{{- range $_, $peer := $peers }}
    {{- $_ := set $peerLabels ((list $peer "access") | join "-") "true" -}}
{{- end }}
{{- toYaml $peerLabels }}
{{- end -}}

{{- define "eric-sc-slf.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-sc-slf.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-sc-slf.labels" . | fromYaml -}}
{{- include "eric-sc-slf.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies
*/}}
{{- define "eric-sc-slf.peer.labels" -}}
{{- $peers := list }}
{{- $peers = append $peers .Values.sc.common.monitor.hostname }}
{{- $peers = append $peers .Values.sc.common.etcd.hostname }}
{{- $peers = append $peers .Values.adp.cm.mediator.hostname }}
{{- $peers = append $peers .Values.adp.pm.server.hostname }}
{{- $peers = append $peers .Values.adp.fh.alarmHandler.hostname }}
{{- $peers = append $peers .Values.adp.log.transformer.hostname }}
{{- template "eric-sc-slf.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}

{{- define "eric-sc-slf.slf.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.slf.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.slf -}}
        {{- if .Values.imageCredentials.slf.repoPath }}
            {{- $repoPath = .Values.imageCredentials.slf.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sc-slf.tapagent.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.tapagent.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.tapagent -}}
        {{- if .Values.imageCredentials.tapagent.repoPath }}
            {{- $repoPath = .Values.imageCredentials.tapagent.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sc-slf.tlskeylogagent.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.tlskeylogagent.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.tlskeylogagent -}}
        {{- if .Values.imageCredentials.tlskeylogagent.repoPath }}
            {{- $repoPath = .Values.imageCredentials.tlskeylogagent.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sc-slf.slf.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.slf.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-slf.tapagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-slf.tlskeylogagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tlskeylogagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-slf.slf.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.slf.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-slf.tapagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapagent.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sc-slf.tlskeylogagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tlskeylogagent.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-sc-slf.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
Return the log streaming type, default is indirect
*/}}
{{- define "eric-sc-slf.streamingMethod" -}}
{{ $streamingMethod := "indirect"}}
{{- if (.Values.log).streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
{{- else if ((.Values.global).log).streamingMethod -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
{{- end -}}
{{- if not (has $streamingMethod (list "indirect" "direct" "dual")) -}}
    {{- fail "Incorrect value for streamingMethod in either global or local-SLF values.yaml. Possible values: indirect, direct or dual" -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define LOGBACK file to be used, note: returns logback xml file name
*/}}
{{- define "eric-sc-slf.logbackFileName" -}}
{{- $streamingMethod := include "eric-sc-slf.streamingMethod" . -}}
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
{{- define "eric-sc-slf.logshipper-enabled" -}}
{{- $streamingMethod := include "eric-sc-slf.streamingMethod" . -}}
{{- $enabled := "false" -}}
{{- if or (eq $streamingMethod "direct") (eq $streamingMethod "dual") -}}
    {{- $enabled = "true" -}}
{{- end -}}
{{- print $enabled -}}
{{- end -}}
