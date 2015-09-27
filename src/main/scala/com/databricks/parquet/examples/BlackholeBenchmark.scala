package com.databricks.parquet.examples

import java.io.File

import com.databricks.parquet.dsl.read
import com.databricks.parquet.utils
import org.scalameter.{Key, config}
import scopt.OptionParser

/**
 * Reading a Parquet file with an empty data model that just discards everything.  Used to profile
 * Parquet reading performance.
 */
object BlackholeBenchmark {
  private val programName = BlackholeBenchmark.getClass.getSimpleName.stripSuffix("$")

  private case class Config(path: Option[String] = None, runs: Int = 1, warmup: Boolean = false)

  def main(args: Array[String]) {
    new OptionParser[Config](programName) {
      opt[Int]('r', "runs")
        .optional()
        .text("Specifies how many times to run the benchmark")
        .action { (runs, config) => config.copy(runs = runs) }

      opt[Unit]('w', "warmup")
        .optional()
        .text("Requires JVM warmup")
        .action { (_, config) => config.copy(warmup = true) }

      arg[File]("<input-file>")
        .required()
        .text("The Parquet file to read")
        .action { (file, config) => config.copy(path = Some(file.getCanonicalPath)) }
    }.parse(args, Config()).foreach { case Config(Some(path), runs, warmup) =>
      val schema = utils.schemaOf(path)
      val time = config(Key.exec.benchRuns -> runs, Key.verbose -> warmup).measure {
        read.blackhole(path, schema)
      }

      println(s"Total time: $time")
    }
  }
}
