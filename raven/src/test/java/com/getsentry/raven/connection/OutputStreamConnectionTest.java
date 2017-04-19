package com.getsentry.raven.connection;

import mockit.*;
import com.getsentry.raven.environment.RavenEnvironment;
import com.getsentry.raven.event.Event;
import com.getsentry.raven.marshaller.Marshaller;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class OutputStreamConnectionTest {
    @Tested
    private OutputStreamConnection outputStreamConnection = null;
    @Injectable
    private Marshaller mockMarshaller = null;
    @Injectable
    private OutputStream mockOutputStream = null;

    @Test
    public void testContentMarshalled(@Injectable final Event mockEvent) throws Exception {
        outputStreamConnection.send(mockEvent);

        new Verifications() {{
            mockMarshaller.marshall(mockEvent, mockOutputStream);
        }};
    }

    @Test
    public void testClose() throws Exception {
        outputStreamConnection.close();

        new Verifications() {{
            mockOutputStream.close();
        }};
    }
}
