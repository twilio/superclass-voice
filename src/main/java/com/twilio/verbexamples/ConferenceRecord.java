package com.twilio.verbexamples;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiFunction;
import java.util.stream.Stream;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class ConferenceRecord {

    private final String conferenceSid;
    private final ConcurrentHashMap<String, ParticipantRecord> participantMap = new ConcurrentHashMap<>();
    private String conferenceStatus = "not-started";
    private String friendlyName="";

    @Override
    public String toString() {
        return "ConferenceRecord{" +
            "conferenceSid='" + conferenceSid + '\'' +
            ", participantMap=" + participantMap +
            ", conferenceStatus='" + conferenceStatus + '\'' +
            ", friendlyName='" + friendlyName + '\'' +
            '}';
    }

    public ConferenceRecord(final String s) {
        conferenceSid = s;
    }

    public String getConferenceSid() {
        return conferenceSid;
    }

    public ParticipantRecord addParticipant(final String psid, final String conferenceSid, final String displayName) {
        return participantMap.computeIfAbsent(psid, sid -> new ParticipantRecord(sid, conferenceSid, displayName));
    }

    public ParticipantRecord modifyParticipant(String participantSid,
        BiFunction<String, ParticipantRecord, ParticipantRecord> participantRecordFunction) {
        return participantMap.computeIfPresent(participantSid, participantRecordFunction);
    }

    public JSONObject handleRequest(final String statusCallbackEvent, final String conferenceSid,
        final Optional<String> callSid) {
        switch (statusCallbackEvent) {
            case "conference-start":
                conferenceStatus = "start";
                return toJSON();

            case "conference-end":
                conferenceStatus = "stop";
                return toJSON();
            case "participant-join":
                if (callSid.isPresent()) {
                    final String realCallSid = callSid.get();
                    addParticipant(realCallSid, conferenceSid, CallAPI.getCallRecord(realCallSid).getFrom());
                    ParticipantRecord modifiedRecord = modifyParticipant(realCallSid, (sid, record) -> {
                        record.setStatus("join");
                        return record;
                    });
                    return modifiedRecord.toJSON();
                }
            case "participant-leave":
                final ParticipantRecord modifiedRecord;
                if (callSid.isPresent()) {
                    final String realCallSid = callSid.get();
                    addParticipant(realCallSid, conferenceSid, CallAPI.getCallRecord(realCallSid).getFrom());

                    modifiedRecord = modifyParticipant(realCallSid, (sid, record) -> {
                        record.setStatus("leave");
                        return record;
                    });
                    return modifiedRecord.toJSON();
                }
        
            default:
                return toJSON();
        }

    }

    private ParticipantRecord getParticipant(final String callSid) {
        return participantMap.get(callSid);
    }

    public JSONObject toJSON() {
        final JSONObject jsonObject = new JSONObject();
        jsonObject.put("callbackType", "conferenceCallback");
        HashMap<String, String> map = new HashMap<>();
        map.put("ConferenceSid", conferenceSid);
        map.put("FriendlyName", friendlyName);
        map.put("Status", conferenceStatus);
        jsonObject.put("params", map);
        return jsonObject;
    }

    public void setFriendlyName(final String friendlyName) {
        this.friendlyName = friendlyName;
    }

    Stream<ParticipantRecord> getParticipantStream(){
        return participantMap.values().stream();
    }

    public static class ParticipantRecord {

        private final String callSid;
        private final String conferenceSid;
        private String status;

        public String getDisplayName() {
            return displayName;
        }

        private final String displayName;

        public ParticipantRecord(final String callSid, final String conferenceSid, final String displayName) {
            this.callSid = callSid;
            this.conferenceSid = conferenceSid;
            this.displayName = displayName;
        }

        public String getCallSid() {
            return callSid;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String newStatus) {
            status = newStatus;
        }

        public String getConferenceSid() {
            return conferenceSid;
        }

        @Override
        public String toString() {
            return "ParticipantRecord{" +
                "callSid='" + callSid + '\'' +
                ", conferenceSid='" + conferenceSid + '\'' +
                ", status='" + status + '\'' +
                ", displayName='" + displayName + '\'' +
                '}';
        }

        public JSONObject toJSON() {
            final JSONObject jsonObject = new JSONObject();
            jsonObject.put("callbackType", "participantCallback");
            HashMap<String, String> map = new HashMap<>();
            map.put("CallSid", callSid);
            map.put("ConferenceSid", conferenceSid);
            map.put("Status", status);
            map.put("DisplayName", displayName);
            jsonObject.put("params", map);
            return jsonObject;
        }


    }
}
