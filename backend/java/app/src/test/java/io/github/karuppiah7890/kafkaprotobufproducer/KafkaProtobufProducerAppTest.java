package io.github.karuppiah7890.kafkaprotobufproducer;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.util.JsonFormat;
import io.github.karuppiah7890.kafkaprotobufproducer.SearchRequestOuterClass.SearchRequest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
    void convertJSONIntoProtobufMessage() throws IOException {
        String protobufMessageAsJSONString = "{\"query\":\"meh-query\",\"pageNumber\":1,\"resultPerPage\":5}";

        JsonFormat.Parser parser = JsonFormat.parser();

//        DescriptorProtos.DescriptorProto descriptorProto = DescriptorProtos.DescriptorProto.newBuilder()
//                .build();
//
        var descriptorFilePath = "/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/build/descriptors/test.desc";

        var descriptorFile = new FileInputStream(descriptorFilePath);

        DescriptorProtos.FileDescriptorSet fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(descriptorFile);

        DescriptorProtos.FileDescriptorProto fileDescriptorProto = fileDescriptorSet.getFileList().stream()
                .filter(fdsProto -> fdsProto.getName().equals("search-request.proto"))
                .findFirst().get();

        Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, );


//
//        DynamicMessage dynamicMessage = DynamicMessage.newBuilder().build();
//
//        Message.Builder messageBuilder;
//        parser.merge(protobufMessageAsJSONString, messageBuilder);
    }
}
