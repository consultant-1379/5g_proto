{{- define "eric-stm-diameter-proxy-grpc.hkln.job" -}}
{{- $containerName := include "eric-stm-diameter-proxy-grpc.hkln.containerName" .root.shh -}}
apiVersion: batch/v1
kind: Job
metadata:
  name: {{ template "eric-stm-diameter-proxy-grpc.hkln.name" .root.top }}-{{ .suffix }}
  labels:
    {{- include "eric-stm-diameter-proxy-grpc.hkln.labels" .root.shh | nindent 4 }}
  annotations:
    {{- $helmHook := dict -}}
    {{- $_ := set $helmHook "helm.sh/hook" .helmHook -}}
    {{- $_ := set $helmHook "helm.sh/hook-weight" .weight -}}
    {{- $_ := set $helmHook "helm.sh/hook-delete-policy" "before-hook-creation,hook-succeeded" -}}
    {{- $commonAnn := fromYaml (include "eric-stm-diameter-proxy-grpc.hkln.annotations" .root.shh) -}}
    {{- include "eric-stm-diameter-proxy-grpc.mergeAnnotations" (dict "location" .root.shh.Template.Name "sources" (list $helmHook $commonAnn)) | trim | nindent 4 }}
spec:
  template:
    metadata:
      labels:
        {{- include "eric-stm-diameter-proxy-grpc.hkln.labels" .root.shh | nindent 8 }}
      annotations:
        {{- $appArmorAnn := include "eric-stm-diameter-proxy-grpc.hkln.appArmorProfileAnn" (dict "root" .root.shh "containerName" $containerName) | fromYaml -}}
        {{- $commonAnn := fromYaml (include "eric-stm-diameter-proxy-grpc.hkln.annotations" .root.shh) -}}
        {{- include "eric-stm-diameter-proxy-grpc.mergeAnnotations" (dict "location" .root.shh.Template.Name "sources" (list $appArmorAnn $commonAnn)) | trim | nindent 8 }}
    spec:
      {{- if include "eric-stm-diameter-proxy-grpc.hkln.pullSecrets" .root.shh }}
      imagePullSecrets:
        - name: {{ template "eric-stm-diameter-proxy-grpc.hkln.pullSecrets" .root.shh }}
      {{- end }}
      containers:
        - name: {{ $containerName }}
          image: {{ include "eric-stm-diameter-proxy-grpc.hkln.image-path" .root.shh }}
          env:
            - name: TZ
              value: {{ include "eric-stm-diameter-proxy-grpc.hkln.timezone" .root.shh }}
          args: [
            "/hooklauncher/hooklauncher",
            "--namespace", {{ .root.shh.Release.Namespace | quote }},

            {{- $chartInfo := include "eric-stm-diameter-proxy-grpc.hkln.chartInfo" .root.top | fromYaml -}}
            {{- range $subChartName, $subChartInfo := $chartInfo }}
            "--job-inventory-secret",
            {{ $subChartInfo.jobInventorySecret | quote }},
            "--this-version",
            {{ $subChartInfo.version | quote }},
            {{- end }}
            "--instance", {{ include "eric-stm-diameter-proxy-grpc.hkln.name" .root.top | quote }},
            "--this-job", {{ include "eric-stm-diameter-proxy-grpc.hkln.name" .root.top }}-{{ .suffix }},
            "--trigger", {{ .trigger | quote }},
            "--cleanup", {{ include "eric-stm-diameter-proxy-grpc.hkln.cleanup" .root.shh | quote }},
            "--terminate-early={{ template "eric-stm-diameter-proxy-grpc.hkln.terminateEarlyOnFailure" .root.shh }}",
            "--incluster"
          ]
          imagePullPolicy: {{ template "eric-stm-diameter-proxy-grpc.hkln.imagePullPolicy" .root.shh }}
          {{- if include "eric-stm-diameter-proxy-grpc.hkln.resources" .root.shh }}
          resources:
            {{- include "eric-stm-diameter-proxy-grpc.hkln.resources" .root.shh | trim | nindent 12 }}
          {{- end }}
          securityContext:
            allowPrivilegeEscalation: false
            privileged: false
            readOnlyRootFilesystem: true
            runAsNonRoot: true
            capabilities:
              drop:
                - ALL
            {{- if include "eric-stm-diameter-proxy-grpc.hkln.seccompProfile" (dict "root" .root.shh "Scope" $containerName) }}
            seccompProfile:
              {{- include "eric-stm-diameter-proxy-grpc.hkln.seccompProfile" (dict "root" .root.shh "Scope" $containerName) | trim | nindent 14 }}
            {{- end }}
      restartPolicy: OnFailure
      serviceAccountName: {{ template "eric-stm-diameter-proxy-grpc.hkln.name" .root.top }}
      {{- if include "eric-stm-diameter-proxy-grpc.hkln.priorityClassName" .root.shh }}
      priorityClassName: {{ include "eric-stm-diameter-proxy-grpc.hkln.priorityClassName" .root.shh }}
      {{- end }}
      {{- if (include "eric-stm-diameter-proxy-grpc.hkln.tolerations" .root.shh | fromYamlArray) }}
      tolerations: {{- include "eric-stm-diameter-proxy-grpc.hkln.tolerations" .root.shh | nindent 8 }}
      {{- end }}
      {{- if include "eric-stm-diameter-proxy-grpc.hkln.nodeSelector" .root.shh }}
      nodeSelector: {{- include "eric-stm-diameter-proxy-grpc.hkln.nodeSelector" .root.shh | trim | nindent 8 }}
      {{- end }}
      {{- if include "eric-stm-diameter-proxy-grpc.hkln.seccompProfile" (dict "root" .root.shh "Scope" "Pod") }}
      securityContext:
        seccompProfile:
          {{- include "eric-stm-diameter-proxy-grpc.hkln.seccompProfile" (dict "root" .root.shh "Scope" "Pod") | trim | nindent 10 }}
      {{- end }}
  backoffLimit: {{ template "eric-stm-diameter-proxy-grpc.hkln.backoffLimit" .root.shh | default 6 }}
{{- end -}}
