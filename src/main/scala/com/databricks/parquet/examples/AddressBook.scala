package com.databricks.parquet.examples

import com.databricks.parquet.utils._

// An DSL example that builds the sample Parquet file described in the following blog post:
//
//   https://blog.twitter.com/2013/dremel-made-simple-with-parquet
//
// This example shows the DSL API using implicits.
object AddressBook {
  def main(args: Array[String]) {
    val path = outputPath(args).toString
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

    import com.databricks.parquet.dsl.write._

    writeDirect(path, schema) { implicit writer =>
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
  }
}
