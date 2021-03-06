# Changelog
All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [Unreleased]

## [0.0.5] - 2021-05-06

### Known Issues and Limitations

- https://snyk.io/vuln/SNYK-JAVA-IONETTY-1042268

### Added

NIAD-1348: Remove TLS for SDS request
NIAD-1124: Update SDS FHIR api link in readme to valid one
NIAD-1080: SDS filter uses reactive operations to preventing blocking

## [0.0.4] - 2021-03-30

### Known Issues and Limitations

- Same as for 0.0.3

### Added

NIAD-1126: Remove default value for ssp domain from yml
NIAD-1182: Global Logging

## [0.0.3] - 2021-03-29

### Known Issues and Limitations

- Same as for 0.0.2

### Added

NIAD-1153: Fixes incorrect encoding of query parameters
NIAD-1126: Connect to GPC via SSP
NIAD-1225: SDS API requests must use X-Correlation-Id

## [0.0.2] - 2021-03-25

### Known Issues and Limitations

- Same as for 0.0.1

### Added

- NIAD-1153 Fixes incorrect encoding of query parameters

## [0.0.1] - 2021-03-17

### Known Issues and Limitations

* Routing via Spine Security Proxy is not yet supported
* Using TLS MA to authenticate GP Connect requests is not yet supported

### Added

- NIAD-1080  Lookup GPC endpoints in SDS Filter
- NIAD-1129  Proxy search for patient documents
- NIAD-1130  Proxy get document
- NIAD-1128  Proxy find a patient
- NIAD-1125  SDS API client
- NIAD-1121  Jenkins pipeline with no deployment
- NIAD-1127  Proxy get structured record
- NIAD-1119  Create initial project structure
- NIAD-1123  Docker Compose files
- NIAD-1119  Create Github and Spring Boot Healthcheck

### Fixed

* Information Disclosure [Medium Severity](https://snyk.io/vuln/SNYK-JAVA-COMGOOGLEGUAVA-1015415) in com.google.guava:guava@29.0-jre
* HTTP Request Smuggling [Medium Severity](https://snyk.io/vuln/SNYK-JAVA-IONETTY-1083991) in io.netty:netty-codec-http2@4.1.59.Final
* Comparison Using Wrong Factors [High Severity](https://snyk.io/vuln/SNYK-JAVA-ORGBOUNCYCASTLE-1052448) in org.bouncycastle:bcprov-jdk15on@1.66
