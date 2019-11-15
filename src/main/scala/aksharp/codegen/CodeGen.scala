package aksharp.codegen

import aksharp.codegen.TypeMapper._
import aksharp.grpc.HelloProto
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType
import com.google.protobuf.Descriptors.ServiceDescriptor
import scalapb.GeneratedFileObject

import scala.jdk.CollectionConverters._

object CodeGen extends App {

  val port = 50051
  val host = "localhost"
  val negotiationType = "NegotiationType.PLAINTEXT"
  val basePackageName = "generated.aksharp.grpc"
  val generatedFileObject: GeneratedFileObject = HelloProto

  val generatedBaseMainPath = "src/main"
  val generatedBaseTestPath = "src/test"

  val grpcClientText = GenerateGrpcClient(
    generatedFileObject = generatedFileObject,
    port = port,
    host = host,
    negotiationType = negotiationType,
    basePackageName = basePackageName
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.client",
    scalaClass = "GrpcClient",
    contents = grpcClientText
  )

  val iGrpcClientText = GenerateIGrpcClient(
    generatedFileObject = generatedFileObject,
    packageName = basePackageName
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.client",
    scalaClass = "IGrpcClient",
    contents = iGrpcClientText
  )

  val grpcMockClient = GenerateGrpcMockClient(
    generatedFileObject = generatedFileObject,
    packageName = basePackageName
  )

  WriteToDisk(
    basePath = generatedBaseMainPath,
    packageName = s"$basePackageName.mock.client",
    scalaClass = "GrpcMockClient",
    contents = grpcMockClient
  )

  generatedFileObject
    .javaDescriptor.getServices.asScala
    .map(s => {
      val serviceMock = GenerateServiceMock(
        s = s,
        packageName = basePackageName
      )

      WriteToDisk(
        basePath = generatedBaseMainPath,
        packageName = s"$basePackageName.mock.services",
        scalaClass = s"${s.getName}Mock",
        contents = serviceMock
      )
    })

    generatedFileObject
    .javaDescriptor.getServices.asScala
    .map(s => {
      val serviceImpl = GenerateServiceImpl(
        s = s,
        packageName = basePackageName
      )

      WriteToDisk(
        basePath = generatedBaseMainPath,
        packageName = s"$basePackageName.services",
        scalaClass = s"${s.getName}Impl",
        contents = serviceImpl
      )
    })



}

object GenerateServiceImpl {
  def apply(
             s: ServiceDescriptor,
             packageName: String
           ): String = {
    s"""
       |package ${packageName}.server.impl
       |
       |import ${packageName}._
       |
       |import scala.concurrent.Future
       |
       |class ${s.getName}Impl extends ${s.getName}Grpc.${s.getName} {
       |
       |${s.getMethods.asScala.map(m => {
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
        s"  ${m.getName.head.toLower}${m.getName.tail}: ${m.getInputType.getName} => ${m.getOutputType.getName} = _ => Future.successful(a${m.getOutputType.getName}())"
      }).mkString(",\n")
    }
       |) extends ${s.getName}Grpc.${s.getName} {
       |${
      s.getMethods.asScala.map(m => {
        s"  override def ${m.getName.head.toLower}${m.getName.tail}(request: ${m.getInputType.getName}): Future[${m.getOutputType.getName}] = ${m.getName}Mock(request)"
      }).mkString("\n")
    }
       |}
       |""".stripMargin
  }
}

object GenerateServiceMock {
  def apply(
             s: ServiceDescriptor,
             packageName: String
           ): String = {
    s"""
       |package $packageName.mock.services
       |
       |import $packageName.mock.services.${s.getName}Mock._
       |import $packageName._
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
             packageName: String
           ): String = {
    s"""
       |package $packageName.mock.client
       |
       |import $packageName.client.IGrpcClient
       |import $packageName.mock.services._
       |import $packageName._
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
             basePackageName: String
           ): String = {
    s"""
       |package $basePackageName.client
       |
       |import $basePackageName.{GreeterGrpc, YellerGrpc}
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