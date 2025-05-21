package com.deveagles.be15_deveagles_be.features.chat.config.websocket;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

  @Value("${websocket.allowed-origins:*}")
  private String allowedOrigins;

  @Value("${websocket.endpoint:/ws}")
  private String endpoint;

  @Value("${websocket.application-destination-prefix:/app}")
  private String applicationDestinationPrefix;

  @Value("${websocket.broker-prefix:/topic}")
  private String brokerPrefix;

  @Value("${websocket.user-destination-prefix:/user}")
  private String userDestinationPrefix;

  @Override
  public void registerStompEndpoints(StompEndpointRegistry registry) {
    registry.addEndpoint(endpoint).setAllowedOrigins(allowedOrigins).withSockJS();
  }

  @Override
  public void configureMessageBroker(MessageBrokerRegistry registry) {
    registry.setUserDestinationPrefix(userDestinationPrefix);
    registry.setApplicationDestinationPrefixes(applicationDestinationPrefix);
    registry.enableSimpleBroker(brokerPrefix);
  }
}
