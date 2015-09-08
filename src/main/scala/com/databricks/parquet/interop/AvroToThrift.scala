package com.databricks.parquet.interop

import scala.collection.JavaConverters._

import com.databricks.parquet.utils._
import com.databricks.parquet.avro.AvroIntArray
import org.apache.parquet.avro.AvroParquetWriter
import org.apache.parquet.thrift.ThriftParquetReader

// This fails, because parquet-thrift tries to find Thrift schema stored in Parquet files as
// extra metadata.
object AvroToThrift {
  def main(args: Array[String]) {
    val path = outputPath(args)

    println(
      s"""Avro schema:
         |${AvroIntArray.getClassSchema.toString(true)}
       """.stripMargin)

    val avroWriter = new AvroParquetWriter[AvroIntArray](path, AvroIntArray.getClassSchema)
    avroWriter.write(AvroIntArray.newBuilder().setF((1 to 3).map(Int.box).asJava).build())
    avroWriter.close()

    println(
      s"""Parquet schema of file $path:
         |${readParquetSchema(path.toString)}
       """.stripMargin)

    val thriftReader = ThriftParquetReader.build(path).build()
    println(
      s"""Thrift record read from file $path:
         |${thriftReader.read()}
       """.stripMargin)
    thriftReader.close()
  }
}
