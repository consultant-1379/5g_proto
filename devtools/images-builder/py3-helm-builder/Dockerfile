ARG BASE_IMAGE
FROM $BASE_IMAGE

ENV HELM_ADDITIONAL_VERSIONS="v3.5.1"
ENV HELM_VERSION=3.8.1
ENV HELM3_DEFAULT_VERSION=v$HELM_VERSION
ENV KUBECTL_VERSION=1.25.5
ENV KUBECTL_ADDITIONAL_VERSIONS="v1.25.5"
ENV KUBECTL_DEFAULT_VERSION=v$KUBECTL_VERSION

RUN zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm ruby2.5

#Removes the RequestsDependencyWarning messages for the requests library
RUN python3.6 -m pip install requests==2.27.1 &&\
	python3.6 -m pip install --upgrade requests &&\
	python3.10 -m pip install requests==2.27.1 &&\
	python3.10 -m pip install --upgrade requests

#Add more versions to install to the array and specify the default to use in HELM_VERSION/KUBECTL_VERSION
RUN for h in $HELM_ADDITIONAL_VERSIONS; do \
      mkdir -p /usr/share/helm/$h; \
      curl "https://get.helm.sh/helm-$h-linux-amd64.tar.gz" | tar -xz -C "/usr/share/helm/$h/" --strip-components=1; \
    done

RUN for k in $KUBECTL_ADDITIONAL_VERSIONS; do \
      mkdir -p /usr/share/kubectl/$k; \
      curl -Lo /usr/share/kubectl/$k/kubectl https://dl.k8s.io/release/$k/bin/linux/amd64/kubectl; \
      chmod 755 /usr/share/kubectl/$k/kubectl; \
    done

LABEL \
    adp.helm3=$HELM3_DEFAULT_VERSION \
    adp.helm3-additonal-versions=$HELM_ADDITIONAL_VERSIONS \
    adp.kubectl=$KUBECTL_DEFAULT_VERSION \
    adp.kubectl-additional-versions=$KUBECTL_ADDITIONAL_VERSIONS
