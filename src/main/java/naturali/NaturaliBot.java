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
import websitegateway.ChatbotGatewayGrpc;
import websitegateway.Wechatwebsite;

import java.util.concurrent.TimeUnit;


public class NaturaliBot extends WeChatBot {


    public NaturaliBot(Config config) {
        super(config);
    }

    @Bind(msgType = MsgType.TEXT)
    public void handleText(WeChatMessage message) {
        System.out.printf("接收到 [{%s}] 的消息: {%s}", message.getName(), message.getText());
        FrameController.instance().showTips("接收到 [" + message.getName() + "] 的消息: {" + message.getText() + "}");
        if (StringUtils.isNotEmpty(message.getName())) {
            this.sendMsg(message.getFromUserName(), "自动回复: " + message.getText());
        } else {
            this.sendMsg(message.getFromUserName(), "自动回复new add: " + message.getText());
        }
        report(message);

    }


    @Override
    protected void other() {
//        super.other();
        boolean canWork = true;
        if (isStrEpmty(this.session().getNickName()) || isStrEpmty(this.session().getNickName())) {
            canWork = false;
            System.out.printf("website登陆失败");
            FrameController.instance().showTips("website登陆失败");
            return;
        }
        if (isStrEpmty(this.authApi().getOrgId())) {
            if (isStrEpmty(this.authApi().getAuthCode())) {
                canWork = false;
                System.out.printf("OAuth登陆失败");
                FrameController.instance().showTips("OAuth登陆失败");
                return;
            }
            this.authApi().setOrgId(getOrgId());//获取orgid兼检测后台运行状况
//            if (isStrEpmty(this.authApi().getOrgId())) {
//                canWork = false;
//                System.out.printf("未获取到所属organization");
//                FrameController.instance().showTips("未获取到所属organization\n\n请检查后台是否连通,或检测您是否已注册为公司成员");
//                return;
//            }
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
    private static ChatbotGatewayGrpc.ChatbotGatewayBlockingStub blockingStub;
    private static String GATEWAY_HOST = "47.94.181.104";
    private static int GATEWAY_PORT = 31934;
//    private static String GATEWAY_HOST = "127.0.0.1";
//    private static int GATEWAY_PORT = 40002;

    public static void initGrpc(String host, int port) {
        channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();

        blockingStub = ChatbotGatewayGrpc.newBlockingStub(channel);
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
            if (response.getText() != null && response.getText() != "") {
                FrameController.instance().showTips("发送给 [" + response.getChatNickName() + "] 的消息: {" + response.getText() + "}");
                this.sendMsg(response.getChatUserName(), response.getText());
            }
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
            return reply.getMsg();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    public static void main(String[] args) {
        Config config = Config.me();
        NaturaliBot helloBot = new NaturaliBot(config.autoLogin(false).showTerminal(true));
        FrameController.instance().init(helloBot, config);
        FrameController.instance().showTips("initializing");
        helloBot.start();
    }

}
