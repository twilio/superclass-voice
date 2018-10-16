package com.twilio.verbexamples;

import com.twilio.Configuration;
import com.twilio.Constants;
import com.twilio.WebHookUtilities;
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
public class PayAPI {

    private static final Logger LOG = LoggerFactory.getLogger(PayAPI.class);

    public static Route payStart(final String to, final String from, final String baseUrl) {
        return (req, res) -> makePayCall(to, from, baseUrl);
    }

    private static String makePayCall(final String to, final String from,
        final String baseUrl) {
        return CallAPI.createCall(to, from, Configuration.BASE_URL,Constants.PAY_URL).toString();
    }

    public static Route renderPay(final String baseUrl) {
        return (request, response) -> {
            CallAPI.updateStatusBasedOnTwimlFetch(request);
            return WebHookUtilities.createResponse()
                                   .e("Pay")
                                   .a("input", "dtmf")
                                   .a("maxAttempts", "2")
                                   .a("timeout", "10")
                                   .a("securityCode", "true")
                                   .a("postalCode", "true")
                                   //.a("credentialSid", SuperClass.CREDENTIAL_SID)
                                   .a("paymentConnector", "STRIPE")
                                   .a("validCardTypes", "master-card visa amex")
                                   .a("tokenType", "reusable")
                                   .a("chargeAmount", "15.00")
                                   .a("currency", "usd")
                                   .a("description", "Lunch charge")
                                   .a("statusCallback", baseUrl + Constants.PAYSTATUS_URL)
                                   .a("action", baseUrl + Constants.PAYACTION_URL)
                                   .asString();
        };
    }

    public static Route paymentStatus() {
        return (request, response) -> {
            final String ccNumber = WebHookUtilities.getParam(request, "PaymentCardNumber");
            final String ccType = WebHookUtilities.getParam(request, "PaymentCardType");
            final String callSid = WebHookUtilities.getCallSid(request);
            final String forValue = WebHookUtilities.getParam(request, "For");

            LOG.info(
                "Callback received:" + forValue + " with PaymentCardNumber:" + ccNumber + " type:" + ccType + " for " +
                    "call:" + callSid);
            response.status(200);
            return "Ok";
        };
    }

    public static Route payAction() {
        return (request, response) -> {
            final String ccNumber = WebHookUtilities.getParam(request, "PaymentCardNumber");
            final String ccType = WebHookUtilities.getParam(request, "PaymentCardType");
            final String result = WebHookUtilities.getParam(request, "Result");
            final String callSid = WebHookUtilities.getCallSid(request);

            if (result.equals("success")) {
                LOG.info(
                    "Callback received with payment confirmation code:" + WebHookUtilities.getParam(request,
                        "PaymentConfirmationCode") + " for call:" + callSid);
                response.status(200);
                return WebHookUtilities.createResponse()
                                       .e("Say")
                                       .t("Credit card number successfully charged");
            } else {
                LOG.info("Failed due to:" + result + " number:" + ccNumber + " for call:" + callSid);
                return WebHookUtilities.createResponse()
                                       .e("Say")
                                       .t("Failed to enter credit card number successfully").asString();

            }
        };
    }

    public static void handlePayCommand(final Session user, final JSONObject jsonObject) {
        makePayCall(Configuration.to, Configuration.from, Configuration.BASE_URL);
    }
}
