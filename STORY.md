# Story

Recently in our project we wanted to create a fake / mock kafka producer which
produces kafka messages in protobuf format

I was thinking of a nice web UI but that's a long shot and too much effort and
lot of work too for building and using too.

But the idea I had in mind was - use JSON input to give the message data, along
with input to give information about the protobuf so that the protobuf
message can be constructed

Since web UI is a long way to go, I thought maybe starting with CLI is simple.
It will be like trying out the kafka console producer, just that this will take
in JSON and protobuf related input and produce protobuf messages into the topic
:)

I'm looking at the JSON Parser to get some help with the whole thing for
converting JSON into Protobuf message

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.Parser.html

I know that the JSON Printer is used to convert Protobuf message into JSON

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.Printer.html

That's how I learned about JSON Parser too

In our current project we used the Printer to convert Protobuf message into
JSON to parse and process the data. It was pretty cool and we had to do it
dynamically for any kind of Protobuf message

Now I'm trying to do the reverse - produce Protobuf messages instead of consume
and parse etc.

Also, another thing that I just saw -

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.TypeRegistry.html

This is useful in case Any type is used it seems. Hmm

Now to test the whole thing with a real kafka, I need to run a Kafka manually
and test it, or I could use testcontainers and run integration tests with
actual kafka! :D

Now, let's first write something basic! :)

Steps

- Create a Sample Proto file with a sample Message - DONE
- Auto generate Java code for serializing and deserializing this message - DONE
- Import a Kafka client library - DONE
- Create a protocol buffer message producer using the Kafka client
  library - DONE
- Run Zookeeper and Kafka locally - DONE
- Run my app to produce some dummy data - DONE
- Consume the dummy data using the kafka protobuf consumer - DONE

That confirms that we are able to produce protobuf messages as we would be able
to consume them using another tool

Next Steps would be

- Try to use JSON format of the protobuf message as input for the tool - 
  PARTIALLY DONE
  - CLI input - direct standard input or file input - a single JSON file maybe -
  PARTIALLY DONE
- Use Protobuf message metadata and parse the JSON input to form Protobuf
  messages - DONE
- Run the app to produce dummy data - DONE
- Consume dummy data with kafka protobuf consumer tool - DONE

Now we can use any JSON message and Protobuf message metadata data to produce
any / most protobuf messages into Kafka. Why do I say "most" instead of "any"
or "all"? This is because I'm not sure if I can send messages with "Any" in the
Protobuf message. It requires more metadata and an extra thing called the Type
Registry. But yeah, it maybe possible :)

Next Steps

- Write automated tests for all the different parts of the code
- Where integration test is needed use testcontainers and JUnit and JUnit
  extensions to run Kafka and use it

Now I'm going to follow the steps

- Create a Sample Proto file with a sample Message

https://developers.google.com/protocol-buffers/

```proto
message Person {
  required string name = 1;
  required int32 id = 2;
  optional string email = 3;
}
```

https://developers.google.com/protocol-buffers/docs/javatutorial

https://developers.google.com/protocol-buffers/docs/proto3

```proto
syntax = "proto3";

message SearchRequest {
  string query = 1;
  int32 page_number = 2;
  int32 result_per_page = 3;
}
```

I'm just going to use that for now! :)

https://developers.google.com/protocol-buffers/docs/reference/java-generated#invocation

I'm looking at how to generate the Java Code. I actually just want to use a
Java Gradle plugin instead of typing long protoc compiler commands :)

https://duckduckgo.com/?t=ffab&q=java+protobuf+gradle&ia=web

I have done this in our project before :)

https://github.com/google/protobuf-gradle-plugin

https://dev.to/techschoolguru/config-gradle-to-generate-java-code-from-protobuf-1cla

```groovy
plugins {
    id "com.google.protobuf" version "0.8.15"
}
```

I'm checking how to provide test resources as input

https://github.com/google/protobuf-gradle-plugin#customizing-source-directories

Ignoring `.idea` directory completely though I already added it as part of first
commit, haha

https://www.toptal.com/developers/gitignore/api/intellij+all

```bash
$ git rm -r --cached backend/java/.idea
```

Not able to find the generate proto as part of gradle tasks

```bash
$ ./gradlew tasks
```

Checking it now

But weirdly the below works!

```bash
$ ./gradlew generateProto

BUILD SUCCESSFUL in 980ms
2 actionable tasks: 2 executed
```

```bash
$ fd . build/
build/extracted-include-protos
build/extracted-include-protos/main
build/extracted-protos
build/extracted-protos/main
```

But no java files were auto generated, hmm

I have `protoc` and some more software too

```bash
$ # pressing tab
$ protoc
protoc                 protoc-gen-go          protoc-gen-grpc-swift  protoc-gen-swift
```

Maybe it was because my proto files are in the test directory, hmm

I ran the below and got this

```bash
 app  master ✘  $ ./gradlew compileJ compileTestJ
-bash: ./gradlew: No such file or directory
 ✘  app  master ✘  $ ls
build		build.gradle	src
 app  master ✘  $ cd ..
 java  master ✘  $ ./gradlew compileJ compileTestJ

> Task :app:compileTestJava FAILED
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:7: error: package com.google.protobuf does not exist
      com.google.protobuf.ExtensionRegistryLite registry) {
                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:11: error: package com.google.protobuf does not exist
      com.google.protobuf.ExtensionRegistry registry) {
                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:717: error: package com.google.protobuf.Descriptors does not exist
  private static final com.google.protobuf.Descriptors.Descriptor
                                                      ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:720: error: package com.google.protobuf.GeneratedMessageV3 does not exist
    com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                                          ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:723: error: package com.google.protobuf.Descriptors does not exist
  public static com.google.protobuf.Descriptors.FileDescriptor
                                               ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:727: error: package com.google.protobuf.Descriptors does not exist
  private static  com.google.protobuf.Descriptors.FileDescriptor
                                                 ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:17: error: package com.google.protobuf does not exist
      com.google.protobuf.MessageOrBuilder {
                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:28: error: package com.google.protobuf does not exist
    com.google.protobuf.ByteString
                       ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:47: error: package com.google.protobuf does not exist
      com.google.protobuf.GeneratedMessageV3 implements
                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:52: error: package com.google.protobuf.GeneratedMessageV3 does not exist
    private SearchRequest(com.google.protobuf.GeneratedMessageV3.Builder<?> builder) {
                                                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:62: error: cannot find symbol
        UnusedPrivateParameter unused) {
        ^
  symbol:   class UnusedPrivateParameter
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:67: error: package com.google.protobuf does not exist
    public final com.google.protobuf.UnknownFieldSet
                                    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:72: error: package com.google.protobuf does not exist
        com.google.protobuf.CodedInputStream input,
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:73: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:74: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:124: error: package com.google.protobuf.Descriptors does not exist
    public static final com.google.protobuf.Descriptors.Descriptor
                                                       ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:130: error: package com.google.protobuf.GeneratedMessageV3 does not exist
    protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                                                    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:159: error: package com.google.protobuf does not exist
    public com.google.protobuf.ByteString
                              ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:205: error: package com.google.protobuf does not exist
    public void writeTo(com.google.protobuf.CodedOutputStream output)
                                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:281: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:286: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:287: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:291: error: package com.google.protobuf does not exist
        com.google.protobuf.ByteString data)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:292: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:296: error: package com.google.protobuf does not exist
        com.google.protobuf.ByteString data,
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:297: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:298: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:302: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:307: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:308: error: package com.google.protobuf does not exist
        throws com.google.protobuf.InvalidProtocolBufferException {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:318: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:330: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:336: error: package com.google.protobuf does not exist
        com.google.protobuf.CodedInputStream input)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:342: error: package com.google.protobuf does not exist
        com.google.protobuf.CodedInputStream input,
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:343: error: package com.google.protobuf does not exist
        com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:373: error: package com.google.protobuf.GeneratedMessageV3 does not exist
        com.google.protobuf.GeneratedMessageV3.Builder<Builder> implements
                                              ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:365: error: package com.google.protobuf.GeneratedMessageV3 does not exist
        com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
                                              ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:690: error: package com.google.protobuf does not exist
    private static final com.google.protobuf.Parser<SearchRequest>
                                            ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:701: error: package com.google.protobuf does not exist
    public static com.google.protobuf.Parser<SearchRequest> parser() {
                                     ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:706: error: package com.google.protobuf does not exist
    public com.google.protobuf.Parser<SearchRequest> getParserForType() {
                              ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:376: error: package com.google.protobuf.Descriptors does not exist
      public static final com.google.protobuf.Descriptors.Descriptor
                                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:382: error: package com.google.protobuf.GeneratedMessageV3 does not exist
      protected com.google.protobuf.GeneratedMessageV3.FieldAccessorTable
                                                      ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:395: error: package com.google.protobuf.GeneratedMessageV3 does not exist
          com.google.protobuf.GeneratedMessageV3.BuilderParent parent) {
                                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:417: error: package com.google.protobuf.Descriptors does not exist
      public com.google.protobuf.Descriptors.Descriptor
                                            ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:452: error: package com.google.protobuf.Descriptors does not exist
          com.google.protobuf.Descriptors.FieldDescriptor field,
                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:458: error: package com.google.protobuf.Descriptors does not exist
          com.google.protobuf.Descriptors.FieldDescriptor field) {
                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:463: error: package com.google.protobuf.Descriptors does not exist
          com.google.protobuf.Descriptors.OneofDescriptor oneof) {
                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:468: error: package com.google.protobuf.Descriptors does not exist
          com.google.protobuf.Descriptors.FieldDescriptor field,
                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:474: error: package com.google.protobuf.Descriptors does not exist
          com.google.protobuf.Descriptors.FieldDescriptor field,
                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:479: error: package com.google.protobuf does not exist
      public Builder mergeFrom(com.google.protobuf.Message other) {
                                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:512: error: package com.google.protobuf does not exist
          com.google.protobuf.CodedInputStream input,
                             ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:513: error: package com.google.protobuf does not exist
          com.google.protobuf.ExtensionRegistryLite extensionRegistry)
                             ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:550: error: package com.google.protobuf does not exist
      public com.google.protobuf.ByteString
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:594: error: package com.google.protobuf does not exist
          com.google.protobuf.ByteString value) {
                             ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:666: error: package com.google.protobuf does not exist
          final com.google.protobuf.UnknownFieldSet unknownFields) {
                                   ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:672: error: package com.google.protobuf does not exist
          final com.google.protobuf.UnknownFieldSet unknownFields) {
                                   ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:13: error: package com.google.protobuf does not exist
        (com.google.protobuf.ExtensionRegistryLite) registry);
                            ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:59: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:66: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:69: error: cannot find symbol
      return this.unknownFields;
                 ^
  symbol: variable unknownFields
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:79: error: package com.google.protobuf.UnknownFieldSet does not exist
      com.google.protobuf.UnknownFieldSet.Builder unknownFields =
                                         ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:80: error: package com.google.protobuf does not exist
          com.google.protobuf.UnknownFieldSet.newBuilder();
                             ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:114: error: package com.google.protobuf does not exist
      } catch (com.google.protobuf.InvalidProtocolBufferException e) {
                                  ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:117: error: package com.google.protobuf does not exist
        throw new com.google.protobuf.InvalidProtocolBufferException(
                                     ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:120: error: cannot find symbol
        this.unknownFields = unknownFields.build();
            ^
  symbol: variable unknownFields
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:121: error: cannot find symbol
        makeExtensionsImmutable();
        ^
  symbol:   method makeExtensionsImmutable()
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:129: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:148: error: package com.google.protobuf does not exist
        com.google.protobuf.ByteString bs =
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:149: error: package com.google.protobuf does not exist
            (com.google.protobuf.ByteString) ref;
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:163: error: package com.google.protobuf does not exist
        com.google.protobuf.ByteString b =
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:164: error: package com.google.protobuf does not exist
            com.google.protobuf.ByteString.copyFromUtf8(
                               ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:169: error: package com.google.protobuf does not exist
        return (com.google.protobuf.ByteString) ref;
                                   ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:194: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:204: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:208: error: package com.google.protobuf does not exist
        com.google.protobuf.GeneratedMessageV3.writeString(output, 1, query_);
                           ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:216: error: cannot find symbol
      unknownFields.writeTo(output);
      ^
  symbol:   variable unknownFields
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:219: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:221: error: cannot find symbol
      int size = memoizedSize;
                 ^
  symbol:   variable memoizedSize
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:226: error: package com.google.protobuf does not exist
        size += com.google.protobuf.GeneratedMessageV3.computeStringSize(1, query_);
                                   ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:229: error: package com.google.protobuf does not exist
        size += com.google.protobuf.CodedOutputStream
                                   ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:233: error: package com.google.protobuf does not exist
        size += com.google.protobuf.CodedOutputStream
                                   ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:236: error: cannot find symbol
      size += unknownFields.getSerializedSize();
              ^
  symbol:   variable unknownFields
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:237: error: cannot find symbol
      memoizedSize = size;
      ^
  symbol:   variable memoizedSize
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:247: error: non-static variable super cannot be referenced from a static context
        return super.equals(obj);
               ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:257: error: cannot find symbol
      if (!unknownFields.equals(other.unknownFields)) return false;
                                     ^
  symbol:   variable unknownFields
  location: variable other of type SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:257: error: cannot find symbol
      if (!unknownFields.equals(other.unknownFields)) return false;
           ^
  symbol:   variable unknownFields
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:263: error: cannot find symbol
      if (memoizedHashCode != 0) {
          ^
  symbol:   variable memoizedHashCode
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:264: error: cannot find symbol
        return memoizedHashCode;
               ^
  symbol:   variable memoizedHashCode
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:274: error: cannot find symbol
      hash = (29 * hash) + unknownFields.hashCode();
                           ^
  symbol:   variable unknownFields
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:275: error: cannot find symbol
      memoizedHashCode = hash;
      ^
  symbol:   variable memoizedHashCode
  location: class SearchRequest
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:313: error: package com.google.protobuf does not exist
      return com.google.protobuf.GeneratedMessageV3
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:320: error: package com.google.protobuf does not exist
      return com.google.protobuf.GeneratedMessageV3
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:325: error: package com.google.protobuf does not exist
      return com.google.protobuf.GeneratedMessageV3
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:332: error: package com.google.protobuf does not exist
      return com.google.protobuf.GeneratedMessageV3
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:338: error: package com.google.protobuf does not exist
      return com.google.protobuf.GeneratedMessageV3
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:345: error: package com.google.protobuf does not exist
      return com.google.protobuf.GeneratedMessageV3
                                ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:349: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:357: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:363: error: method does not override or implement a method from a supertype
    @java.lang.Override
    ^
/Users/karuppiahn/oss/github.com/karuppiah7890/kafka-protobuf-producer/backend/java/app/build/generated/source/proto/test/java/SearchRequestOuterClass.java:381: error: method does not override or implement a method from a supertype
      @java.lang.Override
      ^
100 errors

FAILURE: Build failed with an exception.

* What went wrong:
Execution failed for task ':app:compileTestJava'.
> Compilation failed; see the compiler error output for details.

* Try:
Run with --stacktrace option to get the stack trace. Run with --info or --debug option to get more log output. Run with --scan to get full insights.

* Get more help at https://help.gradle.org

BUILD FAILED in 3s
7 actionable tasks: 5 executed, 2 up-to-date
 ✘  java  master ✘  $
```

:D I think I need to mention package name ? I don't know. I remember package
names were an issue in my work project. Maybe I need to check a bit more

