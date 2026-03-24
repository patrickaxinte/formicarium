package com.example.formicarium.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // enable a simple in-memory broker for destinations prefixed with /topic
        config.enableSimpleBroker("/topic");

        // for @MessageMapping methods, the client sends to /app
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(org.springframework.web.socket.config.annotation.StompEndpointRegistry registry) {
        // main endpoint for Stomp + SockJS
        registry.addEndpoint("/ws")
                .setHandshakeHandler(new org.springframework.web.socket.server.support.DefaultHandshakeHandler() {
                    @Override
                    protected java.security.Principal determineUser(org.springframework.http.server.ServerHttpRequest request, org.springframework.web.socket.WebSocketHandler wsHandler, java.util.Map<String, Object> attributes) {
                        return org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                    }
                })
                .withSockJS();
    }
}
