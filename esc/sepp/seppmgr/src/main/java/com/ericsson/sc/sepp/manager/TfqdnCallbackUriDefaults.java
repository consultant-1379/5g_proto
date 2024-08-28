package com.ericsson.sc.sepp.manager;

import java.util.ArrayList;
import java.util.List;

import com.ericsson.sc.sepp.model.CallbackUriDefault;

public class TfqdnCallbackUriDefaults
{
    List<CallbackUriDefault> callbackUriDefaults = new ArrayList<>();

    public TfqdnCallbackUriDefaults()
    {
        this.createCallbackUriDefaultsSet();
    }

    private void createCallbackUriDefaultsSet()
    {
        List<String> callbackUriJsonPointer = List.of("/callbackReference");

        var callbackUriDefault = new CallbackUriDefault().withApiName("nudm-ee").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/nfStatusNotificationUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nnrf-nfm").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/notificationUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("npcf-smpolicycontrol").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/n2NotifyUri", "/n1n2FailureTxfNotifURI", "/n1NotifyCallbackUri", "/n2NotifyCallbackUri", "/amfStatusUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("namf-comm").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/subscription/eventNotifyUri", "/subscription/subsChangeNotifyUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("namf-evts").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/locationNotificationUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("namf-loc").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/notifUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nchf-spendinglimitcontrol")
                                                     .withApiVersion(1)
                                                     .withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/notifyUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nchf-convergedcharging")
                                                     .withApiVersion(2)
                                                     .withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/smContextStatusUri", "/vsmfPduSessionUri", "/ismfPduSessionUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nsmf-pdusession").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/authUpdateCallbackUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nudm-niddau").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/callbackReference");

        callbackUriDefault = new CallbackUriDefault().withApiName("nudm-sdm").withApiVersion(2).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/deregCallbackUri", "/pcscfRestorationCallbackUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nudm-uecm").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/notifUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("naf-eventexposure").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/notificationURI");

        callbackUriDefault = new CallbackUriDefault().withApiName("nnwdaf-eventssubscription")
                                                     .withApiVersion(1)
                                                     .withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/nfNssaiAvailabilityUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nnssf-nssaiavailability")
                                                     .withApiVersion(1)
                                                     .withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/amfCallBackURI");

        callbackUriDefault = new CallbackUriDefault().withApiName("nlmf-broadcast").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/hgmlcCallBackURI");

        callbackUriDefault = new CallbackUriDefault().withApiName("nlmf-loc").withApiVersion(1).withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);

        callbackUriJsonPointer = List.of("/notifyUri");

        callbackUriDefault = new CallbackUriDefault().withApiName("nchf-convergedcharging")
                                                     .withApiVersion(3)
                                                     .withCallbackUriJsonPointer(callbackUriJsonPointer);

        this.callbackUriDefaults.add(callbackUriDefault);
    }

    public List<CallbackUriDefault> getCallbackUriDefaults()
    {
        return this.callbackUriDefaults;
    }

}
