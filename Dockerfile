FROM ubuntu:latest

# update and install java
RUN apt-get update
RUN apt-get upgrade -y
RUN apt-get install -y --no-install-recommends openjdk-7-jdk

ADD  scalajvm/target/universal/stage /opt/app

EXPOSE 9000

WORKDIR /opt/app/

ENTRYPOINT ["bin/patternmatching"]

CMD ["-mem","300"]
