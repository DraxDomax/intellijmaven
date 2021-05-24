package com.mycompany.qa.httptesting.soaptest;

import com.mycompany.qa.httptesting.soaptest.customhttp.HttpSoapConnection;
import org.xml.sax.InputSource;

import javax.xml.soap.MessageFactory;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.sax.SAXSource;
import java.io.StringReader;

public class SoapClient {
    public static SoapResponse soapCall (String soapEndpointUrl, String soapMessageString) {
        try {
            HttpSoapConnection soapConnection = new HttpSoapConnection();
            SOAPMessage soapRequest = createSoapMessage(soapMessageString);
            SOAPMessage soapResponse = soapConnection.call(soapRequest, soapEndpointUrl);
            soapConnection.close();

            return new SoapResponse(soapResponse);
        } catch (Exception e) {
            throw new RuntimeException("Failed to complete SOAP call", e);
        }
    }

    private static SOAPMessage createSoapMessage (String soapMessageString) {
        try {
            MessageFactory messageFactory = MessageFactory.newInstance();
            SOAPMessage soapMessage = messageFactory.createMessage();
            soapMessage
                    .getSOAPPart()
                    .setContent(
                            new SAXSource(
                                    new InputSource(
                                            new StringReader(soapMessageString))));
            return soapMessage;
        } catch (Exception e) {
            throw new RuntimeException("Could not create SOAP message: ", e);
        }
    }
}
