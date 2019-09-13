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

Use with Cloud Run & Cloud Scheduler
1. Create a Service Account
    1. TODO
    1. Add Cloud Run Invoker Role
1. Deploy this app on Cloud Run
    1. TODO
    1. Authenticated by service account created above
1. Create a Cloud Scheduler job with the following options
    - HTTP Endpoint of your Cloud Run app
    - Payload of JSON like above
    - Auth header of "OIDC Token"
    - Service Account created above
