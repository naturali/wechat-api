//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package naturali;

import io.github.biezhi.wechat.WeChatBot;
import io.github.biezhi.wechat.api.annotation.Bind;
import io.github.biezhi.wechat.api.constant.Config;
import io.github.biezhi.wechat.api.enums.AccountType;
import io.github.biezhi.wechat.api.enums.MsgType;
import io.github.biezhi.wechat.api.model.WeChatMessage;
import io.github.biezhi.wechat.utils.StringUtils;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import websitegateway.ChatBotGatewayGrpc;
import websitegateway.Wechatwebsite;

import java.util.concurrent.TimeUnit;


public class HelloBot extends WeChatBot {

    public HelloBot(Config config) {
        super(config);
    }

    @Bind(msgType = MsgType.TEXT, accountType = AccountType.TYPE_FRIEND)
    public void handleText(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            System.out.printf("接收到 [{%s}] 的消息: {%s}", message.getName(), message.getText());
            this.sendMsg(message.getFromUserName(), "自动回复: " + message.getText());
            report(message);
        }
    }

    /*GRPC with gateway start*/
    private static ManagedChannel channel;
    private static ChatBotGatewayGrpc.ChatBotGatewayBlockingStub blockingStub;
    private static String GATEWAY_HOST = "127.0.0.1";
    private static int GATEWAY_PORT = 40002;

    public static void initGrpc(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();

        blockingStub = ChatBotGatewayGrpc.newBlockingStub(channel);
    }

    public void shutdown() throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void report(WeChatMessage message) {
        Wechatwebsite.Message request = Wechatwebsite.Message.newBuilder()
                .setChatNickName(message.getFromNickName())
                .setChatUserName(message.getFromUserName())
                .setMineNickName(message.getMineNickName())
                .setMineUserName(message.getMineUserName())
                .setChatType(Wechatwebsite.Message.ChatType.RECEIVE)
                .setMsgType(Wechatwebsite.Message.MsgType.TEXT)
                .setText(message.getText()).build();
        initGrpc(GATEWAY_HOST, GATEWAY_PORT);
        try {
            Wechatwebsite.Reply response = blockingStub.reportMessage(request);
            shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        WeChatBot helloBot = new WeChatBot(Config.me().assetsDir("assets/1").autoLogin(true).showTerminal(true));
        Runnable runnable = () -> {
            final TestServer server = new TestServer();
            server.startServer(message -> helloBot.sendMsg(message.getChatUserName(), message.getText()));
        };
        Thread thread = new Thread(runnable);
        thread.start();
        helloBot.start();
    }

    @Override
    protected void other() {
    }
}
