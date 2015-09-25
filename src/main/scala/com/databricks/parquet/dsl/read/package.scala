package com.databricks.parquet.dsl

import scala.collection.mutable

import com.databricks.parquet.utils
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetReader
import org.apache.parquet.io.api.Binary
import org.apache.parquet.schema.MessageTypeParser

package object read {
  private[read] trait MessageEvent
  private[read] case object GroupStart extends MessageEvent
  private[read] case object GroupEnd extends MessageEvent
  private[read] case class IntValue(value: Int) extends MessageEvent
  private[read] case class LongValue(value: Long) extends MessageEvent
  private[read] case class FloatValue(value: Float) extends MessageEvent
  private[read] case class DoubleValue(value: Double) extends MessageEvent
  private[read] case class BooleanValue(value: Boolean) extends MessageEvent
  private[read] case class BinaryValue(value: Binary) extends MessageEvent

  private[read] class MessageEvents {
    private val queue = mutable.Queue.empty[MessageEvent]

    def issue(event: MessageEvent): Unit = {
      queue.enqueue(event)
    }

    def expect(expected: MessageEvent): Unit = {
      val actual = queue.dequeue()
      assert(actual == expected, s"Expecting $expected, but got $actual")
    }

    def purge(): Unit = {
      queue.clear()
    }

    def dump(): Unit = {
      queue.foreach(println)
    }
  }

  def directly(parquetReader: ParquetReader[MessageEvents])(f: MessageEvents => Unit): Unit = {
    try {
      val events = parquetReader.read()
      while (parquetReader.read() != null) {}
      f(events)
    } finally {
      parquetReader.close()
    }
  }

  def directly(path: String)(f: MessageEvents => Unit): Unit = {
    directly(new Path(path))(f)
  }

  def directly(path: String, requestedSchema: String)(f: MessageEvents => Unit): Unit = {
    directly(new Path(path), requestedSchema)(f)
  }

  def directly(path: Path)(f: MessageEvents => Unit): Unit = {
    val parquetSchema = utils.schemaOf(path)
    directly(ParquetReader.builder(new DirectReadSupport(parquetSchema), path).build())(f)
  }

  def directly(path: Path, requestedSchema: String)(f: MessageEvents => Unit): Unit = {
    val parquetSchema = MessageTypeParser.parseMessageType(requestedSchema)
    directly(ParquetReader.builder(new DirectReadSupport(parquetSchema), path).build())(f)
  }

  def discard(path: String): Unit = discard(new Path(path))

  def discard(path: String, requestedSchema: String): Unit = {
    discard(new Path(path), requestedSchema)
  }

  def discard(path: Path): Unit = {
    val parquetSchema = utils.schemaOf(path)
    val parquetReader = ParquetReader
      .builder(new DirectReadSupport(parquetSchema, discard = true), path).build()
    directly(parquetReader) { events => }
  }

  def discard(path: Path, requestedSchema: String): Unit = {
    val parquetSchema = MessageTypeParser.parseMessageType(requestedSchema)
    val parquetReader = ParquetReader
      .builder(new DirectReadSupport(parquetSchema, discard = true), path).build()
    directly(parquetReader) { events => }
  }

  def message(f: => Unit)(implicit events: MessageEvents): Unit = {
    events.expect(GroupStart)
    f
    events.expect(GroupEnd)
  }

  def group(f: => Unit)(implicit events: MessageEvents): Unit = {
    events.expect(GroupStart)
    f
    events.expect(GroupEnd)
  }

  def field(index: Int, name: String)(f: => Unit): Unit = {
    f
  }

  def boolean(value: Boolean)(implicit events: MessageEvents): Unit = {
    events.expect(BooleanValue(value))
  }

  def int(value: Int)(implicit events: MessageEvents): Unit = {
    events.expect(IntValue(value))
  }

  def long(value: Long)(implicit events: MessageEvents): Unit = {
    events.expect(LongValue(value))
  }

  def float(value: Float)(implicit events: MessageEvents): Unit = {
    events.expect(FloatValue(value))
  }

  def double(value: Double)(implicit events: MessageEvents): Unit = {
    events.expect(DoubleValue(value))
  }

  def binary(value: Binary)(implicit events: MessageEvents): Unit = {
    events.expect(BinaryValue(value))
  }

  def string(value: String)(implicit events: MessageEvents): Unit = {
    events.expect(BinaryValue(Binary.fromString(value)))
  }

  def purge()(implicit events: MessageEvents): Unit = {
    events.purge()
  }
}
