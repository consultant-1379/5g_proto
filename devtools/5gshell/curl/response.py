class Response:

    def __init__(self, headers, body):
        self.headers = headers
        self.body = body
        self.location = ""
        self.resultCode = ""
        self.parse()

    def __str__(self):
        return self.headers + "\n" + self.body

    def parse(self):
        items = self.headers.split("\n")
        for item in items:
            if "HTTP" in item:
                self.resultCode = item.replace("< ", "").strip()
            elif "location" in item:
                self.location = item.replace("< location: ", "").strip()

    def getChargingReference(self):
        res = self.location.split("/")
        return res[len(res) - 1]
