#!/bin/bash

set -euo pipefail
set -x

declare project=$GOOGLE_CLOUD_PROJECT
declare service=$K_SERVICE
declare region=$GOOGLE_CLOUD_REGION

declare saname=$service
declare sa=$saname@$project.iam.gserviceaccount.com

#gcloud services enable iam.googleapis.com

gcloud iam service-accounts describe webhook-runner@jw-demo.iam.gserviceaccount.com --project $project &> /dev/null

if [ $? -ne 0 ]; then
  echo "creating service account: $sa"
  gcloud iam service-accounts create $saname \
    --description "$service" \
    --display-name "$service" \
    --project $project
fi

# todo: assign perms

gcloud run services update $service \
  --platform=managed --project=$project --region=$region \
  --service-account=$sa
