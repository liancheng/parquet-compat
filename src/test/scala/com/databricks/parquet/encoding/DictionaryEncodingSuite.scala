package com.databricks.parquet.encoding

import java.lang.reflect.Modifier

import com.databricks.parquet.ParquetSuite
import com.databricks.parquet.dsl._
import com.databricks.parquet.dsl.write.DirectParquetWriter
import org.apache.commons.cli.PosixParser
import org.apache.parquet.column.ParquetProperties.WriterVersion
import org.apache.parquet.hadoop.ParquetFileWriter.Mode
import org.apache.parquet.hadoop.metadata.CompressionCodecName
import org.apache.parquet.tools.Main
import org.apache.parquet.tools.command.DumpCommand
import org.apache.parquet.tools.util.PrettyPrintWriter

class DictionaryEncodingSuite extends ParquetSuite {
  test("play with dictionary encoding") {
    withTempHadoopPath { path =>
      val schema =
        """message root {
          |  required binary f (UTF8);
          |}
        """.stripMargin

      val writer =
        DirectParquetWriter
          .builder(path, schema)
          .withDictionaryEncoding(true)
          .withDictionaryPageSize(128)
          .withPageSize(128)
          .withRowGroupSize(1024 * 2)
          .withWriterVersion(WriterVersion.PARQUET_2_0)
          .withWriteMode(Mode.OVERWRITE)
          .withCompressionCodec(CompressionCodecName.UNCOMPRESSED)
          .build()

      write.directly(writer) { implicit writer =>
        import write._

        0 until 100 foreach { _ =>
          message { implicit consumer =>
            field(0, "f") {
              string("should be dictionary encoded".padTo(64, '.'))
            }
          }
        }

        0 until 100 foreach { i =>
          message { implicit consumer =>
            field(0, "f") {
              string(i.toString.padTo(64, '.'))
            }
          }
        }

        0 until 100 foreach { _ =>
          message { implicit consumer =>
            field(0, "f") {
              string("should be dictionary encoded".padTo(64, '.'))
            }
          }
        }

        0 until 100 foreach { i =>
          message { implicit consumer =>
            field(0, "f") {
              string(i.toString.padTo(64, '.'))
            }
          }
        }
      }

      val dumpCommand = new DumpCommand
      val commandLine = {
        val posixParser = new PosixParser
        val args = Array("--disable-data", path.toString)
        posixParser.parse(dumpCommand.getOptions, args, true)
      }

      fixDumpCommandOutputFormat()
      dumpCommand.execute(commandLine)
    }
  }

  private def fixDumpCommandOutputFormat(): Unit = {
    val defaultWidth = classOf[PrettyPrintWriter].getDeclaredField("DEFAULT_WIDTH")
    val modifiersField = defaultWidth.getClass.getDeclaredField("modifiers")
    modifiersField.setAccessible(true)
    modifiersField.setInt(defaultWidth, defaultWidth.getModifiers & ~Modifier.FINAL)
    defaultWidth.setInt(null, Int.MaxValue)

    Main.out = System.out
  }
}
