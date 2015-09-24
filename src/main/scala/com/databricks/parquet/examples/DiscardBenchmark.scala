package com.databricks.parquet.examples

import com.databricks.parquet.dsl.read
import com.databricks.parquet.utils

/**
 * Reading a Parquet file with an empty data model that just discards everything.  Used to profile
 * Parquet reading performance.
 */
object DiscardBenchmark {
  def main(args: Array[String]) {
    val inputPath = args.head
    read.discard(inputPath, utils.schemaOf(inputPath).toString)
  }
}
