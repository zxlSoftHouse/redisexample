package com.zxl.redisexample.config;

import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisClusterConfiguration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;

import java.util.Arrays;

public class RedisConfig {

    /**
     * redis连接工厂bean
     */
    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 单机版配置
        //RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        //redisStandaloneConfiguration.setHostName(localhost);
        //redisStandaloneConfiguration.setPort(6379);

        // 集群版本
        RedisClusterConfiguration redisStandaloneConfiguration = new RedisClusterConfiguration(Arrays.asList(
                "127.0.0.1:6480",
                "127.0.0.1:6481",
                "127.0.0.1:6482",
                "127.0.0.1:6483",
                "127.0.0.1:6484"
        ));

        RedisConnectionFactory redisConnectionFactory = new JedisConnectionFactory(redisStandaloneConfiguration);
        return redisConnectionFactory;
    }
}
