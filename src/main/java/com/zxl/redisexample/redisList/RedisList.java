package com.zxl.redisexample.redisList;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * 白名单/黑名单
 */
@RestController
public class RedisList {

    @Autowired
    RedisTemplate redisTemplate;

    /**
     * 存人员时将该人员id的hashcode值作为白名单标记
     * @param userId
     */
    @GetMapping(value = "/addUser/{userId}")
    public void addUser(@PathVariable("userId") String userId) {
        // 获取userId的hashCode的值
        int hashValue = Math.abs(userId.hashCode());
        // hashCode和string的bit长度2的32次方取余 拿取元素和数组的映射
        long index = (long)(hashValue % Math.pow(2, 32));
        // 设置redis里面二进制数据中的值，对应位置为true，表示已经占用一个位置 原理为布隆过滤器
        redisTemplate.opsForValue().setBit("user_bloom_filter", index, true);
        // 一些存数据库以及存缓存的操作...

    }

    /**
     *
     * @return
     */
    @GetMapping(value = "/findUserById/{userId}")
    public String findUserById(@PathVariable("userId") String userId) {
        // 获取userId的hashCode的值
        int hashValue = Math.abs(userId.hashCode());
        // hashCode和string的bit长度2的32次方取余 拿取元素和数组的映射
        long index = (long)(hashValue % Math.pow(2, 32));
        // 判断redis是否已经存在该userid的标记
        boolean flag = redisTemplate.opsForValue().getBit("user_bloom_filter", index);
        if (flag) {
            // 返回数据库中或者缓存中的该人员信息..
            return "人员信息";
        } else {
            return "该人员未加入白名单";
        }
    }
}
