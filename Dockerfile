## ---------- Build stage ----------
FROM maven:3-amazoncorretto-21 AS build
WORKDIR /workspace

ARG GH_ACTOR
ARG GH_PACKAGES_TOKEN
ENV MAVEN_SETTINGS_PATH=/tmp/maven-settings.xml

RUN printf '%s\n' \
    '<settings>' \
    '  <servers>' \
    '    <server>' \
    '      <id>github-nimbly-commons</id>' \
    "      <username>${GH_ACTOR}</username>" \
    "      <password>${GH_PACKAGES_TOKEN}</password>" \
    '    </server>' \
    '    <server>' \
    '      <id>github-nimbly-catalog</id>' \
    "      <username>${GH_ACTOR}</username>" \
    "      <password>${GH_PACKAGES_TOKEN}</password>" \
    '    </server>' \
    '    <server>' \
    '      <id>github-nimbly-starters</id>' \
    "      <username>${GH_ACTOR}</username>" \
    "      <password>${GH_PACKAGES_TOKEN}</password>" \
    '    </server>' \
    '  </servers>' \
    '</settings>' \
    > ${MAVEN_SETTINGS_PATH}

COPY pom.xml .
COPY ph-shoes-search-service-core/pom.xml ph-shoes-search-service-core/pom.xml
COPY ph-shoes-text-search-service-web/pom.xml ph-shoes-text-search-service-web/pom.xml
COPY docs ./docs
RUN --mount=type=cache,target=/root/.m2 mvn -s ${MAVEN_SETTINGS_PATH} -q -e -U dependency:go-offline

COPY ph-shoes-search-service-core ./ph-shoes-search-service-core
COPY ph-shoes-text-search-service-web ./ph-shoes-text-search-service-web
RUN --mount=type=cache,target=/root/.m2 mvn -s ${MAVEN_SETTINGS_PATH} -q -pl ph-shoes-text-search-service-web -am package
RUN rm -f ${MAVEN_SETTINGS_PATH}

## ---------- Runtime stage ----------
FROM amazoncorretto:21-alpine AS runtime
ENV APP_HOME=/app \
    JAVA_TOOL_OPTIONS="-XX:+ExitOnOutOfMemoryError -XX:+HeapDumpOnOutOfMemoryError -XX:HeapDumpPath=/tmp -XX:MaxRAMPercentage=75" \
    PORT=8084

RUN addgroup -S spring && adduser -S spring -G spring
RUN apk add --no-cache curl

USER spring:spring
WORKDIR ${APP_HOME}

COPY --from=build /workspace/ph-shoes-text-search-service-web/target/*.jar app.jar

EXPOSE ${PORT}
ENTRYPOINT ["java","-jar","/app/app.jar"]

