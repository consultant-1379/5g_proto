# BSF-load guide

## Introduction

BSF-load tool is a HTTP2 traffic generator designed to generate traffic for BSF. Some terms to familiarize with before reading the rest of the guide are:

- **Traffic set:** It is a set of finite HTTP requests of the same type. The type refers to the specific traffic case for which the requests are generated. In the BSF case, the type of a request can be REGISTER, DEREGISTER or DISCOVERY. A traffic set is characterized by the IP range that it covers, the number of generated requests and the rate of generated requests per second.

- **Workload:** It is a complete traffic scenario, which can vary between a one-shot GET request or a 48 hour traffic run with multiple types of traffic. A Workload can have one or multiple traffic sets. Moreover, the workload is what binds multiple traffic sets together by controlling the order they are executed, the duration and the times of execution. Workload also controls if TLS will be used for the generated requests, if and how the metrics of the traffic execution are exported and the configuration of other low level HTTP client options.

## Management Interface

BSF-load tool provides a management interface via a HTTP API. This HTTP API is well defined in a OpenAPI description file. The API supports the following operations to control the lifecycle of a traffic workload:

##### Start the execution of a workload

To start the execution of a workload, send a HTTP POST request to "/bsf-load/run" and include in the body of the request the configuration of the workload in JSON format.

**Example:**

    curl -X POST http://$API_IP:$PORT/bsf-load/run -H "Content-Type: application/json" -d @config.json | jq

**Notes:**

- The service cannot execute more than one workload concurrently. In case the service is already occupied with another workload it will return the error "Server busy".
- The configuration is validated before starting the execution of the workload. If there are configuration errors, a detailed list of the parameter errors will be returned.
- If the execution of the workload is started successfully, then a workload entry is returned in the response that contains the UUID of the workload (runId).

##### Get all workloads stored by the service

To get all stored workloads, send a HTTP GET request to "/bsf-load/run".

**Example:**

    curl http://$API_IP:$PORT/bsf-load/run | jq

**Notes:**

- The workload entries are returned as a list.

##### Get the information of a workload

To get information for a workload, send a HTTP GET request to "/bsf-load/run/{runId}", where {runId} is the unique identifier of the workload.

**Example:**

    curl http://$API_IP:$PORT/bsf-load/run/{runId} | jq

**Notes:**

- A response with the complete configuration (including default values) of the workload is returned. In the response the current state of the workload is also included, which can be RUNNING, ERROR, COMPLETED and TERMINATED.
- If the workload for the given ID does not exist, '404 NOT FOUND' is returned.

##### Delete all workloads

To delete all workloads from the service, that are not currently in running state, send a HTTP DELETE request to "/bsf-load/run/".

**Example:**

    curl -X DELETE http://$API_IP:$PORT/bsf-load/run | jq

**Notes:**

- Workloads that are currently in state RUNNING will not be deleted.

##### Delete a workload

To delete a workload from the service, send a HTTP DELETE request to "/bsf-load/run/{runId}", where {runId} is the unique identifier of the workload.

**Example:**

    curl -X DELETE http://$API_IP:$PORT/bsf-load/run/{runId} | jq

**Notes:**

- Workloads that are currently in state RUNNING cannot be deleted, so '409 CONFLICT' will be returned.
- If the workload for the given ID does not exist, '404 NOT FOUND' is returned.

##### Terminated a workload

To terminate the execution of a workload, send a HTTP POST request to "/bsf-load/terminate/{runId}", where {runId} is the unique identifier of the workload.

**Example:**

    curl -X POST http://$API_IP:$PORT/bsf-load/terminate/{runId} | jq

**Notes:**

- Workloads that are currently in any state other than RUNNING cannot be terminated, so '409 CONFLICT' will be returned.
- If the workload for the given ID does not exist, '404 NOT FOUND' is returned.

## Configuration options

- Duration (default=0, optional):

When defined, the load tool produces traffic for this duration (in seconds) and then stops, regardless if there are additional traffic sets. If the traffic sets are executed faster than the required duration, then they are repeated until the duration goal is met. If the duration is omitted, then all traffic sets are executed only once.

- Target-host (mandatory):

The host address of the target BSF.

- Target-port (mandatory):

The port of the target BSF.

- Tcp-connections (default=40, optional):

The number of TCP connections used to generate traffic. If set too low, it might hinder BSF load from yielding the required TPS.

- Http2-streams (default=40, optional):

The number of http2 streams used to generate traffic. If set too low, it might hinder BSF load from yielding the required TPS.

- Max-parallel-transactions (default=20000, optional):

The maximum number of allowed parallel transactions. If set too low, it might hinder BSF load from yielding the required TPS.

- Timeout (default=0, optional):

The timeout period in milliseconds before a request fails. This is applied to all traffic sets that do not have an individual timeout parameter configured.

