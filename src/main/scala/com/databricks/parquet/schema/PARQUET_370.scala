package com.databricks.parquet.schema

import com.databricks.parquet.avro.{AvroNestedRecord, AvroSchemaMergingHelper}
import com.databricks.parquet.utils._
import org.apache.hadoop.conf.Configuration
import org.apache.parquet.avro.{AvroParquetReader, AvroReadSupport}

// https://issues.apache.org/jira/browse/PARQUET-370
object PARQUET_370 {
  def main(args: Array[String]): Unit = {
    val path = outputPath(args)
    val schema =
      """message root {
        |  required group n {
        |    optional int32 a;
        |    optional int32 b;
        |  }
        |}
      """.stripMargin

    import com.databricks.parquet.dsl._

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

    println(
      s"""Schema of Parquet file $path:
         |${readParquetSchema(path)}
       """.stripMargin)

    val reader = {
      val conf = new Configuration
      AvroReadSupport.setRequestedProjection(conf, AvroSchemaMergingHelper.getClassSchema)
      AvroParquetReader.builder[AvroSchemaMergingHelper](path).withConf(conf).build()
    }

    val expected =
      AvroSchemaMergingHelper
        .newBuilder()
        .setN(AvroNestedRecord.newBuilder().setC(null).setD(null).build())
        .build()

    println(
      s"""Expected: $expected
         |Actual:   ${reader.read()}
       """.stripMargin)

    reader.close()
  }
}
