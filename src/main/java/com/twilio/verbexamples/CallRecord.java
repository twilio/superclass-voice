package com.twilio.verbexamples;

import org.json.JSONObject;

import java.util.HashMap;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class CallRecord {

    private final String callSid;
    private final String from;
    private final String to;
    private String status;

    @Override
    public String toString() {
        return "CallRecord{" +
            "callSid='" + callSid + '\'' +
            ", from='" + from + '\'' +
            ", to='" + to + '\'' +
            ", status='" + status + '\'' +
            '}';
    }

    public CallRecord(final String callSid, final String from, final String to) {
        this.callSid = callSid;
        this.from = from;
        this.to = to;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public String getTo() {
        return to;
    }


    public String getFrom() {
        return from;
    }


    public String getCallSid() {
        return callSid;
    }

    public JSONObject toJSON() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackType", "callStatusCallback");
        HashMap<String, String> map = new HashMap<>();
        map.put("CallSid", callSid);
        map.put("From", from);
        map.put("To", to);
        map.put("CallStatus", status);
        jsonObject.put("params", map);
        return jsonObject;
    }
}
