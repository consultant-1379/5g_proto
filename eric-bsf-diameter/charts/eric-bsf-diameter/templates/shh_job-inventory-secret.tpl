{{- define "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents-lint-helper" -}}
shh-helper: {{ include "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents" . | trim | nindent 2 }}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents-lint" -}}
{{- $original_yaml := include "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents" . | trim -}}
{{- $helper_yaml := include "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents-lint-helper" . -}}

{{- /* Since fromYaml only works on helper_yaml it would give a wrong line number so fromYamlArray is used*/ -}}
{{- $yaml_error_check := $helper_yaml | fromYaml -}}
{{- $yaml_error_message := $original_yaml | fromYamlArray -}}
{{- if hasKey $yaml_error_check "Error" -}}
  {{- printf "\nError: Invalid yaml eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents \n%s\n\n%s" $original_yaml (index $yaml_error_message 0)| fail -}}
{{- end -}}
{{- end -}}

{{- define "eric-stm-diameter-proxy-grpc.hkln.job-inventory-secret" -}}
apiVersion: v1
kind: Secret
metadata:
  name: {{ include "eric-stm-diameter-proxy-grpc.hkln.job-inventory-secret-name" . }}
  labels:
    {{- include "eric-stm-diameter-proxy-grpc.hkln.labels" . | nindent 4 }}
  annotations:
    {{- $helmHook := dict -}}
    {{- $_ := set $helmHook "helm.sh/hook" "pre-upgrade,pre-rollback,post-install" -}}
    {{- $_ := set $helmHook "helm.sh/hook-weight" "-202" -}}
    {{- $commonAnn := fromYaml (include "eric-stm-diameter-proxy-grpc.hkln.annotations" .) -}}
    {{- include "eric-stm-diameter-proxy-grpc.mergeAnnotations" (dict "location" .Template.Name "sources" (list $helmHook $commonAnn)) | trim | nindent 4 }}
type: Opaque
data:
  job-inventory: {{ include "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents" . | trim | nindent 2 | b64enc }}
{{ include "eric-stm-diameter-proxy-grpc.hkln.job-inventory-contents-lint" . }}
{{- end -}}