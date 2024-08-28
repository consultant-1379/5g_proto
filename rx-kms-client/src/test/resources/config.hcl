listener "tcp" {
  address = "0.0.0.0:8300"
  tls_cert_file = "/vault/config/cert.pem",
  tls_key_file = "/vault/config/key.pem",
  tls_disable_client_certs = "true"
}