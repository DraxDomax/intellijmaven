package com.mycompany.qa.httptesting.soaptest.customhttp;

import com.sun.xml.internal.messaging.saaj.SOAPExceptionImpl;
import com.sun.xml.internal.messaging.saaj.util.Base64;
import com.sun.xml.internal.messaging.saaj.util.ByteInputStream;
import com.sun.xml.internal.messaging.saaj.util.ParseUtil;

import javax.xml.soap.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.security.Provider;
import java.security.Security;
import java.util.Iterator;
import java.util.StringTokenizer;

public class HttpSoapConnection {
    private static final String SSL_PKG = "com.sun.net.ssl.internal.www.protocol";
    private static final String SSL_PROVIDER = "com.sun.net.ssl.internal.ssl.Provider";
    MessageFactory messageFactory;
    boolean closed = false;

    public HttpSoapConnection() throws SOAPException {
        try {
            this.messageFactory = MessageFactory.newInstance("Dynamic Protocol");
        } catch (NoSuchMethodError var2) {
            this.messageFactory = MessageFactory.newInstance();
        } catch (Exception var3) {
            throw new SOAPExceptionImpl("Unable to create message factory", var3);
        }
    }

    public void close() throws SOAPException {
        if (this.closed) {
            throw new SOAPExceptionImpl("Connection already closed");
        } else {
            this.messageFactory = null;
            this.closed = true;
        }
    }

    public SOAPMessage call(SOAPMessage message, Object endPoint) throws SOAPException {
        if (this.closed) {
            throw new SOAPExceptionImpl("Connection is closed");
        } else {
            try {
                endPoint = new URL((String)endPoint);
            } catch (MalformedURLException var8) {
                throw new SOAPExceptionImpl("Bad URL: " + var8.getMessage());
            }
            try {
                SOAPMessage response = this.post(message, (URL)endPoint);
                return response;
            } catch (Exception var7) {
                throw new SOAPExceptionImpl(var7);
            }
        }
    }

    SOAPMessage post(SOAPMessage message, URL endPoint) throws SOAPException, IOException {
        boolean isFailure = false;
        HttpURLConnection httpConnection;

        MimeHeaders headers;
        int responseCode;
        try {
            if (endPoint.getProtocol().equals("https")) {
                this.initHttps();
            }

            URI uri = new URI(endPoint.toString());
            String userInfo = uri.getRawUserInfo();
            if (!endPoint.getProtocol().equalsIgnoreCase("http") && !endPoint.getProtocol().equalsIgnoreCase("https")) {
                throw new IllegalArgumentException("Protocol " + endPoint.getProtocol() + " not supported in URL " + endPoint);
            }

            httpConnection = this.createConnection(endPoint);
            httpConnection.setRequestMethod("POST");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setUseCaches(false);
            httpConnection.setInstanceFollowRedirects(true);
            if (message.saveRequired()) {
                message.saveChanges();
            }

            headers = message.getMimeHeaders();
            Iterator it = headers.getAllHeaders();
            boolean hasAuth = false;

            while(it.hasNext()) {
                MimeHeader header = (MimeHeader)it.next();
                String[] values = headers.getHeader(header.getName());
                if (values.length == 1) {
                    httpConnection.setRequestProperty(header.getName(), header.getValue());
                } else {
                    StringBuilder concat = new StringBuilder();
                    for(int i = 0; i < values.length; ++i) {
                        if (i != 0) {
                            concat.append(',');
                        }
                        concat.append(values[i]);
                    }
                    httpConnection.setRequestProperty(header.getName(), concat.toString());
                }
                if ("Authorization".equals(header.getName())) {
                    hasAuth = true;
                }
            }

            if (!hasAuth && userInfo != null) {
                this.initAuthUserInfo(httpConnection, userInfo);
            }

            try (OutputStream out = httpConnection.getOutputStream()) {
                message.writeTo(out);
                out.flush();
            }

            httpConnection.connect();

            try {
                responseCode = httpConnection.getResponseCode();
                if (responseCode == 500) {
                    isFailure = true;
                } else if (responseCode / 100 != 2) {
                    throw new SOAPExceptionImpl("Bad response: (" + responseCode + httpConnection.getResponseMessage());
                }
            } catch (IOException var36) {
                responseCode = httpConnection.getResponseCode();
                if (responseCode != 500) {
                    throw var36;
                }

                isFailure = true;
            }
        } catch (SOAPException var37) {
            throw var37;
        } catch (Exception var38) {
            throw new SOAPExceptionImpl("Message send failed", var38);
        }

        SOAPMessage response = null;
        InputStream httpIn = null;
        if (responseCode == 200 || isFailure) {
            try {
                headers = new MimeHeaders();
                // Original Java starts at 1, to avoid the MIME header that contains the response code... WTF?!
                int i = 0;

                while(true) {
                    String key = httpConnection.getHeaderFieldKey(i);
                    String value = httpConnection.getHeaderField(i);
                    if (key == null && value == null) {
                        httpIn = isFailure ? httpConnection.getErrorStream() : httpConnection.getInputStream();
                        byte[] bytes = this.readFully(httpIn);
                        int length = httpConnection.getContentLength() == -1 ? bytes.length : httpConnection.getContentLength();
                        if (length != 0) {
                            ByteInputStream in = new ByteInputStream(bytes, length);
                            response = this.messageFactory.createMessage(headers, in);
                        }
                        break;
                    }

                    // ... And they try to kill it here again with checking for null (which it was)...
                    if (key == null && value.contains("HTTP/1.1 ")) {
                        //TODO: The literal should be a constant of SoapTest:
                        headers.addHeader("maybeRC", value);
                    }

                    if (key != null) {
                        StringTokenizer values = new StringTokenizer(value, ",");
                        while(values.hasMoreTokens()) {
                            headers.addHeader(key, values.nextToken().trim());
                        }
                    }
                    ++i;
                }
            } catch (SOAPException var33) {
                throw var33;
            } catch (Exception var34) {
                throw new SOAPExceptionImpl("Unable to read response: " + var34.getMessage());
            } finally {
                if (httpIn != null) {
                    httpIn.close();
                }
                httpConnection.disconnect();
            }
        }
        return response;
    }

