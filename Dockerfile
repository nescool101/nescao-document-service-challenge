FROM openjdk:17-jdk-slim as build
WORKDIR /workspace/app

COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
COPY src src

RUN chmod +x ./mvnw
RUN ./mvnw package -DskipTests

FROM openjdk:17-slim
VOLUME /tmp
COPY --from=build /workspace/app/target/*.jar app.jar
ENV JAVA_OPTS="-Xmx50m -Xms50m"
ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /app.jar"]