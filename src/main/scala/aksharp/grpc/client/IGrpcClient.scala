package aksharp.grpc.client

import aksharp.grpc.GreeterGrpc.Greeter
import aksharp.grpc.YellerGrpc.Yeller

// client interface so it can be mocked/stubbed in tests
trait IGrpcClient {
  // for each service trait
  val greeter: Greeter
  val yeller: Yeller
}
