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
