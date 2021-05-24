package com.mycompany.qa.httptesting.soaptest.customhttp;

import sun.net.www.protocol.http.Handler;
import sun.net.www.protocol.https.AbstractDelegateHttpsURLConnection;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;
import java.io.IOException;
import java.net.Proxy;
import java.net.URL;

public class DelegateHttpsURLConnection extends AbstractDelegateHttpsURLConnection {
    public HttpsURLConnection httpsURLConnection;

    DelegateHttpsURLConnection(URL var1, Proxy var2, Handler var3, HttpsURLConnection var4) throws IOException {
        super(var1, var2, var3);
        this.httpsURLConnection = var4;
    }

    protected SSLSocketFactory getSSLSocketFactory() {
        return this.httpsURLConnection.getSSLSocketFactory();
    }

    protected HostnameVerifier getHostnameVerifier() {
        HostnameVerifier debug = this.httpsURLConnection.getHostnameVerifier();
        return debug;
    }
}