Ah okay, it says that the `com.google.protobuf` package does not exist and
another error -

```log
 error: method does not override or implement a method from a supertype
```

Let me see if I can add a Java dependency to solve this issue. Maybe at just the
test level and not for main source code for now? Or maybe for both? Hmm

https://github.com/protocolbuffers/protobuf

https://github.com/protocolbuffers/protobuf/blob/master/java

https://search.maven.org/search?q=g:com.google.protobuf%20AND%20a:protobuf-java

https://search.maven.org/artifact/com.google.protobuf/protobuf-java/4.0.0-rc-2/bundle

I plan to use the release candidate version itself :P Let's see how that goes!

```groovy
    testImplementation 'com.google.protobuf:protobuf-java:4.0.0-rc-2'
```

Everything works now!! :D

```bash
$ ./gradlew compileJ compileTestJ

BUILD SUCCESSFUL in 1s
7 actionable tasks: 3 executed, 4 up-to-date
```

Below shows the auto generated classes of Protobuf among other things

```bash
$ fd . app/build
app/build/classes
app/build/classes/java
app/build/classes/java/main
app/build/classes/java/main/io
app/build/classes/java/main/io/github
app/build/classes/java/main/io/github/karuppiah7890
app/build/classes/java/main/io/github/karuppiah7890/kafkaprotobufproducer
app/build/classes/java/main/io/github/karuppiah7890/kafkaprotobufproducer/KafkaProtobufProducerApp.class
app/build/classes/java/test
app/build/classes/java/test/SearchRequestOuterClass$SearchRequest$1.class
app/build/classes/java/test/SearchRequestOuterClass$SearchRequest$Builder.class
app/build/classes/java/test/SearchRequestOuterClass$SearchRequest.class
app/build/classes/java/test/SearchRequestOuterClass$SearchRequestOrBuilder.class
app/build/classes/java/test/SearchRequestOuterClass.class
app/build/classes/java/test/io
app/build/classes/java/test/io/github
app/build/classes/java/test/io/github/karuppiah7890
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/KafkaProtobufProducerAppTest.class
app/build/extracted-include-protos
app/build/extracted-include-protos/main
app/build/extracted-include-protos/test
app/build/extracted-include-protos/test/google
app/build/extracted-include-protos/test/google/protobuf
app/build/extracted-include-protos/test/google/protobuf/any.proto
app/build/extracted-include-protos/test/google/protobuf/api.proto
app/build/extracted-include-protos/test/google/protobuf/compiler
app/build/extracted-include-protos/test/google/protobuf/compiler/plugin.proto
app/build/extracted-include-protos/test/google/protobuf/descriptor.proto
app/build/extracted-include-protos/test/google/protobuf/duration.proto
app/build/extracted-include-protos/test/google/protobuf/empty.proto
app/build/extracted-include-protos/test/google/protobuf/field_mask.proto
app/build/extracted-include-protos/test/google/protobuf/source_context.proto
app/build/extracted-include-protos/test/google/protobuf/struct.proto
app/build/extracted-include-protos/test/google/protobuf/timestamp.proto
app/build/extracted-include-protos/test/google/protobuf/type.proto
app/build/extracted-include-protos/test/google/protobuf/wrappers.proto
app/build/extracted-protos
app/build/extracted-protos/main
app/build/extracted-protos/test
app/build/generated
app/build/generated/source
app/build/generated/source/proto
app/build/generated/source/proto/test
app/build/generated/source/proto/test/java
app/build/generated/source/proto/test/java/SearchRequestOuterClass.java
app/build/generated/sources
app/build/generated/sources/annotationProcessor
app/build/generated/sources/annotationProcessor/java
app/build/generated/sources/annotationProcessor/java/main
app/build/generated/sources/annotationProcessor/java/test
app/build/generated/sources/headers
app/build/generated/sources/headers/java
app/build/generated/sources/headers/java/main
app/build/generated/sources/headers/java/test
app/build/tmp
app/build/tmp/compileJava
app/build/tmp/compileJava/source-classes-mapping.txt
app/build/tmp/compileTestJava
app/build/tmp/compileTestJava/source-classes-mapping.txt
```

- Create a Sample Proto file with a sample Message - DONE
- Auto generate Java code for serializing and deserializing this message - DONE

Example to use the protobuf gradle plugin -
https://github.com/google/protobuf-gradle-plugin/blob/master/examples/exampleProject/build.gradle

---

As part of producing Protobuf messages - maybe there's a decision to take
about - Should I create a Kafka topic if it doesn't exist already? Hmm

Maybe not. Let's not create a topic if it doesn't exist already. It's okay.
That's not the main job of a producer. Some admin task, so meh

Some things to take care of to make the tool very generic and flexible -
support all options that a kafka client supports. How? Well, let users provide
config files that they use even for console kafka consumers. The file can
contain all config keys and values in the form of a properties file maybe.
Okay?

---

Release and distribution. I'm currently implementing a Java program. I need to
distribute Jar files in the repo releases or in the maven central or something.

I was thinking about writing the program in Golang too, but in that case, I
should ideally provide executables for all platforms - linux, windows and mac.
Mac - A1 and x64 too I guess. Hmm

Jar files - it's good as long people have Java. There's also minimum Java
version. Usually I will not use very new features, but anyways, it's just a
thing

Java Jars, Golang Executables, cool

I was thinking about using Kotlin, but chucked it. It will take more time to
implement as I forgot syntax and stuff. I was also thinking about how there's
so much new stuff in Java too - RxJava (Reactive Java) etc. Anyways.

For Web UI - one of the ideas was - use front end UI (React or basic Js) - and
backend - Java Spring Boot or even Quarkus or Micronaut framework

UI - Text box to put in JSON message and then protobuf message metadata by
uploading some file like descriptor file or putting some data in a textbox

---

Next things to do

- Import a Kafka client library
- Create a producer using the Kafka client library

https://duckduckgo.com/?t=ffab&q=apache+kafka+java+client&ia=web

https://cwiki.apache.org/confluence/display/KAFKA/Clients

https://cwiki.apache.org/confluence/display/KAFKA/Clients#Clients-AlternativeJava

https://docs.confluent.io/clients-kafka-java/current/overview.html

https://howtoprogram.xyz/2016/05/02/apache-kafka-0-9-java-client-api-example/

https://kafka.apache.org/documentation/#intro_apis

https://kafka.apache.org/documentation.html#producerapi

https://kafka.apache.org/27/javadoc/index.html?org/apache/kafka/clients/producer/KafkaProducer.html

```java
Properties props = new Properties();
props.put("bootstrap.servers", "localhost:9092");
props.put("acks", "all");
props.put("retries", 0);
props.put("linger.ms", 1);
props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

Producer<String, String> producer = new KafkaProducer<>(props);
for (int i = 0; i < 100; i++)
    producer.send(new ProducerRecord<String, String>("my-topic", Integer.toString(i), Integer.toString(i)));

producer.close();
```

```groovy
implementation 'org.apache.kafka:kafka-clients:2.7.0'
```

https://search.maven.org/artifact/org.apache.kafka/kafka-clients/2.7.0/jar

I'm going to start by producing string messages and see if that works by using
the console consumer and then try protobuf messages :D

```java
package io.github.karuppiah7890.kafkaprotobufproducer;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;

import java.util.Properties;

public class KafkaProtobufProducerApp {

    public static void main(String[] args) {
        Properties props = new Properties();
        props.put("bootstrap.servers", "localhost:9092");
        props.put("acks", "all");
        props.put("retries", 0);
        props.put("linger.ms", 1);
        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        Producer<String, String> producer = new KafkaProducer<>(props);
        for (int i = 0; i < 100; i++)
            producer.send(new ProducerRecord<>("my-topic", Integer.toString(i), Integer.toString(i)));

        producer.close();
    }
}
```

I got latest kafka server ( zookeeper and kafka server )

- https://www.apache.org/dyn/closer.cgi?path=/kafka/2.7.0/kafka_2.13-2.7.0.tgz

Running zookeper first

```bash
 kafka_2.13-2.7.0  $ bin/zookeeper-server-start.sh config/zookeeper.properties
[2021-03-20 20:14:35,119] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,120] WARN config/zookeeper.properties is relative. Prepend ./ to indicate that you're sure! (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,129] INFO clientPortAddress is 0.0.0.0:2181 (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,129] INFO secureClientPort is not set (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,131] INFO autopurge.snapRetainCount set to 3 (org.apache.zookeeper.server.DatadirCleanupManager)
[2021-03-20 20:14:35,131] INFO autopurge.purgeInterval set to 0 (org.apache.zookeeper.server.DatadirCleanupManager)
[2021-03-20 20:14:35,131] INFO Purge task is not scheduled. (org.apache.zookeeper.server.DatadirCleanupManager)
[2021-03-20 20:14:35,131] WARN Either no config or no quorum defined in config, running  in standalone mode (org.apache.zookeeper.server.quorum.QuorumPeerMain)
[2021-03-20 20:14:35,135] INFO Log4j 1.2 jmx support found and enabled. (org.apache.zookeeper.jmx.ManagedUtil)
[2021-03-20 20:14:35,148] INFO Reading configuration from: config/zookeeper.properties (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,148] WARN config/zookeeper.properties is relative. Prepend ./ to indicate that you're sure! (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,149] INFO clientPortAddress is 0.0.0.0:2181 (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,149] INFO secureClientPort is not set (org.apache.zookeeper.server.quorum.QuorumPeerConfig)
[2021-03-20 20:14:35,149] INFO Starting server (org.apache.zookeeper.server.ZooKeeperServerMain)
[2021-03-20 20:14:35,152] INFO zookeeper.snapshot.trust.empty : false (org.apache.zookeeper.server.persistence.FileTxnSnapLog)
[2021-03-20 20:14:35,163] INFO Server environment:zookeeper.version=3.5.8-f439ca583e70862c3068a1f2a7d4d068eec33315, built on 05/04/2020 15:53 GMT (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:host.name=localhost (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:java.version=11.0.2 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:java.vendor=Oracle Corporation (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:java.home=/Users/karuppiahn/.jabba/jdk/openjdk@1.11.0-2/Contents/Home (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:java.class.path=/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/activation-1.1.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/aopalliance-repackaged-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/argparse4j-0.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/audience-annotations-0.5.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/commons-cli-1.4.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/commons-lang3-3.8.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-api-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-basic-auth-extension-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-file-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-json-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-mirror-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-mirror-client-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-runtime-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-transforms-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/hk2-api-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/hk2-locator-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/hk2-utils-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-annotations-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-core-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-databind-2.10.5.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-dataformat-csv-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-datatype-jdk8-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-jaxrs-base-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-jaxrs-json-provider-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-module-jaxb-annotations-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-module-paranamer-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-module-scala_2.13-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.activation-api-1.2.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.annotation-api-1.3.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.inject-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.validation-api-2.0.2.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.ws.rs-api-2.1.6.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.xml.bind-api-2.3.2.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javassist-3.25.0-GA.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javassist-3.26.0-GA.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javax.servlet-api-3.1.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javax.ws.rs-api-2.1.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jaxb-api-2.3.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-client-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-common-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-container-servlet-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-container-servlet-core-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-hk2-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-media-jaxb-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-server-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-client-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-continuation-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-http-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-io-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-security-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-server-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-servlet-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-servlets-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-util-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jopt-simple-5.0.4.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-clients-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-log4j-appender-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-raft-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-examples-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-scala_2.13-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-test-utils-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-tools-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka_2.13-2.7.0-sources.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka_2.13-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/log4j-1.2.17.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/lz4-java-1.7.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/maven-artifact-3.6.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/metrics-core-2.2.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-buffer-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-codec-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-common-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-handler-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-resolver-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-transport-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-transport-native-epoll-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-transport-native-unix-common-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/osgi-resource-locator-1.0.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/paranamer-2.8.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/plexus-utils-3.2.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/reflections-0.9.12.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/rocksdbjni-5.18.4.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-collection-compat_2.13-2.2.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-java8-compat_2.13-0.9.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-library-2.13.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-logging_2.13-3.9.2.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-reflect-2.13.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/slf4j-api-1.7.30.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/slf4j-log4j12-1.7.30.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/snappy-java-1.1.7.7.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/zookeeper-3.5.8.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/zookeeper-jute-3.5.8.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/zstd-jni-1.4.5-6.jar (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:java.library.path=/Users/karuppiahn/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:. (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,163] INFO Server environment:java.io.tmpdir=/var/folders/fg/55xcrj215gs2n9gnpz4077y40000gq/T/ (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:java.compiler=<NA> (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:os.name=Mac OS X (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:os.arch=x86_64 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:os.version=10.15.7 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:user.name=karuppiahn (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:user.home=/Users/karuppiahn (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:user.dir=/Users/karuppiahn/Downloads/kafka_2.13-2.7.0 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:os.memory.free=494MB (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:os.memory.max=512MB (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,164] INFO Server environment:os.memory.total=512MB (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,165] INFO minSessionTimeout set to 6000 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,165] INFO maxSessionTimeout set to 60000 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,166] INFO Created server with tickTime 3000 minSessionTimeout 6000 maxSessionTimeout 60000 datadir /tmp/zookeeper/version-2 snapdir /tmp/zookeeper/version-2 (org.apache.zookeeper.server.ZooKeeperServer)
[2021-03-20 20:14:35,178] INFO Using org.apache.zookeeper.server.NIOServerCnxnFactory as server connection factory (org.apache.zookeeper.server.ServerCnxnFactory)
[2021-03-20 20:14:35,181] INFO Configuring NIO connection handler with 10s sessionless connection timeout, 2 selector thread(s), 16 worker threads, and 64 kB direct buffers. (org.apache.zookeeper.server.NIOServerCnxnFactory)
[2021-03-20 20:14:35,188] INFO binding to port 0.0.0.0/0.0.0.0:2181 (org.apache.zookeeper.server.NIOServerCnxnFactory)
[2021-03-20 20:14:35,202] INFO zookeeper.snapshotSizeFactor = 0.33 (org.apache.zookeeper.server.ZKDatabase)
[2021-03-20 20:14:35,205] INFO Snapshotting: 0x0 to /tmp/zookeeper/version-2/snapshot.0 (org.apache.zookeeper.server.persistence.FileTxnSnapLog)
[2021-03-20 20:14:35,209] INFO Snapshotting: 0x0 to /tmp/zookeeper/version-2/snapshot.0 (org.apache.zookeeper.server.persistence.FileTxnSnapLog)
[2021-03-20 20:14:35,225] INFO Using checkIntervalMs=60000 maxPerMinute=10000 (org.apache.zookeeper.server.ContainerManager)
[2021-03-20 20:14:45,794] INFO Creating new log file: log.1 (org.apache.zookeeper.server.persistence.FileTxnLog)
```

Running kafka next

