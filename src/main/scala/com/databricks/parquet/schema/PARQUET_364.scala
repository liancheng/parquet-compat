package com.databricks.parquet.schema

import scala.collection.JavaConverters._

import com.databricks.parquet.avro.AvroArrayOfIntArray
import com.databricks.parquet.thrift.ThriftArrayOfIntArray
import com.databricks.parquet.utils._
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.{AvroParquetReader, AvroParquetWriter, AvroSchemaConverter}
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.thrift.{ThriftParquetWriter, ThriftSchemaConverter}

// PARQUET-364: Parquet-avro cannot decode Avro/Thrift array of primitive array
object PARQUET_364 {
  private def thriftToParquet(path: Path): Unit = {
    println(
      s"""${"=" * 100}
         |Thrift type descriptor and its corresponding Parquet schema:
         |${ThriftSchemaConverter.toStructType(classOf[ThriftArrayOfIntArray])}
         |${"-" * 100}
         |${new ThriftSchemaConverter().convert(classOf[ThriftArrayOfIntArray])}
         |${"=" * 100}
       """.stripMargin)

    val writer =
      new ThriftParquetWriter[ThriftArrayOfIntArray](
        path, classOf[ThriftArrayOfIntArray], CompressionCodecName.UNCOMPRESSED)

    writer.write(new ThriftArrayOfIntArray(Seq(Seq(1: Integer).asJava).asJava))
    writer.close()
  }

  private def avroToParquet(path: Path): Unit = {
    println(
      s"""${"=" * 100}
         |Avro schema and its corresponding Parquet schema:
         |${AvroArrayOfIntArray.getClassSchema.toString(true)}
         |${"-" * 100}
         |${new AvroSchemaConverter().convert(AvroArrayOfIntArray.getClassSchema)}
         |${"=" * 100}
       """.stripMargin)

    val writer = AvroParquetWriter.builder[AvroArrayOfIntArray](path).build()
    writer.write(
      AvroArrayOfIntArray
        .newBuilder()
        .setF(Seq(Seq(1: Integer).asJava).asJava)
        .build())

    writer.close()
  }

  private def parquetToAvro(path: Path): Unit = {
    val reader = AvroParquetReader.builder[AvroArrayOfIntArray](path).build()
    // !! ERROR !!
    //
    // This method call throws exception, because AvroIndexedRecordConverter doesn't handle LIST
    // backwards-compatibility rules well.
    try {
      reader.read()
    } catch { case cause: Throwable =>
      cause.printStackTrace()
    }

    reader.close()
  }

  def main(args: Array[String]) {
    val path = outputPath(args)
    val thriftPath = new Path(path, "thrift")
    val avroPath = new Path(path, "avro")

    thriftToParquet(thriftPath)
    parquetToAvro(thriftPath)

    avroToParquet(avroPath)
    parquetToAvro(avroPath)
  }
}
