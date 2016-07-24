import com.github.bigtoast.sbtthrift.ThriftPlugin._
import com.typesafe.sbt.SbtScalariform.ScalariformKeys.preferences
import net.virtualvoid.sbt.graph.Plugin.graphSettings
import sbtavro.SbtAvro._
import sbtprotobuf.ProtobufPlugin._
import scalariform.formatter.preferences._

lazy val root = (project in file("."))
  .enablePlugins(JmhPlugin)
  .settings(basicSettings: _*)
  .settings(avroPluginSettings: _*)
  .settings(thriftPluginSettings: _*)
  .settings(protobufPluginSettings: _*)
  .settings(dependencySettings: _*)
  .settings(scalariformSettings: _*)

lazy val basicSettings = Seq(
  organization := "com.databricks",
  version := "0.1.0-SNAPSHOT",
  scalaVersion := Dependencies.Versions.scala,
  scalacOptions ++= Seq("-unchecked", "-deprecation", "-feature"),
  javacOptions ++= Seq("-source", "1.7", "-target", "1.7", "-g", "-Xlint:-options"),
  fork := true
)

lazy val dependencySettings = graphSettings ++ Seq(
  retrieveManaged := false,
  resolvers ++= Dependencies.extraResolvers,
  libraryDependencies ++= Dependencies.all,
  // Disables auto conflict resolution
  conflictManager := ConflictManager.strict,
  // Explicitly overrides all conflicting transitive dependencies
  dependencyOverrides ++= Dependencies.overrides
)

lazy val avroPluginSettings = avroSettings ++ Seq(
  // Uses java.lang.String for Avro strings
  stringType in avroConfig := "String",
  // Location of Avro source files
  sourceDirectory in avroConfig <<= sourceDirectory(_ / "main" / "avro"),
  // Location of generated Java files
  javaSource in avroConfig <<= sourceManaged(_ / "main" / "avro" / "gen-java"),
  // Avro version
  version in avroConfig := Dependencies.Versions.avro
)

lazy val thriftPluginSettings = thriftSettings ++ Seq(
  // Location of Thrift source files
  thriftSourceDir in Thrift <<= sourceDirectory(_ / "main" / "thrift"),
  // Location of the generated "gen-java" folder
  thriftOutputDir in Thrift <<= sourceManaged(_ / "main" / "thrift"),
  // Appends the generated "gen-java" folder to managed source directories
  managedSourceDirectories in Compile <+= sourceManaged(_ / "main" / "thrift" / "gen-java")
)

lazy val protobufPluginSettings = protobufSettings ++ Seq(
  // Location of ProtocolBuffer source files
  sourceDirectory in protobufConfig <<= sourceDirectory(_ / "main" / "protobuf"),
  // Location of generated Java files
  javaSource in protobufConfig <<= sourceManaged(_ / "main" / "proto" / "gen-java")
)

lazy val scalariformSettings = SbtScalariform.scalariformSettings ++ Seq(
  preferences := preferences.value
    .setPreference(DoubleIndentClassDeclaration, false)
    .setPreference(SpacesAroundMultiImports, false)
)
