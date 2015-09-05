package com.databricks.parquet.interop

import scala.collection.JavaConverters._

import com.databricks.parquet.avro.AvroIntArray
import com.databricks.parquet.protobuf.ParquetProtobufCompat.ProtoIntArray
import com.databricks.parquet.utils._
import org.apache.parquet.avro.AvroParquetReader
import org.apache.parquet.proto.ProtoParquetWriter

// This fails, because parquet-protobuf and parquet-avro generate incompatible schemas.
object ProtoToAvro {
  def main(args: Array[String]) {
    val path = cleanPath(args.head)

    println(
      s"""ProtoBuf descriptor:
         |${ProtoIntArray.getDescriptor.toProto}
       """.stripMargin)

    val protoWriter = new ProtoParquetWriter[ProtoIntArray](path, classOf[ProtoIntArray])
    protoWriter.write(ProtoIntArray.newBuilder().addAllF((1 to 3).map(Int.box).asJava).build())
    protoWriter.close()

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
