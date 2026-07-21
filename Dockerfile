FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app

COPY acv/pom.xml ./acv/
RUN mvn -f acv/pom.xml dependency:go-offline -B

COPY acv/src ./acv/src
RUN mvn -f acv/pom.xml clean package -DskipTests

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
COPY --from=build /app/acv/target/*.jar app.jar

ENV PORT=8080
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java -Xmx512m -Dserver.port=${PORT} -jar app.jar"]