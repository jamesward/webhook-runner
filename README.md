WebHook Runner
--------------

A web request runs a container image on GCE.

Sample `POST` to `/` request body:
```
{
  "project": "YOUR PROJECT",
  "zone": "us-central1-a",
  "machineType": "n1-standard-1",
  "containerImage": "docker.io/hello-world",
  "name": "OPTIONAL INSTANCE NAME"
}
```

# Deploy
[![Run on Google Cloud](https://deploy.cloud.run/button.svg)](https://deploy.cloud.run)


# Local Dev

1. If needed, login to `gcloud`
1. `./sbt run`
1. `export PROJECT_ID=YOUR_PROJECT_ID`
1. `curl -X POST -H "Content-Type: application/json" -d "{\"project\":\"$PROJECT_ID\",\"zone\":\"us-central1-a\",\"machineType\":\"n1-standard-1\",\"containerImage\":\"docker.io/hello-world\"}" http://localhost:9000`

# Local Dev (Docker)

1. `docker build -t webhook-runner .`
1. `export KEY_FILE=YOUR_SERVICE_ACCOUNT_KEY_FILE`
1. `docker run -p9000:9000 -eAPPLICATION_SECRET=$(cat /dev/urandom|tr -dc 'a-z0-9'|fold -w 16|head -n 1) -eGOOGLE_APPLICATION_CREDENTIALS=/root/user.json -v$KEY_FILE:/root/user.json --entrypoint=/bin/sh webhook-runner -c "gcloud auth activate-service-account --key-file=/root/user.json --quiet && /app/bin/webhook-runner"`
1. `export PROJECT_ID=YOUR_PROJECT_ID`
1. `curl -X POST -H "Content-Type: application/json" -d "{\"project\":\"$PROJECT_ID\",\"zone\":\"us-central1-a\",\"machineType\":\"n1-standard-1\",\"containerImage\":\"docker.io/hello-world\"}" http://localhost:9000`

# Test

1. If needed, login to `gcloud`
1. `export PROJECT_ID=YOUR_PROJECT_ID`
1. (optional) `export SERVICE_ACCOUNT=YOUR_SERVICE_ACCOUNT`
1. `./sbt test`

# TODO

- Instance should shutdown after docker process exits
- Docker logs don't seem to be coming through
- Pass Env Vars (manually, from an existing resource, etc)
- Other create options
- Service Account for container?
