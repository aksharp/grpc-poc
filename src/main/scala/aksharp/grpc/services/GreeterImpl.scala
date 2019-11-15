package aksharp.grpc.services

import aksharp.grpc.{GreeterGrpc, HelloReply, HelloRequest}

import scala.concurrent.Future

// this can be generated with a noop, aka ??? and then will be required to implement
class GreeterImpl extends GreeterGrpc.Greeter {
  override def sayHello(req: HelloRequest): Future[HelloReply] = {
    val reply = HelloReply(message = "Hello " + req.name)
    Future.successful(reply)
  }
}
