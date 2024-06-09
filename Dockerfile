FROM eclipse-temurin:17.0.11_9-jre-jammy
#Name of your JAR file
WORKDIR /app

COPY target/order-0.0.1-RELEASE.jar app.jar

EXPOSE 8080
ENTRYPOINT ["sh","-c","java --add-opens java.base/java.lang=ALL-UNNAMED --add-opens java.base/java.util=ALL-UNNAME -jar ${JAR_FILE}"]