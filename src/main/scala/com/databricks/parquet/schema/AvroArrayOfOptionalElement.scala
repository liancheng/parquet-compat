package com.databricks.parquet.schema

import com.databricks.parquet.avro.AvroArrayOfOptionalInt
import org.apache.parquet.avro.AvroSchemaConverter

// Parquet-avro 1.7.0 and prior versions cannot give correct Parquet schema of Avro arrays of
// optional elements.  The result schema is exactly the same as arrays of required elements.
object AvroArrayOfOptionalElement {
  def main(args: Array[String]) {
    println(
      s"""Avro schema and corresponding Parquet schema:
         |
         |${AvroArrayOfOptionalInt.getClassSchema.toString(true)}
         |
         |${new AvroSchemaConverter().convert(AvroArrayOfOptionalInt.getClassSchema)}
       """.stripMargin)
  }
}
