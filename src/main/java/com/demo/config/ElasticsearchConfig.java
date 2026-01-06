package com.demo.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.JsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.ElasticsearchTransportBase;
import co.elastic.clients.transport.ElasticsearchTransportConfig;
import co.elastic.clients.transport.TransportOptions;
import co.elastic.clients.transport.instrumentation.Instrumentation;
import lombok.Data;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties("elasticsearch")
@Data
public class ElasticsearchConfig {

    private String apiKey;
    private String uri;
    private String host;
    private Short port;
    private String scheme;


    @Bean
    public ElasticsearchClient elasticsearchClient() {


        return ElasticsearchClient.of(builder -> builder.apiKey(apiKey)
                .host(uri)
        );
    }

}
