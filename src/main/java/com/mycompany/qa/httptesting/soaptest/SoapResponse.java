package com.mycompany.qa.httptesting.soaptest;

import javax.xml.soap.SOAPMessage;
import java.util.HashMap;

public class SoapResponse {
    private String responseBody;
    private String responseCodeText;
    private int responseCode;
    //TODO: get all headers nicely:
    private HashMap<String, String> headers;

    public SoapResponse (SOAPMessage response) {
        try {
            responseBody = ParseXml.docToString(
                    response.getSOAPBody().extractContentAsDocument()
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed getting SOAP body from response", e);
        }

        try {
            responseCodeText = response.getMimeHeaders().getHeader("maybeRC")[0];
            responseCode = Integer.parseInt(
                    responseCodeText.split(" ")[1]
            );
        } catch (Exception e) {
            throw new RuntimeException("Failed getting SOAP RC Test from response", e);
        }
    }

    public SoapResponse printResponseBody() {
        System.out.println("*** SOAP response body: \n-----------------------\n"
                + responseBody + "\n");
        return this;
    }

    public boolean checkRC_noException(int expectedRC) {
        return responseCode == expectedRC;
    }
    public SoapResponse testRC(int expectedRC) {
        if (!checkRC_noException(expectedRC)) throw new AssertionError(expectedRC + " was the expected RC. Actrual RC: "+ responseCode);
        return this;
    }
    public SoapResponse testTag(String xPath, String expectedValue) {
        String actualValue = getValueFromResponse(xPath);
        if (!actualValue.equals(expectedValue))
            throw new AssertionError(xPath + " was supposed to contain: " + expectedValue + ", but it has: " + actualValue);
        return this;
    }

    public String getValueFromResponse(String xPath) {
        return ParseXml.readTagValueAtPath(responseBody, "//"+xPath);
    }

    public String getResponseBody() {
        return responseBody;
    }

    //TODO: public SoapResponse checkAgainstSchema() { ... }
}
