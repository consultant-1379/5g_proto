module logforwarder

go 1.18

require (
	ericsson.com/5g/scp/api v0.0.0
	ericsson.com/5g/scp/service v0.0.0
	github.com/envoyproxy/go-control-plane v0.12.0
	github.com/golang/protobuf v1.5.4
	github.com/sirupsen/logrus v1.9.3
	google.golang.org/grpc v1.63.2
)

require (
	github.com/cncf/xds/go v0.0.0-20220314180256-7f1daf1720fc // indirect
	github.com/envoyproxy/protoc-gen-validate v1.0.4 // indirect
	github.com/gogo/protobuf v1.3.2 // indirect
	golang.org/x/net v0.23.0 // indirect
	golang.org/x/sys v0.19.0 // indirect
	golang.org/x/text v0.14.0 // indirect
	golang.org/x/crypto v0.22.0 // indirect
	google.golang.org/genproto v0.0.0-20240415180920-8c6c420018be // indirect
	google.golang.org/protobuf v1.33.0 // indirect
)

// use "replace" to point to the correct local folder
replace ericsson.com/5g/scp/api => /build/eric-sc-logfwdr/gen/ericsson.com/5g/scp/api

replace ericsson.com/5g/scp/service => /build/eric-sc-logfwdr/gen/ericsson.com/5g/scp/service

replace envoy => /build/eric-sc-logfwdr/gen/github.com/envoyproxy/go-control-plane/envoy
