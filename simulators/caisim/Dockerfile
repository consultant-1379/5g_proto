ARG DOCKER_BASE_IMG
FROM $DOCKER_BASE_IMG

ARG CBOS_REPO
RUN zypper addrepo --gpgcheck-strict $CBOS_REPO CBOS_REPO && \
    zypper --gpg-auto-import-keys refresh -f &&\
    zypper --no-refresh --quiet install --no-recommends --auto-agree-with-licenses --no-confirm CBOS_REPO:ca-certificates-mozilla \
                                                                                                CBOS_REPO:curl \
                                                                                                CBOS_REPO:python3-devel \
                                                                                                CBOS_REPO:python3-pip \
                                                                                                CBOS_REPO:wget &&\
    zypper clean -a

### Install dependencies ###
ADD requirements.txt requirements.txt
RUN pip install -r requirements.txt

WORKDIR /app

ADD Sim_API /app/Sim_API
ADD Sim_Objects /app/Sim_Objects

ADD "settings.py" "/app/settings.py"
ADD "Simulator_Service.py" "/app/Simulator_Service.py"
ADD "ApplicationReportSchema.json" "/app/ApplicationReportSchema.json"

EXPOSE 5000

CMD [ "gunicorn", "-t 120", "-w 1", "-b 0.0.0.0:5000", "--log-level=debug", "--capture-output", "Simulator_Service:app"]