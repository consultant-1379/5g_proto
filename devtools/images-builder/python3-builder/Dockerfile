FROM armdocker.rnd.ericsson.se/proj-adp-cicd-drop/bob-python3builder:latest

RUN zypper install -y glibc-locale && \
    zypper install -y glibc-locale-base

ENV LANG='en_US.UTF-8' LANGUAGE='en_US:en' LC_ALL='en_US.UTF-8'

RUN pip3 install --upgrade pip

RUN pip3 install elasticsearch && \
    pip3 install paramiko && \
    pip3 install kubernetes && \
    pip3 install mock && \
    pip3 install more-itertools && \
    pip3 install atomicwrites && \
    pip3 install wcwidth && \
    pip3 install dpath && \
    pip3 install xlsxwriter

CMD ["/bin/bash"]
