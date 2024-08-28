#!/bin/bash
#
# Copy tcpdump + needed libraries to a container
#
# Usage: upload_tcpdump.sh <pod-name>...
#
# Alexander.Langer@ericsson.com
# 2020-06-04

for pod in "$@"
do
pod="${pod//[\'\[\],]/}"
echo "Uploading to $pod ..."
kubectl cp binaries/tcpdump $pod:/usr/sbin/ -c eric-scp-worker
kubectl cp binaries/libpcap.so.1 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libpcap.so.1.8.1 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libsmi.so.2 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libsmi.so.2.0.27 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libnl-genl-3.so.200 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libnl-genl-3.so.200.25.0 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libnl-3.so.200 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libnl-3.so.200.25.0 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libdbus-1.so.3 $pod:/usr/lib64/ -c eric-scp-worker
kubectl cp binaries/libdbus-1.so.3.19.4 $pod:/usr/lib64/ -c eric-scp-worker
done

echo "Done."
