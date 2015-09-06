package com.databricks.parquet.schema

import com.databricks.parquet.avro.AvroArrayOfIntArray
import com.databricks.parquet.thrift.ThriftArrayOfIntArray
import org.apache.parquet.avro.AvroSchemaConverter
import org.apache.parquet.thrift.ThriftSchemaConverter

// PARQUET-364: Parquet-avro cannot decode Avro/Thrift array of primitive array
object PARQUET_364 {
  def main(args: Array[String]) {
    println(
      s"""PARQUET-364: Parquet-avro cannot decode Avro/Thrift array of primitive array
         |
         |${"=" * 80}
         |Avro schema and corresponding Parquet schema:
         |
         |${AvroArrayOfIntArray.getClassSchema.toString(true)}
         |
         |${new AvroSchemaConverter().convert(AvroArrayOfIntArray.getClassSchema)}
         |${"=" * 80}
         |Thrift schema and corresponding Parquet schema:
         |
         |${ThriftSchemaConverter.toStructType(classOf[ThriftArrayOfIntArray])}
         |
         |${new ThriftSchemaConverter().convert(classOf[ThriftArrayOfIntArray])}
         |${"=" * 80}
       """.stripMargin)
  }
}
