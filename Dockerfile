FROM maven:3-ibm-semeru-21-jammy AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM azul/zulu-openjdk-alpine:21-jre-headless
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 9999
CMD ["java", "-jar", "-Duser.timezone=GMT+8", "app.jar"]
