package aksharp.codegen

import aksharp.codegen.TypeMapper._
import aksharp.grpc.HelloProto
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.ServiceDescriptor
import scalapb.GeneratedFileObject

import scala.jdk.CollectionConverters._

object CodeGenFromScalaPB extends App {
  val port = 50051
  val host = "localhost"
  val basePackageName = "generated.aksharp.grpc" // where generated code should live
  val generatedFileObject: GeneratedFileObject = HelloProto // ScalaPB generated Proto file

  CodeGen(
    port = port,
    host = host,
    basePackageName = basePackageName,
    generatedFileObject = generatedFileObject
  )
}

case class CodeGen(
               port: Int,
               host: String,
               basePackageName: String,
               generatedFileObject: GeneratedFileObject,
               generatedBaseMainPath: String = "src/main/scala",
               generatedBaseTestPath: String = "src/test/scala"
             ) {


  val negotiationType = "NegotiationType.PLAINTEXT" // alternatively TLS, but should be ok hardcoded for now.

  val javaPackage = generatedFileObject.javaDescriptor.getOptions.getJavaPackage

  val grpcClientText = GenerateGrpcClient(
    generatedFileObject = generatedFileObject,
    port = port,
    host = host,
    negotiationType = negotiationType,
    basePackageName = basePackageName,
    javaPackage = javaPackage
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.client",
    scalaClass = "GrpcClient",
    contents = grpcClientText
  )

  val iGrpcClientText = GenerateIGrpcClient(
    generatedFileObject = generatedFileObject,
    packageName = basePackageName,
    javaPackage = javaPackage
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.client",
    scalaClass = "IGrpcClient",
    contents = iGrpcClientText
  )

  val grpcMockClient = GenerateGrpcMockClient(
    generatedFileObject = generatedFileObject,
    packageName = basePackageName,
    javaPackage = javaPackage
  )

  WriteToDisk(
    basePath = generatedBaseTestPath,
    packageName = s"$basePackageName.mock.client",
    scalaClass = "GrpcMockClient",
    contents = grpcMockClient
  )

  // mock services
  generatedFileObject
    .javaDescriptor.getServices.asScala
    .map(s => {
      val serviceMock = GenerateServiceMock(
        s = s,
        packageName = basePackageName,
        javaPackage = javaPackage
      )

      WriteToDisk(
        basePath = generatedBaseTestPath,
        packageName = s"$basePackageName.mock.services",
        scalaClass = s"${s.getName}Mock",
        contents = serviceMock
      )
    })

  // services
  generatedFileObject
    .javaDescriptor.getServices.asScala
    .map(s => {
      val serviceImpl = GenerateServiceImpl(
        s = s,
        packageName = basePackageName,
        javaPackage = javaPackage
      )

      WriteToDisk(
        basePath = generatedBaseMainPath,
        packageName = s"$basePackageName.services",
        scalaClass = s"${s.getName}Impl",
        contents = serviceImpl
      )
    })


  val server = GenerateGrpcServer(
    generatedFileObject = generatedFileObject,
    port = port,
    basePackageName = basePackageName,
    javaPackage = javaPackage
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.server",
    scalaClass = "GrpcServer",
    contents = server
  )

  val grpcServerMain = GenerateGrpcServerMain(
    generatedFileObject = generatedFileObject,
    basePackageName = basePackageName
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.server",
    scalaClass = "GrpcServerMain",
    contents = grpcServerMain
  )

}

object GenerateGrpcServerMainRunServiceParams {
  def apply(
             generatedFileObject: GeneratedFileObject
           ): String = {
    generatedFileObject
      .javaDescriptor.getServices.asScala
      .map(s => {
        s"    ${s.getName.head.toLower}${s.getName.tail} = new ${s.getName}Impl"
      }).mkString(",   \n")
  }
}

object GenerateGrpcServerMain {
  def apply(
             generatedFileObject: GeneratedFileObject,
             basePackageName: String
           ): String = {
    s"""
       |package $basePackageName.server
       |
       |import $basePackageName.services._
       |
       |import scala.concurrent.ExecutionContext
       |
       |object GrpcServerMain extends App {
       |
       |  implicit val ec = ExecutionContext.global
       |
       |  GrpcServer.run(
       |${GenerateGrpcServerMainRunServiceParams(generatedFileObject)}
       |  )
       |
       |}
       |
       |""".stripMargin
  }
}

