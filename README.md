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


# TODO

- Instance should shutdown after docker process exits
- Docker logs don't seem to be coming through
- Pass Env Vars (manually, from an existing resource, etc)
- Other create options
- Service Account for container?
