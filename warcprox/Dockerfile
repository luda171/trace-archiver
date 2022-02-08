FROM python:3.6-slim
RUN apt-get update && \
    apt-get install -y libffi-dev libssl-dev

RUN mkdir /warcs
WORKDIR /warcs

RUN pip install warcprox
