FROM openjdk:17-jdk-slim
VOLUME /tmp
COPY erp_mes/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
