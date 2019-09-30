package com.zxl.redisexample.live.controller;

import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 直播中列表，以及消息，进出直播间，redis以及websocket
 */
@RestController
public class LiveController {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 返回人数（土豪）列表
     * @param roomId 房间号
     */
    @RequestMapping("/live/roomInfo")
    public Object roomInfo(String roomId) {
        // 统计在线房间的总人数
        Long count = redisTemplate.opsForZSet().zCard("roominfo::" + roomId);
        // 查询等级排序之后 等级最前面的两个用户
        Set set = redisTemplate.opsForZSet().reverseRangeByScore("roominfo::" + roomId, 10, 5, 0, 2);
        for (Object o : set) {
            System.out.println(o.toString());
        }
        System.out.println("房间号:" + roomId + "人数：" + count);
        return set;
    }
}
