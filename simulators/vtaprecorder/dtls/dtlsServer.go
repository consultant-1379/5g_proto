// SPDX-FileCopyrightText: 2023 The Pion community <https://pion.ly>
// SPDX-License-Identifier: MIT

// Package main implements an example DTLS server which verifies client certificates.
package main

import (
	"context"
	"crypto/tls"
	"crypto/x509"
	"fmt"
	"net"
	"time"

    "github.com/pion/dtls/v2"
    "github.com/pion/dtls/v2/examples/util"
)

func main() {
	// Prepare the IP to connect to
	addr := &net.UDPAddr{IP: net.ParseIP("0.0.0.0"), Port: 4755}
	addrFW := &net.UDPAddr{IP: net.ParseIP("127.0.0.1"), Port: 4754}

	// Create parent context to cleanup handshaking connections on exit.
	ctx, cancel := context.WithCancel(context.Background())
	defer cancel()

	certificate, err := util.LoadKeyAndCertificate("/java-exec/certificates/tls.key",
		"certificates/tls.crt")
	util.Check(err)

	rootCertificate, err := util.LoadCertificate("/java-exec/certificates/cert1.pem")
	util.Check(err)
	certPool := x509.NewCertPool()
	cert, err := x509.ParseCertificate(rootCertificate.Certificate[0])
	util.Check(err)
	certPool.AddCert(cert)

	// Prepare the configuration of the DTLS connection
	config := &dtls.Config{
		Certificates:         []tls.Certificate{certificate},
		//ExtendedMasterSecret: dtls.RequireExtendedMasterSecret,
		ClientAuth:           dtls.RequireAndVerifyClientCert,
		ClientCAs:            certPool,
		// Create timeout context for accepted connection.
		ConnectContextMaker: func() (context.Context, func()) {
			return context.WithTimeout(ctx, 30*time.Second)
		},
	}

	// Connect to a DTLS server
	listener, err := dtls.Listen("udp", addr, config)
	util.Check(err)
	connFW, err := net.DialUDP("udp", nil, addrFW)
	util.Check(err)
	defer func() {
		util.Check(listener.Close())
		util.Check(connFW.Close())
	}()

	fmt.Println("Listening")

		for {
			// Wait for a connection.
			conn, err := listener.Accept()
			util.Check(err)
			data := make([]byte, 10000)
			// defer conn.Close() // TODO: graceful shutdown

			// `conn` is of type `net.Conn` but may be casted to `dtls.Conn`
			// using `dtlsConn := conn.(*dtls.Conn)` in order to to expose
			// functions like `ConnectionState` etc.
			go func(){
			defer func() {
        			if r := recover(); r != nil {
					fmt.Println("Recovered in f", r)
        			}
			}()
			for{
				d, err := conn.Read(data)
				util.Check(err)
				fmt.Println(d)
				fmt.Println(string(data[:d]))
				d, err = connFW.Write(data[:d])
				util.Check(err)
			}
			}()
		}

	for {}
}

