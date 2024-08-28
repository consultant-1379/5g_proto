package com.ericsson.sc.fm;

import static org.testng.Assert.assertTrue;

import java.net.UnknownHostException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import com.ericsson.sc.fm.model.fi.AdditionalInformation.AdditionalInformationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.FaultIndicationBuilder;
import com.ericsson.sc.fm.model.fi.FaultIndication.Severity;
import com.ericsson.utilities.http.WebClientProvider;
import com.ericsson.utilities.reactivex.VertxInstance;
import com.fasterxml.jackson.core.JsonProcessingException;

import io.netty.handler.codec.http.HttpResponseStatus;
import io.vertx.reactivex.core.Vertx;

public class FmAlarmHandlerTest
{
    private FmAlarmHandlerServer alarmHandlerServer;
    private String localAddress;
    private int port;
    private Vertx vertx;
    private static final Logger log = LoggerFactory.getLogger(FmAlarmHandlerTest.class);

    @BeforeMethod
    public void beforeMethod() throws UnknownHostException
    {
        this.vertx = VertxInstance.get();
        this.localAddress = "127.0.0.10";

        this.alarmHandlerServer = new FmAlarmHandlerServer(this.localAddress, 0, false, null, null, this.vertx);
        this.alarmHandlerServer.setStatusCode(HttpResponseStatus.NO_CONTENT);
        this.alarmHandlerServer.setStatusMessage("Successful - No content");
        this.alarmHandlerServer.start().blockingAwait();
        this.port = this.alarmHandlerServer.getPort();
        log.info("FmAlarmServer started ip:{}, port:{}", this.localAddress, this.port);
    }

    @AfterMethod
    public void afterMethod()
    {
        this.alarmHandlerServer.stop().andThen(this.vertx.rxClose().onErrorComplete()).blockingAwait();
    }

    @Test
    public void simpleTest() throws JsonProcessingException, InterruptedException
    {
        var client = WebClientProvider.builder().withHostName(this.localAddress).build(this.vertx);
        var alarmHandler = new FmAlarmHandler(client, this.localAddress, this.port, false);
        var alarmService = new FmAlarmServiceImpl(alarmHandler);
        var faultIndication = new FaultIndicationBuilder().withFaultName("TestFmAlarm") //
                                                          .withFaultyResource("MyTestResource") //
                                                          .withServiceName("MyFaultyService") //
                                                          .withSeverity(Severity.CRITICAL) //
                                                          .withDescription("Testing FIAPI new implementation") //
                                                          .withExpiration(0L) //
                                                          .withAdditionalInformation(new AdditionalInformationBuilder().withAdditionalProperty("faultyResources",
                                                                                                                                               "[ test1, test2 ]") //
                                                                                                                       .build())
                                                          .build();

        alarmService.raise(faultIndication).blockingGet();

        var reqbody = this.alarmHandlerServer.getLastReqBody();
        log.debug("Request Body to check: {}", reqbody.encodePrettily());
        log.info("fault name: {}", reqbody.getString("faultName"));
        assertTrue(reqbody.getString("faultName").equals("TestFmAlarm"), "Value of <faultName> is not equal to TestFmAlarm");
        assertTrue(reqbody.getString("faultyResource").equals("MyTestResource"), "Value of <faultyResource> is not equal to MyTestResource");
        assertTrue(reqbody.getString("serviceName").equals("MyFaultyService"), "Value of <serviceName> is not equal to MyFaultyService");
        assertTrue(reqbody.getString("severity").equals("Critical"), "Value of <severity> is not equal to Critical");
        assertTrue(reqbody.getString("description").equals("Testing FIAPI new implementation"), "Value of <description> is wrong");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDateTime now = LocalDateTime.now();
        assertTrue(reqbody.getString("createdAt").contains(dtf.format(now)), "Value of <createdAt> date is wrong");

        assertTrue(reqbody.getLong("expiration").equals(0L), "Value of <expiration> is not equal to 0");

        assertTrue(reqbody.getJsonObject("additionalInformation").getValue("faultyResources").equals("[ test1, test2 ]"),
                   "Value of <faultName> is not equal to TestFmAlarm");
    }
}
