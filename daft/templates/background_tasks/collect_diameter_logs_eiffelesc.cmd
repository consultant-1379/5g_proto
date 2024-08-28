# This file collects Diameter logs from the pods in realtime and saves
# it to log files under the workspace directory.
@@_PACKAGE_DIR@@/perl/bin/trouble_shooting_collect_pod_logs.pl -n eiffelesc --no-tty -o @@_JOB_TROUBLESHOOTING_LOG_DIR@@/GSSUPP-12317/pod_logs -p 'eric-bsf-diameter-.+,dsl' -p 'eric-stm-diameter-.+,dsl'
