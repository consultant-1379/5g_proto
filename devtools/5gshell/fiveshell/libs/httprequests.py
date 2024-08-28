import requests
import time
from requests.models import Response


# ------------------------------------------------------------------------
# HTTP Request Helper Functions

# Public
def get_request(urls, num_retries=None):
    """Send a HTTP1.1 GET request and return the resultcode and the data
    returned"""
    if isinstance(urls, str):
        urls = [urls]
    out = ""
    for url in urls:
        r = get_single_request(url, num_retries)
        out += str(r.status_code) + ": " + r.text + "\n"
    return out


# Public
def get_single_request(url, num_retries=None):
    """Send a single HTTP1.1 GET request and return the result. 
    Retry a given number of times."""
    if num_retries is None:
        num_retries = 0
    for _ in range(num_retries + 1):  # +1 for the initial try
        try:
            result = requests.get(url)
            return result
        except:
            time.sleep(1)
    # We get here if we couldn't connect (or got another exception) after the
    # specified number of retries. Construct an appropriate respone object:
    response = Response()
    response.code = "Request Timeout"
    response.status_code = 408
    response.text = "Request Timeout"  # or should this be empty?
    return response


# Public
def post_request(urls, num_retries=None):
    """Send a HTTP1.1 POST request and return the resultcode and the data
    returned. You can pass a single URL as a string or a list of URLs."""
    if isinstance(urls, str):
        urls = [urls]
    out = ""
    for url in urls:
        r = post_single_request(url, num_retries)
        out += str(r.status_code) + ": " + r.text + "\n"
    return out


# Public
def post_single_request(url, num_retries=None):
    """Send a single HTTP1.1 POST request and return the result. 
    Retry a given number of times."""
    if num_retries is None:
        num_retries = 0
    for _ in range(num_retries + 1):
        try:
            result = requests.post(url)
            return result
        except:
            time.sleep(1)
    # We get here if we couldn't connect (or got another exception) after the
    # specified number of retries. Construct an appropriate respone object:
    response = Response()
    response.code = "Request Timeout"
    response.status_code = 408
    # response.text = "Request Timeout"  # or should this be empty?
    return response


# Public
def delete_request(urls, num_retries=None):
    """Send a HTTP1.1 DELETE request and return the resultcode and the data
    returned"""
    if isinstance(urls, str):
        urls = [urls]
    out = ""
    for url in urls:
        r = delete_single_request(url, num_retries)
        out += str(r.status_code) + ": " + r.text + "\n"
    return out


# Public
def delete_single_request(url, num_retries=None):
    """Send a single HTTP1.1 DELETE request and return the result. 
    Retry a given number of times."""
    if num_retries is None:
        num_retries = 0
    for _ in range(num_retries + 1):
        try:
            result = requests.delete(url)
            return result
        except:
            time.sleep(1)
    # We get here if we couldn't connect (or got another exception) after the
    # specified number of retries. Construct an appropriate respone object:
    response = Response()
    response.code = "Request Timeout"
    response.status_code = 408
    response.text = "Request Timeout"  # or should this be empty?
    return response
