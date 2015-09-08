package com.databricks.parquet.schema

import java.util

import com.databricks.parquet.avro.{AvroArrayOfOptionalInt, AvroIntArray}
import com.databricks.parquet.utils._
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.{AvroParquetWriter, AvroSchemaConverter}

// Parquet-avro 1.7.0 and prior versions cannot give correct Parquet schema of Avro arrays of
// optional elements.  The result schema is exactly the same as arrays of required elements.
// This issue has been fixed in parquet-avro 1.8.0.
object AvroArrayOfOptionalElement {
  private def avroToParquet(path: Path): Unit = {
    val writer =
      new AvroParquetWriter[AvroArrayOfOptionalInt](
        path, AvroArrayOfOptionalInt.getClassSchema)

    // !! ERROR !!
    //
    // This method call throws exception, because the converted Parquet schema doesn't allow nulls.
    writer.write(
      AvroArrayOfOptionalInt
        .newBuilder()
        .setF(util.Arrays.asList(1: Integer, null, 2: Integer, null))
        .build())

    writer.close()
  }

  def main(args: Array[String]) {
    val path = outputPath(args)

    println(
      s"""${"=" * 100}
         |Avro schema:
         |${AvroArrayOfOptionalInt.getClassSchema.toString(true)}
       """.stripMargin)

    println(
      s"""${"=" * 100}
         |Parquet schema of ${classOf[AvroArrayOfOptionalInt].getName}:
         |${new AvroSchemaConverter().convert(AvroArrayOfOptionalInt.getClassSchema)}
         |${"=" * 100}
         |NOTE:
         |
         |This schema is WRONG. It's exactly the same as the Parquet schema of
         |${classOf[AvroIntArray].getName}, whose elements are required (cannot be null).
         |${"=" * 100}
       """.stripMargin)

    avroToParquet(path)
  }
}
