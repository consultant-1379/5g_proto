FROM python:2.7
EXPOSE 1354
WORKDIR /usr/local/bin
COPY secret-server.py .
RUN chmod 777 secret-server.py
RUN pip install pyyaml
CMD secret-server.py
