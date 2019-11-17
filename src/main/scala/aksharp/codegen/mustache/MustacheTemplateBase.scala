package aksharp.codegen.mustache

import java.io.File

import aksharp.codegen.WriteToDisk
import org.fusesource.scalate.{TemplateEngine, TemplateSource}

trait MustacheTemplateBase[A] {
  val rootElementName: String = "root"
  val templateName: String
  val engine: TemplateEngine
  val mustacheTemplateBasePath: String
  val templateData: A
  val basePackageName: String
  val javaPackage: String

  def generateCode(): String = {
    engine.layout(
      source = TemplateSource.fromFile(new File(mustacheTemplateBasePath + templateName)),
      attributes = Map[String, Any](rootElementName -> templateData)
    )
  }

  def writeToMain(
                   packageName: String
                 ): Unit = writeToDisk(
    basePath = "src/main/scala",
    packageName = packageName
  )

  def writeToTest(
                   packageName: String
                 ): Unit = writeToDisk(
    basePath = "src/test/scala",
    packageName = packageName
  )

  private def writeToDisk(
                           basePath: String,
                           packageName: String
                         ): Unit = {
    WriteToDisk(
      basePath = basePath,
      packageName = packageName,
      scalaClass = templateName.replace(".mustache", ""),
      contents = generateCode()
    )
  }
}
