## ScalaPB feature request: Server/Client/Mock/Impl gRPC Generators 

In addition to generating Scala code for gRPC as it is currently done in ScalaPB, 
it would be great to help reduce writing boilerplate code and standardize on code by adding code generation for:

Following would be generated based on https://github.com/aksharp/grpc-poc/blob/master/src/main/protobuf/hello.proto

#### 1. gRPC Server (to use as main method for the app)
##### Generated code example in this package: `aksharp.grpc.server`
https://github.com/aksharp/grpc-poc/tree/master/src/main/scala/aksharp/grpc/server

#### 2. gRPC Client (to standardize consumption of gRPC endpoints from other services and easily swap test double)
##### Generated code example in this package: `aksharp.grpc.client`
https://github.com/aksharp/grpc-poc/tree/master/src/main/scala/aksharp/grpc/client

#### 3. gRPC Service Implementation (to reduce boilerplate code)
##### Generated code example in this package: `aksharp.grpc.server.impl`
https://github.com/aksharp/grpc-poc/tree/master/src/main/scala/aksharp/grpc/server/impl

#### 4. gRPC Mock Services & Messages (to use for testing with ability to override response values)
##### Generated code example in this package: `aksharp.grpc.mock.services`
https://github.com/aksharp/grpc-poc/tree/master/src/main/scala/aksharp/grpc/mock/services

#### 5. gRPC Mock Client (to use for testing, to mock results from external services. Uses mock services/messages)
##### Generated code example in this package: `aksharp.grpc.mock.client`
https://github.com/aksharp/grpc-poc/tree/master/src/main/scala/aksharp/grpc/mock/client

##### Test examples for `GreeterService` can be found under tests.
##### Property based tests (using mock client):  `GreeterServicePropertyTests`
##### Integration test (calling real service): `GreeterServicePropertyTests`
https://github.com/aksharp/grpc-poc/tree/master/src/test/scala/aksharp

#### 6. gRPC Mock Server (gRPC version of MockServer / Hoverfly / WireMock / etc. Design is work in progress.)
##### Generated code example in this package: `aksharp.grpc.mock.server`
https://github.com/aksharp/grpc-poc/tree/master/src/main/scala/aksharp/grpc/mock/server 




##
This project is runnable. Just use `sbt run` and select which Main method you'd like to run.
