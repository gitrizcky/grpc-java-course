package com.github.gitrizcky.grpc.blog.client;

import com.proto.blog.*;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class BlogClient {
    public static void main(String[] args) {
        System.out.println("Hello I'm a gRPC client for Blog");

        BlogClient main = new BlogClient();
        main.run();
    }

    private void run() {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        BlogServiceGrpc.BlogServiceBlockingStub blogClient = BlogServiceGrpc.newBlockingStub(channel);

        //Create a blog
        Blog blog = Blog.newBuilder()
                .setAuthorId("Rizcky")
                .setTitle("New Blog!")
                .setContent("Hello world this my first blog")
                .build();

        CreateBlogResponse createBlogResponse = blogClient.createBlog(
                CreateBlogRequest.newBuilder()
                        .setBlog(blog)
                        .build()
        );

        System.out.println("Received create blog response");
        System.out.println(createBlogResponse.toString());


        //Read a blog
        String blogId = createBlogResponse.getBlog().getId();
        System.out.println("Read a blog by id :"+blogId);

        System.out.println("Reading blog...");
        ReadBlogResponse readBlogResponse = blogClient.readBlog(ReadBlogRequest.newBuilder()
                .setBlogId(blogId)
                .build());

        System.out.println(readBlogResponse.toString());

//        System.out.println("Reading blog with non existence id");
//        ReadBlogResponse readBlogResponseNotFound = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId("fake-id")
//                .build());


        //Update a blog
        Blog newBlog = Blog.newBuilder()
                .setId(blogId)
                .setAuthorId("Renzi")
                .setTitle("New Updated Blog!")
                .setContent("Hello world, this is my first gRPC blog, I have upadte some more content")
                .build();

        UpdateBlogResponse updateBlogResponse = blogClient.updateBlog(
                UpdateBlogRequest.newBuilder()
                        .setBlog(newBlog)
                        .build());

        System.out.println("Updated blog");
        System.out.println(updateBlogResponse.toString());


        //Delete a blog
//        System.out.println("Deleting blog...");
//        DeleteBlogResponse deleteBlogResponse = blogClient.deleteBlog(
//                DeleteBlogRequest.newBuilder()
//                        .setBlogId(blogId)
//                        .build()
//        );
//
//        System.out.println("Deleted blog...");
//
//        System.out.println("Reading blog...");
//        //should be not found
//        ReadBlogResponse readBlogResponseAfterDeeletion = blogClient.readBlog(ReadBlogRequest.newBuilder()
//                .setBlogId(blogId)
//                .build());


        //Blog Client

        //list blog on database
        blogClient.listBlog(
                ListBlogRequest.newBuilder().build()
        ).forEachRemaining(
                listBlogResponse -> System.out.println(listBlogResponse.getBlog().toString())
        );

    }

}
