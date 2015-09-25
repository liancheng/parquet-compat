package com.databricks.parquet.dsl.read

import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetReader
import org.apache.parquet.schema.MessageTypeParser

object DirectParquetReader {
  def builder(schema: String, path: Path): ParquetReader.Builder[MessageEvents] = {
    val parquetSchema = MessageTypeParser.parseMessageType(schema)
    ParquetReader.builder(new DirectReadSupport(parquetSchema), path)
  }
}
