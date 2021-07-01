package com.cckp.kafka;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

/**
 * @author ljs
 * @version 1.0
 * @description
 * @date 2021/6/30 14:25
 */
public class ProducerFastStart {


    public static void main(String[] args) {
        Properties properties = new Properties();
        properties.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        properties.put("bootstrap.servers", KafkaConstants.BROKER_LIST);

        //配置生产者客户端并创建KafkaProducer实例
        try (KafkaProducer<String, String> producer = new KafkaProducer<>(properties)) {
            ProducerRecord<String, String> record = new ProducerRecord<>(KafkaConstants.TOPIC_NAME, "hello+kafka!!!");
            System.out.println("开始发送");
            producer.send(record);
            System.out.println("发送完成");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
