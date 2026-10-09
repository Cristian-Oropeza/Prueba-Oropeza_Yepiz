# ---------- Build ----------
FROM gradle:8.10-jdk17-alpine AS build
WORKDIR /app
COPY --chown=gradle:gradle . .
RUN gradle bootJar --no-daemon -x test

# ---------- Runtime ----------
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
RUN addgroup -S app && adduser -S app -G app
COPY --from=build /app/build/libs/*.jar app.jar
RUN chown app:app app.jar
USER app
EXPOSE 8081
# Railway inyecta PORT. Le pasamos GC amistoso para 512MB.
ENV JAVA_OPTS="-XX:+UseG1GC -Xmx400m -Xms256m -XX:+UseContainerSupport"
ENV SPRING_PROFILES_ACTIVE=prod
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -jar /app/app.jar --server.port=${PORT:-8081}"]
