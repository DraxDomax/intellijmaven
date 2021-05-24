package com.mycompany.qa.httptesting.soaptest;

public class SoapAction {
    public String action;
    public String actionXml;

    public SoapAction (String action, String initialXml) {
        this.action = action;
        actionXml = initialXml;
    }

    public SoapAction fillIn(String path, String value) {
        actionXml = ParseXml.changeTagValueAtPath(actionXml, "//"+path, value);
        return this;
    }

    public SoapAction printAction() {
        System.out.println("*** Current SOAP action XML: \n----------------------------\n"
                + actionXml + "\n");
        return this;
    }

    public SoapResponse submit() {
        return SoapClient.soapCall(SoapTest.endpointUrl, actionXml);
    }
}
