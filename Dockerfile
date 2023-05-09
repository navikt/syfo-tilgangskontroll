FROM gcr.io/distroless/java17
WORKDIR /app
COPY build/libs/app.jar app.jar
ENV APP_NAME=syfo-tilgangskontroll
ENV JDK_JAVA_OPTIONS="-XX:MaxRAMPercentage=75 -Dspring.profiles.active=remote"
ENV TZ="Europe/Oslo"
EXPOSE 8080
USER nonroot
CMD [ "app.jar" ]
