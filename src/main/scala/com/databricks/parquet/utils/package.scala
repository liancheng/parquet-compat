package com.databricks.parquet

import scala.collection.JavaConverters._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetFileReader
import org.apache.parquet.schema.MessageType

package object utils {
  def cleanPath(path: String): Path = {
    val hadoopPath = new Path(path)
    val fs = hadoopPath.getFileSystem(new Configuration)
    fs.delete(hadoopPath, true)
    hadoopPath
  }

  def readParquetSchema(path: String): MessageType = {
    readParquetSchema(new Path(path))
  }

  def readParquetSchema(path: Path): MessageType = {
    val configuration = new Configuration
    val fs = path.getFileSystem(configuration)
    val parquetFiles = fs.listStatus(path).toSeq.asJava

    val footers = ParquetFileReader.readAllFootersInParallel(configuration, parquetFiles, true)
    footers.asScala.head.getParquetMetadata.getFileMetaData.getSchema
  }
}
