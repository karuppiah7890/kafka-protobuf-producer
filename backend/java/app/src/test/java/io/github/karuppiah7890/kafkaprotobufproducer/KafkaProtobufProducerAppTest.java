package io.github.karuppiah7890.kafkaprotobufproducer;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;
import com.google.protobuf.DynamicMessage;
import com.google.protobuf.util.JsonFormat;
import io.github.karuppiah7890.kafkaprotobufproducer.SearchRequestOuterClass.SearchRequest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.util.Objects;
import java.util.Properties;

import static java.util.Objects.isNull;

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
    void convertJSONIntoProtobufMessage() throws Exception {
        String protobufMessageAsJSONString = "{\"query\":\"meh-query\",\"pageNumber\":1,\"resultPerPage\":5}";

        JsonFormat.Parser parser = JsonFormat.parser();

        var descriptorFilePath = "/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/descriptors/test.desc";

        var descriptorFile = new FileInputStream(descriptorFilePath);

        DescriptorProtos.FileDescriptorSet fileDescriptorSet = DescriptorProtos.FileDescriptorSet.parseFrom(descriptorFile);

        DescriptorProtos.FileDescriptorProto fileDescriptorProto = fileDescriptorSet.getFileList().stream()
                .filter(fdsProto -> fdsProto.getName().equals("search-request.proto"))
                .findFirst().orElse(null);

        if (isNull(fileDescriptorProto)) {
            throw new Exception("File Descriptor Proto value is null!");
        }

        Descriptors.FileDescriptor[] fileDescriptorProtoDependencies = new Descriptors.FileDescriptor[0];
        Descriptors.FileDescriptor fileDescriptor = Descriptors.FileDescriptor.buildFrom(fileDescriptorProto, fileDescriptorProtoDependencies);

        Descriptors.Descriptor descriptor = fileDescriptor.getMessageTypes().stream()
                .filter(messageDescriptor -> Objects.equals(messageDescriptor.getName(), "SearchRequest"))
                .findFirst().orElse(null);

        if (isNull(descriptor)) {
            throw new Exception("Descriptor value is null!");
        }

        DynamicMessage.Builder dynamicMessageBuilder = DynamicMessage.newBuilder(descriptor);

        parser.merge(protobufMessageAsJSONString, dynamicMessageBuilder);

        DynamicMessage dynamicMessage = dynamicMessageBuilder.build();

        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.ByteArraySerializer");

        Producer<String, byte[]> producer = new KafkaProducer<>(props);
        producer.send(new ProducerRecord<>("my-topic", Integer.toString(0), dynamicMessage.toByteArray()));

        producer.close();
    }
}
