FROM maven:3-openjdk-17 AS MAVEN_ENV

COPY pom.xml /tmp/
COPY src /tmp/src/
WORKDIR /tmp/
RUN mvn package -DskipTests=true

FROM maven:3-openjdk-17

RUN mkdir /app
WORKDIR /app

COPY --from=MAVEN_ENV /tmp/target/*.jar /app/app.jar

EXPOSE 3000

ENTRYPOINT ["java", "-jar", "app.jar"]