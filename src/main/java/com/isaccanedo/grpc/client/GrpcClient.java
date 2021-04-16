package com.isaccanedo.grpc.client;

import com.isaccanedo.grpc.HelloRequest;
import com.isaccanedo.grpc.HelloResponse;
import com.isaccanedo.grpc.HelloServiceGrpc;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GrpcClient {
    public static void main(String[] args) throws InterruptedException {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 8080)
            .usePlaintext()
            .build();

        HelloServiceGrpc.HelloServiceBlockingStub stub 
          = HelloServiceGrpc.newBlockingStub(channel);

        HelloResponse helloResponse = stub.hello(HelloRequest.newBuilder()
            .setFirstName("isaccanedo")
            .setLastName("gRPC")
            .build());

        System.out.println("Response received from server:\n" + helloResponse);

        channel.shutdown();
    }
}
