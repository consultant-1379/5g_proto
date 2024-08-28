# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [1.0.20] - 23-05-2024
### Changed
- Compile with latest ericsson libraries

## [1.0.19] - 18-04-2024
### Changed
- Update WCDB certificates due to renaming

## [1.0.18] - 16-04-2024
### Changed
- Update Nrfsim certificates

## [1.0.17] - 11-04-2024
### Changed
- Compile with latest ericsson libraries
- Update maven jib plugin to 3.4.2

## [1.0.16] - 23-02-2024
### Changed
- Compile with latest ericsson libraries (SC1.14.0 branch)

## [1.0.15] - 22-02-2024
### Changed
- Compile with latest ericsson libraries (SC1.14.0 branch)

## [1.0.11] - 15-12-2023
### Changed
- Compile with latest ericsson libraries

## [1.0.10] - 09-11-2023
### Changed
- Compile with latest ericsson libraries
- Switched to Java 17
- Update maven jib plugin to 3.4.0

## [1.0.9] - ??-??-2023
### Added
- ?

## [1.0.8] - 25-07-2023
### Added
- bugFix DND-45156: Change references to CMYP service name

## [1.0.7] - 21-07-2023
- bugFix DND-47821: CI: NRF Suites Failures (BSF, SCP, SEPP) due to schema changes

## [1.0.6] - 05-05-2023
### Added
- NLF

## [1.0.5] - 18-01-2023
### Added
- Default scenario for geored deployment
- Default scenario for client opening netconf over TLS session with CM Yang Provider
- Create pkcs12 certificates in binary and base64 format
- Create certKey.pem with combination of cert.pem and key.pem

## [1.0.4] - 20-12-2022
### Added
- Default scenario for CM Yang Provider for netconf over TLS sessions (install
secrets via Supreme)

## [1.0.3] - 10-11-2022
### Added
- Default scenario for multiple external Logtransformers simulating Lumberjack
external endpoints

## [1.0.2] - 11-05-2022
### Added
- Default scenario for Telegraf to generate key/cert for external telegraf
- fix issue with fetchNodeIp method in KubernetesClient class

## [1.0.1] - 03-04-2022
### Added
- Default scenario for TransformerExternal to generate key/cert for external
lumberjack

## [1.0.0] - 11-02-2022
### Changed
- Output directory of certificates during installation, from .bob to .certificates
- The properties file has a new block for defaultScenarios configuration. You
can specify the output directory and the validity period of certificates for
default scenarios.

### Fixed
- Installation for default scenarios when no SC deployment is present. eg. Simulators

### Added
- -o option with the possibility to set output directory for default scenarios
through command line
- -k option with the possiblity to set the kubeconfig from command line
- Optional kubeconfig attribute in the admin block of properties file
