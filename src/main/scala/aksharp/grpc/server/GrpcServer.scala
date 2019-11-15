package aksharp.grpc.server

import aksharp.grpc._
import com.typesafe.scalalogging.LazyLogging
import io.grpc.Server
import io.grpc.netty.InternalProtocolNegotiator.ProtocolNegotiator
import io.grpc.netty.NettyServerBuilder

import scala.concurrent.ExecutionContext

object GrpcServer extends LazyLogging { self =>
  private[this] var server: Server = null

  // should come from sbt settings
  private val port = 50051

  def run(
           // for each service
           greeter: GreeterGrpc.Greeter,
           yeller: YellerGrpc.Yeller
         )
         (implicit ec: ExecutionContext): Unit = {
    server = NettyServerBuilder
      .forPort(port)
      // for each service
      .addService(GreeterGrpc.bindService(greeter, ec))
      .addService(YellerGrpc.bindService(yeller, ec))
      .build
      .start

    logger.info("Server started, listening on " + port)
    sys.addShutdownHook {
      System.err.println("*** shutting down gRPC server since JVM is shutting down")
      self.stop()
      System.err.println("*** server shut down")
    }

    server.awaitTermination()
  }

  def stop(): Unit = {
    if (server != null) {
      server.shutdownNow()
    }
  }

}
