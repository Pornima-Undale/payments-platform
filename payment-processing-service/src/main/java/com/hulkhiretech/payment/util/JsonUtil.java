package com.hulkhiretech.payment.util;

import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class JsonUtil {
	
	private final ObjectMapper objectMapper;
	
	public <T> T convertJsonToObject(String json, Class<T> clazz) {
		try {
			return objectMapper.readValue(json, clazz);
		} catch (Exception e) {
			log.error("Error converting JSON to Object: {}", e.getMessage(), e);
			throw new RuntimeException("Error converting JSON to Object", e);
		}
	}
	
	public String convertObjectToJson(Object object) {
		try {
			return objectMapper.writeValueAsString(object);
		} catch (Exception e) {
			log.error("Error converting Object to JSON: {}", e.getMessage(), e);
			throw new RuntimeException("Error converting Object to JSON", e);
		}
	}

}
