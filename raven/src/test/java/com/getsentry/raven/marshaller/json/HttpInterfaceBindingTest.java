package com.getsentry.raven.marshaller.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.getsentry.raven.event.interfaces.HttpInterface;
import mockit.Injectable;
import mockit.NonStrictExpectations;
import mockit.Tested;
import org.testng.annotations.Test;
import org.testng.collections.Lists;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static com.getsentry.raven.marshaller.json.JsonComparisonUtil.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

public class HttpInterfaceBindingTest {
    @Tested
    private HttpInterfaceBinding interfaceBinding = null;
    @Injectable
    private HttpInterface mockMessageInterface = null;

    @Test
    public void testHeaders() throws Exception {
        final JsonGeneratorParser jsonGeneratorParser = newJsonGenerator();
        final Map<String, Collection<String>> headers = new HashMap<>();
        headers.put("Header1", Lists.newArrayList("Value1"));
        headers.put("Header2", Lists.newArrayList("Value1", "Value2"));
        final HashMap<String, String> cookies = new HashMap<>();
        cookies.put("Cookie1", "Value1");
        new NonStrictExpectations() {{
            mockMessageInterface.getHeaders();
            result = headers;
            mockMessageInterface.getRequestUrl();
            result = "http://host/url";
            mockMessageInterface.getMethod();
            result = "GET";
            mockMessageInterface.getQueryString();
            result = "query";
            mockMessageInterface.getCookies();
            result = cookies;
            mockMessageInterface.getRemoteAddr();
            result = "1.2.3.4";
            mockMessageInterface.getServerName();
            result = "server-name";
            mockMessageInterface.getServerPort();
            result = 1234;
            mockMessageInterface.getLocalPort();
            result = 5678;
            mockMessageInterface.getProtocol();
            result = "HTTP";
            mockMessageInterface.getLocalAddr();
            result = "5.6.7.8";
            mockMessageInterface.getLocalName();
            result = "local-name";
            mockMessageInterface.getBody();
            result = "body";
        }};

        interfaceBinding.writeInterface(jsonGeneratorParser.generator(), mockMessageInterface);

        JsonNode value = jsonGeneratorParser.value();
        assertThat(value, is(jsonResource("/com/getsentry/raven/marshaller/json/Http1.json")));
    }
}
