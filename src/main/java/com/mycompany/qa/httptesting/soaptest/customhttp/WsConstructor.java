package com.mycompany.qa.httptesting.soaptest.customhttp;

import java.util.HashMap;

public class WsConstructor {
    public static String getActionNs (String wsdlUrl) {
        return "ws";
    }

    public static String soapAddressLocation (String wsdlUrl) {
        return wsdlUrl.split("\\?")[0];
    }

    public static HashMap<String, String> generateActionTemplates (String wsdlUrl) {
        HashMap<String, String> result = new HashMap<>();
        if (wsdlUrl.equals("https://fakeurl?wsdl") ) {
            result.put("getAllDomainsByAutoBill",
                    "<soapenv:Envelope xmlns:soapenv=\"http://schemas.xmlsoap.org/soap/envelope/\"" +
                            "   <soapenv:Header/>\n" +
                            "   <soapenv:Body>\n" +
                            "      <ws:obtainSearchSummary>\n" +
                            "         <userName></userName>\n" +
                            "         <password></password>\n" +
                            "      </ws:obtainSearchSummary>\n" +
                            "   </soapenv:Body>\n" +
                            "</soapenv:Envelope>");
        }
        return result;
    }
}
