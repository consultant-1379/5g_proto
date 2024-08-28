{{- define "eric-stm-diameter-proxy-grpc.name" -}}
{{- if .Values.nameOverride }}
  {{- .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
  {{- $name := default .Chart.Name -}}
  {{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.product-info" }}
ericsson.com/product-name: "DiameterProxyGrpc"
ericsson.com/product-number: "CXD1010304"
ericsson.com/product-revision: "1.42.0
#START. Addition of NF annotation according to GL-D1121-066"
ericsson.com/nf-name: "BSF"
# STOP. Addition of NF annotation according to GL-D1121-066"
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.aggregatedMerge" -}}
  {{- $merged := dict -}}
  {{- $context := .context -}}
  {{- $location := .location -}}

  {{- range $sourceData := .sources -}}
    {{- range $key, $value := $sourceData -}}

      {{- /* FAIL: when the input is not string. */ -}}
      {{- if not (kindIs "string" $value) -}}
        {{- $problem := printf "Failed to merge keys for \"%s\" in \"%s\": invalid type" $context $location -}}
        {{- $details := printf "in \"%s\": \"%s\"." $key $value -}}
        {{- $reason := printf "The merge function only accepts strings as input." -}}
        {{- $solution := "To proceed, please pass the value as a string and try again." -}}
        {{- printf "%s %s %s %s" $problem $details $reason $solution | fail -}}
      {{- end -}}

      {{- if hasKey $merged $key -}}
        {{- $mergedValue := index $merged $key -}}

        {{- /* FAIL: when there are different values for a key. */ -}}
        {{- if ne $mergedValue $value -}}
          {{- $problem := printf "Failed to merge keys for \"%s\" in \"%s\": key duplication in" $context $location -}}
          {{- $details := printf "\"%s\": (\"%s\", \"%s\")." $key $mergedValue $value -}}
          {{- $reason := printf "The same key cannot have different values." -}}
          {{- $solution := "To proceed, please resolve the conflict and try again." -}}
          {{- printf "%s %s %s %s" $problem $details $reason $solution | fail -}}
        {{- end -}}
      {{- end -}}

      {{- $_ := set $merged $key $value -}}

    {{- end -}}
  {{- end -}}
{{- range $key, $value := $merged }}
{{- if contains "\"" $value }}
{{ $key }}: '{{ $value }}'
{{ else }}
{{ $key }}: {{ $value | quote }}
{{- end }}
{{- end -}}

{{- end -}}

#START
{{/*
Generate labels helper function
*/}}
{{- define "eric-bsf.generate-peer-labels" -}}
{{- $peers := index . "peers" -}}
{{- $peerLabels := dict }}
{{- range $_, $peer := $peers }}
    {{- $_ := set $peerLabels ((list $peer "access") | join "-") "true" -}}
{{- end }}
{{- toYaml $peerLabels }}
{{- end -}}

{{/*
Define labels for Network Policies of bsf-diameter
*/}}
{{- define "eric-bsf-diameter.peer.labels" -}}
{{- $peers := list }}
{{- $peers = append $peers .Values.adp.cm.mediator.hostname }}
{{- $peers = append $peers .Values.adp.data.wcdbcd.hostname }}
{{- $peers = append $peers .Values.sc.common.etcd.hostname }}
{{- template "eric-bsf.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}
#STOP

{{- define "eric-stm-diameter-proxy-grpc.mergeAnnotations" -}}
{{- include "eric-stm-diameter-proxy-grpc.aggregatedMerge" (dict "context" "annotations" "location" .location "sources" .sources) }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.mergeLabels" -}}
{{- include "eric-stm-diameter-proxy-grpc.aggregatedMerge" (dict "context" "labels" "location" .location "sources" .sources) }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.standard-labels" -}}
app.kubernetes.io/name: {{ include "eric-stm-diameter-proxy-grpc.name" . }}
app.kubernetes.io/managed-by: {{ .Release.Service }}
app.kubernetes.io/instance: {{ .Release.Name }}
app.kubernetes.io/version: {{ include "eric-stm-diameter-proxy-grpc.version" . }}
helm.sh/chart: {{ include "eric-stm-diameter-proxy-grpc.chart" . }}
chart: {{ include "eric-stm-diameter-proxy-grpc.chart" . }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.config-labels" }}
  {{- $global := (.Values.global).labels -}}
  {{- $service := .Values.labels -}}
  {{- include "eric-stm-diameter-proxy-grpc.mergeLabels" (dict "location" .Template.Name "sources" (list $global $service)) | trim }}
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.config-annotations" }}
  {{- $global := (.Values.global).annotations -}}
  {{- $service := .Values.annotations -}}
  {{- include "eric-stm-diameter-proxy-grpc.mergeAnnotations" (dict "location" .Template.Name "sources" (list $global $service)) | trim }}
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.labels" -}}
{{- $standard := include "eric-stm-diameter-proxy-grpc.standard-labels" . | fromYaml -}}
  {{- $config := include "eric-stm-diameter-proxy-grpc.config-labels" . | fromYaml -}}
  {{- include "eric-stm-diameter-proxy-grpc.mergeLabels" (dict "location" .Template.Name "sources" (list $standard $config)) | trim }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.annotations" -}}
{{- $productInfo := include "eric-stm-diameter-proxy-grpc.product-info" . | fromYaml -}}
  {{- $config := include "eric-stm-diameter-proxy-grpc.config-annotations" . | fromYaml -}}
  {{- include "eric-stm-diameter-proxy-grpc.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $config)) | trim }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.appArmorAnnotation.getAnnotation" -}}
{{- $profile := index . "profile" -}}
{{- $containerName := index . "containerName" -}}
{{- if $profile.type -}}
{{- if eq "runtime/default" (lower $profile.type) }}
container.apparmor.security.beta.kubernetes.io/{{ $containerName }}: "runtime/default"
{{- else if eq "unconfined" (lower $profile.type) }}
container.apparmor.security.beta.kubernetes.io/{{ $containerName }}: "unconfined"
{{- else if eq "localhost" (lower $profile.type) }}
{{- if $profile.localhostProfile }}
{{- $localhostProfileList := (split "/" $profile.localhostProfile) -}}
{{- if $localhostProfileList._1 }}
container.apparmor.security.beta.kubernetes.io/{{ $containerName }}: "localhost/{{ $localhostProfileList._1 }}"
{{- end }}
{{- end }}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.seccompAnnotation.getAnnotation" -}}
{{- $profile := index . "profile" -}}
{{- if $profile.type -}}
{{- if eq "runtimedefault" (lower $profile.type) }}
seccompProfile:
  type: RuntimeDefault
{{- else if eq "unconfined" (lower $profile.type) }}
seccompProfile:
  type: Unconfined
{{- else if eq "localhost" (lower $profile.type) }}
seccompProfile:
  type: Localhost
  localhostProfile: {{ $profile.localhostProfile }}
{{- end }}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.supplementalGroups" -}}
{{- $globalGroups := (list) -}}
{{- if hasKey .Values "global" }}
  {{- if hasKey .Values.global "podSecurityContext" }}
    {{- if hasKey .Values.global.podSecurityContext "supplementalGroups" }}
      {{- $globalGroups = .Values.global.podSecurityContext.supplementalGroups -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- $localGroups := (list) -}}
{{- if hasKey .Values "podSecurityContext" }}
  {{- if hasKey .Values.podSecurityContext "supplementalGroups" }}
    {{- $localGroups = .Values.podSecurityContext.supplementalGroups -}}
  {{- end -}}
{{- end -}}
{{- $mergeGroups := (list) -}}
{{- if $globalGroups }}
  {{- $mergeGroups = $globalGroups -}}
{{- end -}}
{{- if $localGroups }}
  {{- $mergeGroups =   ( concat $mergeGroups $localGroups | uniq ) -}}
{{- end -}}
{{- if $mergeGroups }}
  {{- toYaml $mergeGroups -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.fsGroup.coordinated" -}}
{{- $fsGroup := 10000 -}}
{{- if .Values.global }}
  {{- if .Values.global.fsGroup }}
    {{- if .Values.global.fsGroup.manual }}
      {{- $fsGroup = .Values.global.fsGroup.manual -}}
    {{- else -}}
      {{- if .Values.global.fsGroup.namespace }}
        {{- $fsGroup =  "# The 'default' defined in the Security Policy will be used." -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- $fsGroup -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" -}}
  {{- $keyValues := list -}}
  {{- range $key := (keys . | sortAlpha) -}}
    {{- if eq $key "effect" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- else if eq $key "key" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- else if eq $key "operator" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- else if eq $key "value" -}}
      {{- $keyValues = append $keyValues (printf "%s=%s" $key (index $ $key)) -}}
    {{- end -}}
  {{- end -}}
  {{- printf "%s" (join "," $keyValues) -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.merge-tolerations" -}}
  {{- if (.root.Values.global).tolerations }}
      {{- $globalTolerations := .root.Values.global.tolerations -}}
      {{- $serviceTolerations := list -}}
      {{- if .root.Values.tolerations -}}
        {{- if eq (typeOf .root.Values.tolerations) ("[]interface {}") -}}
          {{- $serviceTolerations = .root.Values.tolerations -}}
        {{- else if eq (typeOf .root.Values.tolerations) ("map[string]interface {}") -}}
          {{- $serviceTolerations = index .root.Values.tolerations .podbasename -}}
        {{- end -}}
      {{- end -}}
      {{- $result := list -}}
      {{- $nonMatchingItems := list -}}
      {{- $matchingItems := list -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" $globalItem -}}
        {{- range $serviceItem := $serviceTolerations -}}
          {{- $serviceItemId := include "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" $serviceItem -}}
          {{- if eq $serviceItemId $globalItemId -}}
            {{- $matchingItems = append $matchingItems $serviceItem -}}
          {{- end -}}
        {{- end -}}
      {{- end -}}
      {{- range $globalItem := $globalTolerations -}}
        {{- $globalItemId := include "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" $globalItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" $matchItem -}}
          {{- if eq $matchItemId $globalItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $globalItem -}}
        {{- end -}}
      {{- end -}}
      {{- range $serviceItem := $serviceTolerations -}}
        {{- $serviceItemId := include "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" $serviceItem -}}
        {{- $matchCount := 0 -}}
        {{- range $matchItem := $matchingItems -}}
          {{- $matchItemId := include "eric-stm-diameter-proxy-grpc.merge-tolerations.get-idetifier" $matchItem -}}
          {{- if eq $matchItemId $serviceItemId -}}
            {{- $matchCount = add1 $matchCount -}}
          {{- end -}}
        {{- end -}}
        {{- if eq $matchCount 0 -}}
          {{- $nonMatchingItems = append $nonMatchingItems $serviceItem -}}
        {{- end -}}
      {{- end -}}
      {{- toYaml (concat $result $matchingItems $nonMatchingItems) -}}
  {{- else -}}
      {{- if .root.Values.tolerations -}}
        {{- if eq (typeOf .root.Values.tolerations) ("[]interface {}") -}}
          {{- toYaml .root.Values.tolerations -}}
        {{- else if eq (typeOf .root.Values.tolerations) ("map[string]interface {}") -}}
          {{- toYaml (index .root.Values.tolerations .podbasename) -}}
        {{- end -}}
      {{- end -}}
  {{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.siptlsTrustedRootCert" -}}
{{- $rootCert := "eric-sec-sip-tls-trusted-root-cert" -}}
{{- if hasKey .Values "global" }}
  {{- if hasKey .Values.global "security" }}
    {{- if hasKey .Values.global.security "tls" }}
      {{- if hasKey .Values.global.security.tls "trustedInternalRootCa" }}
        {{- if hasKey .Values.global.security.tls.trustedInternalRootCa "secret" }}
          {{- $rootCert = .Values.global.security.tls.trustedInternalRootCa.secret -}}
        {{- end -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- $rootCert -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.should.use.legacy.rb" -}}
{{- $useLegacy := true -}}
{{- if .Values.global }}
  {{- if .Values.global.securityPolicy }}
    {{- if .Values.global.securityPolicy.rolekind }}
      {{- if not (eq ((index .Values "securityPolicy" "eric-stm-diameter-proxy-grpc" "rolename")) "eric-stm-diameter-proxy-grpc") }}
        {{- $useLegacy = false -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- $useLegacy -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.security.policy.rolebinding.name" -}}
{{- $roletag := "c" -}}
{{- if eq .Values.global.securityPolicy.rolekind "Role" }}
  {{- $roletag = "r" -}}
{{- end -}}
{{ printf "%s-%s-%s-%s-sp" (include "eric-stm-diameter-proxy-grpc.name" .) "service-account" $roletag (index .Values "securityPolicy" "eric-stm-diameter-proxy-grpc" "rolename") }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.timezone" -}}
{{- $tz := "UTC" -}}
{{- if hasKey .Values "global" }}
  {{- if hasKey .Values.global "timezone" }}
    {{- $tz = .Values.global.timezone | quote  -}}
  {{- end -}}
{{- end -}}
- name: TZ
  value: {{ $tz }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.tls" -}}
{{- $tlsvalue := true -}}
{{- if hasKey .Values "global" }}
  {{- if hasKey .Values.global "security" }}
    {{- if hasKey .Values.global.security "tls" }}
      {{- if hasKey .Values.global.security.tls "enabled" }}
        {{- $tlsvalue = .Values.global.security.tls.enabled -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- $tlsvalue -}}
{{- end -}}

# START. Changes due to DND-31959
{{- define "eric-stm-diameter-proxy-grpc.dcedsc.tls" -}}
{{- $dcedsctls := true -}}
{{- if .Values.service.diameter.client.dcedsc.tls.enabled -}}
    {{- $dcedsctls = .Values.service.diameter.client.dcedsc.tls.enabled -}}
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
# STOP. Changes due to DND-31959

{{- define "eric-stm-diameter-proxy-grpc.pullSecret" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global }}
  {{- if .Values.global.pullSecret }}
    {{- $pullSecret = .Values.global.pullSecret -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.pullSecret }}
  {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.optionalSecretMount" -}}
{{- $optional := true -}}
{{- if hasKey .Values "optionalSecretMount" }}
  {{- $optional = .Values.optionalSecretMount -}}
{{- end -}}
{{- print $optional -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.env" -}}
{{- if hasKey .Values "initialConfig" }}
  {{- if hasKey .Values.initialConfig "dsl" }}
    {{- if hasKey .Values.initialConfig.dsl "env" }}
      {{- toYaml .Values.initialConfig.dsl.env -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.env" -}}
{{- if hasKey .Values "initialConfig" }}
  {{- if hasKey .Values.initialConfig "diameterproxygrpc" }}
    {{- if hasKey .Values.initialConfig.diameterproxygrpc "env" }}
      {{- toYaml .Values.initialConfig.diameterproxygrpc.env -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hooklauncher.env" -}}
{{- if hasKey .Values "initialConfig" }}
  {{- if hasKey .Values.initialConfig "hooklauncher" }}
    {{- if hasKey .Values.initialConfig.hooklauncher "env" }}
      {{- toYaml .Values.initialConfig.hooklauncher.env -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hooklauncher.registryUrl" -}}
{{- $registryUrl := "armdocker.rnd.ericsson.se" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.url }}
      {{- $registryUrl = .Values.global.registry.url -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.hooklauncher.registry }}
  {{- if .Values.imageCredentials.hooklauncher.registry.url }}
    {{- $registryUrl = .Values.imageCredentials.hooklauncher.registry.url -}}
  {{- end -}}
{{- end -}}
{{- print $registryUrl -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hooklauncher.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := index $productInfo "images" "hooklauncher" "repoPath" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.repoPath }}
      {{- $repoPath = .Values.global.registry.repoPath -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.hooklauncher.repoPath }}
  {{- if .Values.imageCredentials.hooklauncher.repoPath }}
    {{- $repoPath = .Values.imageCredentials.hooklauncher.repoPath -}}
  {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hooklauncher.imageName" -}}
{{- $imageName := "eric-lcm-smart-helm-hooks-hooklauncher" -}}
{{- if .Values.images }}
  {{- if .Values.images.hooklauncher }}
    {{- if .Values.images.hooklauncher.name }}
      {{- $imageName = .Values.images.hooklauncher.name -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageName -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hooklauncher.imageTag" -}}
{{- $imageTag := "2.16.0-20" -}}
{{- if .Values.images }}
  {{- if .Values.images.hooklauncher }}
    {{- if .Values.images.hooklauncher.tag }}
      {{- $imageTag = .Values.images.hooklauncher.tag -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageTag -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hooklauncher.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.imagePullPolicy }}
      {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.hooklauncher.registry }}
  {{- if .Values.imageCredentials.hooklauncher.registry.imagePullPolicy }}
    {{- $imagePullPolicy = .Values.imageCredentials.hooklauncher.registry.imagePullPolicy -}}
  {{- end -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.nodeSelector.hooklauncher" -}}
{{- $selector := dict -}}
{{- $localSelector := .Values.nodeSelector -}}
{{- range $key, $value := $localSelector }}
  {{- if eq (typeOf $value) "map[string]interface {}" }}
    {{- $localSelector = omit $localSelector $key -}}
  {{- end -}}
{{- end }}
{{- if $localSelector }}
  {{- $selector = merge $selector $localSelector -}}
{{- else -}}
  {{- if hasKey .Values.nodeSelector "hooklauncher" }}
    {{- $selector = merge $selector .Values.nodeSelector.hooklauncher -}}
  {{- end -}}
{{- end -}}
{{- if .Values.global }}
  {{- if .Values.global.nodeSelector }}
    {{- $selector = merge $selector .Values.global.nodeSelector -}}
  {{- end -}}
{{- end -}}
{{- toYaml $selector | trim -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.env" -}}
{{- if hasKey .Values "initialConfig" }}
  {{- if hasKey .Values.initialConfig "logshipper" }}
    {{- if hasKey .Values.initialConfig.logshipper "env" }}
      {{- toYaml .Values.initialConfig.logshipper.env -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.registryUrl" -}}
{{- $registryUrl := "armdocker.rnd.ericsson.se" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.url }}
      {{- $registryUrl = .Values.global.registry.url -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.logshipper.registry }}
  {{- if .Values.imageCredentials.logshipper.registry.url }}
    {{- $registryUrl = .Values.imageCredentials.logshipper.registry.url -}}
  {{- end -}}
{{- end -}}
{{- print $registryUrl -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := index $productInfo "images" "logshipper" "repoPath" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.repoPath }}
      {{- $repoPath = .Values.global.registry.repoPath -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.logshipper.repoPath }}
  {{- if .Values.imageCredentials.logshipper.repoPath }}
    {{- $repoPath = .Values.imageCredentials.logshipper.repoPath -}}
  {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.imageName" -}}
{{- $imageName := "eric-log-shipper-sidecar" -}}
{{- if .Values.images }}
  {{- if .Values.images.logshipper }}
    {{- if .Values.images.logshipper.name }}
      {{- $imageName = .Values.images.logshipper.name -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageName -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.imageTag" -}}
{{- $imageTag := "19.0.0-18" -}}
{{- if .Values.images }}
  {{- if .Values.images.logshipper }}
    {{- if .Values.images.logshipper.tag }}
      {{- $imageTag = .Values.images.logshipper.tag -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageTag -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.imagePullPolicy }}
      {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.logshipper.registry }}
  {{- if .Values.imageCredentials.logshipper.registry.imagePullPolicy }}
    {{- $imagePullPolicy = .Values.imageCredentials.logshipper.registry.imagePullPolicy -}}
  {{- end -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.registryUrl" -}}
{{- $registryUrl := "armdocker.rnd.ericsson.se" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.url }}
      {{- $registryUrl = .Values.global.registry.url -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.dsl.registry }}
  {{- if .Values.imageCredentials.dsl.registry.url }}
    {{- $registryUrl = .Values.imageCredentials.dsl.registry.url -}}
  {{- end -}}
{{- end -}}
{{- print $registryUrl -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := index $productInfo "images" "dsl" "repoPath" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.repoPath }}
      {{- $repoPath = .Values.global.registry.repoPath -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.dsl.repoPath }}
  {{- if .Values.imageCredentials.dsl.repoPath }}
    {{- $repoPath = .Values.imageCredentials.dsl.repoPath -}}
  {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.imageName" -}}
{{- $imageName := "eric-slt-dsl" -}}
{{- if .Values.images }}
  {{- if .Values.images.dsl }}
    {{- if .Values.images.dsl.name }}
      {{- $imageName = .Values.images.dsl.name -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageName -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.imageTag" -}}
{{- $imageTag := "1.44.0-12" -}}
{{- if .Values.images }}
  {{- if .Values.images.dsl }}
    {{- if .Values.images.dsl.tag }}
      {{- $imageTag = .Values.images.dsl.tag -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageTag -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.imagePullPolicy }}
      {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.dsl.registry }}
  {{- if .Values.imageCredentials.dsl.registry.imagePullPolicy }}
    {{- $imagePullPolicy = .Values.imageCredentials.dsl.registry.imagePullPolicy -}}
  {{- end -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dslIPv6" -}}
{{- $dslipv6 := false -}}
{{- if .Values.global }}
  {{- if .Values.global.internalIPFamily }}
    {{- if eq .Values.global.internalIPFamily "IPv6" }}
      {{- $dslipv6 = true -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.initialConfig.dsl.enableIPv6 }}
  {{- if kindIs "bool" .Values.initialConfig.dsl.enableIPv6 }}
    {{- $dslipv6 = .Values.initialConfig.dsl.enableIPv6 -}}
  {{- end -}}
{{- end -}}
{{- $dslipv6 -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.log-streaming-activated" -}}
  {{- $streamingMethod := (include "eric-stm-diameter-proxy-grpc.log-streaming-method" .) -}}
  {{- if or (eq $streamingMethod "dual") (eq $streamingMethod "direct") -}}
    {{- printf "%t" true -}}
  {{- else -}}
    {{- printf "%t" false -}}
  {{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.log-streaming-method" -}}
  {{- $streamingMethod := "indirect" -}}
  {{- if (((.Values.global).log).streamingMethod) -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
  {{- end -}}
  {{- if ((.Values.log).streamingMethod) -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
  {{- end -}}
  {{- printf "%s" $streamingMethod -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.log-stdout-activated" -}}
  {{- $streamingMethod := (include "eric-stm-diameter-proxy-grpc.log-streaming-method" .) -}}
  {{- if or (eq $streamingMethod "dual") (eq $streamingMethod "indirect") -}}
    {{- printf "%t" true -}}
  {{- else -}}
    {{- printf "%t" false -}}
  {{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.dsl.supervisorPort" -}}
{{- $port := 28889 -}}
{{- if hasKey .Values "initialConfig" }}
  {{- if hasKey .Values.initialConfig "dsl" }}
    {{- if hasKey .Values.initialConfig.dsl "supervisorPort" }}
      {{- $port = int .Values.initialConfig.dsl.supervisorPort -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- printf "%d" $port -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.registryUrl" -}}
{{- $registryUrl := "armdocker.rnd.ericsson.se" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.url }}
      {{- $registryUrl = .Values.global.registry.url -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.diameterproxygrpc.registry }}
  {{- if .Values.imageCredentials.diameterproxygrpc.registry.url }}
    {{- $registryUrl = .Values.imageCredentials.diameterproxygrpc.registry.url -}}
  {{- end -}}
{{- end -}}
{{- print $registryUrl -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := index $productInfo "images" "diameterproxygrpc" "repoPath" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.repoPath }}
      {{- $repoPath = .Values.global.registry.repoPath -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.diameterproxygrpc.repoPath }}
  {{- if .Values.imageCredentials.diameterproxygrpc.repoPath }}
    {{- $repoPath = .Values.imageCredentials.diameterproxygrpc.repoPath -}}
  {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.imageName" -}}
{{- $imageName := "eric-stm-diameter-proxy-grpc" -}}
{{- if .Values.images }}
  {{- if .Values.images.diameterproxygrpc }}
    {{- if .Values.images.diameterproxygrpc.name }}
      {{- $imageName = .Values.images.diameterproxygrpc.name -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageName -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.imageTag" -}}
{{- $imageTag := "1.42.0-11" -}}
{{- if .Values.images }}
  {{- if .Values.images.diameterproxygrpc }}
    {{- if .Values.images.diameterproxygrpc.tag }}
      {{- $imageTag = .Values.images.diameterproxygrpc.tag -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- print $imageTag -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.global }}
  {{- if .Values.global.registry }}
    {{- if .Values.global.registry.imagePullPolicy }}
      {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- if .Values.imageCredentials.diameterproxygrpc.registry }}
  {{- if .Values.imageCredentials.diameterproxygrpc.registry.imagePullPolicy }}
    {{- $imagePullPolicy = .Values.imageCredentials.diameterproxygrpc.registry.imagePullPolicy -}}
  {{- end -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.diameterproxygrpc.supervisorPort" -}}
{{- $port := 28891 -}}
{{- if hasKey .Values "initialConfig" }}
  {{- if hasKey .Values.initialConfig "diameterproxygrpc" }}
    {{- if hasKey .Values.initialConfig.diameterproxygrpc "supervisorPort" }}
      {{- $port = int .Values.initialConfig.diameterproxygrpc.supervisorPort -}}
    {{- end -}}
  {{- end -}}
{{- end -}}
{{- printf "%d" $port -}}
{{- end -}}

#START

{{/*
Create image registry url - Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
*/}}
{{- define "eric-stm-diameter-proxy-grpc.bsfdiameter.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.bsfdiameter.registry -}}
{{- if .Values.imageCredentials.bsfdiameter.registry.url -}}
    {{- $url = .Values.imageCredentials.bsfdiameter.registry.url -}}
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
Create image registry url - Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
*/}}
{{- define "eric-stm-diameter-proxy-grpc.cddjmxexporter.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.cddjmxexporter.registry -}}
{{- if .Values.imageCredentials.cddjmxexporter.registry.url -}}
    {{- $url = .Values.imageCredentials.cddjmxexporter.registry.url -}}
{{- else if .Values.global -}}
    {{- if .Values.global.registry -}}
        {{- if .Values.global.registry.url -}}
            {{- $url = .Values.global.registry.url -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.bsfdiameter.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.bsfdiameter.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.bsfdiameter -}}
        {{- if .Values.imageCredentials.bsfdiameter.repoPath }}
            {{- $repoPath = .Values.imageCredentials.bsfdiameter.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.cddjmxexporter.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.cddjmxexporter.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.cddjmxexporter -}}
        {{- if .Values.imageCredentials.cddjmxexporter.repoPath }}
            {{- $repoPath = .Values.imageCredentials.cddjmxexporter.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.bsfdiameter.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.bsfdiameter.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.cddjmxexporter.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.cddjmxexporter.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.bsfdiameter.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.bsfdiameter.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.cddjmxexporter.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.cddjmxexporter.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
Create image pull policy bsfdiameter - Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
*/}}
{{- define "eric-stm-diameter-proxy-grpc.bsfdiameter.imagePullPolicy" -}}
    {{- $imagePullPolicy := "IfNotPresent" -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.imagePullPolicy -}}
                {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials.bsfdiameter.registry -}}
        {{- if .Values.imageCredentials.bsfdiameter.registry.imagePullPolicy -}}
            {{- $imagePullPolicy = .Values.imageCredentials.bsfdiameter.registry.imagePullPolicy -}}
        {{- end -}}
    {{- end -}}
    {{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy cddjmxexporter - Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
*/}}
{{- define "eric-stm-diameter-proxy-grpc.cddjmxexporter.imagePullPolicy" -}}
    {{- $imagePullPolicy := "IfNotPresent" -}}
    {{- if .Values.global -}}
        {{- if .Values.global.registry -}}
            {{- if .Values.global.registry.imagePullPolicy -}}
                {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
    {{- if .Values.imageCredentials.bsfdiameter.registry -}}
        {{- if .Values.imageCredentials.cddjmxexporter.registry.imagePullPolicy -}}
            {{- $imagePullPolicy = .Values.imageCredentials.cddjmxexporter.registry.imagePullPolicy -}}
        {{- end -}}
    {{- end -}}
    {{- print $imagePullPolicy -}}
{{- end -}}
#STOP

{{- define "eric-stm-diameter-proxy-grpc.customLabel" -}}
app: {{ template "eric-stm-diameter-proxy-grpc.name" . }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.deployment.merged-labels" -}}
{{- $template0 := include "eric-stm-diameter-proxy-grpc.customLabel" . | fromYaml -}}
{{- $base_labels := include "eric-stm-diameter-proxy-grpc.labels" . | fromYaml -}}
{{- include "eric-stm-diameter-proxy-grpc.mergeLabels" (dict "location" .Template.Name "sources" (list $template0 $base_labels)) | trim }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.appArmorAnnotation.dsl" -}}
{{- if .Values.appArmorProfile -}}
{{- $profile := .Values.appArmorProfile }}
{{- if index .Values.appArmorProfile "dsl" -}}
{{- $profile = index .Values.appArmorProfile "dsl" }}
{{- end -}}
{{- include "eric-stm-diameter-proxy-grpc.appArmorAnnotation.getAnnotation" (dict "profile" $profile "containerName" "dsl") }}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.appArmorAnnotation.diameterproxygrpc" -}}
{{- if .Values.appArmorProfile -}}
{{- $profile := .Values.appArmorProfile }}
{{- if index .Values.appArmorProfile "diameterproxygrpc" -}}
{{- $profile = index .Values.appArmorProfile "diameterproxygrpc" }}
{{- end -}}
{{- include "eric-stm-diameter-proxy-grpc.appArmorAnnotation.getAnnotation" (dict "profile" $profile "containerName" "diameterproxygrpc") }}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.appArmorAnnotation.logshipper" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.log-streaming-activated" .) "true" }}
{{- if  eq (default "" ((((.Values).global).logShipper).deployment).model) "static" }}
  {{- include "eric-stm-diameter-proxy-grpc.LsAppArmorProfileAnnotation" . }}
{{- else }}
  {{- include "eric-log-shipper-sidecar.LsAppArmorProfileAnnotation" . }}
{{- end }}
{{- end }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customLabel-1" -}}
sidecar.istio.io/inject: "false"
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customLabel-2" -}}
{{ .Values.initialConfig.dsl.serviceName }}-access: "true"
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customLabel-3" -}}
app: {{ template "eric-stm-diameter-proxy-grpc.name" . }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customLabel-4" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.log-streaming-activated" .) "true" }}
{{ .Values.logShipper.output.logTransformer.host }}-access: "true"
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.pod.merged-labels" -}}
{{- $template0 := include "eric-stm-diameter-proxy-grpc.customLabel-1" . | fromYaml -}}
{{- $template1 := include "eric-stm-diameter-proxy-grpc.customLabel-2" . | fromYaml -}}
{{- $template2 := include "eric-stm-diameter-proxy-grpc.customLabel-3" . | fromYaml -}}
{{- $template3 := include "eric-stm-diameter-proxy-grpc.customLabel-4" . | fromYaml -}}
{{- $base_labels := include "eric-stm-diameter-proxy-grpc.labels" . | fromYaml -}}
{{- include "eric-stm-diameter-proxy-grpc.mergeLabels" (dict "location" .Template.Name "sources" (list $template0 $template1 $template2 $template3 $base_labels)) | trim }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation" -}}
#START
#prometheus.io/scrape: "true"
#STOP
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-1" -}}
{{- if .Values.enableNewScrapePattern }}
prometheus.io/scrape-role: "pod"
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-2" -}}
#START
#prometheus.io/port: "20600"
#STOP
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-3" -}}
#START
#prometheus.io/path: "/metrics"
#STOP
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-4" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.tls" .) "true" }}
#START
#prometheus.io/scheme: "https"
#STOP
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-5" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.tls" .) "false" }}
#START
#prometheus.io/scheme: "http"
#STOP
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-6" -}}
{{- if .Values.pmserver.enableNewScrapePattern }}
prometheus.io/scrape-interval: "15s"
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.customAnnotation-7" -}}
{{- if (index .Values "bandwidth" "eric-stm-diameter-proxy-grpc" "maxEgressRate") }}
kubernetes.io/egress-bandwidth: {{ (index .Values "bandwidth" "eric-stm-diameter-proxy-grpc" "maxEgressRate") }}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.pod.merged-annotations" -}}
{{- $template0 := include "eric-stm-diameter-proxy-grpc.appArmorAnnotation.dsl" . | fromYaml -}}
{{- $template1 := include "eric-stm-diameter-proxy-grpc.appArmorAnnotation.diameterproxygrpc" . | fromYaml -}}
{{- $template2 := include "eric-stm-diameter-proxy-grpc.appArmorAnnotation.logshipper" . | fromYaml -}}
{{- $template3 := include "eric-stm-diameter-proxy-grpc.customAnnotation" . | fromYaml -}}
{{- $template4 := include "eric-stm-diameter-proxy-grpc.customAnnotation-1" . | fromYaml -}}
{{- $template5 := include "eric-stm-diameter-proxy-grpc.customAnnotation-2" . | fromYaml -}}
{{- $template6 := include "eric-stm-diameter-proxy-grpc.customAnnotation-3" . | fromYaml -}}
{{- $template7 := include "eric-stm-diameter-proxy-grpc.customAnnotation-4" . | fromYaml -}}
{{- $template8 := include "eric-stm-diameter-proxy-grpc.customAnnotation-5" . | fromYaml -}}
{{- $template9 := include "eric-stm-diameter-proxy-grpc.customAnnotation-6" . | fromYaml -}}
{{- $template10 := include "eric-stm-diameter-proxy-grpc.customAnnotation-7" . | fromYaml -}}
{{- $base_annotations := include "eric-stm-diameter-proxy-grpc.annotations" . | fromYaml -}}
{{- include "eric-stm-diameter-proxy-grpc.mergeAnnotations" (dict "location" .Template.Name "sources" (list $template0 $template1 $template2 $template3 $template4 $template5 $template6 $template7 $template8 $template9 $template10 $base_annotations)) | trim }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.nodeSelector.eric-stm-diameter-proxy-grpc" -}}
{{- $selector := dict -}}
{{- $localSelector := .Values.nodeSelector -}}
{{- range $key, $value := $localSelector }}
  {{- if eq (typeOf $value) "map[string]interface {}" }}
    {{- $localSelector = omit $localSelector $key -}}
  {{- end -}}
{{- end }}
{{- if $localSelector }}
  {{- $selector = merge $selector $localSelector -}}
{{- else -}}
  {{- if hasKey .Values.nodeSelector "eric-stm-diameter-proxy-grpc" }}
    {{- $selector = merge $selector (index .Values "nodeSelector" "eric-stm-diameter-proxy-grpc") -}}
  {{- end -}}
{{- end -}}
{{- if .Values.global }}
  {{- if .Values.global.nodeSelector }}
    {{- $selector = merge $selector .Values.global.nodeSelector -}}
  {{- end -}}
{{- end -}}
{{- toYaml $selector | trim -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.volumes" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.log-streaming-activated" .) "true" }}
{{- if  eq (default "" ((((.Values).global).logShipper).deployment).model) "static" }}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-volumes" . }}
{{- else }}
  {{- include "eric-log-shipper-sidecar.log-shipper-sidecar-volumes" . }}
{{- end }}
{{- end }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.seccompProfile.dsl" -}}
{{- if .Values.seccompProfile }}
{{- $profile := .Values.seccompProfile }}
{{- if index .Values.seccompProfile "dsl" }}
{{- $profile = index .Values.seccompProfile "dsl" }}
{{- end }}
{{- include "eric-stm-diameter-proxy-grpc.seccompAnnotation.getAnnotation" (dict "profile" $profile) }}
{{- end -}}

{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.mounts" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.log-streaming-activated" .) "true" }}
{{- if  eq (default "" ((((.Values).global).logShipper).deployment).model) "static" }}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-mounts" . }}
{{- else }}
  {{- include "eric-log-shipper-sidecar.log-shipper-sidecar-mounts" . }}
{{- end }}
{{- end }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.seccompProfile.diameterproxygrpc" -}}
{{- if .Values.seccompProfile }}
{{- $profile := .Values.seccompProfile }}
{{- if index .Values.seccompProfile "diameterproxygrpc" }}
{{- $profile = index .Values.seccompProfile "diameterproxygrpc" }}
{{- end }}
{{- include "eric-stm-diameter-proxy-grpc.seccompAnnotation.getAnnotation" (dict "profile" $profile) }}
{{- end -}}

{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper.container" -}}
{{- if eq (include "eric-stm-diameter-proxy-grpc.log-streaming-activated" .) "true" }}
{{- if  eq (default "" ((((.Values).global).logShipper).deployment).model) "static" }}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-container" . }}
{{- else }}
  {{- $logshipperImageDict := dict "logshipperSidecarImage" ((((.Values).global).logShipper).config).image -}}
  {{- include "eric-log-shipper-sidecar.log-shipper-sidecar-container" (mergeOverwrite . $logshipperImageDict ) }}
{{- end }}
{{- end }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents" -}}


{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.securityPolicy.reference.default-restricted-security-policy.0" -}}
{{- if .Values.global }}
  {{- if .Values.global.security }}
    {{- if .Values.global.security.policyReferenceMap }}
      {{- $mapped := (index .Values "global" "security" "policyReferenceMap" "default-restricted-security-policy") -}}
      {{- if $mapped }}
        {{- $mapped -}}
      {{- else -}}
        default-restricted-security-policy
      {{- end -}}
    {{- else -}}
      default-restricted-security-policy
    {{- end -}}
  {{- else -}}
    default-restricted-security-policy
  {{- end -}}
{{- else -}}
  default-restricted-security-policy
{{- end -}}
{{- end -}}
#START
{{/*
Create resources fragment. Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
*/}}
{{- define "eric-stm-diameter-proxy-grpc.bsfdiameter.resources" -}}
{{- $resources := index .Values "resources" "bsfdiameter" -}}
{{- toYaml $resources -}}
{{- end -}}
#STOP
#START. Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC

{{/*
Create cassandra contact point.
*/}}
{{- define "eric-bsf.cassandra.contact_point" -}}
{{- $contact_point := index .Values "cassandra" "contact_point" -}}
{{- toYaml $contact_point -}}
{{- end -}}

{{/*
Create cassandra consistency.
*/}}
{{- define "eric-bsf.cassandra.consistency" -}}
{{- $consistency := index .Values "cassandra" "consistency" -}}
{{- toYaml $consistency -}}
{{- end -}}

{{/*
Create cassandra keyspace.
*/}}
{{- define "eric-bsf.cassandra.keyspace" -}}
{{- $keyspace := index .Values "cassandra" "keyspace" -}}
{{- toYaml $keyspace -}}
{{- end -}}

{{/*
Create cassandra local datacenter.
*/}}
{{- define "eric-bsf.cassandra.datacenter" -}}
{{- $datacenter := index .Values "cassandra" "datacenter" -}}
{{- toYaml $datacenter -}}
{{- end -}}

{{/*
Create cassandra throttler class.
*/}}
{{- define "eric-bsf.cassandra.throttler.class" -}}
{{- $strategy := index .Values "cassandra" "throttler" "class" -}}
{{- toYaml $strategy -}}
{{- end -}}

{{/*
Create cassandra throttler max queue size.
*/}}
{{- define "eric-bsf.cassandra.throttler.max_queue_size" -}}
{{- $option := index .Values "cassandra" "throttler" "max_queue_size" -}}
{{- toYaml $option -}}
{{- end -}}

{{/*
Create cassandra throttler max concurrent requests.
*/}}
{{- define "eric-bsf.cassandra.throttler.max_concurrent_requests" -}}
{{- $factor := index .Values "cassandra" "throttler" "max_concurrent_requests" -}}
{{- toYaml $factor -}}
{{- end -}}

{{/*
Create cassandra auth - user secret.
*/}}
{{- define "eric-bsf.cassandra.auth.userSecret" -}}
{{- $userSecret := index .Values "cassandra" "auth" "userSecret" -}}
{{- toYaml $userSecret -}}
{{- end -}}

# START. Addition of cddjmxexpoter container's metrics
{{/*
Create cassandra metrics - cqlRequests - highestLatency.
*/}}
{{- define "eric-bsf.cassandra.metrics.cqlRequests.highestLatency" -}}
{{- $highestLatency := index .Values "cassandra" "metrics" "cqlRequests" "highestLatency" -}}
{{- toYaml $highestLatency -}}
{{- end -}}

{{/*
Create cassandra metrics - cqlRequests - significantDigits.
*/}}
{{- define "eric-bsf.cassandra.metrics.cqlRequests.significantDigits" -}}
{{- $significantDigits := index .Values "cassandra" "metrics" "cqlRequests" "significantDigits" -}}
{{- toYaml $significantDigits -}}
{{- end -}}

{{/*
Create cassandra metrics - cqlRequests - refreshInterval.
*/}}
{{- define "eric-bsf.cassandra.metrics.cqlRequests.refreshInterval" -}}
{{- $refreshInterval := index .Values "cassandra" "metrics" "cqlRequests" "refreshInterval" -}}
{{- toYaml $refreshInterval -}}
{{- end -}}

{{/*
Create cassandra metrics - throttling - highestLatency.
*/}}
{{- define "eric-bsf.cassandra.metrics.throttling.highestLatency" -}}
{{- $highestLatency := index .Values "cassandra" "metrics" "throttling" "highestLatency" -}}
{{- toYaml $highestLatency -}}
{{- end -}}

{{/*
Create cassandra metrics - throttling - significantDigits.
*/}}
{{- define "eric-bsf.cassandra.metrics.throttling.significantDigits" -}}
{{- $significantDigits := index .Values "cassandra" "metrics" "throttling" "significantDigits" -}}
{{- toYaml $significantDigits -}}
{{- end -}}

{{/*
Create cassandra metrics - throttling - refreshInterval.
*/}}
{{- define "eric-bsf.cassandra.metrics.throttling.refreshInterval" -}}
{{- $refreshInterval := index .Values "cassandra" "metrics" "throttling" "refreshInterval" -}}
{{- toYaml $refreshInterval -}}
{{- end -}}

{{/*
Create cassandra metrics - cqlMessages - highestLatency.
*/}}
{{- define "eric-bsf.cassandra.metrics.cqlMessages.highestLatency" -}}
{{- $highestLatency := index .Values "cassandra" "metrics" "cqlMessages" "highestLatency" -}}
{{- toYaml $highestLatency -}}
{{- end -}}

{{/*
Create cassandra metrics - cqlMessages - significantDigits.
*/}}
{{- define "eric-bsf.cassandra.metrics.cqlMessages.significantDigits" -}}
{{- $significantDigits := index .Values "cassandra" "metrics" "cqlMessages" "significantDigits" -}}
{{- toYaml $significantDigits -}}
{{- end -}}

{{/*
Create cassandra metrics - cqlMessages - refreshInterval.
*/}}
{{- define "eric-bsf.cassandra.metrics.cqlMessages.refreshInterval" -}}
{{- $refreshInterval := index .Values "cassandra" "metrics" "cqlMessages" "refreshInterval" -}}
{{- toYaml $refreshInterval -}}
{{- end -}}
# STOP. Addition of cddjmxexpoter container's metrics


# START. Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
{{/*
Define filecollector enabled
*/}}
{{- define "eric-bsf.fileCollector.enabled" -}}
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

# START - logshipper sidecar
{{/*
Define LOGBACK file to be used, note: returns logback xml file
*/}}
{{- define "eric-bsf-diameter.logbackFileName" -}}
{{- $streamingMethod := include "eric-stm-diameter-proxy-grpc.log-streaming-method" . -}}
{{- $fileName := "logbackInDirect.xml" -}}
{{- if eq "direct" $streamingMethod -}}
    {{- $fileName = "logbackDirect.xml" -}}
{{- else if eq "dual" $streamingMethod -}}
    {{- $fileName = "logback.xml" -}}
{{- end -}}
{{- print $fileName -}}
{{- end -}}
# STOP - logshipper sidecar
# STOP. Additions/Changes for BSF compared to helm chart provided by Diameter Proxy gRPC