    public SOAPMessage get(Object endPoint) throws SOAPException {
        if (this.closed) {
            throw new SOAPExceptionImpl("Connection is closed");
        } else {
            if (endPoint instanceof URL) {
                try {
                    SOAPMessage response = this.doGet((URL)endPoint);
                    return response;
                } catch (Exception var5) {
                    throw new SOAPExceptionImpl(var5);
                }
            } else {
                throw new SOAPExceptionImpl("Bad endPoint type " + endPoint);
            }
        }
    }

    SOAPMessage doGet(URL endPoint) throws SOAPException, IOException {
        boolean isFailure = false;
        HttpURLConnection httpConnection;

        int responseCode;
        try {
            if (endPoint.getProtocol().equals("https")) {
                this.initHttps();
            }

            if (!endPoint.getProtocol().equalsIgnoreCase("http") && !endPoint.getProtocol().equalsIgnoreCase("https")) {
                throw new IllegalArgumentException("Protocol " + endPoint.getProtocol() + " not supported in URL " + endPoint);
            }

            httpConnection = this.createConnection(endPoint);
            httpConnection.setRequestMethod("GET");
            httpConnection.setDoOutput(true);
            httpConnection.setDoInput(true);
            httpConnection.setUseCaches(false);
            HttpURLConnection.setFollowRedirects(true);
            httpConnection.connect();

            try {
                responseCode = httpConnection.getResponseCode();
                if (responseCode == 500) {
                    isFailure = true;
                } else if (responseCode / 100 != 2) {
                    throw new SOAPExceptionImpl("Bad response: (" + responseCode + httpConnection.getResponseMessage());
                }
            } catch (IOException var23) {
                responseCode = httpConnection.getResponseCode();
                if (responseCode != 500) {
                    throw var23;
                }

                isFailure = true;
            }
        } catch (SOAPException var24) {
            throw var24;
        } catch (Exception var25) {
            throw new SOAPExceptionImpl("Get failed", var25);
        }

        SOAPMessage response = null;
        InputStream httpIn = null;
        if (responseCode == 200 || isFailure) {
            try {
                MimeHeaders headers = new MimeHeaders();
                int i = 1;

                while(true) {
                    String key = httpConnection.getHeaderFieldKey(i);
                    String value = httpConnection.getHeaderField(i);
                    if (key == null && value == null) {
                        httpIn = isFailure ? httpConnection.getErrorStream() : httpConnection.getInputStream();
                        if (httpIn != null && httpConnection.getContentLength() != 0 && httpIn.available() != 0) {
                            response = this.messageFactory.createMessage(headers, httpIn);
                        }
                        break;
                    }

                    if (key != null) {
                        StringTokenizer values = new StringTokenizer(value, ",");

                        while(values.hasMoreTokens()) {
                            headers.addHeader(key, values.nextToken().trim());
                        }
                    }

                    ++i;
                }
            } catch (SOAPException var20) {
                throw var20;
            } catch (Exception var21) {
                throw new SOAPExceptionImpl("Unable to read response: " + var21.getMessage());
            } finally {
                if (httpIn != null) {
                    httpIn.close();
                }

                httpConnection.disconnect();
            }
        }

        return response;
    }

    private byte[] readFully(InputStream istream) throws IOException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];

        int num;
        while((num = istream.read(buf)) != -1) {
            bout.write(buf, 0, num);
        }

        byte[] ret = bout.toByteArray();
        return ret;
    }

    private void initHttps() {
        System.setProperty("java.protocol.handler.pkgs", SSL_PKG);
        try {
            Class c = Class.forName(SSL_PROVIDER);
            Provider p = (Provider)c.newInstance();
            Security.addProvider(p);
        } catch (Exception var4) {
            throw new RuntimeException(var4);
        }
    }

    private void initAuthUserInfo(HttpURLConnection conn, String userInfo) {
        if (userInfo != null) {
            int delimiter = userInfo.indexOf(58);
            String user;
            String password;
            if (delimiter == -1) {
                user = ParseUtil.decode(userInfo);
                password = null;
            } else {
                user = ParseUtil.decode(userInfo.substring(0, delimiter++));
                password = ParseUtil.decode(userInfo.substring(delimiter));
            }

            String plain = user + ":";
            byte[] nameBytes = plain.getBytes();
            assert password != null;
            byte[] passwdBytes = password.getBytes();
            byte[] concat = new byte[nameBytes.length + passwdBytes.length];
            System.arraycopy(nameBytes, 0, concat, 0, nameBytes.length);
            System.arraycopy(passwdBytes, 0, concat, nameBytes.length, passwdBytes.length);
            String auth = "Basic " + new String(Base64.encode(concat));
            conn.setRequestProperty("Authorization", auth);
        }

    }

    private HttpURLConnection createConnection_old(URL endpoint) throws IOException {
        HttpURLConnection debug = (HttpURLConnection)endpoint.openConnection();
        return debug;
    }
    private HttpURLConnection createConnection(URL endpoint) throws IOException {
        Handler handler = new Handler();
        return (HttpURLConnection)handler.openConnection(endpoint);
    }
}
