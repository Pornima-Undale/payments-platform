package com.hulkhiretech.payments.service;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import com.hulkhiretech.payments.constant.Constant;
import com.hulkhiretech.payments.http.HttpRequest;
import com.hulkhiretech.payments.http.HttpServiceEngine;
import com.hulkhiretech.payments.paypal.PaypalOAuthToken;
import com.hulkhiretech.payments.util.JsonUtil;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class TokenService {

	private static String accessToken;

	private final HttpServiceEngine httpServiceEngine;

	private final ScheduledExecutorService scheduler;
	
	@Value("${paypal.client.id}")
	private String paypalClientId;
	
	@Value("${paypal.client.secret}")
	private String paypalClientSecret;
	
	@Value("${paypal.oauth.url}")
	private String oauthUrl;
	
	private final JsonUtil jsonUtil;

	public String getAccessToken() {
		log.info("getAccessToken called");

		if (accessToken != null) { 
			log.info("Using cached access token");
			return accessToken;
		}

		log.info("Access token does not exist, generating a new one...");

		HttpRequest httpRequest = prepareOAuthRequest();

		ResponseEntity<String> response = httpServiceEngine.makeHttpCall(httpRequest);
		log.info("OAuth token response status: {}", response.getStatusCode());

		PaypalOAuthToken token = jsonUtil.convertJsonToObject(
				response.getBody(), PaypalOAuthToken.class);
		
		accessToken = token.getAccessToken();
		
		scheduleExpiryOfAccessToken(token);

		log.info("Fetched and cached new access token");
		return accessToken;
	}

	private void scheduleExpiryOfAccessToken(PaypalOAuthToken token) {
		// - 15mins is to ensure that the token is cleared before it actually expires
		// This will avoid issue, where we say token is valid, but paypal rejects it because it is expired. 
		//So we will clear the token 15 mins before it actually expires.
		// put this 15mins in property file.
		long reducedExpiresInSec = token.getExpiresIn() - (15 * 60);

		// set expiry time
		scheduler.schedule(() -> {
			log.info("Access token expired, clearing the token");
			accessToken = null;
		}, reducedExpiresInSec, TimeUnit.SECONDS);
		log.info("Access token will expire in {} seconds", reducedExpiresInSec);
	}

	private HttpRequest prepareOAuthRequest() {
		// Coding for making OAuth API call
		HttpHeaders customHttpHeaders = new HttpHeaders();
		customHttpHeaders.setBasicAuth(paypalClientId, paypalClientSecret);
		customHttpHeaders.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

		MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
		body.add(Constant.GRANT_TYPE, Constant.CLIENT_CREDENTIALS);

		HttpRequest httpRequest = HttpRequest.builder()
				.httpMethod(HttpMethod.POST)
				.url(oauthUrl)
				.httpHeaders(customHttpHeaders)
				.body(body)
				.build();
		return httpRequest;
	}
}
