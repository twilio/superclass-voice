package com.twilio.verbexamples;


import com.twilio.CallbackWebsocketHandler;
import com.twilio.Configuration;
import com.twilio.Constants;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Call;
import com.twilio.type.PhoneNumber;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Route;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import static com.twilio.WebHookUtilities.getCallSid;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class CallAPI {

    private final static Logger LOG = LoggerFactory.getLogger(CallAPI.class);
    private static final ConcurrentHashMap<String, CallRecord> callMap = new ConcurrentHashMap<>();

    public static Route handleCallStatus() {
        return (request, response) -> {
            // Update the call status in the call map
            final String callSid = getCallSid(request);
            final CallRecord callRecord = callMap.compute(callSid, updateCallRecordStatus(request));

            //Broadcast to websocket
            CallbackWebsocketHandler.broadcastJSON(callRecord.toJSON());
            LOG.info("Call status updated; updated call record:" + callRecord.toString());
            return "Ok";
        };
    }

    private static BiFunction<? super String, ? super CallRecord, ? extends CallRecord> updateCallRecordStatus(
        final Request request) {

        return (key, record) -> {
            final CallRecord modifiedRecord = record != null ? record : new CallRecord(request.queryParams("CallSid"),
                request.queryParams("From"), request.queryParams("To"));
            modifiedRecord.setStatus(request.queryParams("CallStatus"));
            return modifiedRecord;
        };
    }

    public static void handleCallCommand(final Session user, final JSONObject jsonObject) {
        // This is a command coming from the websocket.
        if (jsonObject.get("command").toString().equals("Hangup")) {
            hangupCall(jsonObject.get("CallSid").toString());
        }
        if (jsonObject.get("command").toString().equals("Load")) {
            sendAllCalls();
        }
    }

    private static void sendAllCalls() {
        CallbackWebsocketHandler.broadcastJSON(getAllCallRecords());

    }

    private static JSONObject getAllCallRecords() {
        final JSONObject jsonObject =  new JSONObject();
        jsonObject.put("callbackType","allCalls");
        List<JSONObject> allobjects = callMap.values().stream().map(cr->cr.toJSON()).collect(Collectors.toList());
        jsonObject.put("allCalls",allobjects);
        return jsonObject;
    }

    private static void hangupCall(final String callSid) {
        try {
            final Call call;
            final String actualCallSid;
            if (callSid.startsWith("hangup_")) {
                actualCallSid = callSid.substring(7);
            } else {
                actualCallSid = callSid;
            }
            call = Call.updater(Configuration.ACCOUNT_SID, actualCallSid).setStatus(Call.UpdateStatus.COMPLETED)
                       .update();
            LOG.info("Call hung up:" + call.toString());
        } catch (Exception e) {
            LOG.info("Failed to hang up call dues to {}", e);
        }
    }

    public static Call createCall(final String to, final String from, final String baseUrl, final String destUrl) {
        final Call call;
        try {
            call = Call.creator(
                new PhoneNumber(to),
                new PhoneNumber(from),
                new URI(baseUrl + destUrl))
                       .setStatusCallback(Configuration.BASE_URL + Constants.CALL_STATUS_URL)
                       .setStatusCallbackMethod(HttpMethod.POST)
                       .setStatusCallbackEvent(Arrays.asList("initiated", "ringing", "answered", "completed"))
                       .setMethod(HttpMethod.GET)
                       .create();
            LOG.info("Created call:" + call.toString());
            callMap.compute(call.getSid(), handleCreateCall(call));
            return call;
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private static BiFunction<? super String, ? super CallRecord, ? extends CallRecord> handleCreateCall(
        final Call call
    ) {
        return (key, record) -> record != null ? record : new CallRecord(call.getSid(), call.getFrom(), call.getTo());
    }

    public static void updateStatusBasedOnTwimlFetch(final Request request) {
        final CallRecord callRecord = callMap
            .compute(getCallSid(request), updateCallRecordStatusForTwimlFetch(request));
        LOG.info("Call status updated; updated call record:" + callRecord.toString());
        CallbackWebsocketHandler.broadcastJSON(callRecord.toJSON());
    }

    private static BiFunction<? super String, ? super CallRecord, ? extends CallRecord> updateCallRecordStatusForTwimlFetch(
        final Request request) {
        // What is special here is that for incoming calls we don't have the option of specifying a specific
        // "in-progress" event
        // to be sent, instead we assume that the TwiML instructions will answer the call immediately
        // and thus we modify the status to be "in-progress"
        return (key, record) -> {
            final CallRecord modifiedRecord = record != null ? record : new CallRecord(request.queryParams("CallSid"),
                request.queryParams("From"), request.queryParams("To"));
            modifiedRecord.setStatus("in-progress");
            return modifiedRecord;
        };
    }

    public static CallRecord getCallRecord(final String psid) {
        return callMap.get(psid);
    }
}
