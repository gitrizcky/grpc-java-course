package com.github.gitrizcky.grpc.greeting.client;

import com.proto.dummy.DummyServiceGrpc;
import com.proto.greet.GreetRequest;
import com.proto.greet.GreetResponse;
import com.proto.greet.Greeting;
import com.proto.greet.GreetingServiceGrpc;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class GreetingClient {
    public static void main(String[] args) {
        System.out.println("Hello I am grRPC client");

        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext() //disable ssl during development
                .build();

        System.out.println("Creating stub");
        //old and dummy
        //DummyServiceGrpc.DummyServiceBlockingStub syncClient = DummyServiceGrpc.newBlockingStub(channel);
        //System.out.println("Creating async stub");
        //DummyServiceGrpc.DummyServiceFutureStub asyncClient = DummyServiceGrpc.newFutureStub(channel);

        // created greet service client (Blocking - synchronous)
        GreetingServiceGrpc.GreetingServiceBlockingStub greetClient = GreetingServiceGrpc.newBlockingStub(channel);

        // created a protocol buffer greeting message
        Greeting greeting = Greeting.newBuilder()
                .setFirstName("Rizcky")
                .setLastName("Ramadhan")
                .build();

        // do the same for a GreetRequest
        GreetRequest greetRequest = GreetRequest.newBuilder()
                .setGreeting(greeting)
                .build();

        // call the RPC and get the GreetResponse (protocol buffer)
        GreetResponse greetResponse = greetClient.greet(greetRequest);

        // print out the response result
        System.out.println(greetResponse.getResult());

        // do something

        System.out.println("Shutdown channel");
        channel.shutdown();
    }
}
