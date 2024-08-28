{{/* vim: set filetype=mustache: */}}

{{/*
Expand the name of the chart.
We truncate to 20 characters because this is used to set the node identifier in WildFly which is limited to
23 characters. This allows for a replica suffix for up to 99 replicas.
*/}}
{{- define "eric-sepp.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 20 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create version as used by the chart label.
*/}}
{{- define "eric-sepp.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" | quote -}}
{{- end -}}

{{/*
Define TLS, note: returns boolean as string
*/}}
{{- define "eric-sepp.tls" -}}
{{- $sepptls:= true -}}
{{- if .Values.global -}}
    {{- if  .Values.global.security -}}
        {{- if .Values.global.security.tls -}}
            {{- if hasKey .Values.global.security.tls "enabled" -}}
                {{- $sepptls = .Values.global.security.tls.enabled -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $sepptls -}}
{{- end -}}

{{/*
Define dcedsc TLS for manager, note: returns boolean as string
*/}}
{{- define "eric-sepp.dcedsc.tls" -}}
{{- $dcedsctls := true -}}
{{- if (((((.Values.service).manager).client).dcedsc).tls).enabled -}}
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
Define n32c TLS for manager worker internal communication, initiating SEPP
case, note: returns boolean as string
*/}}
{{- define "eric-sepp.n32c.init.tls" -}}
{{- $n32ctls := true -}}
{{- if (((((.Values.service).manager).client).n32c).tls).enabled -}}
    {{- $n32ctls = .Values.service.manager.client.n32c.tls.enabled -}}
{{- else -}}
    {{- if .Values.global -}}
        {{- if .Values.global.security -}}
            {{- if .Values.global.security.tls -}}
                {{- if hasKey .Values.global.security.tls "enabled" -}}
                    {{- $n32ctls = .Values.global.security.tls.enabled -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $n32ctls -}}
{{- end -}}

{{/*
Define n32c TLS for worker - manager internal communication, responding SEPP
case, note: returns boolean as string
*/}}
{{- define "eric-sepp.n32c.resp.tls" -}}
{{- $n32ctls := true -}}
{{- if (((((.Values.service).worker).client).tls).n32c).enabled -}}
    {{- $n32ctls = .Values.service.worker.client.tls.n32c.enabled -}}
{{- else -}}
    {{- if .Values.global -}}
        {{- if .Values.global.security -}}
            {{- if .Values.global.security.tls -}}
                {{- if hasKey .Values.global.security.tls "enabled" -}}
                    {{- $n32ctls = .Values.global.security.tls.enabled -}}
                {{- end -}}
            {{- end -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- $n32ctls -}}
{{- end -}}

