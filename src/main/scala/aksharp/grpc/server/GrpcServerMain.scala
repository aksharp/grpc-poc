package aksharp.grpc.server

import aksharp.grpc.server.impl.{GreeterImpl, YellerImpl}

import scala.concurrent.ExecutionContext

// main method
object GrpcServerMain extends App {

  implicit val ec = ExecutionContext.global

  GrpcServer.run(
    // for every grpc service
    greeter = new GreeterImpl,
    yeller = new YellerImpl
  )

}
