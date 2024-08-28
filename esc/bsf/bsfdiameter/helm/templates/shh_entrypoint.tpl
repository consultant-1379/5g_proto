{{- define "eric-stm-diameter-proxy-grpc.hkln.findSHH" -}}
{{- $currentPath := .path -}}
{{- range $candidate, $context := .root.Subcharts }}
{{- $shhSubcharts := $.shhSubcharts -}}
{{- $productName := "" -}}
{{- with $context -}}
{{- $productName = index (fromYaml (.Files.Get "eric-product-info.yaml")) "productName" -}}
{{- end -}}
{{- if eq $productName "eric-lcm-smart-helm-hooks" -}}
{{- $shhSubchart := append $currentPath $candidate -}}
{{- $shhSubcharts = append $shhSubcharts $shhSubchart -}}
{{- $_ := set $ "shhSubcharts" $shhSubcharts -}}
{{- else -}}
{{- $findSHHArgs := dict "root" $context "path" (append $currentPath $candidate) "shhSubcharts" $shhSubcharts -}}
{{- include "eric-stm-diameter-proxy-grpc.hkln.findSHH" $findSHHArgs -}}
{{- $_ := set $ "shhSubcharts" $findSHHArgs.shhSubcharts -}}
{{- end -}}
{{- end -}}
{{- end -}}


{{- define "eric-stm-diameter-proxy-grpc.hkln.entrypoint" -}}
{{- if and (eq (include "eric-stm-diameter-proxy-grpc.hkln.executor" .) "integration") (has (include "eric-stm-diameter-proxy-grpc.name" .) (include "eric-stm-diameter-proxy-grpc.hkln.executorCharts" . | fromYamlArray )) -}}
{{- $topContext := . -}}
{{- $shhContext := . -}}
{{- $findSHHArgs := dict "root" . "path" list "shhSubcharts" list -}}
{{- include "eric-stm-diameter-proxy-grpc.hkln.findSHH" $findSHHArgs -}}
{{- $paths := $findSHHArgs.shhSubcharts -}}
{{- if eq (len $paths) 0 -}}
{{- fail (printf "Smart Helm Hooks subchart is not included in the integration chart!") -}}
{{- end -}}
{{- if gt (len $paths) 1 -}}
{{- fail (printf "Found multiple Smart Helm Hooks subcharts: %s" $paths) -}}
{{- end -}}
{{- $path := index $paths 0 -}}
{{- $chart := index .Subcharts (first $path) -}}
{{- range $pathSegment := (rest $path) -}}
{{- $chart = index $chart.Subcharts $pathSegment -}}
{{- end -}}
{{- $shhContext = $chart -}}
{{- $_ := set $shhContext "Template" $topContext.Template -}}
{{- include "eric-stm-diameter-proxy-grpc.hkln.manifests" (dict "shh" $shhContext "top" $topContext) -}}
{{- end -}}
{{- end -}}


{{- define "eric-stm-diameter-proxy-grpc.hkln.manifests" -}}
{{- if or (eq (include "eric-stm-diameter-proxy-grpc.hkln.executor" .top) "service") (has (include "eric-stm-diameter-proxy-grpc.name" .top) (include "eric-stm-diameter-proxy-grpc.hkln.executorCharts" .top | fromYamlArray )) -}}
{{ include "eric-stm-diameter-proxy-grpc.hkln.jobs" . }}
{{ include "eric-stm-diameter-proxy-grpc.hkln.rbac" . }}
{{- end -}}
{{- end -}}
