addSbtPlugin("com.thesamet" % "sbt-protoc" % "0.99.27")

libraryDependencies += "com.thesamet.scalapb" %% "compilerplugin" % "0.9.4"


// THIS WOULD BE REAL NICE OT HAVE... ALTHOUGH DOESN'T WORK WITH LATEST SCALAPB AND SCALA 2.13
//resolvers += Resolver.bintrayRepo("beyondthelines", "maven")
//
//libraryDependencies ++= Seq(
////  "com.trueaccord.scalapb" %% "compilerplugin" % "0.6.7",
//  "beyondthelines"         %% "grpcgatewaygenerator" % "0.0.9"
//)