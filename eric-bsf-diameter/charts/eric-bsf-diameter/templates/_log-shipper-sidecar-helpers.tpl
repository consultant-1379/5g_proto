{{/*
Template of Log Shipper sidecar
Version: SIDECAR_VERSION
*/}}

{{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
If release name contains chart name it will be used as a full name.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" }}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- $name := default .Chart.Name .Values.nameOverride -}}
{{- printf "%s" $name | trunc 63 | trimSuffix "-" -}}
{{- end -}}
{{- end }}

{{/*
Expand the name of the chart.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-name" }}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end }}

{{/*
Common name for logshipper resources for hooks
Fullname will be truncated to 55 characters to add the "ls-hook" suffix.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname-for-hooks" -}}
{{- $fullName := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . | trunc 55 | trimSuffix "-" -}}
{{- printf "%s-ls-hook" $fullName -}}
{{- end -}}

{{/*
Merge user-defined annotations with product info (DR-D1121-065, DR-D1121-060)
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-annotations" -}}
  {{- $productAnnotations := dict }}
  {{- $_ := set $productAnnotations "ericsson.com/product-name" (fromYaml (.Files.Get "eric-product-info.yaml")).productName }}
  {{- $_ := set $productAnnotations "ericsson.com/product-number" (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber }}
  {{- $_ := set $productAnnotations "ericsson.com/product-revision" (regexReplaceAll "(.*)[+|-].*" .Chart.Version "${1}") }}

  {{- $globalAnn := (.Values.global).annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-merge-annotations" (dict "location" .Template.Name "sources" (list $productAnnotations $globalAnn $serviceAnn)) | trim }}
{{- end -}}

{{/*
Merge user-defined labels with kubernetes labels (DR-D1121-068, DR-D1121-060)
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-labels" -}}
  {{- $k8sLabels := dict }}
  {{- $_ := set $k8sLabels "app.kubernetes.io/name" (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-name" .) }}
  {{- $_ := set $k8sLabels "app.kubernetes.io/version" (.Chart.Version | replace "+" "_") }}
  {{- $_ := set $k8sLabels "app.kubernetes.io/instance" .Release.Name }}

  {{- $globalLabels := (.Values.global).labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-merge-labels" (dict "location" .Template.Name "sources" (list $k8sLabels $globalLabels $serviceLabels)) | trim }}
{{- end -}}


{{/*
seccompProfile for logshipper container
*/}}
{{- define "eric-stm-diameter-proxy-grpc.LsSeccompProfile" -}}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
{{- if and $default.seccompProfile.logshipper $default.seccompProfile.logshipper.type }}
seccompProfile:
  type: {{ $default.seccompProfile.logshipper.type }}
  {{- if eq $default.seccompProfile.logshipper.type "Localhost" }}
  localhostProfile: {{ $default.seccompProfile.logshipper.localhostProfile }}
  {{- end }}
{{- end }}
{{- end -}}

{{/*
Log Shipper sidecar image for static solution
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-static-sidecar-image" }}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") }}
{{- $registry := default $productInfo.images.logshipper.registry (default (((.Values).global).registry).url (((((.Values).global).imageCredentials).logshipper).registry).url) }}
{{- $repoPath := $productInfo.images.logshipper.repoPath -}}
{{- if or (kindIs "string" (((.Values).global).registry).repoPath) (kindIs "string" ((((.Values).global).imageCredentials).logshipper).repoPath) -}}
    {{- $repoPath =  ternary ((((.Values).global).imageCredentials).logshipper).repoPath (((.Values).global).registry).repoPath  (kindIs "string" ((((.Values).global).imageCredentials).logshipper).repoPath) -}}
{{- end -}}
{{- $name := $productInfo.images.logshipper.name }}
{{- $tag :=  $productInfo.images.logshipper.tag}}
{{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
{{- end -}}
{{- printf "%s/%s%s:%s" $registry $repoPath $name $tag }}
{{- end }}

{{/*
Log Shipper sidecar image.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-image" }}
{{- $registry := default (.logshipperSidecarImage).registry (default (((.Values).global).registry).url (((((.Values).global).imageCredentials).logshipper).registry).url) }}
{{- $repoPath := (.logshipperSidecarImage).repoPath -}}
{{- if or (kindIs "string" (((.Values).global).registry).repoPath) (kindIs "string" ((((.Values).global).imageCredentials).logshipper).repoPath) -}}
    {{- $repoPath =  ternary ((((.Values).global).imageCredentials).logshipper).repoPath (((.Values).global).registry).repoPath  (kindIs "string" ((((.Values).global).imageCredentials).logshipper).repoPath) -}}
{{- end -}}
{{- $name := (.logshipperSidecarImage).name }}
{{- $tag := (.logshipperSidecarImage).tag }}
{{- if $repoPath -}}
    {{- $repoPath = printf "%s/" $repoPath -}}
{{- end -}}
{{- printf "%s/%s%s:%s" $registry $repoPath $name $tag }}
{{- end }}

{{/*
Define AppArmorProfile for logshipper container.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.LsAppArmorProfileAnnotation" -}}
{{- $container := "logshipper" -}}
{{- $acceptedProfiles := list "unconfined" "runtime/default" "localhost" }}
{{- $commonProfile := dict -}}
{{- if .Values.appArmorProfile.type -}}
  {{- $_ := set $commonProfile "type" .Values.appArmorProfile.type -}}
  {{- if and (eq .Values.appArmorProfile.type "localhost") .Values.appArmorProfile.localhostProfile -}}
    {{- $_ := set $commonProfile "localhostProfile" .Values.appArmorProfile.localhostProfile -}}
  {{- end -}}
{{- end -}}
{{- $profiles := dict -}}
{{- if .Values.appArmorProfile.logshipper -}}
    {{- if and (hasKey $.Values.appArmorProfile $container) (index $.Values.appArmorProfile $container "type") -}}
      {{- $_ := set $profiles $container (index $.Values.appArmorProfile $container) -}}
    {{- else -}}
      {{- $_ := set $profiles $container $commonProfile -}}
    {{- end -}}
{{- else -}}
    {{- $_ := set $profiles $container $commonProfile -}}
{{- end -}}
{{- range $key, $value := $profiles -}}
  {{- if $value.type -}}
    {{- if not (has $value.type $acceptedProfiles) -}}
      {{- fail (printf "Unsupported appArmor profile type: %s, use one of the supported profiles %s" $value.type $acceptedProfiles) -}}
    {{- end -}}
    {{- if and (eq $value.type "localhost") (empty $value.localhostProfile) -}}
      {{- fail "The 'localhost' appArmor profile requires a profile name to be provided in localhostProfile parameter." -}}
    {{- end }}
container.apparmor.security.beta.kubernetes.io/{{ $key }}: {{ $value.type }}{{ eq $value.type "localhost" | ternary (printf "/%s" $value.localhostProfile) ""  }}
  {{- end -}}
{{- end -}}
{{- end -}}

{{/*
Log Shipper sidecar log redirection
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-redirect" }}
  {{- if and (((.Values).log).streamingMethod) (ne (((.Values).log).streamingMethod | toString ) "null") -}}
    {{- if (eq ((.Values).log).streamingMethod "dual") -}}
      {{- "all" -}}
    {{- else -}}
      {{- "file" -}}
    {{- end -}}
  {{- else if (((.Values).global).log).streamingMethod -}}
    {{- if (eq (((.Values).global).log).streamingMethod "dual") -}}
      {{- "all" -}}
    {{- else -}}
      {{- "file" -}}
    {{- end -}}
  {{- else -}}
      {{- "file" -}}
{{- end -}}
{{- end -}}

{{/*
Log Shipper sidecar input key message
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-input-key" }}
{{- if eq "true" (((.Values).logShipper).json_decode | toString ) }}
  {{- "input_message" -}}
{{- else -}}
  {{- "message" -}}
{{- end -}}
{{- end -}}

{{/*
Log Shipper sidecar image pull policy.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-image-pull-policy" }}
{{- printf "%s" (default "IfNotPresent" (default (((.Values).global).registry).imagePullPolicy ((((.Values).imageCredentials).logshipper).registry).imagePullPolicy)) }}
{{- end }}

{{/*
Log Shipper sidecar storage path.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-storage-path" }}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
{{- printf "%s" ($default.logShipper.storage.path) }}
{{- end }}

{{/*
Comma separated lists of paths relative to storage path.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-relative-paths" }}
{{- $storagePath := .storagePath }}
{{- $tmp := dict "paths" (list) -}}
{{- range $path := .filePaths -}}
{{- $noop := printf "%s/%s" $storagePath $path | append $tmp.paths | set $tmp "paths" -}}
{{- end -}}
{{- join "," $tmp.paths -}}
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-deployment-model" }}
{{- $image := (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-image" .) -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") }}
  {{- if (((((.Values).global).logShipper).deployment).model) }}
    {{- if eq (((((.Values).global).logShipper).deployment).model) "static" }}
      {{- if ((($productInfo).images).logshipper) }}
        {{- $image = (include "eric-stm-diameter-proxy-grpc.log-shipper-static-sidecar-image" .) -}}
      {{- end -}}
    {{- end -}}
  {{- end -}}
{{- $image }}
{{- end }}

{{/*
Log Shipper sidecar container spec.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-container" }}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
- name: logshipper
  imagePullPolicy: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-image-pull-policy" . }}
  image: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-deployment-model" . }}
  args:
    - stdout-redirect
    - -redirect={{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-redirect" . }}
    - -size=2
    - -rotate=5
    - -logfile={{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-storage-path" . }}/logshipper.log
    - --
    - /opt/fluent-bit/scripts/init.sh
    - --config=/etc/fluent-bit/fluent-bit.conf
  securityContext:
    allowPrivilegeEscalation: false
    privileged: false
    readOnlyRootFilesystem: true
    runAsNonRoot: true
    procMount: Default
    capabilities:
      drop:
        - ALL
    {{ include "eric-stm-diameter-proxy-grpc.LsSeccompProfile" . | indent 4 }}
  env:
    - name: TLS_ENABLED
      {{- if ne "false" (((((.Values).global).security).tls).enabled | toString) }}
      value: "true"
      {{- else }}
      value: "false"
      {{- end }}
    - name: TZ
      value: {{ default "UTC" ((.Values).global).timezone }}
    - name: LOG_PATH
      value: {{ $default.logShipper.storage.path | quote }}
    - name: RUN_AND_EXIT
      value: {{ $default.logShipper.runAndExit  | quote }}
    - name : SHUTDOWN_DELAY
      value: {{ $default.logShipper.shutdownDelay | quote }}
    - name: LOG_LEVEL
      value: {{ $default.logShipper.logLevel | quote }}
    - name: CONTAINER_NAME
      value: logshipper
    - name: NAMESPACE
      valueFrom:
       fieldRef:
         fieldPath: metadata.namespace
    - name: NODE_NAME
      valueFrom:
       fieldRef:
         fieldPath: spec.nodeName
    - name: POD_NAME
      valueFrom:
       fieldRef:
         fieldPath: metadata.name
    - name: POD_UID
      valueFrom:
       fieldRef:
         fieldPath: metadata.uid
    - name: SERVICE_ID
      value: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}
    - name: CA_CERT_PATH
      value: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.trustedInternalRootCa.mountPath" . }}
    - name: CLIENT_CERT_PATH
      value: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tlsCert.mountPath" . }}
    - name: LS_SIDECAR_CERT_FILE
      value: "clicert.pem"
    - name: LS_SIDECAR_KEY_FILE
      value: "cliprivkey.pem"
    - name: LS_SIDECAR_CA_CERT_FILE
      value: "ca.crt"
  resources:
    limits:
      {{- if $default.resources.logshipper.limits.cpu }}
      cpu: {{ $default.resources.logshipper.limits.cpu | quote }}
      {{- end }}
      {{- if $default.resources.logshipper.limits.memory }}
      memory: {{ $default.resources.logshipper.limits.memory | quote }}
      {{- end }}
      {{- if index $default.resources.logshipper.limits "ephemeral-storage" }}
      ephemeral-storage: {{ index $default.resources.logshipper.limits "ephemeral-storage"  | quote }}
      {{- end }}
    requests:
      {{- if $default.resources.logshipper.requests.cpu }}
      cpu: {{ $default.resources.logshipper.requests.cpu | quote }}
      {{- end }}
      {{- if $default.resources.logshipper.requests.memory }}
      memory: {{ $default.resources.logshipper.requests.memory | quote }}
      {{- end }}
      {{- if index $default.resources.logshipper.requests "ephemeral-storage" }}
      ephemeral-storage: {{ index $default.resources.logshipper.requests "ephemeral-storage"  | quote }}
      {{- end }}
  volumeMounts:
    {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-mounts" . | indent 4 }}
    - name: fluentbit-config
      mountPath: /etc/fluent-bit/fluent-bit.conf
      subPath: fluent-bit.conf
      readOnly: true
    - name: fluentbit-config
      mountPath: /etc/fluent-bit/parsers.conf
      subPath: parsers.conf
      readOnly: true
    - name: fluentbit-config
      mountPath: /etc/fluent-bit/inputs.conf
      subPath: inputs.conf
      readOnly: true
    - name: fluentbit-config
      mountPath: /etc/fluent-bit/filters.conf
      subPath: filters.conf
      readOnly: true
    - name: fluentbit-config
      mountPath: /etc/fluent-bit/outputs.conf
      subPath: outputs.conf
      readOnly: true
    {{- if (((.Values).logShipper).filters).enabled }}
    {{- if ((((.Values).logShipper).filters).lua).enabled }}
    {{- $filePath := printf "%s/*.lua" ((((.Values).logShipper).filters).lua).scriptpath -}}
    {{- range $path, $_ := .Files.Glob $filePath }}
    - name: fluentbit-config
      mountPath: /etc/fluent-bit/{{ base $path}}
      subPath: {{ base $path}}
      readOnly: true
    {{ end -}}
    {{ end -}}
    {{ end -}}
    {{- if ne "false" (((((.Values).global).security).tls).enabled | toString) }}
    - name: server-ca-certificate
      mountPath: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.trustedInternalRootCa.mountPath" . }}
      readOnly: true
    - name: lt-http-client-cert
      mountPath: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tlsCert.mountPath" . }}
      readOnly: true
    {{- end }}
{{- end }}

{{/*
Standardized secrets name for certificates (DR-D1123-133)
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tls.secretname" }}
{{- default (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" .) .customLogshipperResourceName }}-log-shipper-sidecar-lt-http-client-cert
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.trustedInternalRootCa.mountPath" }}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
{{- printf "%s/%s" "/run/secrets" $default.global.security.tls.trustedInternalRootCa.secret -}}
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tlsCert.mountPath" }}
{{- printf "%s/%s" "/run/secrets" (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tls.secretname" .) -}}
{{- end }}

{{/*
Log Shipper sidecar container spec for hooks.
Since jobs needs to terminate, .Values.logShipper.runAndExit is set to "true".
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-container-for-hooks" -}}
  {{- $copied := deepCopy . -}}
  {{- $name := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname-for-hooks" . -}}
  {{- $merged := (mergeOverwrite $copied (dict "customLogshipperResourceName" $name)) -}}
  {{- $merged := (mergeOverwrite $merged (dict "Values" (dict "logShipper" (dict "runAndExit" "true")))) -}}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-container" $merged }}
{{- end -}}

{{/*
Log Shipper sidecar LUA filter.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-lua-scripts" }}
{{ $filePath := printf "%s/*.lua" ((((.Values).logShipper).filters).lua).scriptpath -}}
{{ (.Files.Glob $filePath).AsConfig | indent 2 }}
{{- end }}

{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-lua-filters" }}
{{ range ((((.Values).logShipper).filters).lua).rules }}
    [FILTER]
        name    lua
        match   event.*
        {{- with . }}
        script  /etc/fluent-bit/{{ .script }}
        call    {{ .call }}
        {{- end }}
{{- end }}
{{- end }}

{{/*
Log Shipper sidecar shared volume mounts.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-mounts" }}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
- name: eric-stm-diameter-proxy-grpc-storage-path
  mountPath: {{ $default.logShipper.storage.path | quote }}
{{- end }}

{{/*
Log Shipper sidecar volumes.
Optional:
  - customLogshipperResourceName
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-volumes" }}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
- name: eric-stm-diameter-proxy-grpc-storage-path
  emptyDir:
    sizeLimit: {{ required "missing 'logShipper.storage.size'" $default.logShipper.storage.size | quote }}
    {{- if not (eq ( $default.logShipper.storage.medium ) "Ephemeral") }}
    medium: Memory
    {{- end }}
- name: fluentbit-config
  configMap:
    {{- if $default.logShipper.configRenameEnabled }}
    name: {{ default (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" .) .customLogshipperResourceName }}-logshipper-sidecar
    {{- else }}
    name: {{ default (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" .) .customLogshipperResourceName }}-log-shipper-sidecar
    {{- end }}
    items:
      - key: fluent-bit.conf
        path: fluent-bit.conf
      - key: inputs.conf
        path: inputs.conf
      - key: outputs.conf
        path: outputs.conf
      - key: filters.conf
        path: filters.conf
      - key: parsers.conf
        path: parsers.conf
      {{- if (((.Values).logShipper).filters).enabled }}
      {{- if ((((.Values).logShipper).filters).lua).enabled }}
      {{- $filePath := printf "%s/*.lua" ((((.Values).logShipper).filters).lua).scriptpath -}}
      {{- range $path, $_ := .Files.Glob $filePath }}
      - key: {{ base $path }}
        path: {{ base $path -}}
      {{ end -}}
      {{ end -}}
      {{ end -}}
{{- if ne "false" (((((.Values).global).security).tls).enabled | toString) }}
- name: server-ca-certificate
  secret:
    secretName: {{ $default.global.security.tls.trustedInternalRootCa.secret }}
    optional: true
- name: lt-http-client-cert
  secret:
    secretName: {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tls.secretname" . }}
    optional: true
{{- end }}
{{- end -}}

{{/*
Log Shipper sidecar volumes for hooks.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-volumes-for-hooks" -}}
  {{- $copied := deepCopy . -}}
  {{- $name := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname-for-hooks" . -}}
  {{- $merged := (mergeOverwrite $copied (dict "customLogshipperResourceName" $name)) -}}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-volumes" $merged }}
{{- end -}}

{{/*
Create a map with internal default values used for testing purposes.
*/}}
{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-internal-parameters" -}}
  {{- $internal := dict "internal" (dict "output" (dict "logTransformer" (dict "enabled" true))) -}}
  {{ if .Values.internal }}
    {{- mergeOverwrite $internal .Values | toJson -}}
  {{ else }}
    {{- $internal | toJson -}}
  {{ end }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" -}}
  {{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
  {{- $default := dict "logShipper" (dict "storage" (dict "path" "/logs" )) -}}
  {{- $default := merge $default (dict "logShipper" (dict "storage" (dict "size" "" ))) -}}
  {{- $default := merge $default (dict "logShipper" (dict "storage" (dict "medium" "Ephemeral" ))) -}}
  {{- $default := merge $default (dict "logShipper" (dict "output" (dict "logTransformer" (dict "host" "eric-log-transformer" )))) -}}
  {{- $default := merge $default (dict "logShipper" (dict "runAndExit" "false" )) -}}
  {{- $default := merge $default (dict "logShipper" (dict "shutdownDelay" 10 )) -}}
  {{- $default := merge $default (dict "logShipper" (dict "logLevel" "info" )) -}}
  {{- $default := merge $default (dict "logShipper" (dict "json_decode" "false" )) -}}
  {{- $default := merge $default (dict "logShipper" (dict "configRenameEnabled" true )) -}}
  {{- $default := merge $default (dict "seccompProfile" (dict "logshipper" (dict "type" "" ))) -}}
  {{- $default := merge $default (dict "seccompProfile" (dict "logshipper" (dict "localhostProfile" "" ))) -}}
  {{- $default := merge $default (dict "global" (dict "security" (dict "tls" (dict "trustedInternalRootCa" (dict "secret" "eric-sec-sip-tls-trusted-root-cert"))))) -}}
  {{- $default := mergeOverwrite $default .Values -}}
  {{- $default | toJson -}}
{{- end -}}
