package com.databricks.parquet.examples

import java.io.File

import com.databricks.parquet.dsl.read
import com.databricks.parquet.utils
import com.google.common.base.Stopwatch
import scopt.OptionParser

/**
 * Reading a Parquet file with an empty data model that just discards everything.  Used to profile
 * Parquet reading performance.
 */
object BlackholeBenchmark {
  private val programName = BlackholeBenchmark.getClass.getSimpleName.stripSuffix("$")

  private case class Config(path: Option[String] = None, runs: Int = 1, warmupRuns: Int = 0)

  // TODO Try JMH instead of homebrewed benchmark code
  def benchmark(runs: Int, warmupRuns: Int = 0)(f: => Unit) {
    val stopwatch = new Stopwatch()

    (0 until warmupRuns).foreach { i =>
      println(s"Warmup run $i...")
      f
    }

    def run(i: Int) = {
      stopwatch.reset()
      stopwatch.start()
      f
      stopwatch.stop()
      val elapsed = stopwatch.elapsedMillis()
      println(s"Round $i: $elapsed ms")
      elapsed
    }

    val total = (0 until runs).map(i => run(i)).sum.toDouble
    println(s"Average: ${total / runs} ms")
  }

  def main(args: Array[String]) {
    new OptionParser[Config](programName) {
      opt[Int]('r', "runs")
        .optional()
        .text("Specifies how many times to run the benchmark")
        .action { (runs, config) => config.copy(runs = runs) }

      opt[Int]('w', "warmup-runs")
        .optional()
        .text("Requires JVM warmup")
        .action { (runs, config) => config.copy(warmupRuns = runs) }

      arg[File]("<input-file>")
        .required()
        .text("The Parquet file to read")
        .action { (file, config) => config.copy(path = Some(file.getCanonicalPath)) }
    }.parse(args, Config()).foreach { case Config(Some(path), runs, warmup) =>
      val schema = utils.schemaOf(path)
      benchmark(5) {
        read.blackhole(path, schema)
      }
    }
  }
}
