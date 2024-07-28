FROM openjdk:21-slim

WORKDIR /src
COPY . /src

RUN apt-get update && apt-get install -y gradle

RUN bash gradlew buildFatJar

WORKDIR /run
RUN cp /src/build/libs/*.jar /run/auth-server.jar

EXPOSE 8081

CMD java -jar /run/auth-server.jar