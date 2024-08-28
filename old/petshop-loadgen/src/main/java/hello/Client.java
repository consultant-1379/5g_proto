package hello;

import io.reactivex.Flowable;
import io.reactivex.Single;
import io.reactivex.subjects.PublishSubject;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.web.client.HttpResponse;
import io.vertx.ext.web.client.WebClient;
import io.vertx.ext.web.client.WebClientOptions;


public class Client{
    private int port;
    private String host;
    private WebClientOptions options;
    private WebClient client;
    private int maxParallel;

    public Client(Vertx vertx, int port, String host, WebClientOptions options) {
        this.port = port;
        this.host = host;
        this.options = options;
        this.client = WebClient.create(vertx,options);
        this.maxParallel = options.getHttp2MaxPoolSize()*options.getHttp2MultiplexingLimit();
    }

    public Single<ClientRequestState> rxGet(ClientRequestState requestState){
        PublishSubject<ClientRequestState> downStream = PublishSubject.create();
        this.client.get(port, host , requestState.getUri()).send(
                new Handler<AsyncResult<HttpResponse<Buffer>>>() {
                    @Override
                    public void handle(AsyncResult<HttpResponse<Buffer>> ar) {
                        requestState.setResponse(ar.result());
                        requestState.stop();

                        downStream.onNext(requestState);
                        downStream.onComplete();
                    }
                }
        );
        return Single.fromObservable(downStream);
    }

    public Flowable<ClientRequestState> rxGetFlowable(ClientRequestState requestState){
        return rxGet(requestState).toFlowable();
    }

    public int getMaxParallel() {
        return maxParallel;
    }

    public void close(){
        this.client.close();
    }

}
