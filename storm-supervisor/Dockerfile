FROM storm:1.2.4

RUN apt-get update -qq && \
    apt-get install -yq --no-install-recommends \
    	        curl \
		      jq \
		      	     less \
					 emacs  maven

RUN apt-get install -y libffi-dev libssl-dev   python3-pip libjpeg-dev python3.6 python3-setuptools  python3-venv

RUN pip3 install --upgrade pip
RUN mkdir /app && \
    chmod -R a+rx /app
WORKDIR /app
RUN cd /app

#ENV VIRTUAL_ENV=/opt/venv

ENV VIRTUAL_ENV=/app/env
RUN python3 -m venv env
ENV PATH="$VIRTUAL_ENV/bin:$PATH"
RUN env
RUN pwd

#RUN source /app/env/bin/activate
#RUN . env/bin/activate
RUN /bin/bash -c "source /app/env/bin/activate" && pip3 install   warcprox==2.3
#RUN  . /app/env/bin/activate && pip3 install   warcprox==2.3
#ENV PATH="$PATH:/root/.local/bin"
#RUN chown -R "storm:storm" /root/.local/bin
#RUN chmod -R a+rx /root/.local/bin
#USER storm
#RUN chown -R "storm:storm" /warcs/
#RUN chown -R "storm:storm" /certs/
#RUN chmod -R 777 /warcs/