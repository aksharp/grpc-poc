package aksharp.codegen.mustache

import org.fusesource.scalate.TemplateEngine
import scalapb.GeneratedFileObject
import scala.jdk.CollectionConverters._
import GrpcClientCodeGenerator._

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
  override val templateData: Root =
    Root(
      port = port.toString,
      host = host,
      negotiationType = negotiationType,
      basePackageName = basePackageName,
      javaPackage = javaPackage,
      services = services
    )

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