package com.hulkhiretech.payment.config;

import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.autoconfigure.web.client.RestClientBuilderConfigurer;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.hulkhiretech.payment.dto.TransactionDTO;
import com.hulkhiretech.payment.entity.TransactionEntity;
import com.hulkhiretech.payment.util.modelmapper.converter.idtoName.PaymentMethodEnumIdToNameConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.idtoName.PaymentTypeEnumIdToNameConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.idtoName.ProviderEnumIdToNameConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.idtoName.TxnStatusEnumIdToNameConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.nametoid.PaymentMethodEnumConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.nametoid.PaymentTypeEnumConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.nametoid.ProviderEnumConverter;
import com.hulkhiretech.payment.util.modelmapper.converter.nametoid.TxnStatusEnumConverter;

@Configuration
public class AppConfig {

    @Bean
    ModelMapper modelMapper() {
        ModelMapper mapper = new ModelMapper();
        // Use STRICT matching so only properties with exact same names are mapped.
        mapper.getConfiguration()
                .setMatchingStrategy(MatchingStrategies.STRICT);
        
        // Converters that convert name (String) to corresponding id (Integer)
        Converter<String, Integer> paymentMethodEnumConverter = new PaymentMethodEnumConverter();
        Converter<String, Integer> providerEnumConverter = new ProviderEnumConverter();
        Converter<String, Integer> paymentTypeEnumConverter = new PaymentTypeEnumConverter();
        Converter<String, Integer> txnStatusEnumConverter = new TxnStatusEnumConverter();

        // Converters that convert id (Integer) to corresponding name (String)
        Converter<Integer, String> paymentMethodIdToNameConverter = new PaymentMethodEnumIdToNameConverter();
        Converter<Integer, String> providerIdToNameConverter = new ProviderEnumIdToNameConverter();
        Converter<Integer, String> paymentTypeIdToNameConverter = new PaymentTypeEnumIdToNameConverter();
        Converter<Integer, String> txnStatusIdToNameConverter = new TxnStatusEnumIdToNameConverter();

        // Map DTO name fields to Entity id fields using converters
        mapper.addMappings(new PropertyMap<TransactionDTO, TransactionEntity>() {
            @Override
            protected void configure() {
                using(paymentMethodEnumConverter).map(source.getPaymentMethod(), destination.getPaymentMethodId());
                using(providerEnumConverter).map(source.getProvider(), destination.getProviderId());
                using(paymentTypeEnumConverter).map(source.getPaymentType(), destination.getPaymentTypeId());
                using(txnStatusEnumConverter).map(source.getTxnStatus(), destination.getTxnStatusId());
            }
        });

        // Map Entity id fields back to DTO name fields using id->name converters
        mapper.addMappings(new PropertyMap<TransactionEntity, TransactionDTO>() {
            @Override
            protected void configure() {
                using(paymentMethodIdToNameConverter).map(source.getPaymentMethodId(), destination.getPaymentMethod());
                using(providerIdToNameConverter).map(source.getProviderId(), destination.getProvider());
                using(paymentTypeIdToNameConverter).map(source.getPaymentTypeId(), destination.getPaymentType());
                using(txnStatusIdToNameConverter).map(source.getTxnStatusId(), destination.getTxnStatus());
            }
        });
        
        return mapper;
    }
    
    @LoadBalanced
    @Bean
    RestClient.Builder restClientBuilder(RestClientBuilderConfigurer configurer) {
		return configurer.configure(RestClient.builder());
	}
    
    @Bean
	RestClient restClient(RestClient.Builder builder) {
		PoolingHttpClientConnectionManager connectionManager =
				new PoolingHttpClientConnectionManager();
		connectionManager.setMaxTotal(200);
		connectionManager.setDefaultMaxPerRoute(50);
		connectionManager.setConnectionConfigResolver(
				route -> ConnectionConfig.custom()
				.setValidateAfterInactivity(TimeValue.ofSeconds(5))
				.build()
				);
		
		CloseableHttpClient httpClient = HttpClients.custom()
				.setConnectionManager(connectionManager)
				.evictIdleConnections(TimeValue.ofSeconds(30))
				.build();
		
		HttpComponentsClientHttpRequestFactory requestFactory =
				new HttpComponentsClientHttpRequestFactory(httpClient);
		requestFactory.setConnectTimeout(10000);
		requestFactory.setReadTimeout(15000);
		requestFactory.setConnectionRequestTimeout(10000);
		return builder
				.requestFactory(requestFactory)
				.build();
	}
}
