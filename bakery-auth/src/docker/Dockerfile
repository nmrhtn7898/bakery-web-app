FROM openjdk:8-jdk-alpine
ARG NAME
RUN apk update && apk upgrade && apk add netcat-openbsd && apk add curl
COPY $NAME.jar app.jar
COPY run.sh run.sh
RUN chmod +x run.sh
ENTRYPOINT './run.sh'