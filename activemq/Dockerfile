FROM openjdk:8-jre-alpine

RUN wget -O activemq.tar.gz http://archive.apache.org/dist/activemq/5.15.6/apache-activemq-5.15.6-bin.tar.gz

RUN tar -xzf activemq.tar.gz

RUN rm apache-activemq-5.15.6/conf/activemq.xml
COPY activemq.xml apache-activemq-5.15.6/conf

CMD ["apache-activemq-5.15.6/bin/activemq", "console"]