#!/bin/bash

# Fetch new access token
ACCESS_TOKEN=$(gcloud auth application-default print-access-token)

echo $ACCESS_TOKEN
