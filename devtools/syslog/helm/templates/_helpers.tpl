{{- define "eric-syslog.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}


{{- define "eric-syslog.tls" -}}
{{- if .Values.service.tls.enabled | quote -}}
    {{- .Values.service.tls.enabled -}}
{{- else -}}
    {{- if .Values.global -}}
    {{- if .Values.global.tls -}}
    {{- if .Values.global.tls.enabled | quote -}}
        {{- .Values.global.tls.enabled -}}
    {{- end -}}
    {{- end -}}
    {{- end -}}
{{- end -}}
{{- end -}}


{{- define "eric-syslog.cr" -}}
{{- if .Values.service -}}
{{- if .Values.service.tls -}}
{{- if .Values.service.tls.useCr -}}
{{- if .Values.service.tls.useCr.enabled | quote -}}
    {{- .Values.service.tls.useCr.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}


{{/*
Create eric-syslog container image registry url
*/}}
{{- define "eric-syslog.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.syslog.registry }}
{{- if .Values.global -}}
    {{- if .Values.global.registry -}}
    {{- if .Values.global.registry.url -}}
        {{- $url = .Values.global.registry.url -}}
    {{- end -}}
    {{- end -}}
{{- else  if .Values.imageCredentials.registry.url -}}
    {{- $url = .Values.imageCredentials.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}


{{/*
Create eric-syslog container image repository path
*/}}
{{- define "eric-syslog.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.syslog.repoPath }}  
{{- if .Values.global -}}  
    {{- if .Values.global.repoPath -}}
        {{- $url = .Values.global.repoPath -}}  
    {{- end -}}
{{- else  if .Values.imageCredentials.repoPath -}}
    {{- $url = .Values.imageCredentials.repoPath -}}   
{{- end -}}
{{- print $repoPath -}}
{{- end -}}


{{/*
Create eric-syslog container image name
*/}}
{{- define "eric-syslog.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.syslog.name }}
{{- print $image -}}
{{- end -}}


{{/*
Create eric-syslog container tag
*/}}
{{- define "eric-syslog.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $tag := $productInfo.images.syslog.tag }}
{{- print $tag -}}
{{- end -}}


{{/*
Create image pull policy for eric-syslog container
*/}}
{{- define "eric-syslog.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.registry -}}
    {{- if .Values.imageCredentials.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.registry.imagePullPolicy -}}
    {{- end -}}
{{- else if .Values.global -}}
    {{- if .Values.global.registry -}}
        {{- if .Values.global.registry.imagePullPolicy -}}
            {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}