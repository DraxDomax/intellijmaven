package com.mycompany.qa.httptesting.soaptest;

import com.mycompany.qa.httptesting.soaptest.customhttp.WsConstructor;

import java.util.HashMap;

public class SoapTest {
    public static boolean ignoreHostNameCert = false;
    public static boolean ignoreExpiredCert = false;
    public static String loadedWsdlUrl;
    public static String endpointUrl;
    public static String actionNamespace;
    public static HashMap<String, String> actionTemplates;

    public static void loadWsdl (String wsdlUrl) {
        loadedWsdlUrl = wsdlUrl;
        System.out.printf("*** Loaded wsdl url is: %s\n----------------------------\n%n", wsdlUrl);
        endpointUrl = WsConstructor.soapAddressLocation(loadedWsdlUrl);
        actionTemplates = WsConstructor.generateActionTemplates(loadedWsdlUrl);
        actionNamespace = WsConstructor.getActionNs(loadedWsdlUrl);
    }
    public static void ignoreHostNameInHttps(boolean ignore) {
        ignoreHostNameCert = ignore;
    }
    public static void ignoreCertExpiry(boolean ignore) {
        ignoreExpiredCert = ignore;
    }

    public static SoapAction actionTemplate (String action) {
        return new SoapAction(action, actionTemplates.get(action));
    }
}
