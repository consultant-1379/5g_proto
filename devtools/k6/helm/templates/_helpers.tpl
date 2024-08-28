{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-k6.pullSecrets" -}}
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
Create k6 container image registry url
*/}}
{{- define "eric-k6.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.k6.registry -}}  
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
Create k6 container image repository path
*/}}
{{- define "eric-k6.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.k6.repoPath -}}  
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
Create k6 container image name
*/}}
{{- define "eric-k6.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.k6.name -}}
{{- print $image -}}
{{- end -}}


{{/*
Create k6 container tag
*/}}
{{- define "eric-k6.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $tag := $productInfo.images.k6.tag -}}
{{- print $tag -}}
{{- end -}}


{{/*
Create image pull policy for eric-k6 container
*/}}
{{- define "eric-k6.imagePullPolicy" -}}
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