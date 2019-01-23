FROM java:openjdk-8u91-jdk
CMD java ${JAVA_OPTS} -jar eventuate-tram-cdc-mysql-service-0.12.0-SNAPSHOT.jar ${CDC_OPTS}
COPY build/libs/eventuate-tram-cdc-mysql-service-0.12.0-SNAPSHOT.jar .