{{/*
Create setupmanager container image registry url
*/}}
{{- define "eric-sepp.setupmanager.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.setupmanager.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).setupmanager).registry).url -}}
    {{- $url = .Values.imageCredentials.setupmanager.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create manager container image registry url
*/}}
{{- define "eric-sepp.manager.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.manager.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).manager).registry).url -}}
    {{- $url = .Values.imageCredentials.manager.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create setupworker container image registry url
*/}}
{{- define "eric-sepp.setupworker.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.setupworker.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).setupworker).registry).url -}}
    {{- $url = .Values.imageCredentials.setupworker.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create worker container image registry url
*/}}
{{- define "eric-sepp.worker.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.worker.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).worker).registry).url -}}
    {{- $url = .Values.imageCredentials.worker.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create sds container image registry url
*/}}
{{- define "eric-sepp.sds.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.sds.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).sds).registry).url -}}
    {{- $url = .Values.imageCredentials.sds.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create certnotifier container image registry url
*/}}
{{- define "eric-sepp.certnotifier.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.certnotifier.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).certnotifier).registry).url -}}
    {{- $url = .Values.imageCredentials.certnotifier.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create logfwdr container image registry url
*/}}
{{- define "eric-sepp.logfwdr.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.logfwdr.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).logfwdr).registry).url -}}
    {{- $url = .Values.imageCredentials.logfwdr.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}


{{/*
Create image pull policy manager setup container
*/}}
{{- define "eric-sepp.setupmanager.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).setupmanager).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.setupmanager.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy manager container
*/}}
{{- define "eric-sepp.manager.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).manager).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.manager.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy worker setup container
*/}}
{{- define "eric-sepp.setupworker.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).setupworker).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.setupworker.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy worker container
*/}}
{{- define "eric-sepp.worker.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).worker).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.worker.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy sds container
*/}}
{{- define "eric-sepp.sds.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).sds).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.sds.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy certnotifier container
*/}}
{{- define "eric-sepp.certnotifier.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).certnotifier).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.certnotifier.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create tapagent container image registry url
*/}}
{{- define "eric-sepp.tapagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tapagent.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).tapagent).registry).url -}}
    {{- $url = .Values.imageCredentials.tapagent.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create tlskeylogagent container image registry url
*/}}
{{- define "eric-sepp.tlskeylogagent.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tlskeylogagent.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).tlskeylogagent).registry).url -}}
    {{- $url = .Values.imageCredentials.tlskeylogagent.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create tapcollector container image registry url
*/}}
{{- define "eric-sepp.tapcollector.registryUrl" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $url := $productInfo.images.tapcollector.registry -}}
{{- if ((.Values.global).registry).url -}}
    {{- $url = .Values.global.registry.url -}}
{{- end -}}
{{- if (((.Values.imageCredentials).tapcollector).registry).url -}}
    {{- $url = .Values.imageCredentials.tapcollector.registry.url -}}
{{- end -}}
{{- print $url -}}
{{- end -}}

{{/*
Create image pull policy logfwdr container
*/}}
{{- define "eric-sepp.logfwdr.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).logfwdr).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.logfwdr.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy tapagent container
*/}}
{{- define "eric-sepp.tapagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).tapagent).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.tapagent.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy tlskeylogagent container
*/}}
{{- define "eric-sepp.tlskeylogagent.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).tlskeylogagent).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.tlskeylogagent.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull policy tapcollector container
*/}}
{{- define "eric-sepp.tapcollector.imagePullPolicy" -}}
{{- $imagePullPolicy := "IfNotPresent" -}}
{{- if ((.Values.global).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.global.registry.imagePullPolicy -}}
{{- end -}}
{{- if (((.Values.imageCredentials).tapcollector).registry).imagePullPolicy -}}
    {{- $imagePullPolicy = .Values.imageCredentials.tapcollector.registry.imagePullPolicy -}}
{{- end -}}
{{- print $imagePullPolicy -}}
{{- end -}}

{{/*
Create image pull secret, service level parameter takes precedence.
Default:
*/}}
{{- define "eric-sepp.pullSecrets" -}}
{{- $pullSecret := "" -}}
{{- if (.Values.global).pullSecret -}}
    {{- $pullSecret = .Values.global.pullSecret -}}
{{- end -}}
{{- if (.Values.imageCredentials).pullSecret -}}
    {{- $pullSecret = .Values.imageCredentials.pullSecret -}}
{{- end -}}
{{- print $pullSecret -}}
{{- end -}}

{{/*
Create nodeSelector for worker
*/}}
{{- define "eric-sepp-worker.nodeSelector" -}}
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
{{- define "eric-sepp-manager.nodeSelector" -}}
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
    Define tapagent worker enabled
*/}}
{{- define "eric-sepp.tapagent.worker.enabled" -}}
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
{{- define "eric-sepp.tapagent.manager.enabled" -}}
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
{{- define "eric-sepp.tapcollector.worker.enabled" -}}
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

{{- define "eric-sepp-worker.vtap.enabled" -}}
{{- if and ( eq ( (.Values.vtap).enabled | toString ) "true" ) ( eq ( ((.Values.tapcollector).worker).enabled | toString ) "true" )}}
    {{- "true" -}}
{{- else -}}
    {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
    Define rlf enabled
*/}}
{{- define "rlf.enabled" -}}
{{- if  ( eq ( (.Values.rlf).enabled | toString ) "true" ) }}
    {{- "true" -}}
{{- else -}}
    {{- "false" -}}
{{- end -}}
{{- end -}}

{{/*
    Define ipFamilies introduced in K8s for dual stack support
*/}}
{{- define "eric-sepp.ipfamilies" -}}
{{- if .Release.IsInstall }}
{{- if .Values.global }}
  {{- if .Values.global.internalIPFamily }}
ipFamilies: [{{ .Values.global.internalIPFamily | quote }}]
  {{- end }}
{{- end }}
{{- end }}
{{- end }}

{{- define "eric-sepp.ipfamilypolicy" -}}
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

{{- define "eric-sepp.multiVpn.ipfamilypolicy" -}}
{{- if or .Values.service.worker.multiVpn.ipFamilyPolicy .Values.global }}
    {{- if .Values.service.worker.multiVpn.ipFamilyPolicy }}
ipFamilyPolicy: {{ .Values.service.worker.multiVpn.ipFamilyPolicy | quote }}
    {{- else if .Values.global.ipFamilyPolicy }}
ipFamilyPolicy: {{ .Values.global.ipFamilyPolicy | quote }}
    {{- else }}
ipFamilyPolicy: "PreferDualStack"
    {{- end }}
{{- end }}
{{- end }}


{{- define "eric-sepp.ipfamily" -}}
{{- if .Values.global -}}
    {{- if .Values.global.internalIPFamily }}
        {{- .Values.global.internalIPFamily | toString -}}
    {{- else -}}
        {{- "" -}}
    {{- end -}}
{{- else -}}
    {{- "" -}}
{{- end }}
{{- end }}

{{- define "eric-sepp.worker.address" -}}
{{- if eq (include "eric-sepp.ipfamily" .) "V6_ONLY" -}}
    "::"
{{- else -}}
    "0.0.0.0"
{{- end }}
{{- end }}

{{- define "eric-sepp.enabled-IPv4" -}}
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

{{- define "eric-sepp.enabled-IPv6" -}}
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

{{- define "eric-sepp.multiVpn.enabled-IPv4" -}}
{{- if or .Values.service.worker.multiVpn.externalIPv4 .Values.global }}
    {{- if .Values.service.worker.multiVpn.externalIPv4.enabled }}
{{- .Values.service.worker.multiVpn.externalIPv4.enabled -}}
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

{{- define "eric-sepp.multiVpn.enabled-IPv6" -}}
{{- if or .Values.service.worker.multiVpn.externalIPv6 .Values.global }}
    {{- if .Values.service.worker.multiVpn.externalIPv6.enabled }}
{{- .Values.service.worker.multiVpn.externalIPv6.enabled -}}
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
Create IPv4 boolean service/global/<notset>
*/}}
{{- define "eric-sepp-worker-service.enabled-IPv4" -}}
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
{{- define "eric-sepp-worker-service.enabled-IPv6" -}}
{{- if .Values.service.worker.externalIPv6.enabled  -}}
{{- .Values.service.worker.externalIPv6.enabled -}}
{{- else -}}
{{- if .Values.global -}}
{{- if .Values.global.externalIPv6 -}}
{{- if .Values.global.externalIPv6.enabled  -}}
{{- .Values.global.externalIPv6.enabled -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}
{{- end -}}

{{- define "eric-sepp.helm-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | quote }}
ericsson.com/product-revision: {{ .Chart.AppVersion | quote }}
ericsson.com/nf-name: "SEPP"
{{- end}}

{{- define "eric-sepp.config-annotations" }}
{{- if .Values.annotations -}}
{{- range $name, $config := .Values.annotations }}
{{ $name }}: {{ tpl $config $ }}
{{- end }}
{{- end }}
{{- end}}

{{- define "eric-sepp.extcert-annotations" }}
{{- include "eric-sepp.helm-annotations" . }}
{{- if .Values.annotations -}}
{{- include "eric-sepp.config-annotations" . }}
{{- end}}
{{- end}}

{{- define "eric-sepp.workerLB-annotations" }}
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

{{- define "eric-sepp.metallb-annotations" }}
{{- if .Values.service.worker.annotations.loadBalancerIPs }}
metallb.universe.tf/loadBalancerIPs: {{ .Values.service.worker.annotations.loadBalancerIPs }}
{{- end }}
{{- end }}

{{- define "eric-sepp.multiVpn.metallb-annotations" }}
{{- if .Values.service.worker.multiVpn.annotations.loadBalancerIPs }}
metallb.universe.tf/loadBalancerIPs: {{ .Values.service.worker.multiVpn.annotations.loadBalancerIPs }}
{{- end }}
{{- end }}

{{- define "eric-sepp.labels" -}}
{{- include "eric-sepp.de-facto-labels" . -}}
{{- if .Values.labels }}
{{ toYaml .Values.labels }}
{{- end -}}
{{- end -}}

{{/*
Generate labels helper function
*/}}
{{- define "eric-sepp.generate-peer-labels" -}}
{{- $peers := index . "peers" -}}
{{- $peerLabels := dict }}
{{- range $_, $peer := $peers }}
    {{- $_ := set $peerLabels ((list $peer "access") | join "-") "true" -}}
{{- end }}
{{- toYaml $peerLabels }}
{{- end -}}

{{- define "eric-sepp-manager.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-sepp-manager.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-sepp.labels" . | fromYaml -}}
{{- include "eric-sepp.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies for sepp-manager
*/}}
{{- define "eric-sepp-manager.peer.labels" -}}
{{- $peers := list }}
{{- $peers = append $peers .Values.sc.common.monitor.hostname }}
{{- $peers = append $peers .Values.adp.pm.server.hostname}}
{{- $peers = append $peers .Values.adp.cm.mediator.hostname }}
{{- $peers = append $peers .Values.adp.fh.alarmHandler.hostname }}
{{- $peers = append $peers .Values.adp.lm.combinedServer.hostname }}
{{- $peers = append $peers .Values.sc.common.etcd.hostname }}
{{- $peers = append $peers .Values.adp.log.transformer.hostname }}
{{- $peers = append $peers .Values.sc.common.rlf.hostname }}
{{- $peers = append $peers .Values.adp.sec.kms.hostname }}
{{- template "eric-sepp.generate-peer-labels" (dict "peers" $peers) }}
{{- end -}}

{{- define "eric-sepp-worker.pod.labels" -}}
{{- $podLabelsDict := dict }}
{{- $peerLabels := include "eric-sepp-worker.peer.labels" . | fromYaml -}}
{{- $baseLabels := include "eric-sepp.labels" . | fromYaml -}}
{{- include "eric-sepp.mergeLabels" (dict "location" .Template.Name "sources" (list $podLabelsDict $peerLabels $baseLabels)) | trim}}
{{- end -}}

{{/*
Define labels for Network Policies of sepp-worker
*/}}
{{- define "eric-sepp-worker.peer.labels" -}}
{{- $peers1 := list }}
{{- $peers1 = append $peers1 .Values.adp.cm.mediator.hostname }}
{{- $peers1 = append $peers1 .Values.sc.common.manager.hostname }}
{{- $peers1 = append $peers1 .Values.adp.probe.virtualTapBroker.hostname }}
{{- $peers1 = append $peers1 .Values.adp.log.transformer.hostname }}
{{- $peers1 = append $peers1 .Values.sc.common.rlf.hostname }}
{{- template "eric-sepp.generate-peer-labels" (dict "peers" $peers1) }}
{{- end -}}

{{- define "eric-sepp.setupmanager.repoPath" -}}
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

{{- define "eric-sepp.manager.repoPath" -}}
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

{{- define "eric-sepp.setupworker.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.setupworker.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.setupworker -}}
        {{- if .Values.imageCredentials.setupworker.repoPath }}
            {{- $repoPath = .Values.imageCredentials.setupworker.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sepp.worker.repoPath" -}}
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

{{- define "eric-sepp.sds.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.sds.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.sds -}}
        {{- if .Values.imageCredentials.sds.repoPath }}
            {{- $repoPath = .Values.imageCredentials.sds.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sepp.certnotifier.repoPath" -}}
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

{{- define "eric-sepp.logfwdr.repoPath" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $repoPath := $productInfo.images.logfwdr.repoPath -}}
{{- if .Values.imageCredentials -}}
    {{- if .Values.imageCredentials.logfwdr -}}
        {{- if .Values.imageCredentials.logfwdr.repoPath }}
            {{- $repoPath = .Values.imageCredentials.logfwdr.repoPath -}}
        {{- end -}}
    {{- end -}}
{{- end -}}
{{- print $repoPath -}}
{{- end -}}

{{- define "eric-sepp.tapagent.repoPath" -}}
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

{{- define "eric-sepp.tlskeylogagent.repoPath" -}}
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

{{- define "eric-sepp.tapcollector.repoPath" -}}
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

{{- define "eric-sepp.setupmanager.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.setupmanager.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.manager.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.manager.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.tapagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.tlskeylogagent.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tlskeylogagent.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.tapcollector.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapcollector.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.setupworker.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.setupworker.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.worker.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.worker.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.sds.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.sds.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.certnotifier.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.certnotifier.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.logfwdr.image" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.logfwdr.name -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.setupmanager.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.setupmanager.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.manager.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.manager.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.setupworker.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.setupworker.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.worker.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.worker.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.sds.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.sds.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.certnotifier.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.certnotifier.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.logfwdr.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.logfwdr.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.tapagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapagent.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.tlskeylogagent.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tlskeylogagent.tag -}}
{{- print $image -}}
{{- end -}}

{{- define "eric-sepp.tapcollector.tag" -}}
{{- $productInfo := fromYaml (.Files.Get "eric-product-info.yaml") -}}
{{- $image := $productInfo.images.tapcollector.tag -}}
{{- print $image -}}
{{- end -}}

{{/*
Create IPv4 boolean service/global/multiVpn/<notset>
*/}}
{{- define "eric-sepp-worker-service.multiVpn.enabled-IPv4" -}}
{{- if .Values.service.worker.multiVpn.enabled -}}
{{- if .Values.service.worker.multiVpn.externalIPv4.enabled -}}
{{- .Values.service.worker.multiVpn.externalIPv4.enabled -}}
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
{{- end -}}

{{/*
Create IPv6 boolean service/global/multiVpn/<notset>
*/}}
{{- define "eric-sepp-worker-service.multiVpn.enabled-IPv6" -}}
{{- if .Values.service.worker.multiVpn.enabled -}}
{{- if .Values.service.worker.multiVpn.externalIPv6.enabled -}}
{{- .Values.service.worker.multiVpn.externalIPv6.enabled -}}
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
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-sepp.manager.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
If the timezone isn't set by a global parameter, set it to UTC
*/}}
{{- define "eric-sepp.worker.timezone" -}}
{{- if .Values.global -}}
    {{- .Values.global.timezone | default "UTC" | quote -}}
{{- else -}}
    "UTC"
{{- end -}}
{{- end -}}

{{/*
Return the log streaming type, default is indirect
*/}}
{{- define "eric-sepp.streamingMethod" -}}
{{- $streamingMethod := "indirect" -}}
{{- if (.Values.log).streamingMethod -}}
    {{- $streamingMethod = .Values.log.streamingMethod -}}
{{- else if ((.Values.global).log).streamingMethod -}}
    {{- $streamingMethod = .Values.global.log.streamingMethod -}}
{{- end -}}
{{- if not (has $streamingMethod (list "indirect" "direct" "dual")) -}}
    {{- fail "Incorrect value for streamingMethod in either global or local-SEPP values.yaml. Possible values: indirect, direct or dual" -}}
{{- end -}}
{{- print $streamingMethod -}}
{{- end -}}

{{/*
Define LOGBACK file to be used, note: returns logback xml file
*/}}
{{- define "eric-sepp.logbackFileName" -}}
{{- $streamingMethod := include "eric-sepp.streamingMethod" . -}}
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
{{- define "eric-sepp.logshipper-enabled" -}}
{{- $streamingMethod := include "eric-sepp.streamingMethod" . }}
{{- $enabled := "false" -}}
{{- if or (eq "direct" $streamingMethod) (eq "dual" $streamingMethod) -}}
    {{- $enabled = "true" -}}
{{- end -}}
{{- print $enabled -}}
{{- end -}}

{{/*
Return the stdout stream, default is file
*/}}
{{- define "eric-sepp.worker.outStream" -}}
{{- $streamingMethod := include "eric-sepp.streamingMethod" . -}}
{{- $stream := "stdout" }}
{{- if eq "direct" $streamingMethod -}}
    {{- $stream = "file" }}
{{- else if eq "dual" $streamingMethod -}}
    {{- $stream = "all" }}
{{- end -}}
{{- print $stream -}}
{{- end -}}

{{/*
Determines if license monitoring is enabled
*/}}
{{- define "eric-sepp.license-consumer.enabled" -}}
{{- $enabled := "false" -}}
{{- if (((((.Values.global).licenseConsumer).features).monitoring).licenses).enabled }}
{{- $enabled = "true" -}}
{{- end }}
{{- print $enabled -}}
{{- end -}}
