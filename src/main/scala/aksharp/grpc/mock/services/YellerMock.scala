package aksharp.grpc.mock.services

import aksharp.grpc.mock.services.YellerMock._
import aksharp.grpc.{YellReply, YellRequest, YellerGrpc}
import org.scalacheck.Gen

import scala.concurrent.Future

object YellerMock {

  // for each message type
  def aYellRequest(
                    message: String = Gen.alphaNumStr.sample.get
                  ): YellRequest = YellRequest(
    message = message
  )

  // for each message type
  def aYellReply(
                  message: String = Gen.alphaNumStr.sample.get
                ): YellReply = YellReply(
    message = message
  )
}

// for each grpc service
case class YellerMock(
                       // for each grpc service method
                       yellMock: YellRequest => Future[YellReply] = _ => Future.successful(aYellReply())
                     ) extends YellerGrpc.Yeller {
  override def yell(request: YellRequest): Future[YellReply] = yellMock(request)
}
