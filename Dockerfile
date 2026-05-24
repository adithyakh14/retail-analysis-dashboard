FROM eclipse-temurin:21-jdk

WORKDIR /app

COPY src ./src

RUN mkdir -p target/classes && \
    find src/main/java -name "*.java" > sources.txt && \
    javac -d target/classes @sources.txt && \
    cp -r src/main/resources/* target/classes/

EXPOSE 8080

CMD ["java", "-cp", "target/classes", "com.retailproject.RetailDashboardServer"]
