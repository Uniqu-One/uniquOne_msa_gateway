FROM openjdk:11-jre
COPY build/libs/*.jar uniquone_gateway_img.jar
ENTRYPOINT ["java", "-jar", "uniquone_gateway_img.jar"]