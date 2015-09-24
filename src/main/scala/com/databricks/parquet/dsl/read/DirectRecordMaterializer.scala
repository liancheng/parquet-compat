package com.databricks.parquet.dsl.read

import scala.collection.JavaConverters._

import org.apache.parquet.io.api._
import org.apache.parquet.schema.{GroupType, MessageType, Type}

private[read] class DirectRecordMaterializer(schema: MessageType, discard: Boolean = false)
  extends RecordMaterializer[MessageEvents] {

  private val events = if (discard) {
    new MessageEvents {
      override def issue(event: MessageEvent): Unit = ()
    }
  } else {
    new MessageEvents
  }

  override def getRootConverter: GroupConverter = new DirectGroupConverter(schema, events)

  override def getCurrentRecord: MessageEvents = events
}

private[read] class DirectPrimitiveConverter(events: MessageEvents) extends PrimitiveConverter {
  override def addBoolean(value: Boolean): Unit = events.issue(BooleanValue(value))
  override def addInt(value: Int): Unit = events.issue(IntValue(value))
  override def addLong(value: Long): Unit = events.issue(LongValue(value))
  override def addFloat(value: Float): Unit = events.issue(FloatValue(value))
  override def addDouble(value: Double): Unit = events.issue(DoubleValue(value))
  override def addBinary(value: Binary): Unit = events.issue(BinaryValue(value))
}

private[read] class DirectGroupConverter(schema: GroupType, events: MessageEvents)
  extends GroupConverter {

  private val converters = schema.getFields.asScala.map(newConverter)

  override def getConverter(i: Int): Converter = converters(i)

  override def start(): Unit = events.issue(GroupStart)

  override def end(): Unit = events.issue(GroupEnd)

  private def newConverter(fieldType: Type): Converter = {
    if (fieldType.isPrimitive) {
      new DirectPrimitiveConverter(events)
    } else {
      new DirectGroupConverter(fieldType.asGroupType(), events)
    }
  }
}
