package com.databricks.parquet.benchmark

import java.util.concurrent.TimeUnit

import com.databricks.parquet.dsl.read
import com.databricks.parquet.utils
import org.openjdk.jmh.annotations._
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.OptionsBuilder

/**
 * Reading a Parquet file with an empty data model that just discards everything.  Used to profile
 * Parquet reading performance.
 *
 * How to run:
 * {{{
 *   $ ./build/sbt
 *   > set javaOptions += "-Dbenchmark.input-file='path/to/parquet/file'"
 *   > jmh:runMain com.databricks.parquet.benchmark.BlackholeBenchmark
 * }}}
 */
@State(Scope.Benchmark)
class BlackholeBenchmark {
  private val inputFile = System.getProperty("benchmark.input-file")

  private val schema = {
    assert(inputFile != null, "Please set `benchmark.input-file` to the input Parquet file path")
    utils.schemaOf(inputFile)
  }

  @Benchmark
  @BenchmarkMode(Array(Mode.AverageTime))
  @OutputTimeUnit(TimeUnit.MILLISECONDS)
  def readParquetFile(): Unit = {
    read.blackhole(inputFile, schema)
  }
}

object BlackholeBenchmark {
  def main(args: Array[String]) {
    new Runner(
      new OptionsBuilder()
        .include(classOf[BlackholeBenchmark].getSimpleName)
        .warmupIterations(1)
        .measurementIterations(5)
        .forks(1)
        .build())
      .run()
  }
}
