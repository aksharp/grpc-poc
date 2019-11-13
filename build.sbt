name := "grpc-poc"

version := "0.1"

scalaVersion := "2.13.1"

//resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
//libraryDependencies += "beyondthelines" %% "grpcgatewayruntime" % "0.0.9" % "compile,protobuf"

PB.targets in Compile := Seq(
  // compile your proto files into scala source files
  scalapb.gen(
    flatPackage = true,
    grpc = true
  ) -> (sourceManaged in Compile).value

  // GATEWAY WOULD BE AMAZING IF IT WORKED AND WAS PERFORMANT
  //  // generate Swagger spec files into the `resources/specs`
  //  grpcgateway.generators.SwaggerGenerator -> (resourceDirectory in Compile).value / "specs",
  //  // generate the Rest Gateway source code
  //  grpcgateway.generators.GatewayGenerator -> (sourceManaged in Compile).value
)

val scalapbVersion = scalapb.compiler.Version.scalapbVersion
val grpcJavaVersion = scalapb.compiler.Version.grpcJavaVersion


libraryDependencies ++= Seq(
  "io.grpc" % "grpc-netty" % grpcJavaVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime-grpc" % scalapbVersion,
  "com.thesamet.scalapb" %% "scalapb-runtime" % scalapbVersion % "protobuf",
  "org.scalacheck" %% "scalacheck" % "1.14.2",
  "org.scalatest" %% "scalatest" % "3.0.8" % Test,

  // logging
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.2",
  "ch.qos.logback" % "logback-core" % "1.2.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.slf4j" % "slf4j-api" % "1.7.28"

)

val unusedWarnings = (
  "-Ywarn-unused" ::
    Nil
  )

scalacOptions ++= (
  "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    Nil
  ) ::: unusedWarnings