```bash
[2021-03-20 20:14:45,651] INFO Setting -D jdk.tls.rejectClientInitiatedRenegotiation=true to disable client-initiated TLS renegotiation (org.apache.zookeeper.common.X509Util)
[2021-03-20 20:14:45,718] INFO Registered signal handlers for TERM, INT, HUP (org.apache.kafka.common.utils.LoggingSignalHandler)
[2021-03-20 20:14:45,721] INFO starting (kafka.server.KafkaServer)
[2021-03-20 20:14:45,722] INFO Connecting to zookeeper on localhost:2181 (kafka.server.KafkaServer)
[2021-03-20 20:14:45,737] INFO [ZooKeeperClient Kafka server] Initializing a new session to localhost:2181. (kafka.zookeeper.ZooKeeperClient)
[2021-03-20 20:14:45,745] INFO Client environment:zookeeper.version=3.5.8-f439ca583e70862c3068a1f2a7d4d068eec33315, built on 05/04/2020 15:53 GMT (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,745] INFO Client environment:host.name=localhost (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,745] INFO Client environment:java.version=11.0.2 (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,745] INFO Client environment:java.vendor=Oracle Corporation (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,745] INFO Client environment:java.home=/Users/karuppiahn/.jabba/jdk/openjdk@1.11.0-2/Contents/Home (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,745] INFO Client environment:java.class.path=/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/activation-1.1.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/aopalliance-repackaged-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/argparse4j-0.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/audience-annotations-0.5.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/commons-cli-1.4.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/commons-lang3-3.8.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-api-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-basic-auth-extension-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-file-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-json-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-mirror-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-mirror-client-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-runtime-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/connect-transforms-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/hk2-api-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/hk2-locator-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/hk2-utils-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-annotations-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-core-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-databind-2.10.5.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-dataformat-csv-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-datatype-jdk8-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-jaxrs-base-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-jaxrs-json-provider-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-module-jaxb-annotations-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-module-paranamer-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jackson-module-scala_2.13-2.10.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.activation-api-1.2.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.annotation-api-1.3.5.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.inject-2.6.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.validation-api-2.0.2.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.ws.rs-api-2.1.6.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jakarta.xml.bind-api-2.3.2.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javassist-3.25.0-GA.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javassist-3.26.0-GA.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javax.servlet-api-3.1.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/javax.ws.rs-api-2.1.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jaxb-api-2.3.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-client-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-common-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-container-servlet-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-container-servlet-core-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-hk2-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-media-jaxb-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jersey-server-2.31.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-client-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-continuation-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-http-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-io-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-security-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-server-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-servlet-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-servlets-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jetty-util-9.4.33.v20201020.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/jopt-simple-5.0.4.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-clients-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-log4j-appender-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-raft-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-examples-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-scala_2.13-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-streams-test-utils-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka-tools-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka_2.13-2.7.0-sources.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/kafka_2.13-2.7.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/log4j-1.2.17.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/lz4-java-1.7.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/maven-artifact-3.6.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/metrics-core-2.2.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-buffer-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-codec-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-common-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-handler-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-resolver-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-transport-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-transport-native-epoll-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/netty-transport-native-unix-common-4.1.51.Final.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/osgi-resource-locator-1.0.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/paranamer-2.8.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/plexus-utils-3.2.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/reflections-0.9.12.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/rocksdbjni-5.18.4.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-collection-compat_2.13-2.2.0.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-java8-compat_2.13-0.9.1.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-library-2.13.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-logging_2.13-3.9.2.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/scala-reflect-2.13.3.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/slf4j-api-1.7.30.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/slf4j-log4j12-1.7.30.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/snappy-java-1.1.7.7.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/zookeeper-3.5.8.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/zookeeper-jute-3.5.8.jar:/Users/karuppiahn/Downloads/kafka_2.13-2.7.0/bin/../libs/zstd-jni-1.4.5-6.jar (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,746] INFO Client environment:java.library.path=/Users/karuppiahn/Library/Java/Extensions:/Library/Java/Extensions:/Network/Library/Java/Extensions:/System/Library/Java/Extensions:/usr/lib/java:. (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,746] INFO Client environment:java.io.tmpdir=/var/folders/fg/55xcrj215gs2n9gnpz4077y40000gq/T/ (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,746] INFO Client environment:java.compiler=<NA> (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:os.name=Mac OS X (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:os.arch=x86_64 (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:os.version=10.15.7 (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:user.name=karuppiahn (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:user.home=/Users/karuppiahn (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:user.dir=/Users/karuppiahn/Downloads/kafka_2.13-2.7.0 (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:os.memory.free=1013MB (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:os.memory.max=1024MB (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,748] INFO Client environment:os.memory.total=1024MB (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,750] INFO Initiating client connection, connectString=localhost:2181 sessionTimeout=18000 watcher=kafka.zookeeper.ZooKeeperClient$ZooKeeperClientWatcher$@22875539 (org.apache.zookeeper.ZooKeeper)
[2021-03-20 20:14:45,756] INFO jute.maxbuffer value is 4194304 Bytes (org.apache.zookeeper.ClientCnxnSocket)
[2021-03-20 20:14:45,761] INFO zookeeper.request.timeout value is 0. feature enabled= (org.apache.zookeeper.ClientCnxn)
[2021-03-20 20:14:45,764] INFO [ZooKeeperClient Kafka server] Waiting until connected. (kafka.zookeeper.ZooKeeperClient)
[2021-03-20 20:14:45,769] INFO Opening socket connection to server localhost/127.0.0.1:2181. Will not attempt to authenticate using SASL (unknown error) (org.apache.zookeeper.ClientCnxn)
[2021-03-20 20:14:45,777] INFO Socket connection established, initiating session, client: /127.0.0.1:61113, server: localhost/127.0.0.1:2181 (org.apache.zookeeper.ClientCnxn)
[2021-03-20 20:14:45,822] INFO Session establishment complete on server localhost/127.0.0.1:2181, sessionid = 0x10000bb5f7e0000, negotiated timeout = 18000 (org.apache.zookeeper.ClientCnxn)
[2021-03-20 20:14:45,825] INFO [ZooKeeperClient Kafka server] Connected. (kafka.zookeeper.ZooKeeperClient)
[2021-03-20 20:14:46,496] INFO [feature-zk-node-event-process-thread]: Starting (kafka.server.FinalizedFeatureChangeListener$ChangeNotificationProcessorThread)
[2021-03-20 20:14:46,508] INFO Feature ZK node at path: /feature does not exist (kafka.server.FinalizedFeatureChangeListener)
[2021-03-20 20:14:46,509] INFO Cleared cache (kafka.server.FinalizedFeatureCache)
[2021-03-20 20:14:46,694] INFO Cluster ID = ii_2kadCTyamYU3zlNo5TQ (kafka.server.KafkaServer)
[2021-03-20 20:14:46,700] WARN No meta.properties file under dir /tmp/kafka-logs/meta.properties (kafka.server.BrokerMetadataCheckpoint)
[2021-03-20 20:14:46,747] INFO KafkaConfig values:
	advertised.host.name = null
	advertised.listeners = null
	advertised.port = null
	alter.config.policy.class.name = null
	alter.log.dirs.replication.quota.window.num = 11
	alter.log.dirs.replication.quota.window.size.seconds = 1
	authorizer.class.name =
	auto.create.topics.enable = true
	auto.leader.rebalance.enable = true
	background.threads = 10
	broker.id = 0
	broker.id.generation.enable = true
	broker.rack = null
	client.quota.callback.class = null
	compression.type = producer
	connection.failed.authentication.delay.ms = 100
	connections.max.idle.ms = 600000
	connections.max.reauth.ms = 0
	control.plane.listener.name = null
	controlled.shutdown.enable = true
	controlled.shutdown.max.retries = 3
	controlled.shutdown.retry.backoff.ms = 5000
	controller.quota.window.num = 11
	controller.quota.window.size.seconds = 1
	controller.socket.timeout.ms = 30000
	create.topic.policy.class.name = null
	default.replication.factor = 1
	delegation.token.expiry.check.interval.ms = 3600000
	delegation.token.expiry.time.ms = 86400000
	delegation.token.master.key = null
	delegation.token.max.lifetime.ms = 604800000
	delete.records.purgatory.purge.interval.requests = 1
	delete.topic.enable = true
	fetch.max.bytes = 57671680
	fetch.purgatory.purge.interval.requests = 1000
	group.initial.rebalance.delay.ms = 0
	group.max.session.timeout.ms = 1800000
	group.max.size = 2147483647
	group.min.session.timeout.ms = 6000
	host.name =
	inter.broker.listener.name = null
	inter.broker.protocol.version = 2.7-IV2
	kafka.metrics.polling.interval.secs = 10
	kafka.metrics.reporters = []
	leader.imbalance.check.interval.seconds = 300
	leader.imbalance.per.broker.percentage = 10
	listener.security.protocol.map = PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
	listeners = null
	log.cleaner.backoff.ms = 15000
	log.cleaner.dedupe.buffer.size = 134217728
	log.cleaner.delete.retention.ms = 86400000
	log.cleaner.enable = true
	log.cleaner.io.buffer.load.factor = 0.9
	log.cleaner.io.buffer.size = 524288
	log.cleaner.io.max.bytes.per.second = 1.7976931348623157E308
	log.cleaner.max.compaction.lag.ms = 9223372036854775807
	log.cleaner.min.cleanable.ratio = 0.5
	log.cleaner.min.compaction.lag.ms = 0
	log.cleaner.threads = 1
	log.cleanup.policy = [delete]
	log.dir = /tmp/kafka-logs
	log.dirs = /tmp/kafka-logs
	log.flush.interval.messages = 9223372036854775807
	log.flush.interval.ms = null
	log.flush.offset.checkpoint.interval.ms = 60000
	log.flush.scheduler.interval.ms = 9223372036854775807
	log.flush.start.offset.checkpoint.interval.ms = 60000
	log.index.interval.bytes = 4096
	log.index.size.max.bytes = 10485760
	log.message.downconversion.enable = true
	log.message.format.version = 2.7-IV2
	log.message.timestamp.difference.max.ms = 9223372036854775807
	log.message.timestamp.type = CreateTime
	log.preallocate = false
	log.retention.bytes = -1
	log.retention.check.interval.ms = 300000
	log.retention.hours = 168
	log.retention.minutes = null
	log.retention.ms = null
	log.roll.hours = 168
	log.roll.jitter.hours = 0
	log.roll.jitter.ms = null
	log.roll.ms = null
	log.segment.bytes = 1073741824
	log.segment.delete.delay.ms = 60000
	max.connection.creation.rate = 2147483647
	max.connections = 2147483647
	max.connections.per.ip = 2147483647
	max.connections.per.ip.overrides =
	max.incremental.fetch.session.cache.slots = 1000
	message.max.bytes = 1048588
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	min.insync.replicas = 1
	num.io.threads = 8
	num.network.threads = 3
	num.partitions = 1
	num.recovery.threads.per.data.dir = 1
	num.replica.alter.log.dirs.threads = null
	num.replica.fetchers = 1
	offset.metadata.max.bytes = 4096
	offsets.commit.required.acks = -1
	offsets.commit.timeout.ms = 5000
	offsets.load.buffer.size = 5242880
	offsets.retention.check.interval.ms = 600000
	offsets.retention.minutes = 10080
	offsets.topic.compression.codec = 0
	offsets.topic.num.partitions = 50
	offsets.topic.replication.factor = 1
	offsets.topic.segment.bytes = 104857600
	password.encoder.cipher.algorithm = AES/CBC/PKCS5Padding
	password.encoder.iterations = 4096
	password.encoder.key.length = 128
	password.encoder.keyfactory.algorithm = null
	password.encoder.old.secret = null
	password.encoder.secret = null
	port = 9092
	principal.builder.class = null
	producer.purgatory.purge.interval.requests = 1000
	queued.max.request.bytes = -1
	queued.max.requests = 500
	quota.consumer.default = 9223372036854775807
	quota.producer.default = 9223372036854775807
	quota.window.num = 11
	quota.window.size.seconds = 1
	replica.fetch.backoff.ms = 1000
	replica.fetch.max.bytes = 1048576
	replica.fetch.min.bytes = 1
	replica.fetch.response.max.bytes = 10485760
	replica.fetch.wait.max.ms = 500
	replica.high.watermark.checkpoint.interval.ms = 5000
	replica.lag.time.max.ms = 30000
	replica.selector.class = null
	replica.socket.receive.buffer.bytes = 65536
	replica.socket.timeout.ms = 30000
	replication.quota.window.num = 11
	replication.quota.window.size.seconds = 1
	request.timeout.ms = 30000
	reserved.broker.max.id = 1000
	sasl.client.callback.handler.class = null
	sasl.enabled.mechanisms = [GSSAPI]
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.principal.to.local.rules = [DEFAULT]
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism.inter.broker.protocol = GSSAPI
	sasl.server.callback.handler.class = null
	security.inter.broker.protocol = PLAINTEXT
	security.providers = null
	socket.connection.setup.timeout.max.ms = 127000
	socket.connection.setup.timeout.ms = 10000
	socket.receive.buffer.bytes = 102400
	socket.request.max.bytes = 104857600
	socket.send.buffer.bytes = 102400
	ssl.cipher.suites = []
	ssl.client.auth = none
	ssl.enabled.protocols = [TLSv1.2, TLSv1.3]
	ssl.endpoint.identification.algorithm = https
	ssl.engine.factory.class = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.certificate.chain = null
	ssl.keystore.key = null
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.principal.mapping.rules = DEFAULT
	ssl.protocol = TLSv1.3
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.certificates = null
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	transaction.abort.timed.out.transaction.cleanup.interval.ms = 10000
	transaction.max.timeout.ms = 900000
	transaction.remove.expired.transaction.cleanup.interval.ms = 3600000
	transaction.state.log.load.buffer.size = 5242880
	transaction.state.log.min.isr = 1
	transaction.state.log.num.partitions = 50
	transaction.state.log.replication.factor = 1
	transaction.state.log.segment.bytes = 104857600
	transactional.id.expiration.ms = 604800000
	unclean.leader.election.enable = false
	zookeeper.clientCnxnSocket = null
	zookeeper.connect = localhost:2181
	zookeeper.connection.timeout.ms = 18000
	zookeeper.max.in.flight.requests = 10
	zookeeper.session.timeout.ms = 18000
	zookeeper.set.acl = false
	zookeeper.ssl.cipher.suites = null
	zookeeper.ssl.client.enable = false
	zookeeper.ssl.crl.enable = false
	zookeeper.ssl.enabled.protocols = null
	zookeeper.ssl.endpoint.identification.algorithm = HTTPS
	zookeeper.ssl.keystore.location = null
	zookeeper.ssl.keystore.password = null
	zookeeper.ssl.keystore.type = null
	zookeeper.ssl.ocsp.enable = false
	zookeeper.ssl.protocol = TLSv1.2
	zookeeper.ssl.truststore.location = null
	zookeeper.ssl.truststore.password = null
	zookeeper.ssl.truststore.type = null
	zookeeper.sync.time.ms = 2000
 (kafka.server.KafkaConfig)
[2021-03-20 20:14:46,754] INFO KafkaConfig values:
	advertised.host.name = null
	advertised.listeners = null
	advertised.port = null
	alter.config.policy.class.name = null
	alter.log.dirs.replication.quota.window.num = 11
	alter.log.dirs.replication.quota.window.size.seconds = 1
	authorizer.class.name =
	auto.create.topics.enable = true
	auto.leader.rebalance.enable = true
	background.threads = 10
	broker.id = 0
	broker.id.generation.enable = true
	broker.rack = null
	client.quota.callback.class = null
	compression.type = producer
	connection.failed.authentication.delay.ms = 100
	connections.max.idle.ms = 600000
	connections.max.reauth.ms = 0
	control.plane.listener.name = null
	controlled.shutdown.enable = true
	controlled.shutdown.max.retries = 3
	controlled.shutdown.retry.backoff.ms = 5000
	controller.quota.window.num = 11
	controller.quota.window.size.seconds = 1
	controller.socket.timeout.ms = 30000
	create.topic.policy.class.name = null
	default.replication.factor = 1
	delegation.token.expiry.check.interval.ms = 3600000
	delegation.token.expiry.time.ms = 86400000
	delegation.token.master.key = null
	delegation.token.max.lifetime.ms = 604800000
	delete.records.purgatory.purge.interval.requests = 1
	delete.topic.enable = true
	fetch.max.bytes = 57671680
	fetch.purgatory.purge.interval.requests = 1000
	group.initial.rebalance.delay.ms = 0
	group.max.session.timeout.ms = 1800000
	group.max.size = 2147483647
	group.min.session.timeout.ms = 6000
	host.name =
	inter.broker.listener.name = null
	inter.broker.protocol.version = 2.7-IV2
	kafka.metrics.polling.interval.secs = 10
	kafka.metrics.reporters = []
	leader.imbalance.check.interval.seconds = 300
	leader.imbalance.per.broker.percentage = 10
	listener.security.protocol.map = PLAINTEXT:PLAINTEXT,SSL:SSL,SASL_PLAINTEXT:SASL_PLAINTEXT,SASL_SSL:SASL_SSL
	listeners = null
	log.cleaner.backoff.ms = 15000
	log.cleaner.dedupe.buffer.size = 134217728
	log.cleaner.delete.retention.ms = 86400000
	log.cleaner.enable = true
	log.cleaner.io.buffer.load.factor = 0.9
	log.cleaner.io.buffer.size = 524288
	log.cleaner.io.max.bytes.per.second = 1.7976931348623157E308
	log.cleaner.max.compaction.lag.ms = 9223372036854775807
	log.cleaner.min.cleanable.ratio = 0.5
	log.cleaner.min.compaction.lag.ms = 0
	log.cleaner.threads = 1
	log.cleanup.policy = [delete]
	log.dir = /tmp/kafka-logs
	log.dirs = /tmp/kafka-logs
	log.flush.interval.messages = 9223372036854775807
	log.flush.interval.ms = null
	log.flush.offset.checkpoint.interval.ms = 60000
	log.flush.scheduler.interval.ms = 9223372036854775807
	log.flush.start.offset.checkpoint.interval.ms = 60000
	log.index.interval.bytes = 4096
	log.index.size.max.bytes = 10485760
	log.message.downconversion.enable = true
	log.message.format.version = 2.7-IV2
	log.message.timestamp.difference.max.ms = 9223372036854775807
	log.message.timestamp.type = CreateTime
	log.preallocate = false
	log.retention.bytes = -1
	log.retention.check.interval.ms = 300000
	log.retention.hours = 168
	log.retention.minutes = null
	log.retention.ms = null
	log.roll.hours = 168
	log.roll.jitter.hours = 0
	log.roll.jitter.ms = null
	log.roll.ms = null
	log.segment.bytes = 1073741824
	log.segment.delete.delay.ms = 60000
	max.connection.creation.rate = 2147483647
	max.connections = 2147483647
	max.connections.per.ip = 2147483647
	max.connections.per.ip.overrides =
	max.incremental.fetch.session.cache.slots = 1000
	message.max.bytes = 1048588
	metric.reporters = []
	metrics.num.samples = 2
	metrics.recording.level = INFO
	metrics.sample.window.ms = 30000
	min.insync.replicas = 1
	num.io.threads = 8
	num.network.threads = 3
	num.partitions = 1
	num.recovery.threads.per.data.dir = 1
	num.replica.alter.log.dirs.threads = null
	num.replica.fetchers = 1
	offset.metadata.max.bytes = 4096
	offsets.commit.required.acks = -1
	offsets.commit.timeout.ms = 5000
	offsets.load.buffer.size = 5242880
	offsets.retention.check.interval.ms = 600000
	offsets.retention.minutes = 10080
	offsets.topic.compression.codec = 0
	offsets.topic.num.partitions = 50
	offsets.topic.replication.factor = 1
	offsets.topic.segment.bytes = 104857600
	password.encoder.cipher.algorithm = AES/CBC/PKCS5Padding
	password.encoder.iterations = 4096
	password.encoder.key.length = 128
	password.encoder.keyfactory.algorithm = null
	password.encoder.old.secret = null
	password.encoder.secret = null
	port = 9092
	principal.builder.class = null
	producer.purgatory.purge.interval.requests = 1000
	queued.max.request.bytes = -1
	queued.max.requests = 500
	quota.consumer.default = 9223372036854775807
	quota.producer.default = 9223372036854775807
	quota.window.num = 11
	quota.window.size.seconds = 1
	replica.fetch.backoff.ms = 1000
	replica.fetch.max.bytes = 1048576
	replica.fetch.min.bytes = 1
	replica.fetch.response.max.bytes = 10485760
	replica.fetch.wait.max.ms = 500
	replica.high.watermark.checkpoint.interval.ms = 5000
	replica.lag.time.max.ms = 30000
	replica.selector.class = null
	replica.socket.receive.buffer.bytes = 65536
	replica.socket.timeout.ms = 30000
	replication.quota.window.num = 11
	replication.quota.window.size.seconds = 1
	request.timeout.ms = 30000
	reserved.broker.max.id = 1000
	sasl.client.callback.handler.class = null
	sasl.enabled.mechanisms = [GSSAPI]
	sasl.jaas.config = null
	sasl.kerberos.kinit.cmd = /usr/bin/kinit
	sasl.kerberos.min.time.before.relogin = 60000
	sasl.kerberos.principal.to.local.rules = [DEFAULT]
	sasl.kerberos.service.name = null
	sasl.kerberos.ticket.renew.jitter = 0.05
	sasl.kerberos.ticket.renew.window.factor = 0.8
	sasl.login.callback.handler.class = null
	sasl.login.class = null
	sasl.login.refresh.buffer.seconds = 300
	sasl.login.refresh.min.period.seconds = 60
	sasl.login.refresh.window.factor = 0.8
	sasl.login.refresh.window.jitter = 0.05
	sasl.mechanism.inter.broker.protocol = GSSAPI
	sasl.server.callback.handler.class = null
	security.inter.broker.protocol = PLAINTEXT
	security.providers = null
	socket.connection.setup.timeout.max.ms = 127000
	socket.connection.setup.timeout.ms = 10000
	socket.receive.buffer.bytes = 102400
	socket.request.max.bytes = 104857600
	socket.send.buffer.bytes = 102400
	ssl.cipher.suites = []
	ssl.client.auth = none
	ssl.enabled.protocols = [TLSv1.2, TLSv1.3]
	ssl.endpoint.identification.algorithm = https
	ssl.engine.factory.class = null
	ssl.key.password = null
	ssl.keymanager.algorithm = SunX509
	ssl.keystore.certificate.chain = null
	ssl.keystore.key = null
	ssl.keystore.location = null
	ssl.keystore.password = null
	ssl.keystore.type = JKS
	ssl.principal.mapping.rules = DEFAULT
	ssl.protocol = TLSv1.3
	ssl.provider = null
	ssl.secure.random.implementation = null
	ssl.trustmanager.algorithm = PKIX
	ssl.truststore.certificates = null
	ssl.truststore.location = null
	ssl.truststore.password = null
	ssl.truststore.type = JKS
	transaction.abort.timed.out.transaction.cleanup.interval.ms = 10000
	transaction.max.timeout.ms = 900000
	transaction.remove.expired.transaction.cleanup.interval.ms = 3600000
	transaction.state.log.load.buffer.size = 5242880
	transaction.state.log.min.isr = 1
	transaction.state.log.num.partitions = 50
	transaction.state.log.replication.factor = 1
	transaction.state.log.segment.bytes = 104857600
	transactional.id.expiration.ms = 604800000
	unclean.leader.election.enable = false
	zookeeper.clientCnxnSocket = null
	zookeeper.connect = localhost:2181
	zookeeper.connection.timeout.ms = 18000
	zookeeper.max.in.flight.requests = 10
	zookeeper.session.timeout.ms = 18000
	zookeeper.set.acl = false
	zookeeper.ssl.cipher.suites = null
	zookeeper.ssl.client.enable = false
	zookeeper.ssl.crl.enable = false
	zookeeper.ssl.enabled.protocols = null
	zookeeper.ssl.endpoint.identification.algorithm = HTTPS
	zookeeper.ssl.keystore.location = null
	zookeeper.ssl.keystore.password = null
	zookeeper.ssl.keystore.type = null
	zookeeper.ssl.ocsp.enable = false
	zookeeper.ssl.protocol = TLSv1.2
	zookeeper.ssl.truststore.location = null
	zookeeper.ssl.truststore.password = null
	zookeeper.ssl.truststore.type = null
	zookeeper.sync.time.ms = 2000
 (kafka.server.KafkaConfig)
[2021-03-20 20:14:46,789] INFO [ThrottledChannelReaper-Fetch]: Starting (kafka.server.ClientQuotaManager$ThrottledChannelReaper)
[2021-03-20 20:14:46,790] INFO [ThrottledChannelReaper-Produce]: Starting (kafka.server.ClientQuotaManager$ThrottledChannelReaper)
[2021-03-20 20:14:46,791] INFO [ThrottledChannelReaper-Request]: Starting (kafka.server.ClientQuotaManager$ThrottledChannelReaper)
[2021-03-20 20:14:46,792] INFO [ThrottledChannelReaper-ControllerMutation]: Starting (kafka.server.ClientQuotaManager$ThrottledChannelReaper)
[2021-03-20 20:14:46,817] INFO Log directory /tmp/kafka-logs not found, creating it. (kafka.log.LogManager)
[2021-03-20 20:14:46,824] INFO Loading logs from log dirs ArraySeq(/tmp/kafka-logs) (kafka.log.LogManager)
[2021-03-20 20:14:46,826] INFO Attempting recovery for all logs in /tmp/kafka-logs since no clean shutdown file was found (kafka.log.LogManager)
[2021-03-20 20:14:46,831] INFO Loaded 0 logs in 7ms. (kafka.log.LogManager)
[2021-03-20 20:14:46,846] INFO Starting log cleanup with a period of 300000 ms. (kafka.log.LogManager)
[2021-03-20 20:14:46,849] INFO Starting log flusher with a default period of 9223372036854775807 ms. (kafka.log.LogManager)
[2021-03-20 20:14:47,275] INFO Created ConnectionAcceptRate sensor, quotaLimit=2147483647 (kafka.network.ConnectionQuotas)
[2021-03-20 20:14:47,278] INFO Created ConnectionAcceptRate-PLAINTEXT sensor, quotaLimit=2147483647 (kafka.network.ConnectionQuotas)
[2021-03-20 20:14:47,281] INFO Updated PLAINTEXT max connection creation rate to 2147483647 (kafka.network.ConnectionQuotas)
[2021-03-20 20:14:47,284] INFO Awaiting socket connections on 0.0.0.0:9092. (kafka.network.Acceptor)
[2021-03-20 20:14:47,322] INFO [SocketServer brokerId=0] Created data-plane acceptor and processors for endpoint : ListenerName(PLAINTEXT) (kafka.network.SocketServer)
[2021-03-20 20:14:47,360] INFO [ExpirationReaper-0-Produce]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,361] INFO [ExpirationReaper-0-Fetch]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,362] INFO [ExpirationReaper-0-DeleteRecords]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,362] INFO [ExpirationReaper-0-ElectLeader]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,380] INFO [LogDirFailureHandler]: Starting (kafka.server.ReplicaManager$LogDirFailureHandler)
[2021-03-20 20:14:47,380] INFO [broker-0-to-controller-send-thread]: Starting (kafka.server.BrokerToControllerRequestThread)
[2021-03-20 20:14:47,412] INFO Creating /brokers/ids/0 (is it secure? false) (kafka.zk.KafkaZkClient)
[2021-03-20 20:14:47,447] INFO Stat of the created znode at /brokers/ids/0 is: 24,24,1616251487426,1616251487426,1,0,0,72058398798905344,202,0,24
 (kafka.zk.KafkaZkClient)
[2021-03-20 20:14:47,448] INFO Registered broker 0 at path /brokers/ids/0 with addresses: PLAINTEXT://localhost:9092, czxid (broker epoch): 24 (kafka.zk.KafkaZkClient)
[2021-03-20 20:14:47,509] INFO [ExpirationReaper-0-topic]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,513] INFO [ExpirationReaper-0-Heartbeat]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,513] INFO [ExpirationReaper-0-Rebalance]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,526] INFO Successfully created /controller_epoch with initial epoch 0 (kafka.zk.KafkaZkClient)
[2021-03-20 20:14:47,561] INFO [GroupCoordinator 0]: Starting up. (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:14:47,562] INFO [GroupCoordinator 0]: Startup complete. (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:14:47,575] INFO Feature ZK node created at path: /feature (kafka.server.FinalizedFeatureChangeListener)
[2021-03-20 20:14:47,604] INFO [ProducerId Manager 0]: Acquired new producerId block (brokerId:0,blockStartProducerId:0,blockEndProducerId:999) by writing to Zk with path version 1 (kafka.coordinator.transaction.ProducerIdManager)
[2021-03-20 20:14:47,618] INFO [TransactionCoordinator id=0] Starting up. (kafka.coordinator.transaction.TransactionCoordinator)
[2021-03-20 20:14:47,626] INFO [Transaction Marker Channel Manager 0]: Starting (kafka.coordinator.transaction.TransactionMarkerChannelManager)
[2021-03-20 20:14:47,626] INFO [TransactionCoordinator id=0] Startup complete. (kafka.coordinator.transaction.TransactionCoordinator)
[2021-03-20 20:14:47,632] INFO Updated cache from existing <empty> to latest FinalizedFeaturesAndEpoch(features=Features{}, epoch=0). (kafka.server.FinalizedFeatureCache)
[2021-03-20 20:14:47,653] INFO [ExpirationReaper-0-AlterAcls]: Starting (kafka.server.DelayedOperationPurgatory$ExpiredOperationReaper)
[2021-03-20 20:14:47,672] INFO [/config/changes-event-process-thread]: Starting (kafka.common.ZkNodeChangeNotificationListener$ChangeEventProcessThread)
[2021-03-20 20:14:47,680] INFO [SocketServer brokerId=0] Starting socket server acceptors and processors (kafka.network.SocketServer)
[2021-03-20 20:14:47,684] INFO [SocketServer brokerId=0] Started data-plane acceptor and processor(s) for endpoint : ListenerName(PLAINTEXT) (kafka.network.SocketServer)
[2021-03-20 20:14:47,685] INFO [SocketServer brokerId=0] Started socket server acceptors and processors (kafka.network.SocketServer)
[2021-03-20 20:14:47,690] INFO Kafka version: 2.7.0 (org.apache.kafka.common.utils.AppInfoParser)
[2021-03-20 20:14:47,690] INFO Kafka commitId: 448719dc99a19793 (org.apache.kafka.common.utils.AppInfoParser)
[2021-03-20 20:14:47,690] INFO Kafka startTimeMs: 1616251487685 (org.apache.kafka.common.utils.AppInfoParser)
[2021-03-20 20:14:47,692] INFO [KafkaServer id=0] started (kafka.server.KafkaServer)
[2021-03-20 20:14:47,789] INFO [broker-0-to-controller-send-thread]: Recorded new controller, from now on will use broker 0 (kafka.server.BrokerToControllerRequestThread)


[2021-03-20 20:16:36,453] INFO Creating topic my-topic with configuration {} and initial partition assignment HashMap(0 -> ArrayBuffer(0)) (kafka.zk.AdminZkClient)
[2021-03-20 20:16:36,518] INFO [KafkaApi-0] Auto creation of topic my-topic with 1 partitions and replication factor 1 is successful (kafka.server.KafkaApis)
[2021-03-20 20:16:36,624] INFO [ReplicaFetcherManager on broker 0] Removed fetcher for partitions Set(my-topic-0) (kafka.server.ReplicaFetcherManager)
[2021-03-20 20:16:36,679] INFO [Log partition=my-topic-0, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:16:36,690] INFO Created log for partition my-topic-0 in /tmp/kafka-logs/my-topic-0 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> [delete], flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 1073741824, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:16:36,691] INFO [Partition my-topic-0 broker=0] No checkpointed highwatermark is found for partition my-topic-0 (kafka.cluster.Partition)
[2021-03-20 20:16:36,692] INFO [Partition my-topic-0 broker=0] Log loaded for partition my-topic-0 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:36,712] INFO Creating topic __consumer_offsets with configuration {compression.type=producer, cleanup.policy=compact, segment.bytes=104857600} and initial partition assignment HashMap(0 -> ArrayBuffer(0), 1 -> ArrayBuffer(0), 2 -> ArrayBuffer(0), 3 -> ArrayBuffer(0), 4 -> ArrayBuffer(0), 5 -> ArrayBuffer(0), 6 -> ArrayBuffer(0), 7 -> ArrayBuffer(0), 8 -> ArrayBuffer(0), 9 -> ArrayBuffer(0), 10 -> ArrayBuffer(0), 11 -> ArrayBuffer(0), 12 -> ArrayBuffer(0), 13 -> ArrayBuffer(0), 14 -> ArrayBuffer(0), 15 -> ArrayBuffer(0), 16 -> ArrayBuffer(0), 17 -> ArrayBuffer(0), 18 -> ArrayBuffer(0), 19 -> ArrayBuffer(0), 20 -> ArrayBuffer(0), 21 -> ArrayBuffer(0), 22 -> ArrayBuffer(0), 23 -> ArrayBuffer(0), 24 -> ArrayBuffer(0), 25 -> ArrayBuffer(0), 26 -> ArrayBuffer(0), 27 -> ArrayBuffer(0), 28 -> ArrayBuffer(0), 29 -> ArrayBuffer(0), 30 -> ArrayBuffer(0), 31 -> ArrayBuffer(0), 32 -> ArrayBuffer(0), 33 -> ArrayBuffer(0), 34 -> ArrayBuffer(0), 35 -> ArrayBuffer(0), 36 -> ArrayBuffer(0), 37 -> ArrayBuffer(0), 38 -> ArrayBuffer(0), 39 -> ArrayBuffer(0), 40 -> ArrayBuffer(0), 41 -> ArrayBuffer(0), 42 -> ArrayBuffer(0), 43 -> ArrayBuffer(0), 44 -> ArrayBuffer(0), 45 -> ArrayBuffer(0), 46 -> ArrayBuffer(0), 47 -> ArrayBuffer(0), 48 -> ArrayBuffer(0), 49 -> ArrayBuffer(0)) (kafka.zk.AdminZkClient)
[2021-03-20 20:19:36,760] INFO [KafkaApi-0] Auto creation of topic __consumer_offsets with 50 partitions and replication factor 1 is successful (kafka.server.KafkaApis)
[2021-03-20 20:19:37,223] INFO [ReplicaFetcherManager on broker 0] Removed fetcher for partitions HashSet(__consumer_offsets-22, __consumer_offsets-30, __consumer_offsets-25, __consumer_offsets-35, __consumer_offsets-37, __consumer_offsets-38, __consumer_offsets-13, __consumer_offsets-8, __consumer_offsets-21, __consumer_offsets-4, __consumer_offsets-27, __consumer_offsets-7, __consumer_offsets-9, __consumer_offsets-46, __consumer_offsets-41, __consumer_offsets-33, __consumer_offsets-23, __consumer_offsets-49, __consumer_offsets-47, __consumer_offsets-16, __consumer_offsets-28, __consumer_offsets-31, __consumer_offsets-36, __consumer_offsets-42, __consumer_offsets-3, __consumer_offsets-18, __consumer_offsets-15, __consumer_offsets-24, __consumer_offsets-17, __consumer_offsets-48, __consumer_offsets-19, __consumer_offsets-11, __consumer_offsets-2, __consumer_offsets-43, __consumer_offsets-6, __consumer_offsets-14, __consumer_offsets-20, __consumer_offsets-0, __consumer_offsets-44, __consumer_offsets-39, __consumer_offsets-12, __consumer_offsets-45, __consumer_offsets-1, __consumer_offsets-5, __consumer_offsets-26, __consumer_offsets-29, __consumer_offsets-34, __consumer_offsets-10, __consumer_offsets-32, __consumer_offsets-40) (kafka.server.ReplicaFetcherManager)
[2021-03-20 20:19:37,228] INFO [Log partition=__consumer_offsets-3, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,229] INFO Created log for partition __consumer_offsets-3 in /tmp/kafka-logs/__consumer_offsets-3 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,231] INFO [Partition __consumer_offsets-3 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-3 (kafka.cluster.Partition)
[2021-03-20 20:19:37,231] INFO [Partition __consumer_offsets-3 broker=0] Log loaded for partition __consumer_offsets-3 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,239] INFO [Log partition=__consumer_offsets-18, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,240] INFO Created log for partition __consumer_offsets-18 in /tmp/kafka-logs/__consumer_offsets-18 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,241] INFO [Partition __consumer_offsets-18 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-18 (kafka.cluster.Partition)
[2021-03-20 20:19:37,241] INFO [Partition __consumer_offsets-18 broker=0] Log loaded for partition __consumer_offsets-18 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,245] INFO [Log partition=__consumer_offsets-41, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,246] INFO Created log for partition __consumer_offsets-41 in /tmp/kafka-logs/__consumer_offsets-41 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,246] INFO [Partition __consumer_offsets-41 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-41 (kafka.cluster.Partition)
[2021-03-20 20:19:37,246] INFO [Partition __consumer_offsets-41 broker=0] Log loaded for partition __consumer_offsets-41 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,250] INFO [Log partition=__consumer_offsets-10, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,251] INFO Created log for partition __consumer_offsets-10 in /tmp/kafka-logs/__consumer_offsets-10 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,251] INFO [Partition __consumer_offsets-10 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-10 (kafka.cluster.Partition)
[2021-03-20 20:19:37,251] INFO [Partition __consumer_offsets-10 broker=0] Log loaded for partition __consumer_offsets-10 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,255] INFO [Log partition=__consumer_offsets-33, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,256] INFO Created log for partition __consumer_offsets-33 in /tmp/kafka-logs/__consumer_offsets-33 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,256] INFO [Partition __consumer_offsets-33 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-33 (kafka.cluster.Partition)
[2021-03-20 20:19:37,256] INFO [Partition __consumer_offsets-33 broker=0] Log loaded for partition __consumer_offsets-33 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,260] INFO [Log partition=__consumer_offsets-48, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,261] INFO Created log for partition __consumer_offsets-48 in /tmp/kafka-logs/__consumer_offsets-48 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,261] INFO [Partition __consumer_offsets-48 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-48 (kafka.cluster.Partition)
[2021-03-20 20:19:37,261] INFO [Partition __consumer_offsets-48 broker=0] Log loaded for partition __consumer_offsets-48 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,265] INFO [Log partition=__consumer_offsets-19, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,267] INFO Created log for partition __consumer_offsets-19 in /tmp/kafka-logs/__consumer_offsets-19 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,267] INFO [Partition __consumer_offsets-19 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-19 (kafka.cluster.Partition)
[2021-03-20 20:19:37,267] INFO [Partition __consumer_offsets-19 broker=0] Log loaded for partition __consumer_offsets-19 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,272] INFO [Log partition=__consumer_offsets-34, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,274] INFO Created log for partition __consumer_offsets-34 in /tmp/kafka-logs/__consumer_offsets-34 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,274] INFO [Partition __consumer_offsets-34 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-34 (kafka.cluster.Partition)
[2021-03-20 20:19:37,274] INFO [Partition __consumer_offsets-34 broker=0] Log loaded for partition __consumer_offsets-34 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,278] INFO [Log partition=__consumer_offsets-4, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,279] INFO Created log for partition __consumer_offsets-4 in /tmp/kafka-logs/__consumer_offsets-4 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,279] INFO [Partition __consumer_offsets-4 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-4 (kafka.cluster.Partition)
[2021-03-20 20:19:37,279] INFO [Partition __consumer_offsets-4 broker=0] Log loaded for partition __consumer_offsets-4 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,283] INFO [Log partition=__consumer_offsets-11, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,284] INFO Created log for partition __consumer_offsets-11 in /tmp/kafka-logs/__consumer_offsets-11 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,284] INFO [Partition __consumer_offsets-11 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-11 (kafka.cluster.Partition)
[2021-03-20 20:19:37,284] INFO [Partition __consumer_offsets-11 broker=0] Log loaded for partition __consumer_offsets-11 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,288] INFO [Log partition=__consumer_offsets-26, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,289] INFO Created log for partition __consumer_offsets-26 in /tmp/kafka-logs/__consumer_offsets-26 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,289] INFO [Partition __consumer_offsets-26 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-26 (kafka.cluster.Partition)
[2021-03-20 20:19:37,289] INFO [Partition __consumer_offsets-26 broker=0] Log loaded for partition __consumer_offsets-26 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,293] INFO [Log partition=__consumer_offsets-49, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,294] INFO Created log for partition __consumer_offsets-49 in /tmp/kafka-logs/__consumer_offsets-49 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,294] INFO [Partition __consumer_offsets-49 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-49 (kafka.cluster.Partition)
[2021-03-20 20:19:37,295] INFO [Partition __consumer_offsets-49 broker=0] Log loaded for partition __consumer_offsets-49 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,298] INFO [Log partition=__consumer_offsets-39, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,299] INFO Created log for partition __consumer_offsets-39 in /tmp/kafka-logs/__consumer_offsets-39 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,299] INFO [Partition __consumer_offsets-39 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-39 (kafka.cluster.Partition)
[2021-03-20 20:19:37,299] INFO [Partition __consumer_offsets-39 broker=0] Log loaded for partition __consumer_offsets-39 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,303] INFO [Log partition=__consumer_offsets-9, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,304] INFO Created log for partition __consumer_offsets-9 in /tmp/kafka-logs/__consumer_offsets-9 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,304] INFO [Partition __consumer_offsets-9 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-9 (kafka.cluster.Partition)
[2021-03-20 20:19:37,304] INFO [Partition __consumer_offsets-9 broker=0] Log loaded for partition __consumer_offsets-9 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,307] INFO [Log partition=__consumer_offsets-24, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,308] INFO Created log for partition __consumer_offsets-24 in /tmp/kafka-logs/__consumer_offsets-24 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,308] INFO [Partition __consumer_offsets-24 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-24 (kafka.cluster.Partition)
[2021-03-20 20:19:37,308] INFO [Partition __consumer_offsets-24 broker=0] Log loaded for partition __consumer_offsets-24 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,312] INFO [Log partition=__consumer_offsets-31, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,313] INFO Created log for partition __consumer_offsets-31 in /tmp/kafka-logs/__consumer_offsets-31 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,313] INFO [Partition __consumer_offsets-31 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-31 (kafka.cluster.Partition)
[2021-03-20 20:19:37,313] INFO [Partition __consumer_offsets-31 broker=0] Log loaded for partition __consumer_offsets-31 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,318] INFO [Log partition=__consumer_offsets-46, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,319] INFO Created log for partition __consumer_offsets-46 in /tmp/kafka-logs/__consumer_offsets-46 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,319] INFO [Partition __consumer_offsets-46 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-46 (kafka.cluster.Partition)
[2021-03-20 20:19:37,319] INFO [Partition __consumer_offsets-46 broker=0] Log loaded for partition __consumer_offsets-46 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,324] INFO [Log partition=__consumer_offsets-1, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,325] INFO Created log for partition __consumer_offsets-1 in /tmp/kafka-logs/__consumer_offsets-1 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,325] INFO [Partition __consumer_offsets-1 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-1 (kafka.cluster.Partition)
[2021-03-20 20:19:37,325] INFO [Partition __consumer_offsets-1 broker=0] Log loaded for partition __consumer_offsets-1 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,329] INFO [Log partition=__consumer_offsets-16, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,330] INFO Created log for partition __consumer_offsets-16 in /tmp/kafka-logs/__consumer_offsets-16 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,330] INFO [Partition __consumer_offsets-16 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-16 (kafka.cluster.Partition)
[2021-03-20 20:19:37,330] INFO [Partition __consumer_offsets-16 broker=0] Log loaded for partition __consumer_offsets-16 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,334] INFO [Log partition=__consumer_offsets-2, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,335] INFO Created log for partition __consumer_offsets-2 in /tmp/kafka-logs/__consumer_offsets-2 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,335] INFO [Partition __consumer_offsets-2 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-2 (kafka.cluster.Partition)
[2021-03-20 20:19:37,335] INFO [Partition __consumer_offsets-2 broker=0] Log loaded for partition __consumer_offsets-2 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,339] INFO [Log partition=__consumer_offsets-25, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,340] INFO Created log for partition __consumer_offsets-25 in /tmp/kafka-logs/__consumer_offsets-25 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,340] INFO [Partition __consumer_offsets-25 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-25 (kafka.cluster.Partition)
[2021-03-20 20:19:37,340] INFO [Partition __consumer_offsets-25 broker=0] Log loaded for partition __consumer_offsets-25 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,344] INFO [Log partition=__consumer_offsets-40, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,345] INFO Created log for partition __consumer_offsets-40 in /tmp/kafka-logs/__consumer_offsets-40 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,345] INFO [Partition __consumer_offsets-40 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-40 (kafka.cluster.Partition)
[2021-03-20 20:19:37,345] INFO [Partition __consumer_offsets-40 broker=0] Log loaded for partition __consumer_offsets-40 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,348] INFO [Log partition=__consumer_offsets-47, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,349] INFO Created log for partition __consumer_offsets-47 in /tmp/kafka-logs/__consumer_offsets-47 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,350] INFO [Partition __consumer_offsets-47 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-47 (kafka.cluster.Partition)
[2021-03-20 20:19:37,350] INFO [Partition __consumer_offsets-47 broker=0] Log loaded for partition __consumer_offsets-47 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,353] INFO [Log partition=__consumer_offsets-17, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,354] INFO Created log for partition __consumer_offsets-17 in /tmp/kafka-logs/__consumer_offsets-17 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,354] INFO [Partition __consumer_offsets-17 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-17 (kafka.cluster.Partition)
[2021-03-20 20:19:37,354] INFO [Partition __consumer_offsets-17 broker=0] Log loaded for partition __consumer_offsets-17 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,358] INFO [Log partition=__consumer_offsets-32, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,359] INFO Created log for partition __consumer_offsets-32 in /tmp/kafka-logs/__consumer_offsets-32 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,359] INFO [Partition __consumer_offsets-32 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-32 (kafka.cluster.Partition)
[2021-03-20 20:19:37,359] INFO [Partition __consumer_offsets-32 broker=0] Log loaded for partition __consumer_offsets-32 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,363] INFO [Log partition=__consumer_offsets-37, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,365] INFO Created log for partition __consumer_offsets-37 in /tmp/kafka-logs/__consumer_offsets-37 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,365] INFO [Partition __consumer_offsets-37 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-37 (kafka.cluster.Partition)
[2021-03-20 20:19:37,365] INFO [Partition __consumer_offsets-37 broker=0] Log loaded for partition __consumer_offsets-37 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,369] INFO [Log partition=__consumer_offsets-7, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,371] INFO Created log for partition __consumer_offsets-7 in /tmp/kafka-logs/__consumer_offsets-7 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,371] INFO [Partition __consumer_offsets-7 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-7 (kafka.cluster.Partition)
[2021-03-20 20:19:37,371] INFO [Partition __consumer_offsets-7 broker=0] Log loaded for partition __consumer_offsets-7 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,376] INFO [Log partition=__consumer_offsets-22, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,377] INFO Created log for partition __consumer_offsets-22 in /tmp/kafka-logs/__consumer_offsets-22 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,378] INFO [Partition __consumer_offsets-22 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-22 (kafka.cluster.Partition)
[2021-03-20 20:19:37,378] INFO [Partition __consumer_offsets-22 broker=0] Log loaded for partition __consumer_offsets-22 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,381] INFO [Log partition=__consumer_offsets-29, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,382] INFO Created log for partition __consumer_offsets-29 in /tmp/kafka-logs/__consumer_offsets-29 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,382] INFO [Partition __consumer_offsets-29 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-29 (kafka.cluster.Partition)
[2021-03-20 20:19:37,382] INFO [Partition __consumer_offsets-29 broker=0] Log loaded for partition __consumer_offsets-29 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,386] INFO [Log partition=__consumer_offsets-44, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,387] INFO Created log for partition __consumer_offsets-44 in /tmp/kafka-logs/__consumer_offsets-44 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,387] INFO [Partition __consumer_offsets-44 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-44 (kafka.cluster.Partition)
[2021-03-20 20:19:37,387] INFO [Partition __consumer_offsets-44 broker=0] Log loaded for partition __consumer_offsets-44 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,391] INFO [Log partition=__consumer_offsets-14, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,392] INFO Created log for partition __consumer_offsets-14 in /tmp/kafka-logs/__consumer_offsets-14 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,392] INFO [Partition __consumer_offsets-14 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-14 (kafka.cluster.Partition)
[2021-03-20 20:19:37,392] INFO [Partition __consumer_offsets-14 broker=0] Log loaded for partition __consumer_offsets-14 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,395] INFO [Log partition=__consumer_offsets-23, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,396] INFO Created log for partition __consumer_offsets-23 in /tmp/kafka-logs/__consumer_offsets-23 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,397] INFO [Partition __consumer_offsets-23 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-23 (kafka.cluster.Partition)
[2021-03-20 20:19:37,397] INFO [Partition __consumer_offsets-23 broker=0] Log loaded for partition __consumer_offsets-23 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,401] INFO [Log partition=__consumer_offsets-38, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,402] INFO Created log for partition __consumer_offsets-38 in /tmp/kafka-logs/__consumer_offsets-38 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,402] INFO [Partition __consumer_offsets-38 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-38 (kafka.cluster.Partition)
[2021-03-20 20:19:37,402] INFO [Partition __consumer_offsets-38 broker=0] Log loaded for partition __consumer_offsets-38 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,405] INFO [Log partition=__consumer_offsets-8, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,406] INFO Created log for partition __consumer_offsets-8 in /tmp/kafka-logs/__consumer_offsets-8 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,407] INFO [Partition __consumer_offsets-8 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-8 (kafka.cluster.Partition)
[2021-03-20 20:19:37,407] INFO [Partition __consumer_offsets-8 broker=0] Log loaded for partition __consumer_offsets-8 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,410] INFO [Log partition=__consumer_offsets-45, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,411] INFO Created log for partition __consumer_offsets-45 in /tmp/kafka-logs/__consumer_offsets-45 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,411] INFO [Partition __consumer_offsets-45 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-45 (kafka.cluster.Partition)
[2021-03-20 20:19:37,411] INFO [Partition __consumer_offsets-45 broker=0] Log loaded for partition __consumer_offsets-45 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,417] INFO [Log partition=__consumer_offsets-15, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,418] INFO Created log for partition __consumer_offsets-15 in /tmp/kafka-logs/__consumer_offsets-15 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,418] INFO [Partition __consumer_offsets-15 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-15 (kafka.cluster.Partition)
[2021-03-20 20:19:37,418] INFO [Partition __consumer_offsets-15 broker=0] Log loaded for partition __consumer_offsets-15 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,422] INFO [Log partition=__consumer_offsets-30, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,423] INFO Created log for partition __consumer_offsets-30 in /tmp/kafka-logs/__consumer_offsets-30 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,423] INFO [Partition __consumer_offsets-30 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-30 (kafka.cluster.Partition)
[2021-03-20 20:19:37,423] INFO [Partition __consumer_offsets-30 broker=0] Log loaded for partition __consumer_offsets-30 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,427] INFO [Log partition=__consumer_offsets-0, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,428] INFO Created log for partition __consumer_offsets-0 in /tmp/kafka-logs/__consumer_offsets-0 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,428] INFO [Partition __consumer_offsets-0 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,428] INFO [Partition __consumer_offsets-0 broker=0] Log loaded for partition __consumer_offsets-0 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,432] INFO [Log partition=__consumer_offsets-35, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,433] INFO Created log for partition __consumer_offsets-35 in /tmp/kafka-logs/__consumer_offsets-35 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,433] INFO [Partition __consumer_offsets-35 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-35 (kafka.cluster.Partition)
[2021-03-20 20:19:37,433] INFO [Partition __consumer_offsets-35 broker=0] Log loaded for partition __consumer_offsets-35 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,437] INFO [Log partition=__consumer_offsets-5, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,438] INFO Created log for partition __consumer_offsets-5 in /tmp/kafka-logs/__consumer_offsets-5 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,438] INFO [Partition __consumer_offsets-5 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-5 (kafka.cluster.Partition)
[2021-03-20 20:19:37,438] INFO [Partition __consumer_offsets-5 broker=0] Log loaded for partition __consumer_offsets-5 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,441] INFO [Log partition=__consumer_offsets-20, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,442] INFO Created log for partition __consumer_offsets-20 in /tmp/kafka-logs/__consumer_offsets-20 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,442] INFO [Partition __consumer_offsets-20 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-20 (kafka.cluster.Partition)
[2021-03-20 20:19:37,442] INFO [Partition __consumer_offsets-20 broker=0] Log loaded for partition __consumer_offsets-20 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,446] INFO [Log partition=__consumer_offsets-27, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,447] INFO Created log for partition __consumer_offsets-27 in /tmp/kafka-logs/__consumer_offsets-27 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,447] INFO [Partition __consumer_offsets-27 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-27 (kafka.cluster.Partition)
[2021-03-20 20:19:37,447] INFO [Partition __consumer_offsets-27 broker=0] Log loaded for partition __consumer_offsets-27 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,451] INFO [Log partition=__consumer_offsets-42, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,451] INFO Created log for partition __consumer_offsets-42 in /tmp/kafka-logs/__consumer_offsets-42 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,451] INFO [Partition __consumer_offsets-42 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-42 (kafka.cluster.Partition)
[2021-03-20 20:19:37,452] INFO [Partition __consumer_offsets-42 broker=0] Log loaded for partition __consumer_offsets-42 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,455] INFO [Log partition=__consumer_offsets-12, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,456] INFO Created log for partition __consumer_offsets-12 in /tmp/kafka-logs/__consumer_offsets-12 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,456] INFO [Partition __consumer_offsets-12 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-12 (kafka.cluster.Partition)
[2021-03-20 20:19:37,456] INFO [Partition __consumer_offsets-12 broker=0] Log loaded for partition __consumer_offsets-12 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,459] INFO [Log partition=__consumer_offsets-21, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,460] INFO Created log for partition __consumer_offsets-21 in /tmp/kafka-logs/__consumer_offsets-21 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,460] INFO [Partition __consumer_offsets-21 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-21 (kafka.cluster.Partition)
[2021-03-20 20:19:37,460] INFO [Partition __consumer_offsets-21 broker=0] Log loaded for partition __consumer_offsets-21 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,465] INFO [Log partition=__consumer_offsets-36, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,466] INFO Created log for partition __consumer_offsets-36 in /tmp/kafka-logs/__consumer_offsets-36 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,466] INFO [Partition __consumer_offsets-36 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-36 (kafka.cluster.Partition)
[2021-03-20 20:19:37,466] INFO [Partition __consumer_offsets-36 broker=0] Log loaded for partition __consumer_offsets-36 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,470] INFO [Log partition=__consumer_offsets-6, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,471] INFO Created log for partition __consumer_offsets-6 in /tmp/kafka-logs/__consumer_offsets-6 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,472] INFO [Partition __consumer_offsets-6 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-6 (kafka.cluster.Partition)
[2021-03-20 20:19:37,472] INFO [Partition __consumer_offsets-6 broker=0] Log loaded for partition __consumer_offsets-6 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,476] INFO [Log partition=__consumer_offsets-43, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,478] INFO Created log for partition __consumer_offsets-43 in /tmp/kafka-logs/__consumer_offsets-43 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,478] INFO [Partition __consumer_offsets-43 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-43 (kafka.cluster.Partition)
[2021-03-20 20:19:37,478] INFO [Partition __consumer_offsets-43 broker=0] Log loaded for partition __consumer_offsets-43 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,482] INFO [Log partition=__consumer_offsets-13, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,483] INFO Created log for partition __consumer_offsets-13 in /tmp/kafka-logs/__consumer_offsets-13 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,483] INFO [Partition __consumer_offsets-13 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-13 (kafka.cluster.Partition)
[2021-03-20 20:19:37,483] INFO [Partition __consumer_offsets-13 broker=0] Log loaded for partition __consumer_offsets-13 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,486] INFO [Log partition=__consumer_offsets-28, dir=/tmp/kafka-logs] Loading producer state till offset 0 with message format version 2 (kafka.log.Log)
[2021-03-20 20:19:37,487] INFO Created log for partition __consumer_offsets-28 in /tmp/kafka-logs/__consumer_offsets-28 with properties {compression.type -> producer, min.insync.replicas -> 1, message.downconversion.enable -> true, segment.jitter.ms -> 0, cleanup.policy -> compact, flush.ms -> 9223372036854775807, retention.ms -> 604800000, segment.bytes -> 104857600, flush.messages -> 9223372036854775807, message.format.version -> 2.7-IV2, max.compaction.lag.ms -> 9223372036854775807, file.delete.delay.ms -> 60000, max.message.bytes -> 1048588, min.compaction.lag.ms -> 0, message.timestamp.type -> CreateTime, preallocate -> false, index.interval.bytes -> 4096, min.cleanable.dirty.ratio -> 0.5, unclean.leader.election.enable -> false, retention.bytes -> -1, delete.retention.ms -> 86400000, segment.ms -> 604800000, message.timestamp.difference.max.ms -> 9223372036854775807, segment.index.bytes -> 10485760}. (kafka.log.LogManager)
[2021-03-20 20:19:37,487] INFO [Partition __consumer_offsets-28 broker=0] No checkpointed highwatermark is found for partition __consumer_offsets-28 (kafka.cluster.Partition)
[2021-03-20 20:19:37,487] INFO [Partition __consumer_offsets-28 broker=0] Log loaded for partition __consumer_offsets-28 with initial high watermark 0 (kafka.cluster.Partition)
[2021-03-20 20:19:37,490] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-3 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-18 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-41 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-10 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-33 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-48 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-19 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,491] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-34 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-4 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-11 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-26 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-49 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-39 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-9 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-24 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-31 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-46 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-1 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-16 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-2 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-25 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-40 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-47 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-17 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,492] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-32 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-37 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-7 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-22 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-29 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-44 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-14 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-23 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-38 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-8 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-45 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-15 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-30 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-0 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-35 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-5 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-20 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-27 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-42 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-12 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-21 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-36 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,493] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-6 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,494] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-43 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,494] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-13 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,494] INFO [GroupMetadataManager brokerId=0] Scheduling loading of offsets and group metadata from __consumer_offsets-28 (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,495] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-3 in 5 milliseconds, of which 1 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-18 in 5 milliseconds, of which 4 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-41 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-10 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-33 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-48 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-19 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-34 in 4 milliseconds, of which 4 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,496] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-4 in 4 milliseconds, of which 4 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,497] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-11 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,497] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-26 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,497] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-49 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,497] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-39 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,497] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-9 in 5 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,498] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-24 in 6 milliseconds, of which 5 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,498] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-31 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,498] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-46 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,498] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-1 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,498] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-16 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,498] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-2 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-25 in 7 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-40 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-47 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-17 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-32 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-37 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-7 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,499] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-22 in 6 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-29 in 7 milliseconds, of which 6 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-44 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-14 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-23 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-38 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-8 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-45 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,500] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-15 in 7 milliseconds, of which 7 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,501] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-30 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,501] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-0 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,501] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-35 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,501] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-5 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,501] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-20 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,501] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-27 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-42 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-12 in 9 milliseconds, of which 9 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-21 in 9 milliseconds, of which 9 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-36 in 9 milliseconds, of which 9 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-6 in 9 milliseconds, of which 9 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-43 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,502] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-13 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,503] INFO [GroupMetadataManager brokerId=0] Finished loading offsets and group metadata from __consumer_offsets-28 in 8 milliseconds, of which 8 milliseconds was spent in the scheduler. (kafka.coordinator.group.GroupMetadataManager)
[2021-03-20 20:19:37,613] INFO [GroupCoordinator 0]: Preparing to rebalance group console-consumer-99278 in state PreparingRebalance with old generation 0 (__consumer_offsets-46) (reason: Adding new member consumer-console-consumer-99278-1-74f45d58-3bf9-4363-a305-740a9bbbb9c0 with group instance id None) (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:19:37,620] INFO [GroupCoordinator 0]: Stabilized group console-consumer-99278 generation 1 (__consumer_offsets-46) (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:19:37,628] INFO [GroupCoordinator 0]: Assignment received from leader for group console-consumer-99278 for generation 1 (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:19:40,143] INFO [GroupCoordinator 0]: Member[group.instance.id None, member.id consumer-console-consumer-99278-1-74f45d58-3bf9-4363-a305-740a9bbbb9c0] in group console-consumer-99278 has left, removing it from the group (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:19:40,143] INFO [GroupCoordinator 0]: Preparing to rebalance group console-consumer-99278 in state PreparingRebalance with old generation 1 (__consumer_offsets-46) (reason: removing member consumer-console-consumer-99278-1-74f45d58-3bf9-4363-a305-740a9bbbb9c0 on LeaveGroup) (kafka.coordinator.group.GroupCoordinator)
[2021-03-20 20:19:40,144] INFO [GroupCoordinator 0]: Group console-consumer-99278 with generation 2 is now empty (__consumer_offsets-46) (kafka.coordinator.group.GroupCoordinator)
```

