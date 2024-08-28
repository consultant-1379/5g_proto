from flask import Flask, Response, request
import json
import time


app = Flask(__name__)

def make_resp_str(province):
    return f'{{"provinces": [ {{"name": "{province}", "priority": 1 }}, {{"name": "{province}", "priority": 2 }} ] }}'

@app.route("/")
def hello():
    return Response("Hi from your Flask app running in your Docker container!")

@app.route("/nslf-disc/v0/provinces", methods=['GET', 'POST'])
def ccCreate():
    supi_with_imsi = request.args.get('supi')
    supi = int(supi_with_imsi.replace("imsi-", ""))
    print(f"supi: {supi}")
    #time.sleep(60)
    if (supi >= 1000) and (supi < 2000):
      print(" --> province A")
      resp = Response(make_resp_str("provinceA"), 200)
      return resp
    if (supi >= 2000) and (supi < 3000):
      print(" --> province B")
      resp = Response(make_resp_str("provinceB"), 200)
      return resp
    if (supi >= 3000) and (supi < 4000):
      print(" --> province C")
      resp = Response(make_resp_str("provinceC"), 200)
      return resp
    resp = Response("Unknown Province", 404)
    return resp


if __name__ == "__main__":
    app.run("0.0.0.0", port=80, debug=True)
