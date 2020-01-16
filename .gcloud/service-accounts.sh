#!/bin/bash

#set -euo pipefail
#set -x

declare project=$GOOGLE_CLOUD_PROJECT
declare service=$K_SERVICE
declare region=$GOOGLE_CLOUD_REGION

#gcloud services enable iam.googleapis.com

declare invokersaname=$service-invoker
declare invokersa=$invokersaname@$project.iam.gserviceaccount.com

gcloud iam service-accounts describe $invokersa --project $project &> /dev/null

if [ $? -ne 0 ]; then
  echo "creating invoker service account: $invokersa"
  gcloud iam service-accounts create $invokersaname \
    --display-name="$service invoker" \
    --project=$project
fi

echo "allowing $invokersa to call the $service service"
gcloud run services add-iam-policy-binding $service \
  --region="$region" \
  --platform=managed \
  --member="serviceAccount:$invokersa" \
  --role="roles/run.invoker" &> /dev/null

declare runnersaname=$service-runner
declare runnersa=$runnersaname@$project.iam.gserviceaccount.com

gcloud iam service-accounts describe $runnersa --project $project &> /dev/null

if [ $? -ne 0 ]; then
  echo "creating runner service account: $runnersa"
  gcloud iam service-accounts create $runnersaname \
    --display-name="$service runner" \
    --project=$project

  echo "gonna wait 30 seconds for stuff to happen"
  sleep 30
fi

echo "allowing $runnersa to create a GCE instance"
gcloud projects add-iam-policy-binding $project \
  --member=serviceAccount:$runnersa \
  --role=roles/compute.instanceAdmin &> /dev/null

echo "allowing $runnersa to be a serviceAccountUser"
gcloud projects add-iam-policy-binding $project \
  --member=serviceAccount:$runnersa \
  --role=roles/iam.serviceAccountUser &> /dev/null

echo "updating $service to use the service account $runnersa"
gcloud run services update $service \
  --platform=managed --project=$project --region=$region \
  --service-account=$runnersa &> /dev/null

declare endpoint=$(gcloud run services describe $service --platform=managed --region=$region --project=$project --format="value(status.address.url)")

echo ""

echo "If you'd like to run a container on a schedule, visit: "
echo "https://console.cloud.google.com/cloudscheduler/jobs/new?project=$project"
echo "Create a new job with these params:"
echo "Target: HTTP"
echo "URL: $endpoint"
echo "HTTP Method: POST"
echo "Body:"
echo "{"
echo "  \"project\": \"$project\","
echo "  \"zone\": \"$region-a\","
echo "  \"machineType\": \"n1-standard-1\","
echo "  \"containerImage\": \"docker.io/hello-world\""
echo "}"
echo "Select *SHOW MORE* and specify:"
echo "Auth header of 'OIDC Token'"
echo "Service account: $invokersa"
echo "Press *Create* to create the job"

echo ""

echo "To test your webhook runner, run a command like:"
echo "curl -X POST -H \"Content-Type: application/json\" \\"
echo "  -d '{\"project\":\"$project\",\"zone\":\"$region-a\",\"machineType\":\"n1-standard-1\",\"containerImage\":\"docker.io/hello-world\"}' \\"
echo "  -H \"Authorization: Bearer \$(gcloud auth print-identity-token)\" \\"
echo "  $endpoint"
