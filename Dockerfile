FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true

COPY init.sh /init-scripts/init.sh

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dspring.config.additional-location="file:./config.properties" \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local"

