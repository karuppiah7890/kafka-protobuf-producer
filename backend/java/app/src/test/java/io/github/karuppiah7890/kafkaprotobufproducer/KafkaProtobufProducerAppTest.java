package io.github.karuppiah7890.kafkaprotobufproducer;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import io.github.karuppiah7890.kafkaprotobufproducer.SearchRequestOuterClass.SearchRequest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class KafkaProtobufProducerAppTest {
    @Test
    void produceDummyProtobufMessageDataIntoKafka() {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        SearchRequest searchRequest = SearchRequest.newBuilder()
                .setPageNumber(1)
                .setResultPerPage(5)
                .setQuery("meh-query")
                .build();

        Producer<String, byte[]> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++)
            producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), searchRequest.toByteArray()));

        producer.close();
    }

    @Test
    void convertJSONIntoProtobufMessage() {
        String protobufMessageAsJSONString = "{\"query\":\"meh-query\",\"pageNumber\":1,\"resultPerPage\":5}";

        JsonFormat.Parser parser = JsonFormat.parser();

        DescriptorProtos.DescriptorProto descriptorProto = DescriptorProtos.DescriptorProto.newBuilder()
                .build();

        DescriptorProtos.FileDescriptorProto fileDescriptorProto = DescriptorProtos.FileDescriptorProto.newBuilder()
                .build();

        DescriptorProtos.FileDescriptorSet fileDescriptorSet = DescriptorProtos.FileDescriptorSet.newBuilder()
                .build();

        DynamicMessage dynamicMessage = DynamicMessage.newBuilder().build();

        Message.Builder messageBuilder;
        parser.merge(protobufMessageAsJSONString, messageBuilder);
    }
}
