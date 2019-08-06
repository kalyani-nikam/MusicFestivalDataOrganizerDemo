package com.music.festival.demo.rest.client;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.music.festival.demo.rest.client.exception.ResponseParsingException;
import com.music.festival.demo.rest.client.model.Festival;
import org.glassfish.jersey.client.ClientConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.util.backoff.BackOffExecution;
import org.springframework.util.backoff.ExponentialBackOff;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * API Client for festivals API.
 */
@Component
public class MusicFestivalRESTApiClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MusicFestivalRESTApiClient.class);

    /**
     * Defaulted to http://eacodingtest.digital.energyaustralia.com.au/api/v1/
     * if system property rest.uri is not set.
     */
    @Value("${rest.uri:http://eacodingtest.digital.energyaustralia.com.au/api/v1/}")
    private String REST_URI;

    /**
     * Get a list of {@link Festival}s by calling the REST API
     * @return List of {@link Festival}s
     * @throws ResponseParsingException thrown if the response from remote API is invalid.
     */
    public List<Festival> getFestivals() throws ResponseParsingException {
        // Invoke REST API with exponential backoff to handle throttling error
        Response response = invokeRemoteGetWithExponentialBackoff("festivals");
        String responseString = response.readEntity(String.class);

        if(StringUtils.isEmpty(responseString)) {
            LOGGER.info("Empty response string received from the API.");
        } else {
            LOGGER.debug("Response from the API : " + responseString);
        }

        // Deserialize response, prepare list of Festivals.
        ObjectMapper objectMapper = new ObjectMapper();
        List<Festival> festivalList = new ArrayList<>();
        try {
            festivalList = objectMapper.readValue(responseString, new TypeReference<List<Festival>>(){});
        } catch (IOException e) {
            throw new ResponseParsingException("Exception while parsing response string. Cause: " + e.getMessage());
        }

        return festivalList;
    }

    /**
     * Invokes the remote REST API on the given path.
     * Implements exponential backoff to deal with unsuccessful responses.
     * @param path
     * @return Response received from the API
     */
    private Response invokeRemoteGetWithExponentialBackoff(final String path) {
        Client client = ClientBuilder.newClient( new ClientConfig() );
        WebTarget webTarget = client.target(REST_URI).path(path);
        Invocation.Builder invocationBuilder =  webTarget.request(MediaType.APPLICATION_JSON);

        LOGGER.debug("Invoking API at URI : " + webTarget.getUri());

        // Exponential backoff to handle throttling error.
        ExponentialBackOff backoff = new ExponentialBackOff();
        BackOffExecution backOffExecution = backoff.start();

        Response response = null;
        long waitTime = 0;

        while(waitTime != backOffExecution.STOP) {
            response = invocationBuilder.get();

            // Return successful response
            if(response.getStatus() == Response.Status.OK.getStatusCode()) {
                LOGGER.debug("Got a successful response.");
                return response;
            }

            // Increase waitTime and try again
            try {
                waitTime = backOffExecution.nextBackOff();
                if(waitTime != backOffExecution.STOP) {
                    response.close();
                    LOGGER.info("Request for " + webTarget.getUri() + " failed. Backing off for " + waitTime + "ms.");
                    Thread.sleep(waitTime);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
        return response;
    }
}
