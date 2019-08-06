package com.music.festival.demo.rest.client;

import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.rest.client.model.Festival;
import org.glassfish.jersey.client.ClientConfig;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.springframework.test.util.ReflectionTestUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ClientBuilder.class, MusicFestivalRESTApiClient.class})
public class MusicFestivalRESTApiClientTest {

    @Mock
    Client mockCLient;

    @Mock
    WebTarget mockWebTarget;

    @Mock
    Invocation.Builder mockInvocationBuilder;

    @Mock
    Response mockResponse;

    MusicFestivalRESTApiClient apiCLient = new MusicFestivalRESTApiClient();

    @Before
    public void setUp() {
        PowerMockito.mockStatic(ClientBuilder.class);
        when(ClientBuilder.newClient(any(ClientConfig.class))).thenReturn(mockCLient);
        when(mockCLient.target(any(String.class))).thenReturn(mockWebTarget);
        when(mockWebTarget.path("festivals")).thenReturn(mockWebTarget);
        when(mockWebTarget.request(MediaType.APPLICATION_JSON)).thenReturn(mockInvocationBuilder);
        when(mockInvocationBuilder.get()).thenReturn(mockResponse);
        ReflectionTestUtils.setField(apiCLient, "REST_URI", "http://eacodingtest.digital.energyaustralia.com.au/api/v1/");
    }

    @Test
    public void testGetFestivalsWithoutExponentialBackoff() throws ResponseParsingException {
        when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockResponse.readEntity(String.class)).thenReturn(getDummyResponseString());
        List<Festival> festivals = apiCLient.getFestivals();
        assertNotNull(festivals);
        assertEquals(5, festivals.size());
    }

    @Test
    public void testGetFestivalsWithExponentialBackoff() throws ResponseParsingException {
        // Configure mockResponse.getStatus() to return Response.Status.TOO_MANY_REQUESTS twice
        // before returning Response.Status.OK.
        // The first two iterations should trigger exponential backoff implementation.
        when(mockResponse.getStatus()).thenAnswer(new Answer() {
            private int count = 0;

            public Object answer(InvocationOnMock invocation) {
                if (count++ < 2)
                    return Response.Status.TOO_MANY_REQUESTS.getStatusCode();

                return Response.Status.OK.getStatusCode();
            }
        });
        when(mockResponse.readEntity(String.class)).thenReturn(getDummyResponseString());

        List<Festival> festivals = apiCLient.getFestivals();

        // Verify exponential backoff was triggerred.
        verify(mockResponse, times(2)).close();

        assertNotNull(festivals);
        assertEquals(5, festivals.size());
    }

    @Test(expected = ResponseParsingException.class)
    public void testGetFestivalsWithException() throws ResponseParsingException {
        when(mockResponse.getStatus()).thenReturn(Response.Status.OK.getStatusCode());
        when(mockResponse.readEntity(String.class)).thenReturn(getInvalidResponseString());
        List<Festival> festivals = apiCLient.getFestivals();
        fail("Expected exception ResponseParsingException");
    }

    private String getDummyResponseString() {
        return "[{\"name\":\"LOL-palooza\",\"bands\":[{\"name\":\"Werewolf Weekday\",\"recordLabel\":\"XS Recordings\"},{\"name\":\"Jill Black\",\"recordLabel\":\"Fourth Woman Records\"},{\"name\":\"Frank Jupiter\",\"recordLabel\":\"Pacific Records\"},{\"name\":\"Winter Primates\",\"recordLabel\":\"\"}]},{\"name\":\"Small Night In\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\",\"recordLabel\":\"Outerscope\"},{\"name\":\"Green Mild Cold Capsicum\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Yanke East\",\"recordLabel\":\"MEDIOCRE Music\"},{\"name\":\"The Black Dashes\",\"recordLabel\":\"Fourth Woman Records\"}]},{\"name\":\"Trainerella\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Still Bottom Records\"},{\"name\":\"YOUKRANE\",\"recordLabel\":\"Anti Records\"},{\"name\":\"Adrian Venti\",\"recordLabel\":\"Monocracy Records\"},{\"name\":\"Manish Ditch\",\"recordLabel\":\"ACR\"}]},{\"name\":\"Twisted Tour\",\"bands\":[{\"name\":\"Auditones\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\"},{\"name\":\"Summon\",\"recordLabel\":\"Outerscope\"}]},{\"bands\":[{\"name\":\"Critter Girls\",\"recordLabel\":\"ACR\"},{\"name\":\"Propeller\",\"recordLabel\":\"Pacific Records\"}]}]";
    }

    private String getInvalidResponseString() {
        return "[{,\"bands\":[{\"name\":\"Werewolf Weekday\",\"recordLabel\":\"XS Recordings\"},{\"name\":\"Jill Black\",\"recordLabel\":\"Fourth Woman Records\"},{\"name\":\"Frank Jupiter\",\"recordLabel\":\"Pacific Records\"},{\"name\":\"Winter Primates\",\"recordLabel\":\"\"}]},{\"name\":\"Small Night In\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\",\"recordLabel\":\"Outerscope\"},{\"name\":\"Green Mild Cold Capsicum\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Yanke East\",\"recordLabel\":\"MEDIOCRE Music\"},{\"name\":\"The Black Dashes\",\"recordLabel\":\"Fourth Woman Records\"}]},{\"name\":\"Trainerella\",\"bands\":[{\"name\":\"Wild Antelope\",\"recordLabel\":\"Still Bottom Records\"},{\"name\":\"YOUKRANE\",\"recordLabel\":\"Anti Records\"},{\"name\":\"Adrian Venti\",\"recordLabel\":\"Monocracy Records\"},{\"name\":\"Manish Ditch\",\"recordLabel\":\"ACR\"}]},{\"name\":\"Twisted Tour\",\"bands\":[{\"name\":\"Auditones\",\"recordLabel\":\"Marner Sis. Recording\"},{\"name\":\"Squint-281\"},{\"name\":\"Summon\",\"recordLabel\":\"Outerscope\"}]},{\"bands\":[{\"name\":\"Critter Girls\",\"recordLabel\":\"ACR\"},{\"name\":\"Propeller\",\"recordLabel\":\"Pacific Records\"}]}]";
    }

}
