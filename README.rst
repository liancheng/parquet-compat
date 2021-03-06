.. image:: https://travis-ci.org/liancheng/parquet-compat.svg
    :target: https://travis-ci.org/liancheng/parquet-compat

Overview
========

This repository is a playground for investigating various Parquet compatibility/interoperability issues among various Parquet data model implementations like parquet-avro, parquet-thrift, and parquet-protobuf.  It contains:

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

    import com.databricks.parquet.dsl.write

    write.directly("/tmp/parquet/blog-sample.parquet", schema) { implicit writer =>
      import write._

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

.. note::

  Everything in this repository are subject to change.  It's neither a library nor an application for daily use, but just a playground.

How to build
============

This repository is only tested on Mac OS X 10.10.  To build this project, install ProtoBuf, Thrift, and Avro compilers first::

  $ brew install protobuf thrift avro-tools

Then compile and run sample code with SBT::

  $ ./build/sbt clean compile test:compile

Run examples
============

::

  $ ./build/sbt run

Run tests
=========

Run all tests::

  $ ./build/sbt test

Run specified tests::

  $ ./build/sbt test-only <test-suite-name-pattern> -- -t <test-case-name>

or::

  $ ./build/sbt test-only <test-suite-name-pattern> -- -z <test-case-name-pattern>

For example, the following command runs all test cases whose name contains "Avro"::

  $ ./build/sbt test-only "*Suite" -- -z "Avro"

__ https://blog.twitter.com/2013/dremel-made-simple-with-parquet
