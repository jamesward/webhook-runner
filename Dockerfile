FROM adoptopenjdk/openjdk8 as builder

WORKDIR /app
COPY .  /app

RUN ./sbt stage

FROM gcr.io/cloud-builders/gcloud as gcloud

FROM adoptopenjdk/openjdk8

COPY --from=builder /app/target/universal/stage /app

RUN echo "deb [signed-by=/usr/share/keyrings/cloud.google.gpg] https://packages.cloud.google.com/apt cloud-sdk main" | tee -a /etc/apt/sources.list.d/google-cloud-sdk.list
RUN apt-get update
RUN apt-get install apt-transport-https ca-certificates
RUN curl https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo apt-key --keyring /usr/share/keyrings/cloud.google.gpg add -
RUN apt-get update && apt-get install google-cloud-sdk

CMD ["/app/bin/webhook-runner"]
