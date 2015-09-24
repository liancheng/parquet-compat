package com.databricks.parquet.dsl.read

import java.util.{Map => JMap}

import org.apache.hadoop.conf.Configuration
import org.apache.parquet.hadoop.api.ReadSupport.ReadContext
import org.apache.parquet.hadoop.api.{InitContext, ReadSupport}
import org.apache.parquet.io.api.RecordMaterializer
import org.apache.parquet.schema.MessageType

private[read] class DirectReadSupport(requestedSchema: MessageType, discard: Boolean = false)
  extends ReadSupport[MessageEvents] {

  override def init(context: InitContext): ReadContext = {
    // We don't rely on ReadContext to pass in requested schema.
    new ReadContext(requestedSchema)
  }

  override def prepareForRead(
      configuration: Configuration,
      keyValueMetaData: JMap[String, String],
      fileSchema: MessageType,
      readContext: ReadContext): RecordMaterializer[MessageEvents] = {
    new DirectRecordMaterializer(requestedSchema, discard)
  }
}
