package aksharp

import aksharp.grpc.mock.client.GrpcMockClient
import aksharp.grpc.mock.services.{GreeterMock, YellerMock}
import aksharp.grpc.{HelloReply, YellReply}
import aksharp.services.GreeterService
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future}

object GreeterServicePropertyTests extends Properties("GreeterService property tests") {

  property("should greet with a yell!") = forAll(
    Gen.alphaNumStr,
    Gen.alphaNumStr,
    Gen.alphaNumStr
  ) {
    (
      name: String,
      greeterSuffix: String,
      yellerSuffix: String
    ) => {
      implicit val ec = ExecutionContext.global

      val expectedTransformation = s"$name$greeterSuffix$yellerSuffix".toUpperCase

      val mockClient = GrpcMockClient(
        greeter = GreeterMock(
          sayHelloMock = r => Future.successful(HelloReply(message = r.name + greeterSuffix))
        ),
        yeller = YellerMock(
          yellMock = r => Future.successful(YellReply(message = (r.message + yellerSuffix).toUpperCase))
        )
      )

      val service = new GreeterService(
        grpcClient = mockClient
      )

      val futureResponse = service.yellWelcomeMessage(
        name = name
      )

      val response: String = Await.result(futureResponse, Duration.Inf)

      println(s"### check response $response == ${service.welcomeMessage + expectedTransformation}")
      response == service.welcomeMessage + expectedTransformation
    }
  }

}
