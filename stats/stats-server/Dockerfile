FROM eclipse-temurin:21-jre-jammy

VOLUME /tmp

# Этот путь теперь корректен, так как контекст сборки - корень проекта
ARG JAR_FILE=stats/stats-server/target/*.jar
COPY ${JAR_FILE} stats-server.jar

ENTRYPOINT ["sh", "-c", "java ${JAVA_OPTS} -jar /stats-server.jar"]
