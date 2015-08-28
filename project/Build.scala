import com.github.bigtoast.sbtthrift.{ThriftPlugin => Thrift}
import sbt.Keys._
import sbt._
import sbtavro.{SbtAvro => Avro}
import sbtprotobuf.{ProtobufPlugin => ProtoBuf}

object Build extends sbt.Build {
  lazy val parquetCompat =
    Project("parquet-compat", file("."))
      .settings(basicSettings: _*)
      .settings(avroSettings: _*)
      .settings(thriftSettings: _*)
      .settings(protobufSettings: _*)
      .settings(libraryDependencies ++= Dependencies.parquet)
      .settings(libraryDependencies ++= Dependencies.hadoop)
      .settings(libraryDependencies ++= Dependencies.thrift)

  lazy val basicSettings =
    Seq(
      organization := "com.databricks",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.4",
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
      javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-g"))

  lazy val avroSettings =
    Avro.avroSettings ++ Seq(
      // Uses java.lang.String for Avro strings
      Avro.stringType in Avro.avroConfig := "String",
      // Location of Avro source files
      sourceDirectory in Avro.avroConfig <<= sourceDirectory(_ / "main" / "avro"),
      // Location of generated Java files
      javaSource in Avro.avroConfig <<= sourceManaged(_ / "main" / "avro" / "gen-java"),
      // Adds the folder of generated Java files as a Java source folder
      javaSource in Compile <<= (javaSource in Avro.avroConfig),
      // Avro version
      version in Avro.avroConfig := Dependencies.Versions.avro)

  lazy val thriftSettings =
    Thrift.thriftSettings ++ Seq(
      // Location of Thrift source files
      Thrift.thriftSourceDir in Thrift.Thrift <<= sourceDirectory(_ / "main" / "thrift"),
      // Location of the generated "gen-java" folder
      Thrift.thriftOutputDir in Thrift.Thrift <<= sourceManaged(_ / "main" / "thrift"),
      // Adds "gen-java" as a Java source folder
      javaSource in Compile <<= sourceManaged(_ / "main" / "thrift" / "gen-java"))

  lazy val protobufSettings =
    ProtoBuf.protobufSettings ++ Seq(
      // Location of ProtocolBuffer source files
      sourceDirectory in ProtoBuf.protobufConfig <<= sourceDirectory(_ / "main" / "protobuf"),
      // Location of generated Java files
      javaSource in ProtoBuf.protobufConfig <<= sourceManaged(_ / "main" / "proto" / "gen-java"))
}

object Dependencies {
  object Versions {
    val avro = "1.7.7"
    val parquet = "1.7.0"
    val hadoop = "1.2.1"
    val thrift = "0.9.2"
  }

  val parquet = Seq(
    "org.apache.parquet" % "parquet-avro" % Versions.parquet,
    "org.apache.parquet" % "parquet-thrift" % Versions.parquet,
    "org.apache.parquet" % "parquet-protobuf" % Versions.parquet,
    "org.apache.parquet" % "parquet-hive" % Versions.parquet)

  val hadoop = Seq(
    "org.apache.hadoop" % "hadoop-core" % Versions.hadoop)

  val thrift = Seq(
    "org.apache.thrift" % "libthrift" % Versions.thrift)
}
