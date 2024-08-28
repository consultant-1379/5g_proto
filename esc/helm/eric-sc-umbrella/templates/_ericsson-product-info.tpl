{{- define "eric-sc.product-annotations" }}
ericsson.com/product-name: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productName | quote }}
ericsson.com/product-number: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productNumber | replace "_" " " | quote }} 
ericsson.com/product-revision: {{ (fromYaml (.Files.Get "eric-product-info.yaml")).productRevision | quote }}
{{- end}}
