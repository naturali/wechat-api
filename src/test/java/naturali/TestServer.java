package naturali;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import websitegateway.ChatbotGatewayGrpc;
import websitegateway.Wechatwebsite;

import java.io.IOException;

public class TestServer {
    private int port = 50051;
    private Server server;

    private void start() throws IOException {
        server = ServerBuilder.forPort(port)
                .addService(new GreeterImpl())
                .build()
                .start();

        System.out.println("service start...");

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                System.err.println("*** shutting down gRPC server since JVM is shutting down");
                TestServer.this.stop();
                System.err.println("*** server shut down");
            }
        });
    }

    private void stop() {
        if (server != null) {
            server.shutdown();
        }
    }

    // block 一直到退出程序
    private void blockUntilShutdown() throws InterruptedException {
        if (server != null) {
            server.awaitTermination();
        }
    }

    // 实现 定义一个实现服务接口的类
    private class GreeterImpl extends ChatbotGatewayGrpc.ChatbotGatewayImplBase {
        @Override
        public void reportMessage(Wechatwebsite.Message request, StreamObserver<Wechatwebsite.Reply> responseObserver) {
            System.out.printf("@@@@@@@@@@@@reportMessage");
            Wechatwebsite.Reply reply = Wechatwebsite.Reply.newBuilder().setStatus((true)).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }

        @Override
        public void sendClientMessage(Wechatwebsite.Message request, StreamObserver<Wechatwebsite.Reply> responseObserver) {
            System.out.printf("@@@@@@@@@@@@receiveMessage");
            boolean success = false;
            if (websiteServiceListener != null) {
                success = websiteServiceListener.sendMessage(request);
            }
            Wechatwebsite.Reply reply = Wechatwebsite.Reply.newBuilder().setStatus((success)).build();
            responseObserver.onNext(reply);
            responseObserver.onCompleted();
        }
    }

    public interface WebsiteServiceListener {
        boolean sendMessage(Wechatwebsite.Message message);
    }

    private WebsiteServiceListener websiteServiceListener;

    private void setWebsiteServiceListener(WebsiteServiceListener websiteServiceListener) {
        this.websiteServiceListener = websiteServiceListener;
    }

    public void startServer(WebsiteServiceListener websiteServiceListener) {
        this.setWebsiteServiceListener(websiteServiceListener);
        try {
            this.start();
            this.blockUntilShutdown();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final TestServer server = new TestServer();
        server.setWebsiteServiceListener(new WebsiteServiceListener() {
            @Override
            public boolean sendMessage(Wechatwebsite.Message message) {
                System.out.printf("接收到 gateway [{%s}] 的消息: {%s}", message.getChatNickName(), message.getText());
                return false;
            }
        });
        server.start();
        server.blockUntilShutdown();
    }
}
