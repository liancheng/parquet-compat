This is an experimental repository used to investigate various Parquet compatibility/interoperability issues among various Parquet data model implementations like parquet-avro, parquet-thrift, and parquet-protobuf.  It contains:

- Code samples that illustrate various compatibility/interoperability issues.
- A Scala DSL for constructing Parquet files with arbitrary complex structures.

  This DSL is originally inspired by the the ``TestArrayCompatibility.writeDirect()`` method in parquet-avro test code.  I found it super convenient for constructing sample Parquet files to help reproducing compatibility/interoperability bugs.

  Another cool thing is that, with the help of SBT Scala console, you can even use this DSL interactively.  For example, the SBT Scala console snippet below creates a Parquet file described in `Dremel made simple with Parquet`__::

    $ sbt console
    ...
    scala> :paste
    // Entering paste mode (ctrl-D to finish)

    val schema =
      """message AddressBook {
        |  required binary owner (UTF8);
        |  repeated binary ownerPhoneNumbers (UTF8);
        |  repeated group contacts {
        |    required binary name (UTF8);
        |    optional binary phoneNumber (UTF8);
        |  }
        |}
      """.stripMargin

    import com.databricks.parquet.dsl._

    writeDirect("/tmp/parquet/blog-sample.parquet", schema) { implicit writer =>
      message { implicit consumer =>
        field(0, "owner") {
          string("Julien Le Dem")
        }

        field(1, "ownerPhoneNumbers") {
          string("555 123 4567")
          string("555 666 1337")
        }

        field(2, "contacts") {
          group {
            field(0, "name") {
              string("Dmitriy Ryaboy")
            }

            field(1, "phoneNumber") {
              string("555 987 6543")
            }
          }

          group {
            field(0, "name") {
              string("Chris Aniszczyk")
            }
          }
        }
      }

      message { implicit consumer =>
        field(0, "owner") {
          string("A. Nonymous")
        }
      }
    }

    // Exiting paste mode, now interpreting.

__ https://blog.twitter.com/2013/dremel-made-simple-with-parquet
