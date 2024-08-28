


H2LOAD as Docker image.
=======================

Benchmark tool deployed on your KaaS cluster.



Why an own Docker image?
------------------------
DockerHub does not seem to have a decent h2load version ready and the compilation for a local installation differs from Ubuntu and CentOS. Therefore the default make target creates/compiles a new Docker image including the latest h2load SW.


How to install
--------------
Call "make" to compile a new Docker image "armdocker.rnd.ericsson.se/proj-5g-bsf/h2load" will be pushed to armdocker.


How to deploy or attach to h2load on your cluster
-------------------------------------------------
- First time deploy and start (check with "k get deployments"):
$ kubectl run h2load --generator=run-pod/v1 --namespace 5g-bsf-USER -i -t --image armdocker.rnd.ericsson.se/proj-5g-bsf/h2load:latest
- Press ENTER to get a prompt
- exit to leave container (container not stopped)

- To attach to an already running h2load container:
$ kubectl attach h2load --namespace 5g-bsf-USER -c h2load -i -t


How to start a benchmark from within the h2load container
---------------------------------------------------------
- start or attach to the h2load container as described in the last section
- Before starting a benchmark, check whether your service call actually works using curl:
$ 
$ /usr/local/bin/h2load -m20 -c16 -n 5000 http://eric-bsf-worker:80/nbsf-management/v1/pcfBindings?Ipv4Addr=10.47.11.1

