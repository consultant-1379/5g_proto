
# Base image - Alpine
FROM alpine:latest

# Set the working directory
WORKDIR /snmp-tmp

# Copy the current directory contents into the working directory
COPY . /snmp-tmp

# Install the required snmp packages
RUN apk add --update \
    net-snmp \
    net-snmp-tools

# Copy ericsson and depedency mibs to the default directory
RUN cp ericsson-mibs/* /usr/share/snmp/mibs/ && \
    cp depedency-mibs/* /usr/share/snmp/mibs/ && \
    rm -rf ericsson-mibs && \
    rm -rf depedency-mibs && \
    rm Dockerfile

# Update the configuration  
RUN mv -f snmp.conf /etc/snmp/snmp.conf && \
    mv -f snmptrapd.conf /etc/snmp/snmptrapd.conf

ENTRYPOINT snmptrapd -f -Lo | tee /var/log/snmptrap.log


