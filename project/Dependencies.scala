import sbt._

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
    val scala = "2.11.8"
    val scalaXml = "1.0.2"
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

  val scalaModules = Seq(
    "org.scala-lang.modules" %% "scala-xml" % Versions.scalaXml)

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
    protobuf ++ scala ++ scalaModules ++ snappy ++ thrift ++ slf4j
}
