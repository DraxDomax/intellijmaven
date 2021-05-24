package com.mycompany.qa.httptesting.soaptest.customhttp;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;

public class Handler extends sun.net.www.protocol.http.Handler {
    protected String proxy;
    protected int proxyPort;

    protected int getDefaultPort() {
        return 443;
    }

    public Handler() {
        this.proxy = null;
        this.proxyPort = -1;
    }

    protected URLConnection openConnection(URL var1) throws IOException {
        return new HttpsURLConnectionImpl(var1, null, this);
    }
}