package com.twilio;

import com.jamesmurty.utils.XMLBuilder2;
import spark.Request;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class WebHookUtilities {

    public static String getParam(final Request request, final String param) {
        return request.queryMap(param).value();
    }

    public static String getCallSid(final Request request) {
        return getParam(request, "CallSid");
    }
    public static XMLBuilder2 createResponse() {
        return XMLBuilder2.create("Response");
    }

}
