package com.zxl.redisexample.live;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

// 接收消息
@Component
public class WebsocketLiveHandler extends TextWebSocketHandler {

    /**
     * websocket建立成功操作，进入房间--建立连接
     * @param session
     * @throws Exception
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("新用户来了" + session);
        session.getAttributes().get("userId");
        session.getAttributes().get("roomId");
    }

    /**
     * websocket
     * @param session
     * @param status
     * @throws Exception
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        System.out.println("用户走了" + session);
    }
}
