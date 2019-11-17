package aksharp.codegen.mustache

import java.io.File

import aksharp.codegen.WriteToDisk
import aksharp.grpc.HelloProto
import org.fusesource.scalate._
import scalapb.GeneratedFileObject

import scala.jdk.CollectionConverters._

trait MustacheTemplateBase[A] {
  val rootElementName: String = "root"
  val templateName: String
  val engine: TemplateEngine
  val mustacheTemplateBasePath: String
  val root: A
  val basePackageName: String

  def generateCode(): String = {
    engine.layout(
      source = TemplateSource.fromFile(new File(mustacheTemplateBasePath + templateName)),
      attributes = Map[String, Any](rootElementName -> root)
    )
  }

  def writeToMain() = writeToDisk("src/main/scala")
  def writeToTest() = writeToDisk("src/test/scala")

  private def writeToDisk(
                   basePath: String
                 ): Unit = {
    WriteToDisk(
      basePath = basePath,
      packageName = s"$basePackageName.mock.client",
      scalaClass = templateName.replace(".mustache", ""),
      contents = generateCode()
    )
  }
}


object GrpcClientCodeGenerator {

  case class Root(
                   port: String,
                   host: String,
                   negotiationType: String,
                   basePackageName: String,
                   javaPackage: String,
                   services: List[Service]
                 )

  case class Service(
                      serviceName: String,
                      serviceTypeName: String
                    )

}

case class GrpcClientCodeGenerator(
                                    generatedFileObject: GeneratedFileObject,
                                    port: Int,
                                    host: String,
                                    negotiationType: String,
                                    basePackageName: String,
                                    javaPackage: String
                                  )(implicit val engine: TemplateEngine,
                                    val mustacheTemplateBasePath: String
                                  ) extends MustacheTemplateBase[GrpcClientCodeGenerator.Root] {

  import GrpcClientCodeGenerator._

  override val templateName: String = "GrpcClient.mustache"

  val services = generatedFileObject
    .javaDescriptor
    .getServices
    .asScala
    .foldLeft(List.empty[Service]) {
      (acc, s) =>
        acc :+
          Service(
            serviceName = s"${s.getName.head.toLower}${s.getName.tail}",
            serviceTypeName = s.getName
          )
    }
  override val root: Root =
    Root(
      port = port.toString,
      host = host,
      negotiationType = negotiationType,
      basePackageName = basePackageName,
      javaPackage = javaPackage,
      services = services
    )

}


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

  grpcClientCode.writeToMain

  println(grpcClientCode.generateCode())


}
