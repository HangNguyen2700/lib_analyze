FROM maven:3.8.5-openjdk-17 as build
RUN mkdir -p /usr/miner
COPY ./src/ /usr/miner/src/
COPY ./pom.xml /usr/miner/pom.xml
RUN cd /usr/miner && \
    mvn clean package -DskipTests

#FROM openjdk:17-alpine
FROM eclipse-temurin:17-jdk-jammy
COPY --from=build /usr/miner/target/lib_analyze-1.0-SNAPSHOT.jar lib_analyze.jar
COPY system.properties system.properties
COPY lastIndexProcessed lastIndexProcessed
COPY src/main/resources/libraryFiles src/main/resources/libraryFiles
WORKDIR ./
#CMD ["java", "-jar", "lib_analyze.jar", "start"]
CMD ["java", "-Xmx12g", "-jar", "lib_analyze.jar", "start"]