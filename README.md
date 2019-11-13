ScalaPB feature request: 

In addition to generating Scala code for GRPC as it is currently done in ScalaPB, 
it would be great to help reduce writing boilerplate code and standardize on code by adding code generation for:

#### 1. GRPC Server (to use as main method for the app)
##### Generated code example in this package: `aksharp.grpc.server`
#### 2. GRPC Client (to standardize consumption of GRPC endpoints from other services and easily swap test double)
##### Generated code example in this package: `aksharp.grpc.client`
#### 3. GRPC Service Implementation (to reduce boilerplate code)
##### Generated code example in this package: `aksharp.grpc.server.impl`
#### 4. GRPC Mock Services & Messages (to use for testing with ability to override response values)
##### Generated code example in this package: `aksharp.grpc.mock.services`
#### 5. GRPC Mock Client (to use for testing, to mock results from external services. Uses mock services/messages)
##### Generated code example in this package: `aksharp.grpc.mock.client`
##### Test examples for `GreeterService` can be found under tests.
##### Property based tests (using mock client):  `GreeterServicePropertyTests`
##### Integration test (calling real service): `GreeterServicePropertyTests`
#### 6. GRPC Mock Server (GRPC version of MockServer / Hoverfly / WireMock / etc. Design is work in progress.)
##### Generated code example in this package: `aksharp.grpc.mock.server` 

