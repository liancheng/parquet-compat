This is my experimental repository used to investigate various Parquet compatibility/interoperability issues.  One super useful feature it provides is that, you can easily construct arbitrary Parquet files using DSL interactively with the help of SBT Scala console::

    $ sbt console
    ...
    scala> :paste
    // Entering paste mode (ctrl-D to finish)

    import com.databricks.DirectParquetWriter._

    val path = "file:///tmp/parquet/parquet-hive-style.parquet"

    val schema =
      """message m {
        |  optional group f0 (LIST) {
        |    repeated group bag {
        |      optional int32 array_element;
        |    }
        |  }
        |}
      """.stripMargin

    writeDirect(path, schema) { implicit writer =>
      message { implicit rc =>
        field("f0") {
          group {
            field("bag") {
              group {
                field("array_element") {
                  rc.addInteger(0)
                  rc.addInteger(1)
                }
              }
            }
          }
        }
      }

      message { implicit rc =>
        field("f0") {
          group {
            field("bag") {
              group {
                field("array_element") {
                  rc.addInteger(2)
                  rc.addInteger(3)
                }
              }
            }
          }
        }
      }
    }

    // Exiting paste mode, now interpreting.
