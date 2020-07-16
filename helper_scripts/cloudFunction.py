import sys
import json
from flask import jsonify
from google.api_core.client_options import ClientOptions
from google.cloud import automl_v1
from google.cloud.automl_v1.proto import service_pb2
from google.protobuf.json_format import MessageToJson


def inline_text_payload(file_path):
  with open(file_path, 'rb') as ff:
    content = ff.read()
  return {'text_snippet': {'content': content, 'mime_type': 'text/plain'} }

def pdf_payload(file_path):
  return {'document': {'input_config': {'gcs_source': {'input_uris': [file_path] } } } }

def get_prediction(file_path, model_name):
  options = ClientOptions(api_endpoint='automl.googleapis.com')
  prediction_client = automl_v1.PredictionServiceClient(client_options=options)

  #payload = inline_text_payload(file_path)
  payload = pdf_payload(file_path)

  params = {}
  request = prediction_client.predict(model_name, payload, params)
  return request  # waits until request is returned

def predict(file_path):
  model_name = "projects/1040072754811/locations/us-central1/models/TEN4349283170412134400"
  return get_prediction(file_path, model_name)

def runAutoML(request):
    request_json = request.get_json()
    uri = ""
    content_type = request.headers['content-type']
    if content_type == "application/json":
        request_json = request.get_json(silent=True)
        if request_json and 'data' in request_json:
            uri = request_json['data']
        else:
            raise ValueError("JSON is invalid, or missing a 'username' property")
    else:
       name = "Wrong Content Type"

    result =  predict(uri)
    store = dict()
    serialized = MessageToJson(result)
    js = json.loads(serialized)
    for obj in js["payload"]:
      if str(obj["displayName"]) not in store.keys():
        store[str(obj["displayName"])] = []
      store[str(obj["displayName"])].append(str(obj["textExtraction"]["textSegment"]["content"]).lower())
    return jsonify(store)
  
