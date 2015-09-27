package com.databricks.parquet.dsl.read

import scala.collection.JavaConverters._

import org.apache.parquet.io.api._
import org.apache.parquet.schema.{GroupType, MessageType, Type}

private[read] class DirectRecordMaterializer(schema: MessageType, discard: Boolean = false)
  extends RecordMaterializer[MessageEvents] {

  private val events = new MessageEvents

  override def getRootConverter: GroupConverter = {
    if (discard) {
      new BlackholeGroupConverter(schema)
    } else {
      new DirectGroupConverter(schema, events)
    }
  }

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

private[read] class DiscardingPrimitiveConverter extends PrimitiveConverter {
  override def addBoolean(value: Boolean): Unit = ()
  override def addInt(value: Int): Unit = ()
  override def addLong(value: Long): Unit = ()
  override def addFloat(value: Float): Unit = ()
  override def addDouble(value: Double): Unit = ()
  override def addBinary(value: Binary): Unit = ()
}

private[read] class BlackholeGroupConverter(schema: GroupType) extends GroupConverter {
  private val converters = schema.getFields.asScala.map(newConverter)

  override def getConverter(i: Int): Converter = converters(i)

  override def start(): Unit = ()

  override def end(): Unit = ()

  private def newConverter(fieldType: Type): Converter = {
    if (fieldType.isPrimitive) {
      new DiscardingPrimitiveConverter
    } else {
      new BlackholeGroupConverter(fieldType.asGroupType())
    }
  }
}
