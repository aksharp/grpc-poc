package aksharp.grpc.client

import aksharp.grpc.{GreeterGrpc, YellerGrpc}
import io.grpc.netty.{NegotiationType, NettyChannelBuilder}

object GrpcClient extends IGrpcClient {

  // should come from sbt settings
  private val host = "localhost"
  private val port = 50051
  private val negotiationType: NegotiationType = NegotiationType.PLAINTEXT

  // for each service
  lazy val greeter: GreeterGrpc.GreeterStub = GreeterGrpc.stub(
    channel = NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(negotiationType)
      .build
  )

  // for each service
  lazy val yeller = YellerGrpc.stub(
    channel = NettyChannelBuilder
      .forAddress(host, port)
      .negotiationType(negotiationType)
      .build
  )

}



