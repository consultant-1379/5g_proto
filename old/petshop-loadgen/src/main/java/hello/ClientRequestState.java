package hello;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.core.buffer.Buffer;


import java.time.Instant;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

public class ClientRequestState {
    private int randNum;
    private Long startTime = 0L;
    private Long stopTime = 0L;
    private HttpResponse<Buffer> response;

    public ClientRequestState(int randNum) {
        this.randNum = randNum;
        this.startTime = System.nanoTime();
    }

    public void stop(){
        this.stopTime = System.nanoTime();
    }

    public double duration(){
        if(this.stopTime == 0) {
            return 0d;
        }
        return (this.stopTime - this.startTime)/1000000;
    }

    public int getRandNum(){
        return this.randNum;
    }

    public String getUri(){
        return "/pets/" + this.randNum;
    }

    public HttpResponse<Buffer> getResponse() {
        return response;
    }

    public void setResponse(HttpResponse<Buffer> response) {
        this.response = response;
    }

    public boolean isResponseOk(){
        return getResponseBody().getInteger("id") == this.randNum;
    }

    public JsonObject getResponseBody(){
        if(response != null) {
            return this.response.bodyAsJsonObject();
        } else {
            return new JsonObject();
        }
    }

    @Override
    public String toString() {
        return "Random Number: "
                + randNum
                + ", \nBody: "
                + getResponseBody().encodePrettily()
                + ", \nDelay: "
                + duration();
    }
}
