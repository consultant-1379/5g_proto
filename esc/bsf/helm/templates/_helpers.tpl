{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}
{{- define "eric-bsf.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-bsf.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Define TLS, note: returns boolean as string
*/}}
{{- define "eric-bsf.tls" -}}
{{- $bsftls := true -}}
{{- if .Values.global -}}
    {{- if .Values.global.security -}}
        {{- if .Values.global.security.tls -}}
            {{- if hasKey .Values.global.security.tls "enabled" -}}
                {{- $bsftls = .Values.global.security.tls.enabled -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $bsftls -}}
{{- end -}}

{{/*
Define BSF diameter, note: returns boolean as string
*/}}
{{- define "eric-bsf-diameter.enabled" -}}
{{- $bsfdiameter := false -}}
{{- if .Values.global -}}
    {{- if  .Values.global.ericsson -}}
        {{- if  .Values.global.ericsson.bsfdiameter -}}
           {{- if  .Values.global.ericsson.bsfdiameter.enabled -}}
                {{- $bsfdiameter = .Values.global.ericsson.bsfdiameter.enabled -}}
           {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $bsfdiameter -}}
{{- end -}}

{{/*
Define dcedsc TLS for worker/manager, note: returns boolean as string
*/}}
{{- define "eric-bsf.manager.dcedsc.tls" -}}
{{- $dcedsctls := true -}}
{{- if .Values.service.manager.client.dcedsc.tls.enabled -}}
    {{- $dcedsctls = .Values.service.manager.client.dcedsc.tls.enabled -}}
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
Define dcedsc TLS for worker/manager, note: returns boolean as string
*/}}
{{- define "eric-bsf.worker.dcedsc.tls" -}}
{{- $dcedsctls := true -}}
{{- if .Values.service.worker.client.dcedsc.tls.enabled -}}
    {{- $dcedsctls = .Values.service.worker.client.dcedsc.tls.enabled -}}
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
Create setupmanager container image registry url
*/}}
{{- define "eric-bsf.setupmanager.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.setupmanager.registry -}}
{{- if .Values.imageCredentials.setupmanager.registry.url -}}
    {{- $url = .Values.imageCredentials.setupmanager.registry.url -}}
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
Create manager container image registry url
*/}}
{{- define "eric-bsf.manager.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.manager.registry -}}
{{- if .Values.imageCredentials.manager.registry.url -}}
    {{- $url = .Values.imageCredentials.manager.registry.url -}}
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
Create worker container image registry url
*/}}
{{- define "eric-bsf.worker.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.worker.registry -}}
{{- if .Values.imageCredentials.worker.registry.url -}}
    {{- $url = .Values.imageCredentials.worker.registry.url -}}
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
Create cddjmxexporter container image registry url
*/}}
{{- define "eric-bsf.cddjmxexporter.registryUrl" -}}
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

