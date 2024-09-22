FROM maven:3-ibm-semeru-21-jammy AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src
RUN mvn clean package

FROM ibm-semeru-runtimes:open-21-jre-jammy
WORKDIR /app

COPY --from=build /app/target/*.jar app.jar
EXPOSE 9999
CMD ["java", "-jar", "-Duser.timezone=GMT+8", "app.jar"]
