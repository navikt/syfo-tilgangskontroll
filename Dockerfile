FROM navikt/java:8-appdynamics
ENV APPD_ENABLED=true
RUN echo "ldap.password=$(cat /secrets/ldap/ldap/password)" >> config.properties

COPY build/libs/*.jar app.jar

ENV JAVA_OPTS="-Djava.security.egd=file:/dev/./urandom \
               -Dspring.profiles.active=remote \
               -Dhttps.proxyHost=webproxy-nais.nav.no \
               -Dhttps.proxyPort=8088 \
               -Dspring.config.location="file:./config.properties" \
               -Dhttp.nonProxyHosts=*.adeo.no|*.preprod.local"
