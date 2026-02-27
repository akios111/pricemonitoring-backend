FROM gradle:8.2.1-jdk17-jammy as build
WORKDIR /app
COPY build.gradle.kts settings.gradle.kts gradlew gradle/ ./
RUN gradle build -x bootJar || exit 0
COPY . .
RUN gradle build -x test

FROM amazoncorretto:17
RUN yum -y install less
COPY --from=build /app/build/libs/PriceMonitoringBackend.jar server.jar
EXPOSE 8085
CMD java -jar server.jar