{{/*
Create certnotifier container image registry url
*/}}
{{- define "eric-bsf.certnotifier.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.certnotifier.registry -}}
{{- if .Values.imageCredentials.certnotifier.registry.url -}}
    {{- $url = .Values.imageCredentials.certnotifier.registry.url -}}
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
{{- define "eric-bsf.tapagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tapagent.registry -}}
{{- if .Values.imageCredentials.tapagent.registry.url -}}
    {{- $url = .Values.imageCredentials.tapagent.registry.url -}}
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
{{- define "eric-bsf.tlskeylogagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tlskeylogagent.registry -}}
{{- if .Values.imageCredentials.tlskeylogagent.registry.url -}}
    {{- $url = .Values.imageCredentials.tlskeylogagent.registry.url -}}
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
Create tapcollector container image registry url
*/}}
{{- define "eric-bsf.tapcollector.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tapcollector.registry -}}
{{- if .Values.imageCredentials.tapcollector.registry.url -}}
    {{- $url = .Values.imageCredentials.tapcollector.registry.url -}}
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
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-bsf.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if .Values.imageCredentials.pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
{{- else -}}
    {{- if .Values.global -}}
        {{- if .Values.global.pullSecret -}}
            {{- $pullSecret = .Values.global.pullSecret -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create image pull policy manager setup container
*/}}
{{- define "eric-bsf.setupmanager.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.setupmanager.registry -}}
    {{- if .Values.imageCredentials.setupmanager.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.setupmanager.registry.imagePullPolicy -}}
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

{{/*
Create image pull policy manager container
*/}}
{{- define "eric-bsf.manager.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.manager.registry -}}
    {{- if .Values.imageCredentials.manager.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.manager.registry.imagePullPolicy -}}
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

{{/*
Create image pull policy worker container
*/}}
{{- define "eric-bsf.worker.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.worker.registry -}}
    {{- if .Values.imageCredentials.worker.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.worker.registry.imagePullPolicy -}}
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

{{/*
Create image pull policy tapagent container
*/}}
{{- define "eric-bsf.tapagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.tapagent.registry -}}
    {{- if .Values.imageCredentials.tapagent.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.tapagent.registry.imagePullPolicy -}}
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

{{/*
Create image pull policy tlskeylogagent container
*/}}
{{- define "eric-bsf.tlskeylogagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.tlskeylogagent.registry -}}
    {{- if .Values.imageCredentials.tlskeylogagent.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.tlskeylogagent.registry.imagePullPolicy -}}
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

{{/*
Create image pull policy tapcollector container
*/}}
{{- define "eric-bsf.tapcollector.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.tapcollector.registry -}}
    {{- if .Values.imageCredentials.tapcollector.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.tapcollector.registry.imagePullPolicy -}}
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

{{/*
Create image pull policy worker container
*/}}
{{- define "eric-bsf.cddjmxexporter.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.cddjmxexporter.registry -}}
    {{- if .Values.imageCredentials.cddjmxexporter.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.cddjmxexporter.registry.imagePullPolicy -}}
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

Create image pull policy certnotifier container
*/}}
{{- define "eric-bsf.certnotifier.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if .Values.imageCredentials.certnotifier.registry -}}
    {{- if .Values.imageCredentials.certnotifier.registry.imagePullPolicy -}}
        {{- $imagePullPolicy = .Values.imageCredentials.certnotifier.registry.imagePullPolicy -}}
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

{{/*
Create cassandra auth - user secret.
*/}}
{{- define "eric-bsf-data-wide-column-database-cd.security.auth.cql.adminSecret" -}}
{{- $adminSecret := index .Values "cassandra" "auth" "adminSecret" -}}
{{- toYaml $adminSecret -}}
{{- end -}}

{{/*
Create nodeSelector for worker
*/}}
{{- define "eric-bsf-worker.nodeSelector" -}}
{{- $nodeSelector := dict -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
        {{- $nodeSelector = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector }}
    {{- if .Values.nodeSelector.worker }}
        {{- range $key, $localValue := .Values.nodeSelector.worker -}}
            {{- if hasKey $nodeSelector $key -}}
                {{- $globalValue := index $nodeSelector $key -}}
                {{- if ne $globalValue $localValue -}}
                    {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
        {{- $nodeSelector = merge $nodeSelector .Values.nodeSelector.worker -}}
    {{- end -}}
{{- end -}}
{{- if $nodeSelector -}}
    {{- toYaml $nodeSelector | indent 8 | trim -}}
{{- end -}}
{{- end -}}

{{/*
Create nodeSelector for manager
*/}}
{{- define "eric-bsf-manager.nodeSelector" -}}
{{- $nodeSelector := dict -}}
{{- if .Values.global -}}
    {{- if .Values.global.nodeSelector -}}
        {{- $nodeSelector = .Values.global.nodeSelector -}}
    {{- end -}}
{{- end -}}
{{- if .Values.nodeSelector }}
    {{- if .Values.nodeSelector.manager }}
        {{- range $key, $localValue := .Values.nodeSelector.manager -}}
            {{- if hasKey $nodeSelector $key -}}
                {{- $globalValue := index $nodeSelector $key -}}
                {{- if ne $globalValue $localValue -}}
                    {{- printf "nodeSelector \"%s\" is specified in both global (%s: %s) and service level (%s: %s) with differing values which is not allowed." $key $key $globalValue $key $localValue | fail -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
        {{- $nodeSelector = merge $nodeSelector .Values.nodeSelector.manager -}}
    {{- end -}}
{{- end -}}
{{- if $nodeSelector -}}
    {{- toYaml $nodeSelector | indent 8 | trim -}}
{{- end -}}
{{- end -}}

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

{{/*
    Define tapagent worker enabled
*/}}
{{- define "eric-bsf.tapagent.worker.enabled" -}}
{{- if  .Values.tapagent.worker -}}
    {{- if  .Values.tapagent.worker.enabled -}}
        {{- .Values.tapagent.worker.enabled | toString -}}
    {{- else -}}
        {{- "false" -}}
    {{- end -}}
{{- else -}}
    {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
    Define tapagent manager enabled
*/}}
{{- define "eric-bsf.tapagent.manager.enabled" -}}
{{- if  .Values.tapagent.manager -}}
    {{- if  .Values.tapagent.manager.enabled -}}
        {{- .Values.tapagent.manager.enabled | toString -}}
    {{- else -}}
        {{- "false" -}}
    {{- end -}}
{{- else -}}
    {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
    Define tapcollector worker enabled
*/}}
{{- define "eric-bsf.tapcollector.worker.enabled" -}}
{{- if  .Values.tapcollector.worker -}}
    {{- if  .Values.tapcollector.worker.enabled -}}
        {{- .Values.tapcollector.worker.enabled | toString -}}
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
{{- define "eric-bsf.ipfamilies" -}}
{{- if .Release.IsInstall }}
{{- if .Values.global }}
  {{- if .Values.global.internalIPFamily }}
ipFamilies: [{{ .Values.global.internalIPFamily | quote }}]
  {{- end }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-bsf.ipfamilypolicy" -}}
{{- if or .Values.service.worker.ipFamilyPolicy .Values.global }}
    {{- if .Values.service.worker.ipFamilyPolicy }}
ipFamilyPolicy: {{ .Values.service.worker.ipFamilyPolicy | quote }}
    {{- else if .Values.global.ipFamilyPolicy }}
ipFamilyPolicy: {{ .Values.global.ipFamilyPolicy | quote }}
    {{- else }}
ipFamilyPolicy: "PreferDualStack"
    {{- end }}
{{- end }}
{{- end }}


{{- define "eric-bsf.ipfamily" -}}
{{- if .Values.global -}}
    {{- if .Values.global.internalIPFamily }}
        {{- .Values.global.internalIPFamily | toString -}}
    {{- else -}}
        {{- "IPv4" -}}
    {{- end -}}
{{- else -}}
    {{- "IPv4" -}}
{{- end }}
{{- end }}

{{- define "eric-bsf.enabled-IPv4" -}}
{{- if or .Values.service.worker.externalIPv4 .Values.global }}
    {{- if .Values.service.worker.externalIPv4.enabled }}
{{- .Values.service.worker.externalIPv4.enabled -}}
    {{- else }}
        {{- if .Values.global }}
            {{- if .Values.global.externalIPv4 }}
                {{- if .Values.global.externalIPv4.enabled }}
{{- .Values.global.externalIPv4.enabled -}}
                {{- end }}
            {{- end }}
        {{- end }}
    {{- end }}
{{- end }}
{{- end }}

{{- define "eric-bsf.enabled-IPv6" -}}
{{- if or .Values.service.worker.externalIPv6 .Values.global }}
    {{- if .Values.service.worker.externalIPv6.enabled }}
{{- .Values.service.worker.externalIPv6.enabled -}}
    {{- else }}
        {{- if .Values.global }}
            {{- if .Values.global.externalIPv6 }}
                {{- if .Values.global.externalIPv6.enabled }}
{{- .Values.global.externalIPv6.enabled -}}
                {{- end }}
            {{- end }}
        {{- end }}
    {{- end }}
{{- end }}
{{- end }}

{{/*
    Define vtap worker enabled
*/}}
{{- define "eric-bsf-worker.vtap.enabled" -}}
{{- if  .Values.tapcollector.worker -}}
    {{- if  .Values.tapcollector.worker.enabled -}}
        {{- if  .Values.global -}}
            {{- if  .Values.global.ericsson -}}
                {{- if  .Values.global.ericsson.pvtb -}}
                    {{- if  .Values.global.ericsson.pvtb.enabled -}}
                        {{- if and ( eq ( .Values.global.ericsson.pvtb.enabled | toString ) "true" ) ( eq ( .Values.tapcollector.worker.enabled | toString ) "true" )}}
                            {{- "true" -}}
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

{{/*
Create IPv4 boolean service/global/<notset>
*/}}
{{- define "eric-bsf-worker-service.enabled-IPv4" -}}
{{- if .Values.service.worker.externalIPv4.enabled -}}
{{- .Values.service.worker.externalIPv4.enabled -}}
{{- else -}}
{{- if .Values.global -}}
{{- if .Values.global.externalIPv4 -}}
{{- if .Values.global.externalIPv4.enabled -}}
{{- .Values.global.externalIPv4.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{/*
Create IPv6 boolean service/global/<notset>
*/}}
{{- define "eric-bsf-worker-service.enabled-IPv6" -}}
{{- if .Values.service.worker.externalIPv6.enabled -}}
{{- .Values.service.worker.externalIPv6.enabled -}}
{{- else -}}
{{- if .Values.global -}}
{{- if .Values.global.externalIPv6 -}}
{{- if .Values.global.externalIPv6.enabled -}}
{{- .Values.global.externalIPv6.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-bsf.chartName" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{- define "eric-bsf.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productRevision | quote }}
ericsson.com/nf-name: "BSF"
{{- end}}

{{- define "eric-bsf.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-bsf.workerLB-annotations" }}
{{- if .Values.service -}}
{{- if .Values.service.worker -}}
{{- if .Values.service.worker.annotations -}}
{{- if .Values.service.worker.annotations.cloudProviderLB -}}
{{- range $name, $config := .Values.service.worker.annotations.cloudProviderLB }}
{{ $name }}: {{ tpl $config $ }}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-bsf.metallb-annotations" }}
{{- if .Values.service.worker.annotations.loadBalancerIPs }}
metallb.universe.tf/loadBalancerIPs: {{ .Values.service.worker.annotations.loadBalancerIPs }}
{{- end }}
{{- end }}

{{- define "eric-bsf.labels" -}}
{{- include "eric-bsf.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

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

{{- define "eric-bsf-manager.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-bsf-manager.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-bsf.labels" . | fromYaml -}}
{{- include "eric-bsf.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies for bsf-manager
*/}}
{{- define "eric-bsf-manager.peer.labels" -}}
{{- $peers := list }}
{{- $peers = append $peers .Values.adp.cm.yangProvider.hostname }}
{{- $peers = append $peers .Values.adp.cm.mediator.hostname }}
{{- $peers = append $peers .Values.adp.fh.alarmHandler.hostname }}
{{- $peers = append $peers .Values.adp.pm.server.hostname }}
{{- $peers = append $peers .Values.adp.lm.combinedServer.hostname }}
{{- $peers = append $peers .Values.sc.common.etcd.hostname }}
{{- $peers = append $peers .Values.adp.data.wcdbcd.hostname }}
{{- $peers = append $peers .Values.adp.log.transformer.hostname }}
{{- $peers = append $peers .Values.sc.common.monitor.hostname }}
{{- $peers = append $peers .Values.adp.sec.kms.hostname }}
{{- template "eric-bsf.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}

{{- define "eric-bsf-worker.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-bsf-worker.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-bsf.labels" . | fromYaml -}}
{{- include "eric-bsf.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies of bsf-worker
*/}}
{{- define "eric-bsf-worker.peer.labels" -}}
{{- $peers1 := list }}
{{- $peers1 = append $peers1 .Values.adp.cm.mediator.hostname }}
{{- $peers1 = append $peers1 .Values.adp.pm.server.hostname }}
{{- $peers1 = append $peers1 .Values.sc.common.etcd.hostname }}
{{- $peers1 = append $peers1 .Values.adp.data.wcdbcd.hostname }}
{{- $peers1 = append $peers1 .Values.sc.common.manager.hostname }}
{{- $peers1 = append $peers1 .Values.adp.log.transformer.hostname }}
{{- $peers1 = append $peers1 .Values.sc.common.monitor.hostname }}
{{- $peers1 = append $peers1 .Values.adp.sec.kms.hostname }}
{{- template "eric-bsf.generate-peer-labels" (dict "peers" $peers1) }}
{{- end -}}

{{- define "eric-bsf.setupmanager.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.setupmanager.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.setupmanager -}}
        {{- if .Values.imageCredentials.setupmanager.repoPath }}
            {{- $repoPath = .Values.imageCredentials.setupmanager.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-bsf.manager.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.manager.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.manager -}}
        {{- if .Values.imageCredentials.manager.repoPath }}
            {{- $repoPath = .Values.imageCredentials.manager.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-bsf.worker.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.worker.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.worker -}}
        {{- if .Values.imageCredentials.worker.repoPath }}
            {{- $repoPath = .Values.imageCredentials.worker.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-bsf.cddjmxexporter.repoPath" -}}
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

{{- define "eric-bsf.certnotifier.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.certnotifier.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.certnotifier -}}
        {{- if .Values.imageCredentials.certnotifier.repoPath }}
            {{- $repoPath = .Values.imageCredentials.certnotifier.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-bsf.tapagent.repoPath" -}}
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

{{- define "eric-bsf.tlskeylogagent.repoPath" -}}
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

{{- define "eric-bsf.tapcollector.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.tapcollector.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.tapcollector -}}
        {{- if .Values.imageCredentials.tapcollector.repoPath }}
            {{- $repoPath = .Values.imageCredentials.tapcollector.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-bsf.setupmanager.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.setupmanager.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.manager.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.manager.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.worker.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.worker.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.cddjmxexporter.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.cddjmxexporter.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.certnotifier.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.certnotifier.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.tapagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.tlskeylogagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tlskeylogagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.tapcollector.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapcollector.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.setupmanager.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.setupmanager.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.manager.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.manager.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.worker.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.worker.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.cddjmxexporter.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.cddjmxexporter.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.certnotifier.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.certnotifier.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.tapagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapagent.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.tlskeylogagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tlskeylogagent.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-bsf.tapcollector.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapcollector.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-bsf.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
Return the log streaming type, default is indirect
*/}}
{{- define "eric-bsf.streamingMethod" -}}
{{- $streamingMethod := "indirect" -}}
{{- if (.Values.log).streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
{{- else if ((.Values.global).log).streamingMethod -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
{{- end -}}
{{- if not (has $streamingMethod (list "indirect" "direct" "dual")) -}}
    {{- fail "Incorrect value for streamingMethod in either global or local-bsf values.yaml. Possible values: indirect, direct or dual" -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define LOGBACK file to be used, note: returns logback xml file
*/}}
{{- define "eric-bsf.logbackFileName" -}}
{{- $streamingMethod := include "eric-bsf.streamingMethod" . -}}
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
{{- define "eric-bsf.logshipper-enabled" -}}
{{- $streamingMethod := include "eric-bsf.streamingMethod" . -}}
{{- $enabled := "false" -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) -}}
    {{- $enabled = "true" -}}
{{- end -}}
{{- print $enabled -}}
{{- end -}}

{{/*
Determines if license monitoring is enabled
*/}}
{{- define "eric-bsf.license-consumer.enabled" -}}
{{- $enabled := "false" -}}
{{- if ((((((.Values).global).licenseConsumer).features).monitoring).licenses).enabled }}
{{- $enabled = "true" -}}
{{- end }}
{{- print $enabled -}}
{{- end -}}
