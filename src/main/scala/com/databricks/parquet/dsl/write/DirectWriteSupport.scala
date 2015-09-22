package com.databricks.parquet.dsl.write

import scala.collection.JavaConverters._

import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.hadoop.ParquetWriter
import org.apache.parquet.hadoop.api.WriteSupport
import org.apache.parquet.hadoop.api.WriteSupport.WriteContext
import org.apache.parquet.io.api.RecordConsumer
import org.apache.parquet.schema.{MessageType, MessageTypeParser}

private[dsl] class DirectWriteSupport(schema: MessageType, metadata: Map[String, String])
  extends WriteSupport[RecordBuilder] {

  private var recordConsumer: RecordConsumer = _

  override def init(configuration: Configuration): WriteContext = {
    new WriteContext(schema, metadata.asJava)
  }

  override def write(writeRecord: RecordBuilder): Unit = {
    recordConsumer.startMessage()
    writeRecord(recordConsumer)
    recordConsumer.endMessage()
  }

  override def prepareForWrite(recordConsumer: RecordConsumer): Unit = {
    this.recordConsumer = recordConsumer
  }
}

object DirectParquetWriter {
  class Builder(path: Path) extends ParquetWriter.Builder[RecordBuilder, Builder](path) {
    private var schema: MessageType = _

    private var metadata: Map[String, String] = Map.empty[String, String]

    def withSchema(schema: MessageType): Builder = {
      this.schema = schema
      self()
    }

    def withMetadata(metadata: Map[String, String]): Builder = {
      this.metadata = metadata
      self()
    }

    def withConf(pairs: (String, String)*): Builder = {
      val conf = new Configuration
      pairs.foreach { case (key, value) => conf.set(key, value) }
      withConf(conf)
    }

    override def getWriteSupport(conf: Configuration): WriteSupport[RecordBuilder] = {
      new DirectWriteSupport(schema, metadata)
    }

    override def self(): Builder = this
  }

  def builder(path: Path): Builder = new Builder(path)

  def builder(path: Path, schema: String): Builder =
    builder(path).withSchema(MessageTypeParser.parseMessageType(schema))
}
