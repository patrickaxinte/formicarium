package com.example.formicarium.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // activeaza un broker simplu in memorie pentru destinatii prefixate cu /topic
        config.enableSimpleBroker("/topic");

        // pentru metodele @MessageMapping, clientul trimite catre /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint-ul principal pentru Stomp + SockJS
        registry.addEndpoint("/ws").withSockJS();
    }
}
