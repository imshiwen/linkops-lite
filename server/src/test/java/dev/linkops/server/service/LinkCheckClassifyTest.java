package dev.linkops.server.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.net.ConnectException;
import java.net.UnknownHostException;
import java.net.http.HttpTimeoutException;
import java.security.cert.CertificateExpiredException;
import javax.net.ssl.SSLHandshakeException;
import org.junit.jupiter.api.Test;

class LinkCheckClassifyTest {

    @Test
    void classifiesStatusCodes() {
        assertEquals("ok", LinkCheckService.classify(200));
        assertEquals("ok", LinkCheckService.classify(301));
        assertEquals("dead", LinkCheckService.classify(404));
        assertEquals("dead", LinkCheckService.classify(410));
        assertEquals("blocked", LinkCheckService.classify(403));
        assertEquals("blocked", LinkCheckService.classify(429));
        assertEquals("error", LinkCheckService.classify(500));
    }

    @Test
    void classifiesExceptions() {
        // 本机网络可能导致的失败:不判死
        assertEquals("unreachable", LinkCheckService.classify(new HttpTimeoutException("t")));
        assertEquals("unreachable", LinkCheckService.classify(new IOException(new UnknownHostException("x"))));
        assertEquals("unreachable", LinkCheckService.classify(new ConnectException("refused")));
        assertEquals("unreachable", LinkCheckService.classify(new SSLHandshakeException("Remote host terminated the handshake")));
        // 证书问题是确定信号
        var expired = new SSLHandshakeException("bad cert");
        expired.initCause(new CertificateExpiredException("NotAfter: 2023"));
        assertEquals("tls_invalid", LinkCheckService.classify(new IOException(expired)));
        assertEquals("error", LinkCheckService.classify(new IOException("other")));
    }
}
