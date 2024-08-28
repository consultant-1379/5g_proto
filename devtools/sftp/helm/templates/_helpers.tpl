{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-atmoz-sftp.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if .Values.global -}}
        {{- if .Values.global.pullSecret -}}
            {{- $pullSecret = .Values.global.pullSecret -}}
        {{- end -}}
{{- else if .Values.imageCredentials.registry.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.registry.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}


{{/*
Create atmoz-sftp container image registry url
*/}}
{{- define "eric-atmoz-sftp.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.sftp.registry }}
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
Create atmoz-sftp container image repository path
*/}}
{{- define "eric-atmoz-sftp.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.sftp.repoPath }}  
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
Create atmoz-sftp container image name
*/}}
{{- define "eric-atmoz-sftp.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.sftp.name }}
{{- print $image -}}
{{- end -}}


{{/*
Create atmoz-sftp container tag
*/}}
{{- define "eric-atmoz-sftp.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $tag := $productInfo.images.sftp.tag }}
{{- print $tag -}}
{{- end -}}


{{/*
Create image pull policy for atmoz-sftp container
*/}}
{{- define "eric-atmoz-sftp.imagePullPolicy" -}}
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
