package com.databricks.parquet.dsl

import com.databricks.parquet.ParquetSuite
import com.databricks.parquet.dsl.read.DirectParquetReader

class DslSuite extends ParquetSuite {
  test("direct read/write") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required int32 f0;
          |}
        """.stripMargin

      write.directly(path, schema) { implicit writer =>
        import write._

        message { implicit consumer =>
          field(0, "f0") {
            int(0)
          }
        }

        message { implicit consumer =>
          field(0, "f0") {
            int(0)
          }
        }
      }

      read.directly(path) { implicit events =>
        import read._

        message {
          field(0, "f0") {
            int(0)
          }
        }

        message {
          field(0, "f0") {
            int(0)
          }
        }
      }
    }
  }

  test("direct write and purge") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required int32 f0;
          |}
        """.stripMargin

      write.directly(path, schema) { implicit writer =>
        import write._

        message { implicit consumer =>
          field(0, "f0") {
            int(0)
          }
        }

        message { implicit consumer =>
          field(0, "f0") {
            int(0)
          }
        }
      }

      read.directly(path) { implicit events =>
        read.purge()
      }
    }
  }

  test("discard all records") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required int32 f0;
          |}
        """.stripMargin

      write.directly(path, schema) { implicit writer =>
        import write._

        message { implicit consumer =>
          field(0, "f0") {
            int(0)
          }
        }

        message { implicit consumer =>
          field(0, "f0") {
            int(0)
          }
        }
      }

      read.blackhole(path)
    }
  }

  test("read with filter") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required int32 f0;
          |}
        """.stripMargin

      write.directly(path, schema) { implicit writer =>
        import write._

        message { implicit consumer => field(0, "f0") { int(0) } }
        message { implicit consumer => field(0, "f0") { int(1) } }
        message { implicit consumer => field(0, "f0") { int(2) } }
        message { implicit consumer => field(0, "f0") { int(3) } }
      }

      val reader = {
        import read.filter._
        DirectParquetReader.builder(schema, path).withFilter(int("f0") < 0).build()
      }

      read.directly(reader) { implicit events =>
        read.nothing()
      }
    }
  }
}
