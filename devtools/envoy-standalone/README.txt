To deploy
make deploy

export ENVOY_POD=$(kubectl get pods --namespace 5g-bsf-$USER -l "app=envoy-standalone" -o jsonpath="{.items[0].metadata.name}")

To prepare, copy the <envoy_config.yaml> file to the envoy pod:
k cp 28-supi-lua-notify-via-slf-cg.yaml $ENVOY_POD:28-supi-lua-notify-via-slf-cg.yaml

To start enovy
k exec -it $ENVOY_POD sh
envoy -c 28-supi-lua-notify-via-slf-cg.yaml

