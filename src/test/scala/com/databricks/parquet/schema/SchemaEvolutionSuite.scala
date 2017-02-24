package com.databricks.parquet.schema

import com.databricks.parquet.ParquetSuite
import com.databricks.parquet.avro.{AvroParquet370, AvroParquet370Nested}
import com.databricks.parquet.dsl.{read, write}
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.avro.{AvroParquetReader, AvroReadSupport}
import org.apache.parquet.io.ParquetDecodingException
import org.apache.parquet.schema.Types
import org.apache.parquet.schema.PrimitiveType.PrimitiveTypeName._
import org.apache.parquet.schema.OriginalType._
import org.scalatest.exceptions.TestFailedException

class SchemaEvolutionSuite extends ParquetSuite {
  test("PARQUET-370: nested records are not properly read if none of their fields are requested") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required group n {
          |    optional int32 a;
          |    optional int32 b;
          |  }
          |}
        """.stripMargin

      write.directly(path, schema) { implicit writer =>
        write.message { implicit consumer =>
          write.field(0, "n") {
            write.group {
              write.field(0, "a") { write.int(0) }
              write.field(1, "b") { write.int(1) }
            }
          }
        }
      }

      val reader = {
        val conf = new Configuration
        AvroReadSupport.setRequestedProjection(conf, AvroParquet370.getClassSchema)
        AvroParquetReader.builder[AvroParquet370](path).withConf(conf).build()
      }

      withParquetReader(reader) { reader =>
        val actual = reader.read()
        val expected =
          AvroParquet370.newBuilder()
            .setN(
              AvroParquet370Nested.newBuilder()
                .setC(null)
                .setD(null)
                .build()
            ).build()

        expectException[TestFailedException] {
          assert(actual.toString === expected.toString)
        }
      }
    }
  }

  test("PARQUET-379: merge primitive types") {
    val expected =
      Types.buildMessage()
        .addField(
          Types
            .required(INT32)
            .as(DECIMAL)
            .precision(9)
            .scale(0)
            .named("f")
        ).named("root")

    expectException[TestFailedException] {
      assert(expected.union(expected) === expected)
    }
  }

  test("PARQUET-893") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required group f0 {
          |    optional int32 f00;
          |  }
          |  required int32 f1;
          |}
          |""".stripMargin

      write.directly(path, schema) { implicit writer =>
        write.message { implicit consumer =>
          write.field(0, "f0") {
            write.group {
              write.field(0, "f00") { write.int(0) }
            }
          }

          write.field(1, "f1") { write.int(0) }
        }
      }

      val requestedSchema =
        """message root {
          |  required group f0 {
          |    optional int32 f01;
          |  }
          |  required int32 f1;
          |}
          |""".stripMargin

      expectException[ParquetDecodingException] {
        read.directly(path, requestedSchema) { implicit events =>
          read.purge()
        }
      }
    }
  }
}
