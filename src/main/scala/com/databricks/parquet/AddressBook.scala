package com.databricks.parquet

import com.databricks.parquet.utils.cleanPath

// https://blog.twitter.com/2013/dremel-made-simple-with-parquet
object AddressBook {
  def main(args: Array[String]) {
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

    writeDirect(cleanPath(args.head).toString, schema) { implicit writer =>
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
