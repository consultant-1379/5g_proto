import ssl
import flask
import sys
import os

sys.path.append(os.path.dirname(os.path.realpath(__file__)))

app = flask.Flask(__name__)
from environs import Env

from Sim_API.Services_Sim_API.CAI_Sim_API import *

if __name__ == '__main__':
    env = Env()
    env.read_env()  # read .env file, if it exists
    
    if env.bool("SECURE", True):
        cert_path = env('SIM_CERT_PATH', '/app')
        print('SIM_CERT_PATH {}'.format(cert_path))
        app_key = cert_path + os.sep + 'cai-key.pem'
        app_key_password = None
        app_cert = cert_path + os.sep + 'cai-cert.pem'
        ca_cert = cert_path + os.sep + '/ca/ca-cert.pem'
        print('APP KEY {}'.format(app_key))
        print('APP CERT {}'.format(app_cert))
        print('CA CERT {}'.format(ca_cert))
        
        context = ssl.SSLContext(ssl.PROTOCOL_TLSv1_2)
        # context.verify_mode = ssl.CERT_REQUIRED
        context.load_verify_locations(ca_cert)
        context.load_cert_chain(certfile=app_cert, keyfile=app_key, password=app_key_password)
        app.run('0.0.0.0', 5000, ssl_context=context, debug=True)
    else:
        app.run('0.0.0.0', 5000, debug=True)