object GenerateGrpcServerAddServices {
  def apply(
             generatedFileObject: GeneratedFileObject
           ): String = {
    generatedFileObject
      .javaDescriptor.getServices.asScala
      .map(s => {
        s"      .addService(${s.getName}Grpc.bindService(${s.getName.head.toLower}${s.getName.tail}, ec))"
      }).mkString("     \n")
  }
}

object GenerateGrpcServerRunServiceParams {
  def apply(
             generatedFileObject: GeneratedFileObject
           ): String = {
    generatedFileObject
      .javaDescriptor.getServices.asScala
      .map(s => {
        s"    ${s.getName.head.toLower}${s.getName.tail}: ${s.getName}Grpc.${s.getName}"
      }).mkString(",    \n")
  }
}

object GenerateGrpcServer {
  def apply(
             generatedFileObject: GeneratedFileObject,
             port: Int,
             basePackageName: String,
             javaPackage: String
           ): String = {
    s"""
       |package $basePackageName.server
       |
       |import $javaPackage._
       |import com.typesafe.scalalogging.LazyLogging
       |import io.grpc.Server
       |import io.grpc.netty.NettyServerBuilder
       |import scala.concurrent.ExecutionContext
       |
       |object GrpcServer extends LazyLogging { self =>
       |  private[this] var server: Server = null
       |
       |  private val port = $port
       |
       |  def run(
       |${GenerateGrpcServerRunServiceParams(generatedFileObject)}
       |)
       |(implicit ec: ExecutionContext): Unit = {
       |    server = NettyServerBuilder
       |      .forPort(port)
       |${GenerateGrpcServerAddServices(generatedFileObject)}
       |      .build
       |      .start
       |
       |    logger.info("Server started, listening on " + port)
       |    sys.addShutdownHook {
       |      System.err.println("*** shutting down gRPC server since JVM is shutting down")
       |      self.stop()
       |      System.err.println("*** server shut down")
       |    }
       |
       |    server.awaitTermination()
       |  }
       |
       |  def stop(): Unit = {
       |    if (server != null) {
       |      server.shutdownNow()
       |    }
       |  }
       |
       |}
       |
       |""".stripMargin
  }
}

object GenerateServiceImpl {
  def apply(
             s: ServiceDescriptor,
             packageName: String,
             javaPackage: String
           ): String = {
    s"""
       |package ${packageName}.services
       |
       |import $javaPackage._
       |
       |import scala.concurrent.Future
       |
       |class ${s.getName}Impl extends ${s.getName}Grpc.${s.getName} {
       |
       |${
      s.getMethods.asScala.map(m => {
        s"  override def ${m.getName.head.toLower}${m.getName.tail}(request: ${m.getInputType.getName}): Future[${m.getOutputType.getName}] = ???"
      }).mkString("\n")
    }
       |}
       |
       |""".stripMargin
  }
}

object TypeMapper {
  private val m = Map[JavaType, String](
    JavaType.BOOLEAN -> "Boolean",
    JavaType.BYTE_STRING -> "Array[Byte]",
    JavaType.DOUBLE -> "Double",
    JavaType.ENUM -> "JavaType.ENUM is not supported",
    JavaType.FLOAT -> "Float",
    JavaType.INT -> "Int",
    JavaType.LONG -> "Long",
    JavaType.MESSAGE -> "JavaType.MESSAGE is not supported",
    JavaType.STRING -> "String"
  )

  def toScalaType(javaType: JavaType): String = {
    m.getOrElse(javaType, s"Could not find match for JavaType: ${javaType.toString}")
  }
}


object GenerateMockMessage {
  private def fieldAssignmentList(
                                   messageType: com.google.protobuf.Descriptors.Descriptor
                                 ): String = {
    messageType.getFields.asScala.map(f => {
      s"${f.getName} = ${f.getName} "
    }).mkString(",\n")
  }

  private def getGeneratorForType(
                                   scalaType: String
                                 ): String = {


    val generators = Map(
      "String" -> "Gen.alphaNumStr.sample.get",
      "Boolean" -> "Gen.oneOf(Seq(true, false)).sample.get",
      "Double" -> "Gen.choose(min = Double.MinValue, max = Double.MaxValue).sample.get",
      "Float" -> "Gen.choose(min = Float.MinValue, max = Float.MaxValue).sample.get",
      "Int" -> "Gen.choose(min = Int.MinValue, max = Int.MaxValue).sample.get",
      "Long" -> "Gen.choose(min = Long.MinValue, max = Long.MaxValue).sample.get",
      "Array[Byte]" -> "Gen.alphaNumStr.sample.get.getBytes"
    )

    generators.getOrElse(scalaType, s"Generator not found for Scala Type $scalaType")

  }

