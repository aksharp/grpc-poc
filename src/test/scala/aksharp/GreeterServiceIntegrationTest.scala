package aksharp

import aksharp.grpc.client.GrpcClient
import aksharp.services.GreeterService
import org.scalatest.{Matchers, WordSpec}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext}

class GreeterServiceIntegrationTest extends WordSpec with Matchers {

  implicit val ec = ExecutionContext.global

  //TODO: this requires a server started. Please run GrpcServerMain.scala before running this test.

  // tests real call to externally running service
  "should greet with a yell - call for real" in {

    val service = new GreeterService(
      grpcClient = GrpcClient
    )

    val futureResponse = service.yellWelcomeMessage(
      name = "Alex"
    )

    val response: String = Await.result(futureResponse, Duration.Inf)


    response should be("HELLO THERE AND WELCOME! HELLO ALEX!!!")

  }

}
