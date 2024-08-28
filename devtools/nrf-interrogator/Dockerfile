FROM python:3.8.2-buster

RUN apt-get update
RUN apt-get install -y vim

ADD app.py /app.py
ADD requirements.txt /requirements.txt
RUN pip install --trusted-host pypi.python.org -r requirements.txt

EXPOSE 80

ENTRYPOINT ["/bin/sleep", "infinity"]
