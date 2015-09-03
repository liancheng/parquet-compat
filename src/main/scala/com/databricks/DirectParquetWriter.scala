package com.databricks

import scala.collection.JavaConverters.mapAsJavaMapConverter

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.api.WriteSupport
import org.apache.parquet.hadoop.api.WriteSupport.WriteContext
import org.apache.parquet.io.api.RecordConsumer
import org.apache.parquet.schema.{MessageTypeParser, MessageType}

object DirectParquetWriter {
  type RecordBuilder = RecordConsumer => Unit

  private class DirectWriteSupport(schema: MessageType, metadata: Map[String, String])
    extends WriteSupport[RecordConsumer => Unit] {

    private var recordConsumer: RecordConsumer = _

    override def init(configuration: Configuration): WriteContext = {
      new WriteContext(schema, metadata.asJava)
    }

    override def write(writeRecord: RecordBuilder): Unit = {
      recordConsumer.startMessage()
      writeRecord(recordConsumer)
      recordConsumer.endMessage()
    }

    override def prepareForWrite(recordConsumer: RecordConsumer): Unit = {
      this.recordConsumer = recordConsumer
    }
  }

  def writeDirect
      (path: String, schema: String, metadata: Map[String, String] = Map.empty)
      (f: ParquetWriter[RecordBuilder] => Unit): Unit = {
    val messageType = MessageTypeParser.parseMessageType(schema)
    val writeSupport = new DirectWriteSupport(messageType, metadata)
    val parquetWriter = new ParquetWriter[RecordBuilder](new Path(path), writeSupport)
    try f(parquetWriter) finally parquetWriter.close()
  }

  def message(builder: RecordBuilder)(implicit writer: ParquetWriter[RecordBuilder]): Unit = {
    message(writer)(builder)
  }

  def message(writer: ParquetWriter[RecordBuilder])(builder: RecordBuilder): Unit = {
    writer.write(builder)
  }

  def group(f: => Unit)(implicit consumer: RecordConsumer): Unit = {
    group(consumer)(f)
  }

  def group(consumer: RecordConsumer)(f: => Unit): Unit = {
    consumer.startGroup()
    f
    consumer.endGroup()
  }

  def field(name: String)(f: => Unit)(implicit consumer: RecordConsumer): Unit = {
    field(consumer, name)(f)
  }

  def field(name: String, index: Int)(f: => Unit)(implicit consumer: RecordConsumer): Unit = {
    field(consumer, name, index)(f)
  }

  def field(consumer: RecordConsumer, name: String)(f: => Unit): Unit = {
    field(consumer, name, 0)(f)
  }

  def field(consumer: RecordConsumer, name: String, index: Int)(f: => Unit): Unit = {
    consumer.startField(name, index)
    f
    consumer.endField(name, index)
  }
}
