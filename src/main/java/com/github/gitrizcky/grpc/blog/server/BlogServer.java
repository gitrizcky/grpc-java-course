package com.github.gitrizcky.grpc.blog.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.protobuf.services.ProtoReflectionService;

import java.io.IOException;

public class BlogServer {
    public static void main(String[] args) throws IOException, InterruptedException {

        Server server = ServerBuilder.forPort(50052)
                .addService(new BlogServiceImpl())
                .addService(ProtoReflectionService.newInstance()) //reflection
                .build();

        System.out.println("Start Blog Service");
        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread( () ->{
            System.out.println("Receive Shutdown Request");
            server.shutdown();
            System.out.println("Successfully stopped the server");
        }));

        server.awaitTermination();

    }
}
