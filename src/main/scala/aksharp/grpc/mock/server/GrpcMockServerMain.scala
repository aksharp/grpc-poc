package aksharp.grpc.mock.server

import aksharp.grpc.mock.services.{GreeterMock, YellerMock}
import aksharp.grpc.server.GrpcServer

import scala.concurrent.ExecutionContext

// currently optional to generate
// would make sense to generate to deploy as a stand-alone mock server
// to make it more useable, more thought needs to go into state set-up (
//  possibly expose endpoints to set state and then look up by UUID on request. should then UUID be part of every message? how to secure in prod, JWT, etc?
object GrpcMockServerMain extends App {

  implicit val ec = ExecutionContext.global

  GrpcServer.run(
    greeter = new GreeterMock,
    yeller = new YellerMock
  )

}
