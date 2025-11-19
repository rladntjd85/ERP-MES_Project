FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
COPY erp_mes/build/libs/*.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
