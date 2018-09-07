//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package naturali;

import io.github.biezhi.wechat.WeChatBot;
import io.github.biezhi.wechat.api.annotation.Bind;
import io.github.biezhi.wechat.api.constant.Config;
import io.github.biezhi.wechat.api.enums.MsgType;
import io.github.biezhi.wechat.api.model.WeChatMessage;
import io.github.biezhi.wechat.utils.DateUtils;
import io.github.biezhi.wechat.utils.StringUtils;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import websitegateway.ChatBotGatewayGrpc;
import websitegateway.Wechatwebsite;

import java.util.concurrent.TimeUnit;


public class NaturaliBot extends WeChatBot {


    public NaturaliBot(Config config) {
        super(config);
    }

    @Bind(msgType = MsgType.TEXT)
    public void handleText(WeChatMessage message) {
        if (StringUtils.isNotEmpty(message.getName())) {
            System.out.printf("接收到 [{%s}] 的消息: {%s}", message.getName(), message.getText());
            this.sendMsg(message.getFromUserName(), "自动回复: " + message.getText());
            report(message);
        }
    }


    @Override
    protected void other() {
//        super.other();
        boolean canWork = true;
        if (isStrEpmty(this.session().getNickName()) || isStrEpmty(this.session().getNickName())) {
            canWork = false;
            System.out.printf("website登陆失败");
        }
        if (isStrEpmty(this.authApi().getAuthCode())) {
            canWork = false;
            System.out.printf("Oauth登陆失败");
        }
        this.authApi().setOrgId(getOrgId());
        if (isStrEpmty(this.authApi().getOrgId())) {
            canWork = false;
            System.out.printf("没有获取到所属organization");
        }
        while (canWork) {
            request();
            DateUtils.sleep(1000);
        }
        System.out.printf("信息不全，登陆失败");

    }


    boolean isStrEpmty(String string) {
        return string == null || "".equals(string);
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
                .setOrgId(this.authApi().getOrgId())
                .setText(message.getText()).build();
        initGrpc(GATEWAY_HOST, GATEWAY_PORT);
        try {
            Wechatwebsite.Reply response = blockingStub.reportMessage(request);
            shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void request() {
        Wechatwebsite.BaseInfo baseInfo = Wechatwebsite.BaseInfo.newBuilder()
                .setMineNickName(this.session().getNickName())
                .setMineUserName(this.session().getUserName())
                .setOrgId(this.authApi().getOrgId())
                .setAuthCode(this.authApi().getAuthCode()).build();
        initGrpc(GATEWAY_HOST, GATEWAY_PORT);
        try {
            Wechatwebsite.Message response = blockingStub.requestMessage(baseInfo);
            shutdown();
            this.sendMsg(response.getChatUserName(), response.getText());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String getOrgId() {
        Wechatwebsite.BaseInfo baseInfo = Wechatwebsite.BaseInfo.newBuilder()
                .setMineNickName(this.session().getNickName())
                .setMineUserName(this.session().getUserName())
                .setOrgId("")
                .setAuthCode(this.authApi().getAuthCode()).build();
        initGrpc(GATEWAY_HOST, GATEWAY_PORT);
        try {
            Wechatwebsite.Reply reply = blockingStub.getOrgId(baseInfo);
            shutdown();
            System.out.println("………………………………………………………………………………" + reply);
            return reply.getMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        Config config = Config.me();
        NaturaliBot helloBot = new NaturaliBot(config.autoLogin(true).showTerminal(true));
        FrameController.instance().init(helloBot, config);
        helloBot.start();
    }

}
