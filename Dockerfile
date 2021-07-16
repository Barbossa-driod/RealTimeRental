FROM openjdk:11-jdk

# Setup log directory
RUN mkdir -p /var/log/connector/realtimerental
RUN chmod 777 -R /var/log/connector/realtimerental

# Setup user/group for running application
RUN groupadd spring && useradd -g spring spring
USER spring:spring

ARG DEPENDENCY=target/dependency
COPY ${DEPENDENCY}/BOOT-INF/lib /app/lib
COPY ${DEPENDENCY}/META-INF /app/META-INF
COPY ${DEPENDENCY}/BOOT-INF/classes /app

ENTRYPOINT ["java","-cp","app:app/lib/*","com.safely.batch.connector.Application"]