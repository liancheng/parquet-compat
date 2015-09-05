package com.databricks.parquet.schema

import com.databricks.parquet.protobuf.ParquetProtobufCompat.ProtoArrayOfOptionalInt
import org.apache.parquet.proto.ProtoSchemaConverter

// Unlike Parquet, ProtoBuf isn't able to represent arrays of optional primitive types.  The closest
// stuff in ProtoBuf is an array of a required group with a single optional field.
object ProtoArrayOfOptionalElement {
  def main(args: Array[String]) {
    val descriptorProto = ProtoArrayOfOptionalInt.getDescriptor.toProto
    println(
      s"""${"=" * 80}
         |ProtoBuf descriptor of ${classOf[ProtoArrayOfOptionalInt].getName}:
         |$descriptorProto
       """.stripMargin)

    val messageType = new ProtoSchemaConverter().convert(classOf[ProtoArrayOfOptionalInt])

    println(
      s"""${"=" * 80}
         |Parquet schema of ${classOf[ProtoArrayOfOptionalInt].getName}
         |$messageType
       """.stripMargin)
  }
}
