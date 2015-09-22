package com.databricks.parquet.examples

import com.databricks.parquet.utils._

// An DSL example that builds the sample Parquet file described in the following blog post:
//
//   https://blog.twitter.com/2013/dremel-made-simple-with-parquet
//
// This example shows the DSL API without using implicits.
object AddressBookWithoutImplicits {
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

    writeDirect(path, schema) { writer =>
      message(writer) { rc =>
        field(rc, 0, "owner") {
          string(rc, "Julien Le Dem")
        }

        field(rc, 1, "ownerPhoneNumbers") {
          string(rc, "555 123 4567")
          string(rc, "555 666 1337")
        }

        field(rc, 2, "contacts") {
          group(rc) {
            field(rc, 0, "name") {
              string(rc, "Dmitriy Ryaboy")
            }

            field(rc, 1, "phoneNumber") {
              string(rc, "555 987 6543")
            }
          }

          group(rc) {
            field(rc, 0, "name") {
              string(rc, "Chris Aniszczyk")
            }
          }
        }
      }

      message(writer) { rc =>
        field(rc, 0, "owner") {
          string(rc, "A. Nonymous")
        }
      }
    }
  }
}
