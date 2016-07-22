import scalariform.formatter.preferences._

import com.github.bigtoast.sbtthrift.{ThriftPlugin => Thrift}
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys.preferences
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import pl.project13.scala.sbt.JmhPlugin
import sbt.Keys._
import sbt._
import sbtavro.{SbtAvro => Avro}
import sbtprotobuf.{ProtobufPlugin => ProtoBuf}

object Build extends sbt.Build {
  lazy val parquetCompat =
    Project("parquet-compat", file("."))
      .enablePlugins(JmhPlugin)
      .enablePlugins(SbtScalariform)
      .settings(basicSettings: _*)
      .settings(avroSettings: _*)
      .settings(thriftSettings: _*)
      .settings(protobufSettings: _*)
      .settings(dependencySettings: _*)
      .settings(scalariformSettings: _*)

  lazy val basicSettings =
    Seq(
      organization := "com.databricks",
      version := "0.1.0-SNAPSHOT",
      scalaVersion := Dependencies.Versions.scala,
      scalacOptions ++= Seq("-unchecked", "-deprecation"),
      javacOptions ++= Seq("-source", "1.6", "-target", "1.6", "-g"),
      fork := true)

  lazy val dependencySettings =
    graphSettings ++ Seq(
      retrieveManaged := false,
      resolvers ++= Dependencies.extraResolvers,
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
      // Avro version
      version in Avro.avroConfig := Dependencies.Versions.avro)

  lazy val thriftSettings =
    Thrift.thriftSettings ++ Seq(
      // Location of Thrift source files
      Thrift.thriftSourceDir in Thrift.Thrift <<= sourceDirectory(_ / "main" / "thrift"),
      // Location of the generated "gen-java" folder
      Thrift.thriftOutputDir in Thrift.Thrift <<= sourceManaged(_ / "main" / "thrift"),
      // Appends the generated "gen-java" folder to managed source directories
      managedSourceDirectories in Compile <+= sourceManaged(_ / "main" / "thrift" / "gen-java"))

  lazy val protobufSettings =
    ProtoBuf.protobufSettings ++ Seq(
      // Location of ProtocolBuffer source files
      sourceDirectory in ProtoBuf.protobufConfig <<= sourceDirectory(_ / "main" / "protobuf"),
      // Location of generated Java files
      javaSource in ProtoBuf.protobufConfig <<= sourceManaged(_ / "main" / "proto" / "gen-java"))

  lazy val scalariformSettings =
    SbtScalariform.scalariformSettings ++ Seq(
      preferences := preferences.value
        .setPreference(DoubleIndentClassDeclaration, false)
        .setPreference(SpacesAroundMultiImports, false)
        .setPreference(PreserveDanglingCloseParenthesis, true))
}

object Dependencies {
  object Versions {
    val asm = "5.0.4"
    val avro = "1.7.7"
    val commonsCodec = "1.6"
    val commonsLang = "2.6"
    val commonsLogging = "1.1.1"
    val elephantBird = "4.4"
    val hadoop = "1.2.1"
    val jackson = "1.9.13"
    val log4j = "1.2.16"
    val parquetFormat = "2.3.0-incubating"
    val parquetMr = "1.8.1"
    val paranamer = "2.6"
    val protobuf = "2.5.0"
    val scala = "2.10.4"
    val scalaMeter = "0.7"
    val scalaTest = "2.2.5"
    val scopt = "3.3.0"
    val slf4j = "1.6.4"
    val snappy = "1.1.1.6"
    val thrift = "0.9.2"
  }

  val extraResolvers = Seq(
    Resolver.mavenLocal,
    Resolver.sonatypeRepo("public"),
    "Twitter Maven" at "http://maven.twttr.com")

  val asm = Seq(
    "org.ow2.asm" % "asm" % Versions.asm)

  val avro = Seq(
    "org.apache.avro" % "avro" % Versions.avro)

  val commons = Seq(
    "commons-codec" % "commons-codec" % Versions.commonsCodec,
    "commons-lang" % "commons-lang" % Versions.commonsLang,
    "commons-logging" % "commons-logging" % Versions.commonsLogging)

  val elephantBird = Seq(
    "com.twitter.elephantbird" % "elephant-bird-core" % Versions.elephantBird)

  val jackson = Seq(
    "org.codehaus.jackson" % "jackson-core-asl" % Versions.jackson,
    "org.codehaus.jackson" % "jackson-mapper-asl" % Versions.jackson)

  val hadoop = Seq(
    "org.apache.hadoop" % "hadoop-core" % Versions.hadoop)

  val log4j = Seq(
    "log4j" % "log4j" % Versions.log4j)

  val parquetFormat = Seq(
    "org.apache.parquet" % "parquet-format" % Versions.parquetFormat)

  val paranamer = Seq(
    "com.thoughtworks.paranamer" % "paranamer" % "2.6")

  val parquetMr = Seq(
    "org.apache.parquet" % "parquet-avro" % Versions.parquetMr,
    "org.apache.parquet" % "parquet-thrift" % Versions.parquetMr,
    "org.apache.parquet" % "parquet-protobuf" % Versions.parquetMr,
    "org.apache.parquet" % "parquet-hive" % Versions.parquetMr,
    "org.apache.parquet" % "parquet-tools" % Versions.parquetMr)

  val protobuf = Seq(
    "com.google.protobuf" % "protobuf-java" % Versions.protobuf)

  val scala = Seq(
    "org.scala-lang" % "scala-library" % Versions.scala,
    "org.scala-lang" % "scala-reflect" % Versions.scala)

  val scalaMeter = Seq(
    "com.storm-enroute" %% "scalameter" % Versions.scalaMeter)


  val scalaTest = Seq(
    "org.scalatest" %% "scalatest" % Versions.scalaTest % "test")

  val scopt = Seq(
    "com.github.scopt" %% "scopt" % Versions.scopt)

  val slf4j = Seq(
    "org.slf4j" % "slf4j-api" % Versions.slf4j,
    "org.slf4j" % "slf4j-log4j12" % Versions.slf4j,
    "org.slf4j" % "jul-to-slf4j" % Versions.slf4j)

  val snappy = Seq(
    "org.xerial.snappy" % "snappy-java" % Versions.snappy)

  val thrift = Seq(
    "org.apache.thrift" % "libthrift" % Versions.thrift)

  val test = scalaMeter ++ scalaTest

  val all = test ++ hadoop ++ log4j ++ parquetMr ++ scalaMeter ++ scopt ++ slf4j ++ thrift

  val overrides = Set.empty ++
    asm ++ avro ++ commons ++ elephantBird ++ jackson ++ paranamer ++ parquetFormat ++
    protobuf ++ scala ++ snappy ++ thrift ++ slf4j
}
