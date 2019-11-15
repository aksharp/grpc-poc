package aksharp.codegen

import java.io.{BufferedWriter, File, FileWriter}

object WriteToDisk {

  def apply(
             basePath: String,
             packageName: String,
             scalaClass: String,
             contents: String
           ): Unit = {
    val fullPath = s"$basePath/${packageName.replace(".", "/")}"
    new File(fullPath).mkdirs()
    val file = new File(s"$fullPath/$scalaClass.scala")
    val bw = new BufferedWriter(new FileWriter(file, false))
    bw.write(contents)
    bw.close()
  }

}