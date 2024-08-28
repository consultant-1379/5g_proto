#!/usr/bin/env python
#Simple server that serves authentification token and username for artifactories 
import BaseHTTPServer
import time
import yaml
import os
from urlparse import urlparse, parse_qs

HOST_NAME = ''
PORT_NUMBER = 1354 # Maybe set this to 9000.
 
 
class MyHandler(BaseHTTPServer.BaseHTTPRequestHandler):
    def do_HEAD(s):
        s.send_response(200)
        s.send_header("Content-type", "text/html")
        s.end_headers()

    def modify_url(s,url):
        if "https://" in url or "http://" in url:
            url = url[url.index("://")+3:]
        modified_url = url.split("/")[0]
        return modified_url

    def get_repo_cred(s,url,filename):
        url = s.modify_url(url)
        with open(filename, 'r') as stream:
            try:
                repos = yaml.safe_load(stream)
            except yaml.YAMLError as exc:
                print(exc)
        if repos is None:
            sys.exit()
        for repo in repos.get("repositories"):
            if str(url) in repo.get('url'):
                return repo.get('username'),repo.get('password')
        return None, None

    def do_GET(s):
         """Respond to a GET request."""
         s.send_response(200)
         s.send_header("Content-type", "text/html")
         s.end_headers()
         query_components = parse_qs(urlparse(s.path).query,keep_blank_values=True)
         if "url" in query_components:
             url = query_components["url"][0]
             #s.wfile.write("<p>Your arguments: %s</p>" % url)
             filename = "/files/helm_repositories.yaml"
             if not os.path.exists(filename):
                 s.wfile.write("File %s does not exist" % filename)
                 return
             username,password = s.get_repo_cred(url,filename)
         if "username" in query_components and username:
             s.wfile.write(username)
             return
         if "password" in query_components and password:
             s.wfile.write(password)
             return
 
if __name__ == '__main__':
   server_class = BaseHTTPServer.HTTPServer
   httpd = server_class((HOST_NAME, PORT_NUMBER), MyHandler)
   print(time.asctime(), "Server Starts - %s:%s" % (HOST_NAME, PORT_NUMBER))
   try:
       httpd.serve_forever()
   except KeyboardInterrupt:
       pass
   httpd.server_close()
   print(time.asctime(), "Server Stops - %s:%s" % (HOST_NAME, PORT_NUMBER))
