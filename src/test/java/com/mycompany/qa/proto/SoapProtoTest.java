package com.mycompany.qa.proto;

import com.mycompany.qa.httptesting.soaptest.SoapResponse;
import com.mycompany.qa.httptesting.soaptest.SoapTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

public class SoapProtoTest {
    @BeforeAll
    public static void setUP () {
        SoapTest.loadWsdl("https://fakeurl?wsdl");
        SoapTest.ignoreHostNameInHttps(true);
    }

    @Test
    @Tag("surefiredemo")
    public void proto () {

        SoapResponse currentResponse =
        SoapTest
                .actionTemplate("getAllDomainsByAutoBill")
                    .fillIn("autoBillDetailsDTO/apiKey/apiKey", "myvalue")
                    .fillIn("autoBillDetailsDTO/tag", "myTag")
                .submit()
                    .testTag("pageSize", "500")
                    .testTag("totalPages", "1");

        String[] meaninglessArrayForCounting = currentResponse.getResponseBody().split("<domainNames>");
        System.out.println(meaninglessArrayForCounting.length);
    }
}
