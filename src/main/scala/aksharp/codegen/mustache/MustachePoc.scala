package aksharp.codegen.mustache

import aksharp.grpc.HelloProto
import org.fusesource.scalate._
import scalapb.GeneratedFileObject

object MustachePoc extends App {

  val generatedFileObject: GeneratedFileObject = HelloProto
  val port = 50051
  val host = "localhost"
  val basePackageName = "generated.aksharp.grpc"
  val negotiationType = "NegotiationType.PLAINTEXT" // alternatively TLS, but should be ok hardcoded for now.
  val javaPackage = generatedFileObject.javaDescriptor.getOptions.getJavaPackage
  implicit val engine: TemplateEngine = new TemplateEngine
  implicit val mustacheTemplateBasePath: String = "src/main/scala/aksharp/codegen/mustache/"

  val grpcClientCode = GrpcClientCodeGenerator(
    generatedFileObject = generatedFileObject,
    port = port,
    host = host,
    negotiationType = negotiationType,
    basePackageName = basePackageName,
    javaPackage = javaPackage
  )

  grpcClientCode.writeToMain(
    packageName = s"$basePackageName.client"
  )

  println(grpcClientCode.generateCode())


}
