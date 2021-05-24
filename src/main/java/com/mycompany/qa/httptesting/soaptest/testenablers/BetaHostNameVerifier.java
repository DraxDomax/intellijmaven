package com.mycompany.qa.httptesting.soaptest.testenablers;

import com.mycompany.qa.httptesting.soaptest.SoapTest;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLSession;
import java.security.cert.Certificate;

public class BetaHostNameVerifier implements HostnameVerifier {
    @Override
    public boolean verify(String hostExpectedByClient, SSLSession sslSession) {
        if (SoapTest.ignoreExpiredCert) {
            return true;
        } else {
            boolean hostVerified = false;

            Certificate[] hostCertificates = null;
            try {
                hostCertificates = sslSession.getPeerCertificates();
            } catch (SSLPeerUnverifiedException e) {
                e.printStackTrace();
            }

            if (hostCertificates != null && hostCertificates.length > 0) {
                for (Certificate currentCertificate : hostCertificates) {
                    try {
                        String subject = currentCertificate
                                .toString()
                                .split("Subject: ")[1]
                                .split("\n")[0];
                        if (subject.contains(hostExpectedByClient)) {
                            hostVerified = true;
                            break;
                        }
                    } catch (Exception e) {
                        throw new AssertionError("Problem with host certificate structure (missing 'Subject: '?", e);
                    }

                }
            } else {
                System.out.println("*** Warning! Server did not provide any certificates for an HTTP-S request ***");
            }

            return hostVerified;
        }
    }
}
