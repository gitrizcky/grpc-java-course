package com.github.gitrizcky.grpc.blog.server;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.*;
import com.mongodb.client.result.DeleteResult;
import com.proto.blog.*;
import io.grpc.Status;
import io.grpc.stub.StreamObserver;
import org.bson.Document;
import org.bson.types.ObjectId;

import static com.mongodb.client.model.Filters.eq;

public class BlogServiceImpl extends BlogServiceGrpc.BlogServiceImplBase {

    private MongoClient mongoClient = MongoClients.create("mongodb://localhost:27017");
    private MongoDatabase database = mongoClient.getDatabase("mydb");
    private MongoCollection<Document> collection = database.getCollection("blog");


    @Override
    public void createBlog(CreateBlogRequest request, StreamObserver<CreateBlogResponse> responseObserver) {

        System.out.println("Receive Create a Blog Request...");
        Blog blog = request.getBlog();

        Document doc = new Document("author_id", blog.getAuthorId())
                .append("title", blog.getTitle())
                .append("content", blog.getContent());

        System.out.println("Inserting blog ...");
        collection.insertOne(doc);

        //get mongodb generated _id
        String id  = doc.getObjectId("_id").toString();
        System.out.println("Inserted blog: "+id);

        CreateBlogResponse response = CreateBlogResponse.newBuilder()
                .setBlog(blog.toBuilder().setId(id).build())
                .build();

        responseObserver.onNext(response);
        responseObserver.onCompleted();
    }

    @Override
    public void readBlog(ReadBlogRequest request, StreamObserver<ReadBlogResponse> responseObserver) {
        System.out.println("Receive blog request...");
        String blogId = request.getBlogId();

        System.out.println("Searching for a blog...");
        Document result = null;

        try{
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e){
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The Blog with corresponding ID was not found...")
                            .asRuntimeException()
            );
        }

        if (result  == null){
            System.out.println("Blog not found...");
            responseObserver.onError(
                    Status.NOT_FOUND
                    .withDescription("The Blog with corresponding ID was not found...")
                    .asRuntimeException()
            );
        }else{
            System.out.println("Blog found, sending response...");
            Blog blog = documentToBlog(result);

            responseObserver.onNext(ReadBlogResponse.newBuilder().setBlog(blog).build());
            responseObserver.onCompleted();
        }

    }

    @Override
    public void updateBlog(UpdateBlogRequest request, StreamObserver<UpdateBlogResponse> responseObserver) {
        System.out.println("Receive update blog request...");
        String blogId = request.getBlog().getId();

        Blog blog = request.getBlog();

        System.out.println("Searching for a blog, so we can update it...");
        Document result = null;

        try{
            result = collection.find(eq("_id", new ObjectId(blogId)))
                    .first();
        } catch (Exception e){
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The Blog with corresponding ID was not found...")
                            .asRuntimeException()
            );
        }

        if(result == null){
            System.out.println("Blog not found...");
            //we dont have a match
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The Blog with corresponding ID was not found...")
                            .asRuntimeException()
            );
        }else{
            Document replacement = new Document("author_id", blog.getAuthorId())
                    .append("title", blog.getTitle())
                    .append("content", blog.getContent())
                    .append("_id",new ObjectId(blogId));

            System.out.println("Replacing a blog in database...");
            collection.replaceOne(eq("_id", result.getObjectId("_id")), replacement);

            System.out.println("Replaced! sending as a response....");
            responseObserver.onNext(
                    UpdateBlogResponse.newBuilder()
                            .setBlog(documentToBlog(replacement))
                            .build()
            );

            responseObserver.onCompleted();
        }
    }

    @Override
    public void deleteBlog(DeleteBlogRequest request, StreamObserver<DeleteBlogResponse> responseObserver) {
        System.out.println("Received Delete blog request");

        String blogId = request.getBlogId();
        DeleteResult result = null;

        try{
            result = collection.deleteOne(eq("_id", new ObjectId(blogId)));

        } catch (Exception e){
            System.out.println("Blog not found...");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The Blog with corresponding ID was not found...")
                            .asRuntimeException()
            );
        }

        if(result.getDeletedCount() ==0){
            System.out.println("Blog not found...");
            responseObserver.onError(
                    Status.NOT_FOUND
                            .withDescription("The Blog with corresponding ID was not found...")
                            .asRuntimeException()
            );
        }else{
            System.out.println("Blog was deleted");
            responseObserver.onNext(DeleteBlogResponse.newBuilder()
                    .setBlogId(blogId)
                    .build());

            responseObserver.onCompleted();
        }
    }

    @Override
    public void listBlog(ListBlogRequest request, StreamObserver<ListBlogResponse> responseObserver) {
        System.out.println("Receive List Blog Request...");
        collection.find().iterator().forEachRemaining(
                document -> responseObserver.onNext(
                        ListBlogResponse.newBuilder()
                                .setBlog(documentToBlog(document)).build()
                )
        );

        responseObserver.onCompleted();
    }

    private Blog documentToBlog(Document document){
        return Blog.newBuilder()
                .setAuthorId(document.getString("author_id"))
                .setTitle(document.getString("title"))
                .setContent(document.getString("content"))
                .setId(document.getObjectId("_id").toString())
                .build();
    }
}