I know. Lots of logs. 😅

I ran the program in IntelliJ using run feature

```bash
8:16:32 PM: Executing task 'KafkaProtobufProducerApp.main()'...

> Task :app:extractIncludeProto
> Task :app:extractProto UP-TO-DATE
> Task :app:generateProto NO-SOURCE
> Task :app:compileJava
> Task :app:processResources NO-SOURCE
> Task :app:classes

> Task :app:KafkaProtobufProducerApp.main()

BUILD SUCCESSFUL in 4s
4 actionable tasks: 3 executed, 1 up-to-date
SLF4J: Failed to load class "org.slf4j.impl.StaticLoggerBinder".
SLF4J: Defaulting to no-operation (NOP) logger implementation
SLF4J: See http://www.slf4j.org/codes.html#StaticLoggerBinder for further details.
8:16:36 PM: Task execution finished 'KafkaProtobufProducerApp.main()'.
```

I'm going to try to consume from `my-topic` kafka topic. Also, I'm wondering
how the topic exists or if it exists. Maybe it does exist. Let's see ! :)

```bash
$ ./bin/kafka-console-consumer.sh -h
h is not a recognized option
Option                                   Description
------                                   -----------
--bootstrap-server <String: server to    REQUIRED: The server(s) to connect to.
  connect to>
--consumer-property <String:             A mechanism to pass user-defined
  consumer_prop>                           properties in the form key=value to
                                           the consumer.
--consumer.config <String: config file>  Consumer config properties file. Note
                                           that [consumer-property] takes
                                           precedence over this config.
--enable-systest-events                  Log lifecycle events of the consumer
                                           in addition to logging consumed
                                           messages. (This is specific for
                                           system tests.)
--formatter <String: class>              The name of a class to use for
                                           formatting kafka messages for
                                           display. (default: kafka.tools.
                                           DefaultMessageFormatter)
--from-beginning                         If the consumer does not already have
                                           an established offset to consume
                                           from, start with the earliest
                                           message present in the log rather
                                           than the latest message.
--group <String: consumer group id>      The consumer group id of the consumer.
--help                                   Print usage information.
--isolation-level <String>               Set to read_committed in order to
                                           filter out transactional messages
                                           which are not committed. Set to
                                           read_uncommitted to read all
                                           messages. (default: read_uncommitted)
--key-deserializer <String:
  deserializer for key>
--max-messages <Integer: num_messages>   The maximum number of messages to
                                           consume before exiting. If not set,
                                           consumption is continual.
--offset <String: consume offset>        The offset id to consume from (a non-
                                           negative number), or 'earliest'
                                           which means from beginning, or
                                           'latest' which means from end
                                           (default: latest)
--partition <Integer: partition>         The partition to consume from.
                                           Consumption starts from the end of
                                           the partition unless '--offset' is
                                           specified.
--property <String: prop>                The properties to initialize the
                                           message formatter. Default
                                           properties include:
                                          print.timestamp=true|false
                                          print.key=true|false
                                          print.offset=true|false
                                          print.partition=true|false
                                          print.headers=true|false
                                          print.value=true|false
                                          key.separator=<key.separator>
                                          line.separator=<line.separator>
                                          headers.separator=<line.separator>
                                          null.literal=<null.literal>
                                          key.deserializer=<key.deserializer>
                                          value.deserializer=<value.
                                           deserializer>
                                          header.deserializer=<header.
                                           deserializer>
                                         Users can also pass in customized
                                           properties for their formatter; more
                                           specifically, users can pass in
                                           properties keyed with 'key.
                                           deserializer.', 'value.
                                           deserializer.' and 'headers.
                                           deserializer.' prefixes to configure
                                           their deserializers.
--skip-message-on-error                  If there is an error when processing a
                                           message, skip it instead of halt.
--timeout-ms <Integer: timeout_ms>       If specified, exit if no message is
                                           available for consumption for the
                                           specified interval.
--topic <String: topic>                  The topic id to consume on.
--value-deserializer <String:
  deserializer for values>
--version                                Display Kafka version.
--whitelist <String: whitelist>          Regular expression specifying
                                           whitelist of topics to include for
                                           consumption.

```

