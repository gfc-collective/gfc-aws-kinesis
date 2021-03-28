# gfc-aws-kinesis [![Maven Central](https://maven-badges.herokuapp.com/maven-central/org.gfccollective/gfc-aws-kinesis_2.12/badge.svg?style=plastic)](https://maven-badges.herokuapp.com/maven-central/org.gfccollective/gfc-aws-kinesis_2.12) [![Build Status](https://github.com/gfc-collective/gfc-aws-kinesis/workflows/Scala%20CI/badge.svg)](https://github.com/gfc-collective/gfc-aws-kinesis/actions) [![Coverage Status](https://coveralls.io/repos/gfc-collective/gfc-aws-kinesis/badge.svg?branch=main&service=github)](https://coveralls.io/github/gfc-collective/gfc-aws-kinesis?branch=main)

Scala wrapper around AWS Kinesis Client Library.

A fork and new home of the now unmaintained Gilt Foundation Classes (`com.gilt.gfc`), now called the [GFC Collective](https://github.com/gfc-collective), maintained by some of the original authors.


## Getting gfc-aws-kinesis

The latest version is 1.0.0, released on 21/Jan/2020 and cross-built against Scala 2.12.x and 2.13.x.

If you're using SBT, add the following line to your build file:

```scala
libraryDependencies += "org.gfccollective" %% "gfc-aws-kinesis" % "1.0.0"
```

SBT Akka stream (2.5.x) dependency:

```scala
libraryDependencies += "org.gfccollective" %% "gfc-aws-kinesis-akka" % "1.0.0"
```

For Maven and other build tools, you can visit [search.maven.org](http://search.maven.org/#search%7Cga%7C1%7Corg.gfccollective).
(This search will also list other available libraries from the GFC Collective.)

# Basic usage

Consume events:

```scala

  implicit object StringRecordReader extends KinesisRecordReader[String]{
    override def apply(r: Record) : String = new String(r.getData.array(), "UTF-8")
  }

  val config = KCLConfiguration("consumer-name", "kinesis-stream-name")

  KCLWorkerRunner(config).runAsyncSingleRecordProcessor[String](1 minute) { a: String =>
     // .. do something with A
     Future.successful(())
  }
```

Publish events:

```scala

  implicit object StringRecordWriter extends KinesisRecordWriter[String] {
    override def toKinesisRecord(a: String) : KinesisRecord = {
      KinesisRecord("partition-key", a.getBytes("UTF-8"))
    }
  }

  val publisher = KinesisPublisher()

  val messages = Seq("Hello World!", "foo bar", "baz bam")

  val result: Future[KinesisPublisherBatchResult] = publisher.publishBatch("kinesis-stream-name", messages)
```

# DynamoDB streaming

Create the adapter client
```scala
val streamAdapterClient: AmazonDynamoDBStreamsAdapterClient =
    new AmazonDynamoDBStreamsAdapterClient()
```

Pass the adapter client in the configuration
```scala
val streamSource = {
    val streamConfig = KinesisStreamConsumerConfig[Option[A]](
      applicationName,
      config.stream,
      regionName = Some(config.region),
      checkPointInterval = config.checkpointInterval,
      initialPositionInStream = config.streamPosition,
      dynamoDBKinesisAdapterClient = streamAdapterClient
    )
    KinesisStreamSource(streamConfig).mapMaterializedValue(_ => NotUsed)
  }
```

Pass an implicit kinesis record reader suitable for dynamodb events
```scala
implicit val kinesisRecordReader
      : KinesisRecordReader[Option[A]] =
      new KinesisRecordReader[Option[A]] {
        override def apply(record: Record): Option[A] = {
          record match {
            case recordAdapter: RecordAdapter =>
              val dynamoRecord: DynamoRecord =
                recordAdapter.getInternalObject
              dynamoRecord.getEventName match {
                case "INSERT" =>
                  ScanamoFree
                    .read[A](
                      dynamoRecord.getDynamodb.getNewImage)
                    .toOption
                case _ => None
              }
            case _ => None
          }
        }
      }
```

Consume e.g. using a sink

```scala
val targetSink = Sink.actorRefWithAck(target, startMsg, ackMsg, Done)

streamSource
  .filter(!_.isEmpty)
  .map(_.get)
  .log(applicationName)(log)
  .runWith(targetSink)
```

## License

Licensed under the Apache License, Version 2.0: http://www.apache.org/licenses/LICENSE-2.0


