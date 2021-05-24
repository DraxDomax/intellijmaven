package com.mycompany.qa.httptesting.soaptest.customhttp;

import com.mycompany.qa.httptesting.soaptest.SoapTest;
import com.mycompany.qa.httptesting.soaptest.testenablers.BetaExpirationTrustManager;
import com.mycompany.qa.httptesting.soaptest.testenablers.BetaHostNameVerifier;

import javax.net.ssl.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.KeyStore;
import java.security.Principal;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;

public abstract class HttpsURLConnection extends HttpURLConnection {
    protected HostnameVerifier hostnameVerifier;
    private static SSLSocketFactory defaultSSLSocketFactory = null;
    private SSLSocketFactory sslSocketFactory;

    HttpsURLConnection(URL var1) {
        super(var1);
        this.hostnameVerifier = new BetaHostNameVerifier();
        if (SoapTest.ignoreExpiredCert) {
            this.sslSocketFactory = buildSoapTestSslContext().getSocketFactory();
        } else {
            this.sslSocketFactory = getSSLSocketFactory();
        }
    }

    public abstract String getCipherSuite();

    public abstract Certificate[] getLocalCertificates();

    public abstract Certificate[] getServerCertificates() throws SSLPeerUnverifiedException;

    public Principal getPeerPrincipal() throws SSLPeerUnverifiedException {
        Certificate[] var1 = this.getServerCertificates();
        return ((X509Certificate)var1[0]).getSubjectX500Principal();
    }

    public Principal getLocalPrincipal() {
        Certificate[] var1 = this.getLocalCertificates();
        return var1 != null ? ((X509Certificate)var1[0]).getSubjectX500Principal() : null;
    }

    public void setHostnameVerifier(HostnameVerifier var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("no HostnameVerifier specified");
        } else {
            this.hostnameVerifier = var1;
        }
    }

    public HostnameVerifier getHostnameVerifier() {
        return this.hostnameVerifier;
    }

    public static void setDefaultSSLSocketFactory(SSLSocketFactory var0) {
        if (var0 == null) {
            throw new IllegalArgumentException("no default SSLSocketFactory specified");
        } else {
            SecurityManager var1 = System.getSecurityManager();
            if (var1 != null) {
                var1.checkSetFactory();
            }

            defaultSSLSocketFactory = var0;
        }
    }

    public static SSLSocketFactory getDefaultSSLSocketFactory() {
        if (defaultSSLSocketFactory == null) {
            defaultSSLSocketFactory = (SSLSocketFactory)SSLSocketFactory.getDefault();
        }

        return defaultSSLSocketFactory;
    }

    public void setSSLSocketFactory(SSLSocketFactory var1) {
        if (var1 == null) {
            throw new IllegalArgumentException("no SSLSocketFactory specified");
        } else {
            SecurityManager var2 = System.getSecurityManager();
            if (var2 != null) {
                var2.checkSetFactory();
            }

            this.sslSocketFactory = var1;
        }
    }

    public SSLSocketFactory getSSLSocketFactory() {
        return this.sslSocketFactory;
    }

    private static SSLContext buildSoapTestSslContext () {
        TrustManagerFactory factory;
        try {
            factory = TrustManagerFactory.getInstance("X509");
            factory.init((KeyStore) null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        TrustManager[] trustManagers = factory.getTrustManagers();
        for (int i = 0; i < trustManagers.length; i++) {
            if (trustManagers[i] instanceof X509TrustManager) {
                trustManagers[i] = new BetaExpirationTrustManager((X509TrustManager) trustManagers[i]);
            }
        }

        SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagers, null);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return sslContext;
    }
}
