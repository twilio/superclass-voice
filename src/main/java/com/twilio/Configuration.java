package com.twilio;

/**
 * Copyright (c) 2018, Twilio Inc.
 *
 * @author Christer Fahlgren
 */
public class Configuration {

    //
    // Configuration you can override with system properties
    //
    public static final String ACCOUNT_SID = System.getProperty("AccountSid","");
    public static final String AUTH_TOKEN = System.getProperty("AuthToken","");
    private static final String DOMAIN = System.getProperty("Domain", "");
    private static final String BASE_HTTPS_URL = "https://" + DOMAIN;
    public static final String BASE_URL = "http://" + DOMAIN;
    private static final String CREDENTIAL_SID = System
        .getProperty("CredentialSid", "");
    public static final String from = System.getProperty("fromNumber", "");
    public static final String to = System.getProperty("toNumber", "");
}
