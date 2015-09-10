package com.databricks.parquet.interop

import scala.collection.JavaConverters._

import com.databricks.parquet.ParquetSuite
import com.databricks.parquet.avro.AvroIntArray
import com.databricks.parquet.protobuf.ParquetProtobufCompat.ProtoIntArray
import com.databricks.parquet.thrift.ThriftIntArray
import org.apache.avro.generic.GenericRecord

class InteroperabilitySuite extends ParquetSuite {
  test("read a Parquet record containing a Thrift \"list<i32>\" back as an Avro \"array<int>\"") {
    withTempHadoopPath { path =>
      val intArray = Seq(1: Integer, 2: Integer).asJava

      withThriftParquetWriter(path, classOf[ThriftIntArray]) {
        _.write(new ThriftIntArray(intArray))
      }

      withAvroParquetReader[GenericRecord](path) { reader =>
        assert(intArray === reader.read().get("f"))
      }
    }
  }

  test("read a Parquet record containing an Avro \"array<int>\" as a Thrift \"list<i32>\"") {
    withTempHadoopPath { path =>
      val intArray = Seq(1: Integer, 2: Integer).asJava

      withAvroParquetWriter[AvroIntArray](path, AvroIntArray.getClassSchema) {
        _.write(AvroIntArray.newBuilder().setF(intArray).build())
      }

      withThriftParquetReader[ThriftIntArray](path) { reader =>
        assert(intArray === reader.read().getF)
      }
    }
  }

  test("read Parquet record containing a ProtoBuf \"repeated int32\" as an Avro \"array<int>\"") {
    withTempHadoopPath { path =>
      val intArray = Seq(1: Integer, 2: Integer).asJava

      withProtoParquetWriter[ProtoIntArray](path, classOf[ProtoIntArray]) {
        _.write(ProtoIntArray.newBuilder().addAllF(intArray).build())
      }

      withAvroParquetReader[GenericRecord](path) { reader =>
        assert(intArray === reader.read())
      }
    }
  }
}
