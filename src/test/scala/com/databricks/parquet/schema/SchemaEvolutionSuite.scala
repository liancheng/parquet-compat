package com.databricks.parquet.schema

import com.databricks.parquet.ParquetSuite
import com.databricks.parquet.avro.{AvroParquet370, AvroParquet370Nested}
import com.databricks.parquet.dsl.write._
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.avro.{AvroParquetReader, AvroReadSupport}

class SchemaEvolutionSuite extends ParquetSuite {
  test("PARQUET-370: Nested records are not properly read if none of their fields are requested") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required group n {
          |    optional int32 a;
          |    optional int32 b;
          |  }
          |}
        """.stripMargin

      writeDirect(path.toString, schema) { implicit writer =>
        message { implicit consumer =>
          field(0, "n") {
            group {
              field(0, "a") { int(0) }
              field(1, "b") { int(1) }
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
                .build())
            .build()

        try {
          assert(actual === expected)
        } catch { case cause: Throwable =>
          fail(
            s"""Expected: $expected
               |Actual:   $actual
             """.stripMargin,
            cause)
        }
      }
    }
  }
}
