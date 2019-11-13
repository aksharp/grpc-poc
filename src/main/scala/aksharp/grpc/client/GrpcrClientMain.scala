package aksharp.grpc.client

import aksharp.grpc.{HelloRequest, YellRequest}

import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}

// just an example on how to use, can be generated as part of the docs
object GrpcrClientMain extends App {
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global

  for {
    greet <- GrpcClient.greeter.sayHello(
      request = HelloRequest(
        name = "Alex"
      )
    )
    yell <- GrpcClient.yeller.yell(
      request = YellRequest(
        message = "hey there"
      )
    )
  } yield {
    println(greet.message)
    println(yell.message)
  }

  Thread.sleep(2000)
}
