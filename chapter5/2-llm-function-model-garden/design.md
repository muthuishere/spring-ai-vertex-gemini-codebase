

All GCP Models Endpoint

Anthrophic

The request.json file 
```json
{
  "anthropic_version": "vertex-2023-10-16",
  "messages": [
    {
      "role": "user",
      "content": [
        {
          "type": "image",
          "source": {
            "type": "base64",
            "media_type": "image/png",
            "data": "iVBORw0KGg..."
          }
        },
        {
          "type": "text",
          "text": "What is in this image?"
        }
      ]
    }
  ],
  "max_tokens": 256,
  "stream": true
}
```

```shell



PROJECT_ID=your-gcp-project-id
MODEL_ID=claude-3-5-sonnet@20240620

# Pick one region:
LOCATION=europe-west1
# LOCATION=us-east5

curl -X POST \
  -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -H "Content-Type: application/json; charset=utf-8" \
  -d @request.json \
"https://$LOCATION-aiplatform.googleapis.com/v1/projects/$PROJECT_ID/locations/$LOCATION/publishers/anthropic/models/$MODEL_ID:streamRawPredict"
```


Gemini

```shell
MODEL_ID="gemini-1.5-pro-001"
PROJECT_ID="YOUR_PROJECT_ID"

curl \
  -X POST \
  -H "Authorization: Bearer $(gcloud auth application-default print-access-token)" \
  -H "Content-Type: application/json" \
  https://$LOCATION-aiplatform.googleapis.com/v1/projects/${PROJECT_ID}/locations/$LOCATION/publishers/google/models/${MODEL_ID}:streamGenerateContent -d \
  $'{
    "contents": {
      "role": "user",
      "parts": [
        {
        "fileData": {
          "mimeType": "image/png",
          "fileUri": "gs://generativeai-downloads/images/scones.jpg"
          }
        },
        {
          "text": "Describe this picture."
        }
      ]
    }
  }'
```



Mistral


```shell
MODEL="mistral-large"
MODEL_VERSION="2407"
$MODELID="$MODEL@$MODEL_VERSION"


url="https://$LOCATION-aiplatform.googleapis.com/v1/projects/$PROJECT_ID/locations/$LOCATION/publishers/mistralai/models/$MODELID:rawPredict"


curl \
  -X POST \
  -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -H "Content-Type: application/json" \
  $url \
  --data '{
    "model": "'"$MODEL"'",
    "temperature": 0,
    "messages": [
      {"role": "user", "content": "What is the best French cheese?"}
    ]
}'
```


llama

```shell
ENDPOINT=us-central1-aiplatform.googleapis.com
REGION=us-central1
PROJECT_ID="YOUR_PROJECT_ID"

curl \
  -X POST \
  -H "Authorization: Bearer $(gcloud auth print-access-token)" \
  -H "Content-Type: application/json" https://$LOCATION-aiplatform.googleapis.com/v1beta1/projects/${PROJECT_ID}/locations/${LOCATION}/endpoints/openapi/chat/completions \
  -d '{"model":"meta/llama3-405b-instruct-maas", "stream":true, "messages":[{"role": "user", "content": "Summer travel plan to Paris"}]}'
```