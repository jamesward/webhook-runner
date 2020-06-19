#!/bin/bash

#set -euo pipefail
#set -x

declare project=$GOOGLE_CLOUD_PROJECT
declare service=$K_SERVICE
declare region=$GOOGLE_CLOUD_REGION

#gcloud services enable iam.googleapis.com

declare num=$(cat /dev/urandom|tr -dc '0-9'|fold -w 4|head -n 1)

declare invokersaname=$service-invoker-$num
declare invokersa=$invokersaname@$project.iam.gserviceaccount.com

echo "creating invoker service account: $invokersa"
gcloud iam service-accounts create $invokersaname \
  --display-name="$service invoker" \
  --project=$project

echo "allowing $invokersa to call the $service service"
gcloud run services add-iam-policy-binding $service \
  --project=$project \
  --region=$region \
  --platform=managed \
  --member="serviceAccount:$invokersa" \
  --role="roles/run.invoker" &> /dev/null

declare runnersaname=$service-runner-$num
declare runnersa=$runnersaname@$project.iam.gserviceaccount.com

echo "creating runner service account: $runnersa"
gcloud iam service-accounts create $runnersaname \
  --display-name="$service runner" \
  --project=$project

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
echo "  \"containerImage\": \"gcr.io/cr-demo-235923/hello-webhook-runner\""
echo "}"
echo "Select *SHOW MORE* and specify:"
echo "Auth header of 'OIDC Token'"
echo "Service account: $invokersa"
echo "Press *Create* to create the job"

echo ""

echo "To test your webhook runner, run a command like:"
echo "curl -X POST -H \"Content-Type: application/json\" \\"
echo "  -d '{\"project\":\"$project\",\"zone\":\"$region-a\",\"machineType\":\"n1-standard-1\",\"containerImage\":\"gcr.io/cr-demo-235923/hello-webhook-runner\"}' \\"
echo "  -H \"Authorization: Bearer \$(gcloud auth print-identity-token)\" \\"
echo "  $endpoint"

echo ""

echo "View container logs:"
echo "https://console.cloud.google.com/logs/viewer?project=$project&advancedFilter=(resource.type=\"gce_instance\" AND logName=\"projects/$project/logs/cos_containers\")"
