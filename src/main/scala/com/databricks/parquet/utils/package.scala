package com.databricks.parquet

import java.nio.file.{FileSystems, Files}

import scala.collection.JavaConverters._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.schema.MessageType

package object utils {
  def outputPath(args: Array[String]): Path = {
    val path = args
      .headOption
      .map(FileSystems.getDefault.getPath(_))
      .getOrElse(Files.createTempFile("parquet-", ".parquet"))

    Files.deleteIfExists(path)
    new Path(path.toFile.getCanonicalPath)
  }

  def schemaOf(path: String): MessageType = {
    schemaOf(new Path(path))
  }

  def schemaOf(path: Path): MessageType = {
    val configuration = new Configuration
    val fs = path.getFileSystem(configuration)
    val footers = ParquetFileReader.readAllFootersInParallel(configuration, fs.getFileStatus(path))
    footers.asScala.map(_.getParquetMetadata.getFileMetaData.getSchema).reduce(_ union _)
  }
}