```bash
$ ./bin/kafka-console-consumer.sh \
> --bootstrap-server localhost:9092 \
> --topic my-topic \
> --from-beginning
0
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99


^CProcessed a total of 100 messages
```

Coooool! So, it worked!! :D

Next I need to send protocol buffer messages, hmm

Btw

- Import a Kafka client library - DONE

Also, I have also done some more steps like

- Run Zookeeper and Kafka locally

But I need to keep doing them again and again ;) :P

Also, the below two too

- Run my app to produce some dummy data
- Consume the dummy data using the kafka protobuf consumer

I ran a console consumer though, instead of a protobuf one. Soon! :)

Now, how to control or customize the serializer, hmm. Specifically the value
serializer. Key can always be string. :) At least in my case.

https://duckduckgo.com/?t=ffab&q=java%3A+kafka+send+protobuf+message&ia=web

https://dzone.com/articles/how-to-use-protobuf-with-apache-kafka-and-schema-r

https://www.vijaykonnackal.com/protobuf-kafka-message/

https://docs.confluent.io/platform/current/schema-registry/serdes-develop/serdes-protobuf.html

Looks like I don't need to do much to produce a protobuf message as Kafka
supports binary messages by default! :D Let's see how that works!

I noticed this here -

https://www.vijaykonnackal.com/protobuf-kafka-message/

