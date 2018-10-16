package com.twilio.verbexamples;

import com.twilio.CallbackWebsocketHandler;
import com.twilio.Configuration;
import com.twilio.Constants;
import com.twilio.WebHookUtilities;
import com.twilio.http.HttpMethod;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Gather;
import com.twilio.twiml.voice.Say;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Route;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class GatherAPI {

    private final static Logger LOG = LoggerFactory.getLogger(GatherAPI.class);

    public static Route handleGather(String baseUrl) {
        return (request, response) -> {
            LOG.info("Secure gather request for call:" + WebHookUtilities.getCallSid(request));
            CallAPI.updateStatusBasedOnTwimlFetch(request);

            return new VoiceResponse.Builder()
                .gather(
                    new Gather.Builder()
                        .method(HttpMethod.POST)
                        .action(baseUrl + Constants.GATHERCALLBACK_URL)
                        .say(new Say.Builder("Enter your credit card number").build())
                        .build())
                .build().toXml();
        };
    }

    public static Route gatherCallback() {
        return (request, response) -> {

            LOG.info(
                "Callback received with Digits:" + WebHookUtilities
                    .getParam(request, "Digits") + " for call:" + WebHookUtilities
                    .getCallSid(request));
            CallbackWebsocketHandler.broadcastCallback("gatherCallback", request.queryMap().toMap());

            return new VoiceResponse.Builder()
                .say(new Say.Builder("Thanks!")
                    .build()).build()
                                              .toXml();
        };
    }

    public static Route gatherStart(final String to, final String from, final String baseUrl) {
        return (req, res) -> makeGatherCall(to, from, baseUrl);
    }

    private static String makeGatherCall(final String to, final String from,
        final String baseUrl)  {
        return CallAPI.createCall(to, from, Configuration.BASE_URL, Constants.GATHER_URL).toString();
    }

    public static void handleGatherCommand(final Session user, final JSONObject jsonObject) {
        LOG.info("handling gather command:"+jsonObject.toString());
        if (jsonObject.get("command").toString().equals("Call")){
            makeGatherCall(Configuration.to, Configuration.from, Configuration.BASE_URL);
        }
    }
}
