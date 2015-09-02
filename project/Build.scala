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
      .settings(dependencySettings: _*)

  lazy val basicSettings =
    Seq(
      organization := "com.databricks",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := "2.10.4",
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
      javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-g"))

  lazy val dependencySettings =
    Seq(
      retrieveManaged := true,
      libraryDependencies ++= Dependencies.all,
      // Disables auto conflict resolution
      conflictManager := ConflictManager.strict,
      // Explicitly overrides all conflicting transitive dependencies
      dependencyOverrides ++= Dependencies.overrides)

  lazy val avroSettings =
    Avro.avroSettings ++ Seq(
      // Uses java.lang.String for Avro strings
      Avro.stringType in Avro.avroConfig := "String",
      // Location of Avro source files
      sourceDirectory in Avro.avroConfig <<= sourceDirectory(_ / "main" / "avro"),
      // Location of generated Java files
      javaSource in Avro.avroConfig <<= sourceManaged(_ / "main" / "avro" / "gen-java"),
      // Appends Avro generated Java source directory to managed source directories
      managedSourceDirectories in Compile <+= sourceManaged(_ / "main" / "avro" / "gen-java"),
      // Avro version
      version in Avro.avroConfig := Dependencies.Versions.avro)

  lazy val thriftSettings =
    Thrift.thriftSettings ++ Seq(
      // Location of Thrift source files
      Thrift.thriftSourceDir in Thrift.Thrift <<= sourceDirectory(_ / "main" / "thrift"),
      // Location of the generated "gen-java" folder
      Thrift.thriftOutputDir in Thrift.Thrift <<= sourceManaged(_ / "main" / "thrift"),
      // Appends Thrift generated Java source directory to managed source directories
      managedSourceDirectories in Compile <+= sourceManaged(_ / "main" / "thrift" / "gen-java"))

  lazy val protobufSettings =
    ProtoBuf.protobufSettings ++ Seq(
      // Location of ProtocolBuffer source files
      sourceDirectory in ProtoBuf.protobufConfig <<= sourceDirectory(_ / "main" / "protobuf"),
      // Location of generated Java files
      javaSource in ProtoBuf.protobufConfig <<= sourceManaged(_ / "main" / "proto" / "gen-java"),
      // Appends Avro generated Java source directory to managed source directories
      managedSourceDirectories in Compile <+= sourceManaged(_ / "main" / "proto" / "gen-java"))
}

object Dependencies {
  object Versions {
    val avro = "1.7.7"
    val commonsCodec = "1.6"
    val commonsLang = "2.6"
    val commonsLogging = "1.1.1"
    val elephantBird = "4.4"
    val hadoop = "1.2.1"
    val jackson = "1.9.13"
    val parquet = "1.7.0"
    val protobuf = "2.5.0"
    val slf4j = "1.6.4"
    val snappy = "1.1.1.6"
    val thrift = "0.9.2"
  }

  val commons = Seq(
    "commons-codec" % "commons-codec" % Versions.commonsCodec,
    "commons-lang" % "commons-lang" % Versions.commonsLang,
    "commons-logging" % "commons-logging" % Versions.commonsLogging)

  val parquet = Seq(
    "org.apache.parquet" % "parquet-avro" % Versions.parquet,
    "org.apache.parquet" % "parquet-thrift" % Versions.parquet,
    "org.apache.parquet" % "parquet-protobuf" % Versions.parquet,
    "org.apache.parquet" % "parquet-hive" % Versions.parquet)

  val hadoop = Seq(
    "org.apache.hadoop" % "hadoop-core" % Versions.hadoop)

  val thrift = Seq(
    "org.apache.thrift" % "libthrift" % Versions.thrift)

  val jackson = Seq(
    "org.codehaus.jackson" % "jackson-core-asl" % Versions.jackson,
    "org.codehaus.jackson" % "jackson-mapper-asl" % Versions.jackson)

  val all = hadoop ++ parquet ++ thrift

  val overrides = Set(
    "com.google.protobuf" % "protobuf-java" % Versions.protobuf,
    "com.twitter.elephantbird" % "elephant-bird-core" % Versions.elephantBird,
    "org.apache.avro" % "avro" % Versions.avro,
    "org.slf4j" % "slf4j-api" % Versions.slf4j,
    "org.xerial.snappy" % "snappy-java" % Versions.snappy) ++ commons ++ jackson ++ thrift
}