It says

```
The producer side of things is easy. All messages on Kafka are binary is a
direct match to protobuf. However, there is a challenge on the consumer side.
Unlike JSON which is self describing format, a protobuf message cannot be
de-serialized without prior knowledge of the message type. So it is imperative
that additional metadata is included by the producer to describe the message.

Similar to HTTP headers, Kafka supports message headers that can carry any
metadata. The below snippets outline a strategy to serialize and de-serialize
Protobuf messages.
```

But the code, it uses something called `KafkaTemplate` which is a Spring thing
and I'm using plain kafka client java libray

https://duckduckgo.com/?t=ffab&q=apache+kafka+template+java&ia=web

https://docs.spring.io/spring-kafka/api/org/springframework/kafka/core/KafkaTemplate.html

So, I need to check how to use protocol buffers as messages, hmm

I think I need to check for what the serializer will be for the value so that
I can use it in this line

```java
props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
```

I checked the code for the `StringSerializer` and found it here -

`/Users/karuppiahn/.gradle/caches/modules-2/files-2.1/org.apache.kafka/kafka-clients/2.7.0/cf59e01c5f72438a227c0bd3feec183574bb7779/kafka-clients-2.7.0-sources.jar!/org/apache/kafka/common/serialization/StringSerializer.java`

Along with the String Serializer, I was thinking that I might have to use the
Byte or Byte Array or some related binary Serializer

