package aksharp.services

import aksharp.grpc.{HelloRequest, YellRequest}
import aksharp.grpc.client.IGrpcClient

import scala.concurrent.{ExecutionContext, Future}

// example service
class GreeterService(
                      grpcClient: IGrpcClient
                    )(implicit ec: ExecutionContext) {

  val welcomeMessage = "HELLO THERE AND WELCOME! "

  def yellWelcomeMessage(name: String): Future[String] =
    for {
      greeting <- grpcClient.greeter.sayHello(
        request = HelloRequest(
          name = name
        )
      )
      yelledGreeting <- grpcClient.yeller.yell(
        request = YellRequest(
          message = greeting.message
        )
      )

    } yield {
      welcomeMessage + yelledGreeting.message
    }

}
