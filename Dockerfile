FROM adoptopenjdk/openjdk8 as builder

WORKDIR /app
COPY .  /app

RUN ./sbt stage

FROM gcr.io/google.com/cloudsdktool/cloud-sdk:alpine

RUN apk --update add openjdk8-jre

COPY --from=builder /app/target/universal/stage /app

CMD ["/app/bin/webhook-runner"]
