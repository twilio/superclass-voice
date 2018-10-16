package com.twilio;

import com.twilio.verbexamples.CallAPI;
import com.twilio.verbexamples.ConferenceAPI;
import com.twilio.verbexamples.PayAPI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static spark.Spark.get;
import static spark.Spark.init;
import static spark.Spark.post;
import static spark.Spark.staticFiles;
import static spark.Spark.webSocket;

import com.twilio.verbexamples.GatherAPI;

public class SuperClass {


    public static void main(String[] args) {

        final Logger LOG = LoggerFactory.getLogger(SuperClass.class);

        Twilio.init(Configuration.ACCOUNT_SID, Configuration.AUTH_TOKEN);
        staticFiles.location("/public"); //index.html is served at localhost:4567 (default port)
        staticFiles.expireTime(600);
        webSocket("/websocket", CallbackWebsocketHandler.class);
        init();


        //
        // Entrypoint for call status
        //
        post(Constants.CALL_STATUS_URL, CallAPI.handleCallStatus());
        post(Constants.GATHERCALLBACK_URL, GatherAPI.gatherCallback());

        //
        // Entrypoint to render a "Secure gather"
        //
        get(Constants.GATHER_URL, GatherAPI.handleGather(Configuration.BASE_URL));
        post(Constants.GATHERCALLBACK_URL, GatherAPI.gatherCallback());

        //
        // Entry point to trigger a call to the "to" number for the Secure Gather scenario
        //
        get(Constants.START_GATHER_URL, GatherAPI.gatherStart(
            Configuration.from, Configuration.to, Configuration.BASE_URL));

        //
        // Entrypoints for Pay
        //
        get(Constants.PAY_URL, PayAPI.renderPay(Configuration.BASE_URL));
        post(Constants.PAYSTATUS_URL, PayAPI.paymentStatus());
        post(Constants.PAYACTION_URL, PayAPI.payAction());

        //
        // Entry point to trigger a call to the "to" number for the Pay scenario
        //
        get(Constants.STARTPAY_URL, PayAPI.payStart(Configuration.from, Configuration.to, Configuration.BASE_URL));

        //
        // Entrypoints for Conference
        //
        get(Constants.CONFERENCE_URL, ConferenceAPI.handleConference(Configuration.BASE_URL));
        post(Constants.CONFERENCE_STATUS_URL, ConferenceAPI.handleConferenceStatus());
        post(Constants.CONFERENCE_WAITURL, ConferenceAPI.handleWait());
        get(Constants.CONFERENCE_START, ConferenceAPI.conferenceStart(Configuration.from, Configuration.to, Configuration.BASE_URL));

    }

}