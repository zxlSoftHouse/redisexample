package com.zxl.redisexample.config;

import com.zxl.redisexample.websocket.live.WebsocketLiveHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

public class WebSocketConfig {

    // websocket拦截
    @Bean
    public WebSocketConfigurer webSocketConfig(WebsocketLiveHandler websocketLiveHandler) {
        return new WebSocketConfigurer() {
            @Override
            public void registerWebSocketHandlers(WebSocketHandlerRegistry webSocketHandlerRegistry) {
                webSocketHandlerRegistry.addHandler(websocketLiveHandler, "/ws/live").setAllowedOrigins("*");
            }
        };
    }
}
