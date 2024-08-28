{{/*
Template of Log Shipper sidecar
Version: SIDECAR_VERSION
Optional:
  - customLogshipperResourceName
  - customLogshipperAnnotations
*/}}

{{- define "eric-stm-diameter-proxy-grpc.logshipper-sidecar-configmap" -}}
{{- $default := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-default-value" .) }}
{{- $defaultName := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}
{{- $name := default $defaultName .customLogshipperResourceName }}
{{- $key := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-input-key" . }}
apiVersion: v1
kind: ConfigMap
metadata:
  {{- if $default.logShipper.configRenameEnabled }}
  name: {{ $name }}-logshipper-sidecar
  {{- else }}
  name: {{ $name }}-log-shipper-sidecar
  {{- end }}
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
data:
  fluent-bit.conf: |
    @INCLUDE /etc/fluent-bit/inputs.conf
    @INCLUDE /etc/fluent-bit/outputs.conf
    @INCLUDE /etc/fluent-bit/filters.conf

    [SERVICE]
        flush           3
        grace           10
        log_level       {{ $default.logShipper.logLevel }}
        parsers_file    /etc/fluent-bit/parsers.conf
        http_server     on
        http_listen     localhost
        http_port       2020
        storage.metrics on

  parsers.conf: |
    [PARSER]
        name   json
        format json
    {{- range ((.Values).logShipper).parsers }}
    {{- if eq .name "json" }}
    {{- fail "parameter 'logShipper.parsers.name' cannot have value 'json'" }}
    {{- end }}
    [PARSER]
        name   {{ required "missing 'logShipper.parsers.name'" .name }}
        format regex
        regex  {{ required "missing 'logShipper.parsers.pattern'" .pattern }}
    {{- end }}
    {{- range ((.Values).logShipper).multilines }}
    {{- if and (ne .name "docker") (ne .name "go") (ne .name "java") (ne .name "cri") (ne .name "python")}}
    [MULTILINE_PARSER]
        name {{ required "missing 'logShipper.multilines.name'" .name }}
        type regex
        flush_timeout 1000
        rule "start_state" {{ printf "\"%s\"" (required "missing 'logShipper.multilines.start.pattern'" (.start).pattern)  }} {{ required "missing 'logShipper.multilines.start.next'" (.start).next | quote  }}
        {{- range .cont }}
        rule {{ required "missing 'logShipper.multilines.cont.name'" .name | quote  }} {{ printf "\"%s\"" (required "missing 'logShipper.multilines.cont.pattern'" .pattern) }} {{ required "missing 'logShipper.multilines.cont.next'" .next | quote  }}
        {{- end }}
    {{- else }}
    {{- fail "parameter 'logShipper.multilines.name' cannot have value 'docker','go','python','cri','java'" }}
    {{- end }}
    {{- end }}

  {{- if (((.Values).logShipper).filters).enabled }}
  {{- if ((((.Values).logShipper).filters).lua).enabled }}
  {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-lua-scripts" . }}
  {{- end }}
  {{- end }}
  inputs.conf: |
    {{- $storagePath := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-storage-path" . }}
    [INPUT]
        name              tail
        tag               event.fluent-bit
        alias             log_shipper
        buffer_chunk_size 32k
        buffer_max_size   32k
        path              {{ $storagePath }}/logshipper.log
        path_key          filename
        read_from_head    true
        refresh_interval  5
        rotate_wait       10
        skip_empty_lines  off
        skip_long_lines   off
        key               {{ $key }}
        db                {{ $storagePath }}/logshipper.db
        db.sync           normal
        db.locking        true
        db.journal_mode   off
        parser            json
        mem_buf_limit     1MB
    {{- range $i, $v := (((.Values).logShipper).input).files }}
    {{- if $v.enabled }}
    [INPUT]
        name              tail
        tag               event.file{{ $i }}
        alias             file{{ $i }}
        buffer_chunk_size 32k
        buffer_max_size   {{ default "32k" (.buffer).maxSize }}
        read_from_head    true
        refresh_interval  5
        rotate_wait       10
        skip_empty_lines  off
        skip_long_lines   {{ default "off" (.skipLongLines) }}
        key               {{ $key }}
        db                {{ $storagePath }}/file{{ $i }}.db
        db.sync           normal
        db.locking        true
        db.journal_mode   off
        {{- if .paths }}
        path              {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-relative-paths" (dict "storagePath" $storagePath "filePaths" .paths) }}
        path_key          filename
        {{- end }}
        {{- if .exclusions }}
        exclude_path      {{ $storagePath }}/logshipper.log,{{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-relative-paths" (dict "storagePath" $storagePath "filePaths" .exclusions) }}
        {{- else }}
        exclude_path      {{ $storagePath }}/logshipper.log
        {{- end }}
        mem_buf_limit     {{ default 1 .memory }}MB
    {{- end }}
    {{- end }}

  filters.conf: |
    {{- if eq "true" ($default.logShipper.json_decode | toString) }}
    [FILTER]
        name          modify
        match         event.*
        condition     Matching_keys_have_matching_values (^{{ $key }}) (^{.*})$
        add           is_json          true
    {{- end }}
    {{- range $i, $v := (((.Values).logShipper).input).files }}
    {{- if and $v.enabled $v.parsers }}
    {{- if ge (len $v.parsers) 1 }}
    [FILTER]
        name     parser
        match    event.file{{ $i }}
        {{- range $v.parsers }}
        parser   {{ . }}
        {{- end }}
        key_name {{ $key }}
        reserve_data true
    {{- if eq "true" ($default.logShipper.json_decode | toString) }}
        preserve_key true
    {{- end }}
    {{- end }}
    {{- else }}
    [FILTER]
        name     parser
        match    event.file{{ $i }}
        parser   json
        key_name {{ $key }}
        reserve_data true
    {{- if eq "true" ($default.logShipper.json_decode | toString) }}
        preserve_key true
    {{- end }}
    {{- end }}
    {{- if $v.multilines }}
    [FILTER]
        name                    multiline
        match                   event.file{{ $i }}
        multiline.key_content   {{ $key }}
        {{- range $v.multilines }}
        multiline.parser        {{ . }}
        {{- end }}
    {{- end }}
    {{- end }}
    {{- if (((.Values).logShipper).filters).enabled -}}
    {{- if ((((.Values).logShipper).filters).lua).enabled }}
    {{- include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-lua-filters" . }}
    {{- end }}
    {{- if ((((.Values).logShipper).filters).modify).enabled -}}
    {{- range $_, $v := .Values.logShipper.filters.modify.rules }}
    {{- if or (regexMatch "^match" $v) (regexMatch "^name" $v) }}
    {{- fail "Invalid modify filter configuration" -}}
    {{- else }}
    {{ printf "[FILTER]" -}}
    {{ printf "name modify" | nindent 8 -}}
    {{ printf "match event.*" | nindent 8 -}}
    {{- $v  | nindent 8 -}}
    {{ end -}}
    {{ end -}}
    {{ end -}}
    {{ end }}
    {{- if eq "true" ($default.logShipper.json_decode | toString) }}
    [FILTER]
        name          modify
        match         event.fluent-bit
        condition     key_does_not_exist version
        add           version            1.2.0
    [FILTER]
        name          modify
        match         event.fluent-bit
        condition     key_does_not_exist service_id
        add           service_id         {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}
    [FILTER]
        name          modify
        match         event.*
        condition     key_does_not_exist is_json
        rename        filename           tmp
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      filename*
        nest_under    extradata.fluentbit
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  extra_data
        add_prefix    extradata.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      extradata.*
        nest_under    extra_data
        remove_prefix extradata.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      *
        nest_under    plainmsg
        add_prefix    json.
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  plainmsg
    [FILTER]
        name          modify
        match         event.*
        condition     key_does_not_exist is_json
        rename        json.tmp        filename
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      json.*
        nest_under    json
        remove_prefix json.
    {{- end }}
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  extra_data
        add_prefix    extradata.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      filename*
        nest_under    fluentbit
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      fluentbit*
        wildcard      extradata.*
        nest_under    extra_data
        remove_prefix extradata.
    {{- if eq "true" ($default.logShipper.json_decode | toString) }}
    [FILTER]
        name          modify
        match         event.*
        add           kubernetes.labels.app_kubernetes_io/name         {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}
    [FILTER]
        name          modify
        match         event.*
        add           kubernetes.namespace ${NAMESPACE}
    [FILTER]
        name          modify
        match         event.*
        add           kubernetes.node.name ${NODE_NAME}
    [FILTER]
        name          modify
        match         event.*
        add           kubernetes.pod.name ${POD_NAME}
    [FILTER]
        name          modify
        match         event.*
        add           kubernetes.pod.uid ${POD_UID}
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  kubernetes.labels
        add_prefix    kubernetes.labels.
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  kubernetes.node
        add_prefix    kubernetes.node.
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  kubernetes.pod
        add_prefix    kubernetes.pod.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      kubernetes.labels.*
        nest_under    kubernetes.labels
        remove_prefix kubernetes.labels.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      kubernetes.node.*
        nest_under    kubernetes.node
        remove_prefix kubernetes.node.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      kubernetes.pod.*
        nest_under    kubernetes.pod
        remove_prefix kubernetes.pod.
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  kubernetes
        add_prefix    kubernetes.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      kubernetes.*
        nest_under    kubernetes
        remove_prefix kubernetes.
    {{- end }}
    [FILTER]
        name  modify
        match event.fluent-bit
        add   metadata.container_name ${CONTAINER_NAME}
    {{- if eq "false" ($default.logShipper.json_decode | toString) }}
    [FILTER]
        name          modify
        match         event.*
        condition     key_does_not_exist version
        add           version            1.2.0
    [FILTER]
        name          modify
        match         event.*
        condition     key_does_not_exist service_id
        add           service_id         {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname" . }}
    [FILTER]
        name          modify
        match         event.*
        condition     key_does_not_exist severity
        add           severity           info
    [FILTER]
        name  modify
        match event.*
        add   metadata.namespace ${NAMESPACE}
        add   metadata.node_name ${NODE_NAME}
        add   metadata.pod_name  ${POD_NAME}
        add   metadata.pod_uid   ${POD_UID}
    {{- end }}
    [FILTER]
        name          nest
        match         event.*
        operation     lift
        nested_under  metadata
        add_prefix    metadata.
    [FILTER]
        name          nest
        match         event.*
        operation     nest
        wildcard      metadata.*
        nest_under    metadata
        remove_prefix metadata.

  outputs.conf: |
    {{- $i := fromJson (include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-internal-parameters" .) -}}
    {{- if $i.internal.output.logTransformer.enabled }}
    [OUTPUT]
        name                 http
        match                event.*
        alias                log_transformer
        json_date_key        false
        host                 {{ $default.logShipper.output.logTransformer.host }}
        {{- if ne "false" (((((.Values).global).security).tls).enabled | toString) }}
        port                 9443
        {{- else }}
        port                 9080
        {{- end }}
        retry_limit          false
        log_response_payload false
        format               json
        {{- if ne "false" (((((.Values).global).security).tls).enabled | toString) }}
        tls                  on
        tls.verify           true
        tls.ca_file          {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.trustedInternalRootCa.mountPath" . }}/${LS_SIDECAR_CA_CERT_FILE}
        tls.crt_file         {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tlsCert.mountPath" . }}/${LS_SIDECAR_CERT_FILE}
        tls.key_file         {{ include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar.tlsCert.mountPath" . }}/${LS_SIDECAR_KEY_FILE}
        {{- end }}
    {{- end }}
    {{- if ((((.Values).logShipper).output).stdout).enabled }}
    [OUTPUT]
        name             stdout
        match            event.*
        alias            stdout
        format           json_lines
        json_date_format false
    {{- end }}
    {{- if ((((.Values).logShipper).output).file).enabled }}
    [OUTPUT]
        name   file
        match  event.*
        alias  file
        format plain
        path   {{ dir (printf "%s/" $storagePath) }}
        file   {{ base "output.log" }}
    {{- end }}
{{- end -}}

{{/*
Logshipper sidecar configmap for hook
Inputs:
  - root
  - customLogshipperAnnotations
*/}}
{{- define "eric-stm-diameter-proxy-grpc.logshipper-sidecar-configmap-for-hooks" -}}
  {{- $copied := deepCopy .root -}}
  {{- $merged := (mergeOverwrite $copied (dict "customLogshipperAnnotations" .customLogshipperAnnotations)) -}}

  {{- $name := include "eric-stm-diameter-proxy-grpc.log-shipper-sidecar-fullname-for-hooks" .root -}}
  {{- $merged := (mergeOverwrite $merged (dict "customLogshipperResourceName" $name)) -}}
  {{- include "eric-stm-diameter-proxy-grpc.logshipper-sidecar-configmap" $merged }}
{{- end -}}
