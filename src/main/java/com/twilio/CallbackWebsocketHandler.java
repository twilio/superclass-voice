package com.twilio;

import com.twilio.verbexamples.CallAPI;
import com.twilio.verbexamples.ConferenceAPI;
import com.twilio.verbexamples.GatherAPI;
import com.twilio.verbexamples.PayAPI;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@WebSocket
public class CallbackWebsocketHandler {

    final static private Logger LOG = LoggerFactory.getLogger(CallbackWebsocketHandler.class);

    private static final Map<Session, String> websocketSessionMap = new ConcurrentHashMap<>();
    private static int nextUserNumber = 1; //Assign to username for next connecting user


    public static void broadcastCallback(final String callbackType, final Map<String, String[]> params) {

        LOG.info("Trying to broadcast:" + callbackType + " with no of params:" + params.size());

        websocketSessionMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(new JSONObject()
                    .put("callbackType", callbackType)
                    .put("params", convertQueryMapToMap(params))
                ));
            } catch (Exception e) {
                e.printStackTrace();
                LOG.info("Failed to convert " + e.toString());

            }
        });
    }

    public static void broadcastJSON(final JSONObject params) {

        LOG.info("Broadcasting:" + params.toString());

        websocketSessionMap.keySet().stream().filter(Session::isOpen).forEach(session -> {
            try {
                session.getRemote().sendString(String.valueOf(params));
            } catch (Exception e) {
                e.printStackTrace();
                LOG.info("Failed to convert " + e.toString());

            }
        });
    }

    private static Map<String, String> convertQueryMapToMap(final Map<String, String[]> params) {
        final Map<String, String> convMap = new HashMap<>();
        params.forEach((key, value) -> Arrays.stream(value).forEach(v -> convMap.put(key, v)));
        return convMap;
    }


    @OnWebSocketConnect
    public void onConnect(Session user) {
        LOG.info("Connected :" + user.toString());

        String username = "User" + nextUserNumber++;
        websocketSessionMap.put(user, username);
    }

    @OnWebSocketClose
    public void onClose(Session user, int statusCode, String reason) {
        LOG.info("Disconnected :" + user.toString() + " status :" + statusCode + " reason:" + reason);
        websocketSessionMap.remove(user);
    }

    @OnWebSocketMessage
    public void onMessage(Session user, String message) {
        try {
            LOG.info("Received WS message:" + message);

            JSONObject jsonObject = new JSONObject(message);
            switch (jsonObject.get("messageType").toString()) {
                case CommandTypes.LOGIN:
                    handleLogin(user, jsonObject);
                    break;
                case CommandTypes.PAY:
                    PayAPI.handlePayCommand(user, jsonObject);
                    break;
                case CommandTypes.GATHER:
                    GatherAPI.handleGatherCommand(user, jsonObject);
                    break;
                case CommandTypes.CONFERENCE:
                    ConferenceAPI.handleConferenceCommand(user, jsonObject);
                    break;
                case CommandTypes.CALL:
                    CallAPI.handleCallCommand(user, jsonObject);
                    break;
                default:

            }
        } catch (Exception e) {
            LOG.info("exception while handling websocket message {}",message, e);
        }
    }


    private void handleLogin(Session session, JSONObject jsonObject) {
        websocketSessionMap.put(session, jsonObject.getString("user"));
        LOG.info("Received login:" + jsonObject.toString());
    }

}