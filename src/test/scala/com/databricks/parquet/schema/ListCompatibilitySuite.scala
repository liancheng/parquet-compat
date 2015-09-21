package com.databricks.parquet.schema

import scala.collection.JavaConverters._

import com.databricks.parquet.ParquetSuite
import com.databricks.parquet.avro._
import com.databricks.parquet.dsl._
import org.apache.avro.generic.{GenericData, GenericRecord}

class ListCompatibilitySuite extends ParquetSuite {
  test("PARQUET-364: read a Parquet record containing an Avro \"array<array<int>>\"") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required group f (LIST) {
          |    repeated group array (LIST) {
          |      repeated int32 array;
          |    }
          |  }
          |}
        """.stripMargin

      writeDirect(path.toString, schema) { implicit writer =>
        message { implicit consumer =>
          field(0, "f") {
            group {
              field(0, "array") {
                group {
                  field(0, "array") {
                    int(0)
                    int(1)
                  }
                }

                group {
                  field(0, "array") {
                    int(2)
                    int(3)
                  }
                }
              }
            }
          }
        }
      }

      withAvroParquetReader[GenericRecord](path) { reader =>
        val expected = Seq(
          Seq(0: Integer, 1: Integer).asJava,
          Seq(2: Integer, 3: Integer).asJava).asJava

        assert(reader.read().get("f") === expected)
      }
    }
  }

  test("PARQUET-364: read a Parquet record containing a Thrift \"list<list<i32>>\"") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required group f (LIST) {
          |    repeated group f_tuple (LIST) {
          |      repeated int32 f_tuple_tuple;
          |    }
          |  }
          |}
        """.stripMargin

      writeDirect(path.toString, schema) { implicit writer =>
        message { implicit consumer =>
          field(0, "f") {
            group {
              field(0, "f_tuple") {
                group {
                  field(0, "f_tuple_tuple") {
                    int(0)
                    int(1)
                  }
                }

                group {
                  field(0, "f_tuple_tuple") {
                    int(2)
                    int(3)
                  }
                }
              }
            }
          }
        }
      }

      withAvroParquetReader[GenericRecord](path) { reader =>
        val expected = Seq(
          Seq(0: Integer, 1: Integer).asJava,
          Seq(2: Integer, 3: Integer).asJava).asJava

        assert(reader.read().get("f") === expected)
      }
    }
  }

  test("PARQUET-364: read a Parquet record containing an Avro array of single-element record") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required group f (LIST) {
          |    repeated group array {
          |      required int32 element;
          |    }
          |  }
          |}
        """.stripMargin

      writeDirect(path.toString, schema) { implicit writer =>
        message { implicit consumer =>
          field(0, "f") {
            group {
              field(0, "array") {
                group { field(0, "element") { int(0) } }
                group { field(0, "element") { int(1) } }
              }
            }
          }
        }
      }

      withAvroParquetReader[GenericRecord](path) { reader =>
        val record = reader.read()
        val f = record.get("f").asInstanceOf[GenericData.Array[GenericRecord]]
        assert(f.get(0).get("element") === 0)
        assert(f.get(1).get("element") === 1)
      }
    }
  }

  test("PARQUET-364: Profile example provided in parquet-dev list") {
    withTempHadoopPath { path =>
      val expected = {
        val nest2 = Seq.tabulate(2) { i =>
          val nest1 = Nest1.newBuilder().setMpi(0 + i).setTs(1 + i).build()
          Nest2.newBuilder().setNest1(nest1).build()
        }.asJava
        Profile.newBuilder().setNest2(nest2).build()
      }

      withAvroParquetWriter[Profile](path, Profile.getClassSchema) { writer =>
        writer.write(expected)
      }

      withAvroParquetReader[Profile](path) { reader =>
        assert(reader.read() === expected)
      }
    }
  }

  test("write an Avro array of optional integers") {
    withTempHadoopPath { path =>
      withAvroParquetWriter[AvroArrayOfOptionalInts](
        path, AvroArrayOfOptionalInts.getClassSchema) { writer =>
        val arrayOfOptionalInts = Seq(1: Integer, null, 2: Integer, null).asJava
        writer.write(AvroArrayOfOptionalInts.newBuilder().setF(arrayOfOptionalInts).build())
      }
    }
  }
}
