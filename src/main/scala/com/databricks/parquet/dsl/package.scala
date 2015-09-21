package com.databricks.parquet

import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.io.api.{Binary, RecordConsumer}
import org.apache.parquet.schema.MessageTypeParser

package object dsl {
  type RecordBuilder = RecordConsumer => Unit

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

  class DecimalConsumer(value: BigDecimal, consumer: RecordConsumer) {
    def asInt(): Unit = {
      consumer.addInteger(value.underlying().longValue().toInt)
    }

    def asLong(): Unit = {
      consumer.addLong(value.underlying().longValue())
    }

    def asBinary(): Unit = {
      val unscaledBytes = value.underlying().unscaledValue().toByteArray
      consumer.addBinary(Binary.fromByteArray(unscaledBytes))
    }
  }

  def decimal(value: BigDecimal)(implicit consumer: RecordConsumer): DecimalConsumer = {
    new DecimalConsumer(value, consumer)
  }
}
