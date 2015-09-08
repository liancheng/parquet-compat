package com.databricks.parquet.interop

import scala.collection.JavaConverters._

import com.databricks.parquet.utils._
import com.databricks.parquet.avro.AvroIntArray
import com.databricks.parquet.thrift.ThriftIntArray
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.thrift.{ThriftParquetWriter, ThriftSchemaConverter}

// This works.
object ThriftToAvro {
  def main(args: Array[String]) {
    val path = outputPath(args)

    println(
      s"""Thrift type descriptor:
         |${ThriftSchemaConverter.toStructType(classOf[ThriftIntArray])}
       """.stripMargin)

    val thriftWriter =
      new ThriftParquetWriter[ThriftIntArray](
        path, classOf[ThriftIntArray], CompressionCodecName.UNCOMPRESSED)

    thriftWriter.write(new ThriftIntArray((1 to 3).map(Int.box).asJava))
    thriftWriter.close()

    println(
      s"""Parquet schema of file $path:
         |${readParquetSchema(path.toString)}
       """.stripMargin)

    val avroReader = AvroParquetReader.builder[AvroIntArray](path).build()
    println(
      s"""Avro record read from file $path:
         |${avroReader.read()}
       """.stripMargin)
    avroReader.close()
  }
}