  private def argumentListWithDefaults(
                                        messageType: com.google.protobuf.Descriptors.Descriptor
                                      ): String = {
    messageType.getFields.asScala.map(f => {
      val scalaType: String = toScalaType(f.getJavaType)
      s"${f.getName}: $scalaType = ${getGeneratorForType(scalaType)}"
    }).mkString(",\n")
  }

  def apply(
             messageType: com.google.protobuf.Descriptors.Descriptor
           ): String = {
    s"""
       |  def a${messageType.getName}(
       |                    ${argumentListWithDefaults(messageType)}
       |                 ): ${messageType.getName} = ${messageType.getName}(
        ${fieldAssignmentList(messageType)}
       |  )
       |""".stripMargin
  }
}

object GenerateMockMessages {
  def apply(s: ServiceDescriptor): String = {
    s.getMethods.asScala.map(m => {
      List(
        GenerateMockMessage(
          messageType = m.getInputType
        ),
        GenerateMockMessage(
          messageType = m.getOutputType
        )
      ).mkString("\n")
    }).mkString("\n")
  }
}

object GenerateServiceMockInner {
  def apply(s: ServiceDescriptor): String = {
    s"""
       |case class ${s.getName}Mock(
       |${
      s.getMethods.asScala.map(m => {
        s"  ${m.getName.head.toLower}${m.getName.tail}Mock: ${m.getInputType.getName} => Future[${m.getOutputType.getName}] = _ => Future.successful(a${m.getOutputType.getName}())"
      }).mkString(",\n")
    }
       |) extends ${s.getName}Grpc.${s.getName} {
       |${
      s.getMethods.asScala.map(m => {
        s"  override def ${m.getName.head.toLower}${m.getName.tail}(request: ${m.getInputType.getName}): Future[${m.getOutputType.getName}] = ${m.getName.head.toLower}${m.getName.tail}Mock(request)"
      }).mkString("\n")
    }
       |}
       |""".stripMargin
  }
}

object GenerateServiceMock {
  def apply(
             s: ServiceDescriptor,
             packageName: String,
             javaPackage: String
           ): String = {
    s"""
       |package $packageName.mock.services
       |
       |import $javaPackage._
       |import org.scalacheck.Gen
       |import ${s.getName}Mock._
       |import scala.concurrent.Future
       |
       |object ${s.getName}Mock {
       |${GenerateMockMessages(s)}
       |}
       |
       |${GenerateServiceMockInner(s)}
       |""".stripMargin
  }
}


object GenerateGrpcMockClientInner {
  def apply(
             generatedFileObject: GeneratedFileObject
           ): String = {
    generatedFileObject
      .javaDescriptor.getServices.asScala
      .map(s => {

        s"  ${s.getName.head.toLower}${s.getName.tail}: ${s.getName}Grpc.${s.getName} = new ${s.getName}Mock"
      }).mkString(",\n")
  }
}

object GenerateGrpcMockClient {
  def apply(
             generatedFileObject: GeneratedFileObject,
             packageName: String,
             javaPackage: String
           ): String = {
    s"""
       |package $packageName.mock.client
       |
       |import $javaPackage._
       |import $packageName.client.IGrpcClient
       |import $packageName.mock.services._
       |
       |case class GrpcMockClient(
       |${GenerateGrpcMockClientInner(generatedFileObject)}
       |) extends IGrpcClient
       |""".stripMargin
  }
}

object GenerateIGrpcClient {
  def apply(
             generatedFileObject: GeneratedFileObject,
             packageName: String,
             javaPackage: String
           ): String = {
    s"""
       |package $packageName.client
       |
       |${
      generatedFileObject.javaDescriptor.getServices.asScala
        .map(s => {
          s"import $javaPackage.${s.getName}Grpc.${s.getName}"
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
             basePackageName: String,
             javaPackage: String
           ): String = {
    s"""
       |package $basePackageName.client
       |
       |import $javaPackage._
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
  lazy val ${s.getName.head.toLower}${s.getName.tail}: ${s.getName}Grpc.${s.getName}Stub = ${s.getName}Grpc.stub(
    channel = NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(negotiationType)
      .build
    )
  """
        }).
        mkString("\n")
    }

       |}

       |""".stripMargin
  }
}