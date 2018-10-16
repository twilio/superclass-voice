package com.twilio.verbexamples;

import com.twilio.CallbackWebsocketHandler;
import com.twilio.Configuration;
import com.twilio.Constants;
import com.twilio.WebHookUtilities;
import com.twilio.http.HttpMethod;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.twiml.VoiceResponse;
import com.twilio.twiml.voice.Conference;
import com.twilio.twiml.voice.Conference.Event;
import com.twilio.twiml.voice.Dial;
import com.twilio.twiml.voice.Pause;
import com.twilio.twiml.voice.Redirect;
import com.twilio.twiml.voice.Say;
import com.twilio.type.PhoneNumber;
import org.eclipse.jetty.websocket.api.Session;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Route;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class ConferenceAPI {

    private final static Logger LOG = LoggerFactory.getLogger(ConferenceAPI.class);
    private static final ConcurrentHashMap<String, ConferenceRecord> conferenceMap = new ConcurrentHashMap<>();

    public static Route handleConference(String baseUrl) {
        return (request, response) -> {
            LOG.info("Joining conference for call:" + WebHookUtilities.getCallSid(request));
            CallAPI.updateStatusBasedOnTwimlFetch(request);
            return new VoiceResponse.Builder()
                .dial(
                    new Dial.Builder().conference(
                        new Conference.Builder("MyConference")
                            .statusCallback(baseUrl + Constants.CONFERENCE_STATUS_URL)
                            .statusCallbackMethod(HttpMethod.POST)
                            .statusCallbackEvents(Arrays
                                .asList(Event.START, Event.END, Event.JOIN, Event.LEAVE, Event.HOLD, Event.SPEAKER,
                                    Event.MUTE))
                            .waitUrl(baseUrl + Constants.CONFERENCE_WAITURL)
                            .build()).build())
                .build().toXml();
        };
    }

    public static Route handleConferenceStatus() {
        return (request, response) -> {
            final ConferenceRecord record = conferenceMap.compute(
                request.queryParamOrDefault("ConferenceSid", "<DEFAULT>"),
                handleConferenceRequest(request));
            LOG.info("Conferencestatus: {}", record.toString());
            return "Ok";
        };
    }

    private static BiFunction<? super String, ? super ConferenceRecord, ? extends ConferenceRecord> handleConferenceRequest(
        final Request request) {
        return (BiFunction<String, ConferenceRecord, ConferenceRecord>) (conferenceSid, conferenceRecord) -> {
            LOG.info("Computing new conference status:{}", conferenceSid);

            final ConferenceRecord record = conferenceRecord != null ? conferenceRecord : new ConferenceRecord(
                conferenceSid);
            final String friendlyName = request.queryParams("FriendlyName");
            record.setFriendlyName(friendlyName);
            Optional<String> participantOptional = Optional.ofNullable(request.queryParams("CallSid"));
            participantOptional
                .ifPresent(psid -> record.addParticipant(psid, conferenceSid, CallAPI.getCallRecord(psid).getFrom()));
            final JSONObject callbackEvent = record
                .handleRequest(request.queryParams("StatusCallbackEvent"), conferenceSid, participantOptional);
            CallbackWebsocketHandler.broadcastJSON(callbackEvent);
            return record;
        };
    }

    public static Route conferenceStart(final String to, final String from, final String baseUrl) {
        return (req, res) -> makeConferenceCall(to, from, baseUrl);
    }

    private static String makeConferenceCall(final String to, final String from,
        final String baseUrl) {
        return CallAPI.createCall(to, from, baseUrl, Constants.CONFERENCE_URL).toString();
    }

    public static void handleConferenceCommand(final Session user, final JSONObject jsonObject) {
        LOG.info("handling conference command:" + jsonObject.toString());
        if (jsonObject.get("command").toString().equals("Call")) {
            makeConferenceCall(Configuration.to, Configuration.from, Configuration.BASE_URL);
        }
        if (jsonObject.get("command").toString().equals("LoadConferences")) {
            sendAllConferences();
        }
        if (jsonObject.get("command").toString().equals("LoadParticipants")) {
            sendAllParticipants();
        }
        if (jsonObject.get("command").toString().equals("sendThanks")) {
            sendThanksToAllParticipants();
        }
    }

    private static void sendThanksToAllParticipants() {
        Set<String> numberSet = conferenceMap.values().stream()
                                             .flatMap(cr -> cr.getParticipantStream().map(
                                                 ConferenceRecord.ParticipantRecord::getDisplayName))
                                             .collect(Collectors.toSet());
        numberSet.stream().forEach(ConferenceAPI::sendThankYou);

    }

    private static void sendThankYou(final String number) {
        Message
            .creator(
                new PhoneNumber(number), // to
                new PhoneNumber(Configuration.from), // from
                "Thank you for being awesome and supporting the Superclass Voice 201 Demo!")
            .create();
    }


    private static void sendAllConferences() {
        CallbackWebsocketHandler.broadcastJSON(getAllConferenceRecords());
    }

    private static void sendAllParticipants() {
        CallbackWebsocketHandler.broadcastJSON(getAllParticipantRecords());
    }

    private static JSONObject getAllConferenceRecords() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackType", "allConferences");
        List<JSONObject> allobjects = conferenceMap.values().stream().map(ConferenceRecord::toJSON)
                                                   .collect(Collectors.toList());
        jsonObject.put("allConferences", allobjects);
        return jsonObject;
    }

    private static JSONObject getAllParticipantRecords() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackType", "allParticipants");
        final List<JSONObject> allobjects = conferenceMap.values().stream()
                                                         .flatMap(cr -> cr.getParticipantStream().map(
                                                             ConferenceRecord.ParticipantRecord::toJSON))
                                                         .collect(Collectors.toList());
        jsonObject.put("allParticipants", allobjects);
        return jsonObject;
    }

    public static Route handleWait() {
        return (request, response) -> {
            LOG.info("Waiting for conference start for call:" + WebHookUtilities.getCallSid(request));

            return new VoiceResponse.Builder()
                .say(
                    new Say.Builder("Your conference will start very soon!").build()
                )
                .pause(
                    new Pause.Builder().length(5).build()
                ).redirect(new Redirect.Builder("").build()).build()
                .toXml();
        };
    }
}
