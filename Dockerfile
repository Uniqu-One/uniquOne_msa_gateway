FROM openjdk:11-jre
COPY build/libs/msa_gateway-0.0.1-SNAPSHOT.jar app/gateway_server.jar
WORKDIR /app
ENTRYPOINT ["java", "-jar", "gateway_server.jar"]