package aksharp.grpc.mock.services

import aksharp.grpc.mock.services.GreeterMock._
import aksharp.grpc.{GreeterGrpc, HelloReply, HelloRequest}
import org.scalacheck.Gen

import scala.concurrent.Future

object GreeterMock {
  // for each message type
  def aHelloRequest(
                     name: String = Gen.alphaNumStr.sample.get
                   ): HelloRequest = HelloRequest(
    name = name
  )

  // for each message type
  def aHelloReply(
                   message: String = Gen.alphaNumStr.sample.get
                 ): HelloReply = HelloReply(
    message = message
  )
}

// for each grpc service
case class GreeterMock(
                        // for each grpc service method
                        sayHelloMock: HelloRequest => Future[HelloReply] = _ => Future.successful(aHelloReply())
                      ) extends GreeterGrpc.Greeter {
  override def sayHello(request: HelloRequest): Future[HelloReply] = sayHelloMock(request)
}
