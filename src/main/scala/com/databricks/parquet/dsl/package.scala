package com.databricks.parquet


import scala.collection.JavaConverters._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.api.WriteSupport
import org.apache.parquet.hadoop.api.WriteSupport.WriteContext
import org.apache.parquet.io.api.{Binary, RecordConsumer}
import org.apache.parquet.schema.{MessageType, MessageTypeParser}

package object dsl {
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

  def writeDirect(path: String, schema: String)(f: ParquetWriter[RecordBuilder] => Unit): Unit = {
    writeDirect(new Path(path), schema, Map.empty[String, String])(f)
  }

  def writeDirect
      (path: String, schema: String, metadata: Map[String, String])
      (f: ParquetWriter[RecordBuilder] => Unit): Unit = {
    writeDirect(new Path(path), schema, metadata)(f)
  }

  def writeDirect
      (path: Path, schema: String, metadata: Map[String, String])
      (f: ParquetWriter[RecordBuilder] => Unit): Unit = {
    val messageType = MessageTypeParser.parseMessageType(schema)
    val writeSupport = new DirectWriteSupport(messageType, metadata)
    val parquetWriter = new ParquetWriter[RecordBuilder](path, writeSupport)
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

  def field(index: Int, name: String)(f: => Unit)(implicit consumer: RecordConsumer): Unit = {
    field(consumer, index, name)(f)
  }

  def field(consumer: RecordConsumer, index: Int, name: String)(f: => Unit): Unit = {
    consumer.startField(name, index)
    f
    consumer.endField(name, index)
  }

  def int(value: Int)(implicit consumer: RecordConsumer): Unit = {
    int(consumer, value)
  }

  def int(consumer: RecordConsumer, value: Int): Unit = {
    consumer.addInteger(value)
  }

  def long(value: Long)(implicit consumer: RecordConsumer): Unit = {
    long(consumer, value)
  }

  def long(consumer: RecordConsumer, value: Long): Unit = {
    consumer.addLong(value)
  }

  def boolean(value: Boolean)(implicit consumer: RecordConsumer): Unit = {
    boolean(consumer, value)
  }

  def boolean(consumer: RecordConsumer, value: Boolean): Unit = {
    consumer.addBoolean(value)
  }

  def string(value: String)(implicit consumer: RecordConsumer): Unit = {
    string(consumer, value)
  }

  def string(consumer: RecordConsumer, value: String): Unit = {
    binary(consumer, Binary.fromString(value))
  }

  def binary(value: Binary)(implicit consumer: RecordConsumer): Unit = {
    binary(consumer, value)
  }

  def binary(consumer: RecordConsumer, value: Binary): Unit = {
    consumer.addBinary(value)
  }

  def float(value: Float)(implicit consumer: RecordConsumer): Unit = {
    float(consumer, value)
  }

  def float(consumer: RecordConsumer, value: Float): Unit = {
    consumer.addFloat(value)
  }

  def double(value: Double)(implicit consumer: RecordConsumer): Unit = {
    double(consumer, value)
  }

  def double(consumer: RecordConsumer, value: Double): Unit = {
    consumer.addDouble(value)
  }
}
