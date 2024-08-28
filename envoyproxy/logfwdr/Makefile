
logforwarder: logforwarder.go  outlierevent.go
	env CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -o -s -w $@
	#env CGO_ENABLED=0 GOOS=linux GOARCH=amd64 go build -a -o $@

format:
	gofmt -w logforwarder.go

clean:
	rm logforwarder

test:
	go test logforwarder_test.go