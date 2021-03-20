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
- Import a Kafka client library
- Create a producer using the Kafka client library
- Run Zookeeper and Kafka locally
- Run my app to produce some dummy data
- Consume the dummy data using the kafka protobuf consumer

That confirms that we are able to produce protobuf messages as we would be able
to consume them using another tool

Next Steps would be

- Try to use JSON format of the protobuf message as input for the tool
  - CLI input - direct standard input or file input - a single JSON file maybe
- Use Protobuf message metadata and parse the JSON input to form Protobuf
  messages
- Run the app to produce dummy data
- Consume dummy data with kafka protobuf consumer tool

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
