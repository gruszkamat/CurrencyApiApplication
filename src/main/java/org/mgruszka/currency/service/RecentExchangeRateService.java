package org.mgruszka.currency.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.mgruszka.currency.exceptions.NBApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class RecentExchangeRateService {

    private static final String API_URL = "https://api.nbp.pl/api/exchangerates/rates/A/USD/";

    public double getUSDExchangeRate() {

        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format(API_URL)))
                    .build();
            try {
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() != HttpStatus.OK.value()) {
                    throw new NBApiException("Failed to fetch data from API. HTTP Status Code: " + response.statusCode());
                }
                ObjectMapper objectMapper = new ObjectMapper();
                JsonNode responseJson = objectMapper.readTree(response.body());

                JsonNode rates = responseJson.path("rates");
                if (rates.isArray() && !rates.isEmpty()) {
                    JsonNode rate = rates.get(0);
                    return rate.path("mid").asDouble();
                } else {
                    throw new NBApiException("Invalid response format. Unable to extract exchange rate.");
                }
            } catch (InterruptedException | IOException e) {
                throw new NBApiException("Error during http request to NBP API.");
            }
        }


    }
}
