{{- define "eric-sc.pvtb-recording-rules" }}
groups:
- name: pvtb_metrics
  rules:
  - record: pvtb_diff_recv_send_successes_packets_total_rate90s
    expr: rate(pvtb_recv_successes_total[90s]) - on(domain) group_right() (rate(pvtb_send_successes_total[90s]))
  - record: pvtb_diff_recv_send_successes_packets_ratio
    expr: ((pvtb_diff_recv_send_successes_packets_total_rate90s / on(domain) group_left() ((rate(pvtb_recv_successes_total[90s])> 20))) or (0 * rate(pvtb_send_successes_total[90s]))) *100
  - record: scp_pvtb_received_total_rate
    expr: rate(pvtb_recv_successes_total{domain='sc-scp'}[90s]) + on(domain) group_left() (rate(pvtb_recv_failures_total{domain='sc-scp'}[90s]) or (0 * rate(pvtb_recv_successes_total{domain='sc-scp'}[90s])))
  - record: scp_pvtb_send_total_rate
    expr: rate(pvtb_send_successes_total{domain='sc-scp'}[90s]) + (rate(pvtb_send_failures_total{domain='sc-scp'}[90s]) or (0 * rate(pvtb_send_successes_total{domain='sc-scp'}[90s])))
  - record: scp_pvtb_received_failure_ratio
    expr: ((rate(pvtb_recv_failures_total{domain='sc-scp'}[90s]) or (0 * scp_pvtb_received_total_rate)) / scp_pvtb_received_total_rate) * 100
  - record: scp_pvtb_send_failure_ratio
    expr: ((rate(pvtb_send_failures_total{domain='sc-scp'}[90s]) or (0 * scp_pvtb_send_total_rate)) / scp_pvtb_send_total_rate) * 100
  - record: sepp_pvtb_received_total_rate
    expr: rate(pvtb_recv_successes_total{domain='sc-sepp'}[90s]) + on(domain) group_left() (rate(pvtb_recv_failures_total{domain='sc-sepp'}[90s]) or (0 * rate(pvtb_recv_successes_total{domain='sc-sepp'}[90s])))
  - record: sepp_pvtb_send_total_rate
    expr: rate(pvtb_send_successes_total{domain='sc-sepp'}[90s]) + (rate(pvtb_send_failures_total{domain='sc-sepp'}[90s]) or (0 * rate(pvtb_send_successes_total{domain='sc-sepp'}[90s])))
  - record: sepp_pvtb_received_failure_ratio
    expr: ((rate(pvtb_recv_failures_total{domain='sc-sepp'}[90s]) or (0 * sepp_pvtb_received_total_rate)) / sepp_pvtb_received_total_rate) * 100
  - record: sepp_pvtb_send_failure_ratio
    expr: ((rate(pvtb_send_failures_total{domain='sc-sepp'}[90s]) or (0 * sepp_pvtb_send_total_rate)) / sepp_pvtb_send_total_rate) * 100
  - record: bsf_pvtb_received_total_rate
    expr: rate(pvtb_recv_successes_total{domain='sc-bsf'}[90s]) + on(domain) group_left() (rate(pvtb_recv_failures_total{domain='sc-bsf'}[90s]) or (0 * rate(pvtb_recv_successes_total{domain='sc-bsf'}[90s])))
  - record: bsf_pvtb_send_total_rate
    expr: rate(pvtb_send_successes_total{domain='sc-bsf'}[90s]) + (rate(pvtb_send_failures_total{domain='sc-bsf'}[90s]) or (0 * rate(pvtb_send_successes_total{domain='sc-bsf'}[90s])))
  - record: bsf_pvtb_received_failure_ratio
    expr: ((rate(pvtb_recv_failures_total{domain='sc-bsf'}[90s]) or (0 * bsf_pvtb_received_total_rate)) / bsf_pvtb_received_total_rate) * 100
  - record: bsf_pvtb_send_failure_ratio
    expr: ((rate(pvtb_send_failures_total{domain='sc-bsf'}[90s]) or (0 * bsf_pvtb_send_total_rate)) / bsf_pvtb_send_total_rate) * 100
{{- end }}