`ByteArraySerializer`
`ByteBufferSerializer`
`BytesSerializer`

That's all seems to be there. Looking at all the serialize methods, I see all
of them return a byte array. `byte[]`

Oops, I guess I should have looked for what's the input as part of the data
argument. Only for one of them it's `byte[]` it's the `ByteArraySerializer` one.
Hmm. Looks like Protocol Buffer will be able to give an array of bytes. But I
gotta check and see how it all fits together and if this is the right thing

https://duckduckgo.com/?q=apache+kafka+value+serializer+for+protocol+buffer+messages&t=ffab&ia=web

https://laptrinhx.com/serializer-deserializer-for-kafka-to-serialize-deserialize-protocol-buffers-messages-3895596127/

https://medium.com/data-rocks/protobuf-as-an-encoding-format-for-apache-kafka-cad4709a668d

Cool, I'm just gonna use one of the byte related serializers. I just read how
the StringSerializer works, so, all good :)

Now, I'm trying to use the auto generated protobuf java class in my code. I
realized I was using it in the main app code but the sample protos are in the
test part and the same goes for in the build auto generated classes too - they
are part of test.

Also, it doesn't have package name. So, that's a big problem.

https://developers.google.com/protocol-buffers/docs/proto3#packages

By mistake I wrote this

```proto
package = "io.github.karuppiah7890.kafkaprotobufproducer"
```

Fixed it to this

```proto
package io.github.karuppiah7890.kafkaprotobufproducer;
```

Right, for Java I need to use

```proto
option java_package = "io.github.karuppiah7890.kafkaprotobufproducer";
```

https://developers.google.com/protocol-buffers/docs/javatutorial#defining-your-protocol-format

Still not able to refer to the package even though there's a Java package now
under which the auto generated code is present. I think there's a little
disconnect though.

My test class output is in a different directory - `build/classes` but the
proto auto generated classes are in a different directory - `build/generated`

Hmm. Should I somehow include the generated files too in the Test? Hmm

Gotta check how to do that!

https://duckduckgo.com/?t=ffab&q=java%3A+gradle+include+generated+files+in+test&ia=web

https://stackoverflow.com/questions/28345705/how-can-i-add-a-generated-source-folder-to-my-source-path-in-gradle

Wow. This worked -

```groovy
application {
    // Define the main class for the application.
    mainClass = 'App'
    sourceSets {
        test {
            proto {
                srcDir 'src/test/resources/testdata/protos'
            }

            java {
                srcDir 'build/generated'
            }
        }
    }
}
```

Given it's all test data - the proto files and date, I think it only makes
sense to only use it in tests. Maybe I could make it a bit better though

```groovy
application {
    // Define the main class for the application.
    mainClass = 'App'
    sourceSets {
        test {
            proto {
                srcDir 'src/test/resources/testdata/protos'
            }

            java {
                srcDir 'build/generated/source/proto/test'
            }
        }
    }
}
```

This looks better! :)

Now, the other thing about only using it in test is, ideally this tool is not
dependent on any particular proto file so why include any now, though it could
be removed in the future once I'm done with all the building, but I could still
just use some sample data in test itself and in test alone, even now

So, let's produce data with Junit test instead of the app main :)

```java
package io.github.karuppiah7890.kafkaprotobufproducer;

import io.github.karuppiah7890.kafkaprotobufproducer.SearchRequestOuterClass.SearchRequest;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Test;

import java.util.Properties;

class KafkaProtobufProducerAppTest {
    @Test
    void produceDataIntoKafka() {
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
}
```

Wow! I was able to produce the protobuf messages :D :D

```bash
Last login: Sat Mar 20 23:31:05 on ttys004
You have new mail.
 kafka_2.13-2.7.0  $ ./bin/kafka-console-consumer.sh --bootstrap-server localhost:9092 --topic my-topic --from-beginning
0
1
2
3
4
5
6
7
8
9
10
11
12
13
14
15
16
17
18
19
20
21
22
23
24
25
26
27
28
29
30
31
32
33
34
35
36
37
38
39
40
41
42
43
44
45
46
47
48
49
50
51
52
53
54
55
56
57
58
59
60
61
62
63
64
65
66
67
68
69
70
71
72
73
74
75
76
77
78
79
80
81
82
83
84
85
86
87
88
89
90
91
92
93
94
95
96
97
98
99

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query

	meh-query
^CProcessed a total of 200 messages
```

Since I pulled the messages from the beginning, I got all of them. I also
didn't use any consumer group ID. I think it's high time I start using a
consumer group ID so that kafka can keep track of the last message I consumed :)

Now, let me try to use the protocol buffer consumer and see how it works. Let
me change the topic name and produce only protobuf messages in it so that it's
purely made of protobuf messages.

Or I could delete the topic, hmm. Let me try that.

https://dev.to/de_maric/how-to-delete-records-from-a-kafka-topic-464g

```bash
./bin/kafka-topics.sh --bootstrap-server localhost:9092 \
--topic my-topic \
--delete
```

Cool, that's done! :)

Ran the test again!

Gonna use the kafka protocol buffer consumer now! :D

```bash
$ kafka-protobuf-console-consumer --help
usage: kafka-protobuf-console-consumer [<flags>]

Flags:
      --help                     Show context-sensitive help (also try --help-long and --help-man).
  -v, --version                  Version
  -d, --debug                    Enable Sarama logs
  -b, --broker-list=localhost:9092 ...
                                 List of brokers to connect
  -c, --consumer-group=CONSUMER-GROUP
                                 Consumer group to use
  -t, --topic=TOPIC              Topic name
      --proto-dir=PROTO-DIR ...  /foo/dir1 /bar/dir2 (add all dirs used by imports)
      --file=FILE                will be baz/a.proto that's in /foo/dir1/baz/a.proto
      --message=MESSAGE          Proto message name
      --from-beginning           Read from beginning
      --pretty                   Format output
      --with-separator           Adds separator between messages. Useful with --pretty

```

```bash
$ kafka-protobuf-console-consumer \
--broker-list localhost:9092 \
--topic my-topic \
--proto-dir app/src/test/resources/testdata/protos/ \
--file search-request.proto \
--message SearchRequest \
--consumer-group march-20-2021-23-38-40 \
--pretty \
--with-separator \
--from-beginning

Message topic:"my-topic" partition:0 offset:0
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:1
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:2
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:3
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:4
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:5
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:6
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:7
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:8
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:9
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:10
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:11
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:12
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:13
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:14
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:15
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:16
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:17
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:18
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:19
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:20
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:21
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:22
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:23
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:24
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:25
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:26
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:27
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:28
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:29
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:30
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:31
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:32
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:33
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:34
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:35
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:36
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:37
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:38
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:39
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:40
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:41
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:42
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:43
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:44
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:45
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:46
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:47
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:48
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:49
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:50
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:51
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:52
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:53
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:54
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:55
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:56
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:57
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:58
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:59
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:60
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:61
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:62
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:63
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:64
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:65
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:66
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:67
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:68
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:69
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:70
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:71
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:72
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:73
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:74
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:75
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:76
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:77
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:78
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:79
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:80
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:81
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:82
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:83
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:84
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:85
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:86
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:87
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:88
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:89
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:90
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:91
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:92
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:93
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:94
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:95
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:96
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:97
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:98
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
Message topic:"my-topic" partition:0 offset:99
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
^C

```

This is fantastic! :D

- Create a protocol buffer message producer using the Kafka client
  library - DONE
- Run Zookeeper and Kafka locally - DONE
- Run my app to produce some dummy data - DONE
- Consume the dummy data using the kafka protobuf consumer - DONE

---

Next is

- Try to use JSON format of the protobuf message as input for the tool
  - CLI input - direct standard input or file input - a single JSON file maybe
- Use Protobuf message metadata and parse the JSON input to form Protobuf
  messages

Gonna use the below as sample input :)

```java
"{\"query\":\"meh-query\",\"pageNumber\":1,\"resultPerPage\":5}"
```

To get `JsonFormat.Parser` I need to import another library - util library :)

I think this is what we are looking for

https://search.maven.org/artifact/com.google.protobuf/protobuf-java-util/4.0.0-rc-2/bundle

:D :D

Checking this out now

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.Parser.html

I think I can only use `ignoringUnknownFields()` factory method

Oh. That's an instance method. The factor method is this

```java
JsonFormat.Parser parser = JsonFormat.parser();
```

It was right there here

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.html

🤦

Anyways, it's cool that I found it! :D

This is the implementation of the `parser` factory method

```java
public static Parser parser() {
    return new Parser(
        com.google.protobuf.TypeRegistry.getEmptyTypeRegistry(),
        TypeRegistry.getEmptyTypeRegistry(),
        false,
        Parser.DEFAULT_RECURSION_LIMIT);
}
```

So, the `false` refers to the `ignoreUnknownFields`. So, ignore is false, so it
will not ignore and instead throw errors. I guess it's good? I don't know.
Hmm. For now I'll leave it. So, JSON should always have only the known fields.
Okay! :)

