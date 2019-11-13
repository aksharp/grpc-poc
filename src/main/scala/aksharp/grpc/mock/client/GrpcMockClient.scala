package aksharp.grpc.mock.client

import aksharp.grpc.client.IGrpcClient
import aksharp.grpc.mock.services.{GreeterMock, YellerMock}
import aksharp.grpc.{GreeterGrpc, YellerGrpc}

case class GrpcMockClient(
                           // for each grpc service
                           greeter: GreeterGrpc.Greeter = new GreeterMock,
                           yeller: YellerGrpc.Yeller = new YellerMock
                         ) extends IGrpcClient


