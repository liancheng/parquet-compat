package com.databricks.parquet

import java.io.File
import java.nio.file.Files
import java.util.logging.{Logger => JulLogger}

import com.google.protobuf.Message
import org.apache.avro.Schema
import org.apache.avro.generic.IndexedRecord
import org.apache.hadoop.fs.Path
import org.apache.parquet.Log
import org.apache.parquet.avro.{AvroParquetReader, AvroParquetWriter}
import org.apache.parquet.hadoop.ParquetReader
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.proto.ProtoParquetWriter
import org.apache.parquet.thrift.{ThriftParquetReader, ThriftParquetWriter}
import org.apache.thrift.TBase
import org.scalatest.FunSuite

abstract class ParquetSuite extends FunSuite {
  val parquetLogger = JulLogger.getLogger(classOf[Log].getPackage.getName)
  parquetLogger.getHandlers.foreach(parquetLogger.removeHandler)

  private def deleteRecursively(file: File): Unit = {
    Option(file.listFiles()).toSeq.flatten.foreach(deleteRecursively)
    file.delete()
  }

  def withTempPath(f: File => Unit): Unit = {
    val directory = Files.createTempDirectory("parquet-").toFile
    directory.delete()
    try f(directory) finally deleteRecursively(directory)
  }

  def withTempStringPath(f: String => Unit): Unit = {
    val directory = Files.createTempDirectory("parquet-").toFile
    directory.delete()
    try f(directory.getCanonicalPath) finally deleteRecursively(directory)
  }

  def withTempHadoopPath(f: Path => Unit): Unit = {
    val directory = Files.createTempDirectory("parquet-").toFile
    directory.delete()
    try f(new Path(directory.getCanonicalPath)) finally deleteRecursively(directory)
  }

  def withThriftParquetWriter[T <: TBase[_, _]]
      (path: Path,
       thriftClass: Class[T],
       compressionCodecName: CompressionCodecName = CompressionCodecName.UNCOMPRESSED)
      (f: ThriftParquetWriter[T] => Unit): Unit = {
    val writer = new ThriftParquetWriter[T](path, thriftClass, compressionCodecName)
    try f(writer) finally writer.close()
  }

  def withAvroParquetWriter[T <: IndexedRecord]
      (path: Path,
       schema: Schema,
       compressionCodecName: CompressionCodecName = CompressionCodecName.UNCOMPRESSED)
      (f: AvroParquetWriter[T] => Unit): Unit = {
    val writer = new AvroParquetWriter[T](path, schema)
    try f(writer) finally writer.close()
  }

  def withProtoParquetWriter[T <: Message]
      (path: Path,
       protoClass: Class[T],
       compressionCodecName: CompressionCodecName = CompressionCodecName.UNCOMPRESSED)
      (f: ProtoParquetWriter[T] => Unit): Unit = {
    val writer = new ProtoParquetWriter[T](path, protoClass)
    try f(writer) finally writer.close()
  }

  def withParquetReader[T](reader: ParquetReader[T])(f: ParquetReader[T] => Unit): Unit = {
    try f(reader) finally reader.close()
  }

  def withThriftParquetReader[T <: TBase[_, _]]
      (path: Path)
      (f: ParquetReader[T] => Unit): Unit = {
    withParquetReader[T](ThriftParquetReader.build[T](path).build())(f)
  }

  def withAvroParquetReader[T <: IndexedRecord]
      (path: Path)
      (f: ParquetReader[T] => Unit): Unit = {
    withParquetReader[T](AvroParquetReader.builder[T](path).build())(f)
  }
}
