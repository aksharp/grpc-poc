package aksharp.codegen

import aksharp.grpc.HelloProto
import scalapb.GeneratedFileObject

import scala.jdk.CollectionConverters._

object CodeGen extends App {

  val port = 50051
  val host = "localhost"
  val negotiationType = "NegotiationType.PLAINTEXT"
  val packageName = "aksharp.grpc"

  val generatedFileObject: GeneratedFileObject = HelloProto

  val grpcClientText = GenerateGrpcClient(
    generatedFileObject = generatedFileObject,
    port = port,
    host = host,
    negotiationType = negotiationType,
    packageName = packageName
  )

  val iGrpcClientText = GenerateIGrpcClient(
    generatedFileObject = generatedFileObject,
    packageName = packageName
  )

  println(iGrpcClientText)

}

object GenerateIGrpcClient {
  def apply(
             generatedFileObject: GeneratedFileObject,
             packageName: String
           ): String = {
    s"""
       |package $packageName.client
       |
       |${
      generatedFileObject.javaDescriptor.getServices.asScala
        .map(s => {
          s"import $packageName.${s.getName}Grpc.${s.getName}"
        }).mkString("\n")
    }
       |
       |trait IGrpcClient {
       |${
      generatedFileObject
        .javaDescriptor.getServices.asScala
        .map(s => {
          s"  val ${s.getName.head.toLower}${s.getName.tail}: ${s.getName}"
        }).mkString("\n")
    }
       |}
       |
       |""".stripMargin
  }
}

object GenerateGrpcClient {
  def apply(
             generatedFileObject: GeneratedFileObject,
             port: Int,
             host: String,
             negotiationType: String,
             packageName: String
           ): String = {
    s"""
       |package $packageName.client
       |
       |import $packageName.{GreeterGrpc, YellerGrpc}
       |import io.grpc.netty.{NegotiationType, NettyChannelBuilder}
       |
       |object GrpcClient extends IGrpcClient {
       |
       |  private val host = "$host"
       |  private val port = $port

       |  private val negotiationType: NegotiationType = $negotiationType

       |${
      generatedFileObject.javaDescriptor.getServices.asScala
        .map(s => {
          s"""
  lazy val greeter: ${s.getName}Grpc.${s.getName}Stub = ${s.getName}Grpc.stub(
    channel = NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(negotiationType)
  """
        }).
        mkString("\n")
    }

       |}

       |""".stripMargin
  }
}