Now, let's try to parse the json :D

There are two methods that I see to help with this

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.Parser.html#merge-java.io.Reader-com.google.protobuf.Message.Builder-

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/util/JsonFormat.Parser.html#merge-java.lang.String-com.google.protobuf.Message.Builder-

One is based on `Reader` as input for getting JSON, another is based on `String`
as input for getting JSON. Let's try the JSON one :)

Now I'm here

```java
@Test
void convertJSONIntoProtobufMessage() {
	String protobufMessageAsJSONString = "{\"query\":\"meh-query\",\"pageNumber\":1,\"resultPerPage\":5}";

	JsonFormat.Parser parser = JsonFormat.parser();
	Message.Builder messageBuilder;
	parser.merge(protobufMessageAsJSONString, messageBuilder);
}
```

Now, I need to see how to get a message builder! :)

Especially for the given proto. I know three things that one usually needs for
this

protos directory / directories path - which contains all protos, this is most
times important as people spread the protos across multiple files in multiple
directories and then import them in different places. So, proto files may not
be stand alone and will depend on other protos by using `import`

proto file name - the particular proto file we are interested in, in which the
message we are interested in is present, the message which we want to build and
produce as kafka message

message name - the particular protobuf message name which we want to build.
The name should be identical to what's present in the proto file name as far as
I know!

Now, let's get to it! :)

Also, another thing that I have noticed is that, sometimes instead of the whole
protos directory path (that is the directory/directories) itself is not needed
but instead a descriptor file that too a standalone one is all that one needs
and then proto file name and the message name. The descriptor file will have the
content of all the protos and the imports in a stand alone manner :) This can
be generated when you run the protoc command :)

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/Message.Builder.html

I remember solving this problem with a colleague. We use some dynamic message
thingy I think. Let me go get that first.

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/DynamicMessage.Builder.html

I'ms stuck here now

```java
@Test
void convertJSONIntoProtobufMessage() {
	String protobufMessageAsJSONString = "{\"query\":\"meh-query\",\"pageNumber\":1,\"resultPerPage\":5}";

	JsonFormat.Parser parser = JsonFormat.parser();

	DynamicMessage dynamicMessage = DynamicMessage.newBuilder().build();

	Message.Builder messageBuilder;
	parser.merge(protobufMessageAsJSONString, messageBuilder);
}
```

I think I should provide a builder to the `merge` method. So, no need to create
`DynamicMessage`, but just `DynamicMessage.Builder` is enough I think. But how
will I get the final protobuf Dynamic Message? Hmm

I guess after passing the builder, I need to parse with `merge` and then finally
call `.build()` method on the buidler, hmm. Makes sense

Let's check how to work with Dynamic Message

It says

"An implementation of Message that can represent arbitrary types, given a Descriptors.Descriptor."

Let's see how to create it then! :)

I'm now checking about Descriptors, hmm

`/Users/karuppiahn/.gradle/caches/modules-2/files-2.1/com.google.protobuf/protobuf-java/4.0.0-rc-2/c49a358fffacfeede6082a58ded63b71a5b2126/protobuf-java-4.0.0-rc-2-sources.jar!/com/google/protobuf/Descriptors.java`

Dynamic Message is here

`/Users/karuppiahn/.gradle/caches/modules-2/files-2.1/com.google.protobuf/protobuf-java/4.0.0-rc-2/c49a358fffacfeede6082a58ded63b71a5b2126/protobuf-java-4.0.0-rc-2-sources.jar!/com/google/protobuf/DynamicMessage.java`

In Descriptors, there are a lot of them, hmm

`FieldDescriptorProto`

```java
private void setProto(final FieldDescriptorProto proto) {
	this.proto = proto;
}
```

And then `FileDescriptorProto`

```java
private void setProto(final FileDescriptorProto proto) {
      this.proto = proto;

      for (int i = 0; i < messageTypes.length; i++) {
    ...
```

There's also `FileDescriptorSet` hmm

`/Users/karuppiahn/Library/Application Support/JetBrains/IdeaIC2020.3/plugins/protobuf-editor.jar!/include/google/protobuf/descriptor.proto`

FileDescriptorSet - complete list of proto files as one thing

FileDescriptorProto - one complete proto file

DescriptorProto - describes message type

I'm slowly starting to think if the below could lead somewhere, hmm

```java
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
```

There's this thing about FileDescriptor

https://developers.google.com/protocol-buffers/docs/reference/java/com/google/protobuf/Descriptors.FileDescriptor.html

There's a

```java
// Construct a FileDescriptor.
static Descriptors.FileDescriptor 	buildFrom(DescriptorProtos.FileDescriptorProto proto, Descriptors.FileDescriptor[] dependencies)

// Construct a FileDescriptor.
static Descriptors.FileDescriptor 	buildFrom(DescriptorProtos.FileDescriptorProto proto, Descriptors.FileDescriptor[] dependencies, boolean allowUnknownDependencies)
```

I now remember what we did in our project. We actually had multiple protocol
buffer messages spread across multiple files and there were multiple imports to
each other, we also imported some google's proto stuff

We used the `buildFrom` like static methods seen above to create the program.

Let me check more :)

`FileDescriptorSet` is just a collection of / list of `FileDescriptorProto`

```proto
// The protocol compiler can output a FileDescriptorSet containing the .proto
// files it parses.
message FileDescriptorSet {
  repeated FileDescriptorProto file = 1;
}
```

For `FileDescriptorProto` and `DescriptorProto`, see below

```proto
// Describes a complete .proto file.
message FileDescriptorProto {
  optional string name = 1;     // file name, relative to root of source tree
  optional string package = 2;  // e.g. "foo", "foo.bar", etc.

  // Names of files imported by this file.
  repeated string dependency = 3;
  // Indexes of the public imported files in the dependency list above.
  repeated int32 public_dependency = 10;
  // Indexes of the weak imported files in the dependency list.
  // For Google-internal migration only. Do not use.
  repeated int32 weak_dependency = 11;

  // All top-level definitions in this file.
  repeated DescriptorProto message_type = 4;
  repeated EnumDescriptorProto enum_type = 5;
  repeated ServiceDescriptorProto service = 6;
  repeated FieldDescriptorProto extension = 7;

  optional FileOptions options = 8;

  // This field contains optional information about the original source code.
  // You may safely remove this entire field without harming runtime
  // functionality of the descriptors -- the information is needed only by
  // development tools.
  optional SourceCodeInfo source_code_info = 9;

  // The syntax of the proto file.
  // The supported values are "proto2" and "proto3".
  optional string syntax = 12;
}

// Describes a message type.
message DescriptorProto {
  optional string name = 1;

  repeated FieldDescriptorProto field = 2;
  repeated FieldDescriptorProto extension = 6;

  repeated DescriptorProto nested_type = 3;
  repeated EnumDescriptorProto enum_type = 4;

  message ExtensionRange {
    optional int32 start = 1;  // Inclusive.
    optional int32 end = 2;    // Exclusive.

    optional ExtensionRangeOptions options = 3;
  }
  repeated ExtensionRange extension_range = 5;

  repeated OneofDescriptorProto oneof_decl = 8;

  optional MessageOptions options = 7;

  // Range of reserved tag numbers. Reserved tag numbers may not be used by
  // fields or extension ranges in the same message. Reserved ranges may
  // not overlap.
  message ReservedRange {
    optional int32 start = 1;  // Inclusive.
    optional int32 end = 2;    // Exclusive.
  }
  repeated ReservedRange reserved_range = 9;
  // Reserved field names, which may not be used by fields in the same message.
  // A given name may only be reserved once.
  repeated string reserved_name = 10;
}
```

For now, I'll start with a simple proto and then do for more protos with imports
to internal and external (like google) protos :)

Checking the docs regarding the Dynamic Message!

https://developers.google.com/protocol-buffers/docs/reference/java-generated#utility-classes

https://developers.google.com/protocol-buffers/docs/reference/google.protobuf

https://codeburst.io/using-dynamic-messages-in-protocol-buffers-in-scala-9fda4f0efcb3

https://developers.google.com/protocol-buffers/docs/techniques#self-description

I have generated the `.desc` file now

```groovy
protobuf {
    generateProtoTasks {
        all().each { task ->
            // If true, will generate a descriptor_set.desc file under
            // $generatedFilesBaseDir/$sourceSet. Default is false.
            // See --descriptor_set_out in protoc documentation about what it is.
            task.generateDescriptorSet = true

            // Allows to override the default for the descriptor set location
            task.descriptorSetOptions.path =
                    "${projectDir}/build/descriptors/${task.sourceSet.name}.dsc"

            // If true, the descriptor set will contain line number information
            // and comments. Default is false.
            task.descriptorSetOptions.includeSourceInfo = true

            // If true, the descriptor set will contain all transitive imports and
            // is therefore self-contained. Default is false.
            task.descriptorSetOptions.includeImports = true
        }
    }
}
```

```bash
$ ./gradlew clean compileJ compileTestJ

BUILD SUCCESSFUL in 1s
8 actionable tasks: 8 executed
$ ls
app		gradle		gradlew		gradlew.bat	settings.gradle
$ fd . app/build
app/build/classes
app/build/classes/java
app/build/classes/java/main
app/build/classes/java/main/io
app/build/classes/java/main/io/github
app/build/classes/java/main/io/github/karuppiah7890
app/build/classes/java/main/io/github/karuppiah7890/kafkaprotobufproducer
app/build/classes/java/main/io/github/karuppiah7890/kafkaprotobufproducer/KafkaProtobufProducerApp.class
app/build/classes/java/test
app/build/classes/java/test/io
app/build/classes/java/test/io/github
app/build/classes/java/test/io/github/karuppiah7890
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/KafkaProtobufProducerAppTest.class
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/SearchRequestOuterClass$SearchRequest$1.class
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/SearchRequestOuterClass$SearchRequest$Builder.class
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/SearchRequestOuterClass$SearchRequest.class
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/SearchRequestOuterClass$SearchRequestOrBuilder.class
app/build/classes/java/test/io/github/karuppiah7890/kafkaprotobufproducer/SearchRequestOuterClass.class
app/build/descriptors
app/build/descriptors/test.dsc
app/build/extracted-include-protos
app/build/extracted-include-protos/main
app/build/extracted-include-protos/test
app/build/extracted-include-protos/test/google
app/build/extracted-include-protos/test/google/protobuf
app/build/extracted-include-protos/test/google/protobuf/any.proto
app/build/extracted-include-protos/test/google/protobuf/api.proto
app/build/extracted-include-protos/test/google/protobuf/compiler
app/build/extracted-include-protos/test/google/protobuf/compiler/plugin.proto
app/build/extracted-include-protos/test/google/protobuf/descriptor.proto
app/build/extracted-include-protos/test/google/protobuf/duration.proto
app/build/extracted-include-protos/test/google/protobuf/empty.proto
app/build/extracted-include-protos/test/google/protobuf/field_mask.proto
app/build/extracted-include-protos/test/google/protobuf/source_context.proto
app/build/extracted-include-protos/test/google/protobuf/struct.proto
app/build/extracted-include-protos/test/google/protobuf/timestamp.proto
app/build/extracted-include-protos/test/google/protobuf/type.proto
app/build/extracted-include-protos/test/google/protobuf/wrappers.proto
app/build/extracted-protos
app/build/extracted-protos/main
app/build/extracted-protos/test
app/build/generated
app/build/generated/source
app/build/generated/source/proto
app/build/generated/source/proto/test
app/build/generated/source/proto/test/java
app/build/generated/source/proto/test/java/io
app/build/generated/source/proto/test/java/io/github
app/build/generated/source/proto/test/java/io/github/karuppiah7890
app/build/generated/source/proto/test/java/io/github/karuppiah7890/kafkaprotobufproducer
app/build/generated/source/proto/test/java/io/github/karuppiah7890/kafkaprotobufproducer/SearchRequestOuterClass.java
app/build/generated/sources
app/build/generated/sources/annotationProcessor
app/build/generated/sources/annotationProcessor/java
app/build/generated/sources/annotationProcessor/java/main
app/build/generated/sources/annotationProcessor/java/test
app/build/generated/sources/headers
app/build/generated/sources/headers/java
app/build/generated/sources/headers/java/main
app/build/generated/sources/headers/java/test
app/build/tmp
app/build/tmp/compileJava
app/build/tmp/compileJava/source-classes-mapping.txt
app/build/tmp/compileTestJava
app/build/tmp/compileTestJava/source-classes-mapping.txt
```

---

I'm now struggling to get some File Descriptor Set and File Descriptor stuff
figured out. Hmm

I need to pass in empty dependencies for now. But the in the past I remember how
we had to provide dependencies in the right order or else things just broke 🤷
And right order is a lot of work! But it was needed as we had too many proto
files and all.

```java
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
```

I have to pass in an empty array for dependencies field. But I couldn't do it.
Gotta check how to do that tomorrow!

https://duckduckgo.com/?t=ffab&q=java+create+empty+array&ia=web&iax=qa

https://alabamacodecamp.com/java/how-to-make-an-array-empty-in-java.html

https://alabamacodecamp.com/java/how-to-initialize-an-empty-array-in-java.html

---

I finally managed to get the whole thing working! :D 

I had to fix some build.gradle issues. For example I had used the extension
`.dsc` instead of `.desc`. Yeah. 🤦 And I had used `.desc` in my code. Anyways

Yayayay!!!!


```bash
$ kafka-protobuf-console-consumer -b localhost:9092 -t my-topic --proto-dir app/src/test/resources/testdata/protos/ --file search-request.proto --message SearchRequest --consumer-group march-22-2021-23-33-40 --pretty --with-separator --from-beginning 
Starting 3.0.0 build-on 2019-09-01.03:43:59 with consumer group: march-22-2021-23-33-40

panic: File: search-request.proto not found in: [app/src/test/resources/testdata/protos/]


goroutine 1 [running]:
main.main()
	/Users/yogeshsr/go/src/github.com/yogeshsr/kafka-protobuf-console-consumer/main.go:87 +0x82c

$ ls
README.md	STORY.md	backend

$ cd backend/java/
 java  main ✘  $ kafka-protobuf-console-consumer -b localhost:9092 -t my-topic --proto-dir app/src/test/resources/testdata/protos/ --file search-request.proto --message SearchRequest --consumer-group march-22-2021-23-33-40 --pretty --with-separator --from-beginning 
Starting 3.0.0 build-on 2019-09-01.03:43:59 with consumer group: march-22-2021-23-33-40

Message topic:"my-topic" partition:0 offset:0
{
	"query": "meh-query",
	"pageNumber": 1,
	"resultPerPage": 5
}
--------------------------------- end message -----------------------------------------
^C
```

----

- Try to use JSON format of the protobuf message as input for the tool - 
  PARTIALLY DONE
  - CLI input - direct standard input or file input - a single JSON file maybe -
  PARTIALLY DONE
- Use Protobuf message metadata and parse the JSON input to form Protobuf
  messages - DONE
- Run the app to produce dummy data - DONE
- Consume dummy data with kafka protobuf consumer tool - DONE

---

I need to make it a bit better now.

- Try to use JSON format of the protobuf message as input for the tool
  - CLI input - direct standard input or file input - a single JSON file maybe

I need to do the whole thing in the main method and use user input and not
hardcoded values! :)