- Tls (optional):

Defines the Tls configuration options. If it is not defined the tls configuration object is set up with its default values.

- Metrics (optional):

Defines the metrics configuration options. If it is not defined the metrics configuration object is set up with its default values.

- Setup-traffic-mix (default=[], optional):

Defines a list of traffic sets that are executed only once before the actual traffic mix. The execution of the setup traffic mix is not taken into consideration when counting down the duration of the traffic mix execution.

- Traffic-mix (default=[], optional):

Defines a list of traffic sets that are executed repeatedly until the duration is met. If duration is not defined, then the traffic sets are executed only once.

##### Tls configuration options

- Tls-enabled (default=false, optional):

This is a boolean parameter that defines if tls communication is enabled.

- Verify-host (default=false, optional):

This is a boolean parameter that defines if host verification is enabled.

- Cert-path (default="/opt/bsf-load/certificates/cert.pem", optional):

Defines the path where the tls certificate is located. This is where the certificate is mounted from the related secret when the application runs in a pod.

- Key-path (default="/opt/bsf-load/certificates/key.pem", optional):

Defines the path where the tls private key is located. This is where the key is mounted from the related secret when the application runs in a pod.

##### Metrics configuration options

- Export-metrics (default=[], optional):

A list with the methods of exporting metrics. Possible values: ["CSV_FILE", "PROMETHEUS"]

- Csv-convert-rates-to (default="SECONDS", optional):

Convert rates to the given time unit. Applicable only if CSV_FILE exporting is enabled.

- Csv-convert-durations-to (default="MILLISECONDS", optional):

Convert durations to the given time unit. Applicable only if CSV_FILE exporting is enabled.

- Csv-metrics-directory (default="/opt/bsf-load/metrics", optional):

The directory where the CSV files are stored. Applicable only if CSV_FILE exporting is enabled.

- Csv-name-prefix (default="metrics", optional):

The prefix of the directory that contains the exported CSV files. Applicable only if CSV_FILE exporting is enabled.

- Csv-poll-interval (default=1, optional):

The polling interval in seconds of the exported values in the CSV files. Applicable only if CSV_FILE exporting is enabled.

- Pm-enable-percentiles (default=false, optional):

Enables or disables percentiles. Produces an additional time series for each requested percentile. This percentile is computed locally, and so it can't be aggregated with percentiles computed across other dimensions (e.g. in a different instance). Applicable only if PROMETHEUS exporting is enabled.

- Pm-target-percentiles (default=["0.5", "0.75", "0.9", "0.99"], optional)

The target percentiles. Applicable only if PROMETHEUS exporting is enabled.

- Pm-percentile-precision (default=1, optional)

The number of digits of precision to maintain on the dynamic range histogram used to compute percentile approximations. The higher the degrees of precision, the more accurate the approximation is at the cost of more memory. Applicable only if PROMETHEUS exporting is enabled.

- Pm-percentile-metrics (default=["vertx.http.client.response.time"], optional)

The name of the metrics for which percentiles are produced. Applicable only if PROMETHEUS exporting is enabled.

- Pm-enable-histogram-buckets (default=false, optional)

Enables or disables the publication of a histogram that can be used to generate aggregable percentile approximations on the Prometheus monitoring system. Applicable only if PROMETHEUS exporting is enabled.

- Pm-histogram-buckets-metrics (default=["vertx.http.client.response.time"], optional)

The name of the metrics for which histogram buckets are produced. Applicable only if PROMETHEUS exporting is enabled.

##### Traffic set configuration options

Defines a traffic set of requests.

- Ip-range (conditional):

Defines the ip-range of the requests. It has two fields, the start-IP and the range. If the required number of requests is greater than the IP range, then the generated requests start over from the starting IP of the IP range. It is mandatory with the exception of deregister traffic sets with traffic-set-ref.

- Name (mandatory):

Uniquely identifies this specific traffic set. This is used to cross-reference between the traffic sets, allowing to refer to register traffic sets from deregister traffic sets.

- Num-requests (conditional):

The traffic set is executed until the number of requests is reached. It is mandatory with the exception of deregister traffic sets with traffic-set-ref.

- Order (mandatory):

The order is used to prioritize the traffic set execution in a traffic mix. The order must be unique inside a group of traffic sets.

- Timeout (optional):

The timeout period in milliseconds before a request fails. If this is set to 0, then no timeout is used for the requests. This takes precedence over the global timeout configuration option. When left empty, the global timeout value is applied.

- Tps (mandatory):

The number of requests per seconds.

- Traffic-set-ref (optional):

References the name of a register traffic set. The referenced set must belong in the same group of traffic sets, either setup or traffic mix.

- Type (mandatory):

The type of requests, which can be register, deregister or  discovery.
