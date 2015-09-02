import scala.collection.JavaConverters.seqAsJavaListConverter

import com.databricks.avro.{AvroNullableIntArray, AvroArrayOfIntArray, AvroIntArray}
import com.databricks.protobuf.ParquetProtobufCompat.ProtoIntArray
import com.databricks.thrift.ThriftIntArray
import org.apache.hadoop.conf.Configuration
import org.apache.hadoop.fs.Path
import org.apache.parquet.avro.{AvroParquetReader, AvroParquetWriter, AvroSchemaConverter}
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.proto.ProtoParquetWriter
import org.apache.parquet.thrift.{ThriftParquetReader, ThriftParquetWriter}

object AvroArrayOfIntArrayTest {
  def main(args: Array[String]): Unit = {
    val schema = AvroArrayOfIntArray.getClassSchema
    println(schema.toString(true))

    val messageType = new AvroSchemaConverter().convert(schema)
    println(messageType)
  }
}

object AvroNullableIntArrayTest {
  def main(args: Array[String]) {
    val schema = AvroNullableIntArray.getClassSchema
    println(schema.toString(true))

    val messageType = new AvroSchemaConverter().convert(schema)
    println(messageType)
  }
}

object ThriftToAvro {
  def main(args: Array[String]) {
    val path = new Path("file:///tmp/parquet/thrift")
    val fs = path.getFileSystem(new Configuration)
    fs.delete(path, true)

    val thriftWriter = new ThriftParquetWriter[ThriftIntArray](
      path, classOf[ThriftIntArray], CompressionCodecName.UNCOMPRESSED)

    thriftWriter.write(new ThriftIntArray((1 to 3).map(Int.box).asJava))
    thriftWriter.close()

    val avroReader = AvroParquetReader.builder[AvroIntArray](path).build()
    println(avroReader.read())
    avroReader.close()
  }
}

object AvroToThrift {
  def main(args: Array[String]) {
    val path = new Path("file:///tmp/parquet/avro")
    val fs = path.getFileSystem(new Configuration)
    fs.delete(path, true)

    val avroWriter = new AvroParquetWriter[AvroIntArray](path, AvroIntArray.getClassSchema)
    avroWriter.write(AvroIntArray.newBuilder().setF((1 to 3).map(Int.box).asJava).build())
    avroWriter.close()

    val thriftReader = ThriftParquetReader.build(path).build()
    println(thriftReader.read())
    thriftReader.close()
  }
}

object ProtoToAvro {
  def main(args: Array[String]) {
    val path = new Path("file:///tmp/parquet/proto")
    val fs = path.getFileSystem(new Configuration)
    fs.delete(path, true)

    val protoWriter = new ProtoParquetWriter[ProtoIntArray](path, classOf[ProtoIntArray])
    protoWriter.write(ProtoIntArray.newBuilder().addAllF((1 to 3).map(Int.box).asJava).build())
    protoWriter.close()

    val avroReader = AvroParquetReader.builder[AvroIntArray](path).build()
    println(avroReader.read())
    avroReader.close()
  }
}
