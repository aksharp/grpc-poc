package aksharp.grpc.server.impl

import aksharp.grpc.{YellReply, YellRequest, YellerGrpc}

import scala.concurrent.Future

// this can be generated with a noop, aka ??? and then will be required to implement
class YellerImpl extends YellerGrpc.Yeller {
  override def yell(request: YellRequest): Future[YellReply] = {
    Future.successful(
      YellReply(
        message = request.message.toUpperCase + "!!!"
      )
    )
  }
}
