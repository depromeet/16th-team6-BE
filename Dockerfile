FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace/app

ARG VERSION=0.0.1

COPY . .

RUN ./gradlew build -x test --parallel
RUN mkdir -p build/extracted && (java -Djarmode=layertools -jar build/libs/atcha-${VERSION}.jar extract --destination build/extracted)

FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp

ARG VERSION=0.0.1
ARG EXTRACTED=/workspace/app/build/extracted

RUN apk update
RUN apk --no-cache add curl

COPY --from=build ${EXTRACTED}/dependencies/ ./
COPY --from=build ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=build ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=build ${EXTRACTED}/application/ ./
ENTRYPOINT ["java", "-Duser.timezone=Asia/Seoul", "org.springframework.boot.loader.launch.JarLauncher"]
