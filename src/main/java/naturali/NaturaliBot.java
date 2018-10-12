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

import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Handler;


public class NaturaliBot extends WeChatBot {


    public NaturaliBot(Config config) {
        super(config);
    }

    @Bind(msgType = MsgType.TEXT)
    public void handleText(WeChatMessage message) {
        System.out.printf("接收到 [{%s}] 的消息: {%s}", message.getNickName(), message.getText());
        FrameController.instance().showTips("接收到 [" + message.getNickName() + "] 的消息: {" + message.getText() + "}");
//        if (StringUtils.isNotEmpty(message.getName())) {
//            this.sendMsg(message.getFromUserName(), "自动回复: " + message.getText());
//        } else {
//            this.sendMsg(message.getFromUserName(), "自动回复new add: " + message.getText());
//        }
        report(message);

    }


    @Override
    protected void other() {
//        super.other();
        while (true) {
            DateUtils.sleep(1000);
            if (isStrEpmty(this.session().getNickName()) || isStrEpmty(this.session().getNickName())) {
                System.out.printf("website登陆失败");
                FrameController.instance().showTips("website登陆失败");
                DateUtils.sleep(5000);
                return;
            }
            if (isStrEpmty(this.authApi().getOrgId())) {
                if (isStrEpmty(this.authApi().getAuthCode())) {
                    System.out.printf("OAuth登陆失败");
                    FrameController.instance().showTips("OAuth登陆失败");
                    this.authApi().login(false);
                    continue;
                }
                Wechatwebsite.BaseInfo baseInfo = getOrgId();
                if (baseInfo != null && baseInfo.getOrgId() != null && baseInfo.getUnionId() != null) {
                    this.authApi().setUnionName(this.session().getNickName());
                    this.authApi().setUnionId(baseInfo.getUnionId());
                    this.authApi().setOrgId(baseInfo.getOrgId());//获取orgid兼检测后台运行状况
                    FrameController.instance().resetOrg(this.authApi().getOrgId(), this.authApi().getUnionName());
                }
                if (isStrEpmty(this.authApi().getOrgId())) {
                    System.out.printf("未获取到所属organization");
                    FrameController.instance().showTips("未获取到所属organization\n\n请检查后台是否连通,或检测您是否已注册为公司成员，5s后会重新尝试");
                    DateUtils.sleep(5000);
                    continue;
                }
            }
            request();
        }
    }


    boolean isStrEpmty(String string) {
        return string == null || "".equals(string);
    }

    /*GRPC with gateway start*/
//    private static String GATEWAY_HOST = "47.94.181.104";
//    private static int GATEWAY_PORT = 31934;
    private static String GATEWAY_HOST = "127.0.0.1";
    private static int GATEWAY_PORT = 40002;

    public void shutdown(ManagedChannel channel) throws InterruptedException {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    }

    public void report(WeChatMessage message) {
        Wechatwebsite.Message request = Wechatwebsite.Message.newBuilder()
                .setChatNickName(message.getFromNickName())
                .setMineNickName(message.getMineNickName())
                .setChatType(Wechatwebsite.Message.ChatType.RECEIVE)
                .setMsgType(Wechatwebsite.Message.MsgType.TEXT)
                .setOrgId(this.authApi().getOrgId())
                .setUnionId(this.authApi().getUnionId())
                .setText(message.getText()).build();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(GATEWAY_HOST, GATEWAY_PORT)
                .usePlaintext(true)
                .build();
        ChatbotGatewayGrpc.ChatbotGatewayBlockingStub blockingStub = ChatbotGatewayGrpc.newBlockingStub(channel);
        try {
            Wechatwebsite.Reply response = blockingStub.reportMessage(request);
            shutdown(channel);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void request() {
        Wechatwebsite.BaseInfo baseInfo = Wechatwebsite.BaseInfo.newBuilder()
                .setMineNickName(this.session().getNickName())
                .setOrgId(this.authApi().getOrgId())
                .setUnionId(this.authApi().getUnionId())
                .setAuthCode(this.authApi().getAuthCode()).build();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(GATEWAY_HOST, GATEWAY_PORT)
                .usePlaintext(true)
                .build();
        ChatbotGatewayGrpc.ChatbotGatewayBlockingStub blockingStub = ChatbotGatewayGrpc.newBlockingStub(channel);
        Wechatwebsite.MessageList response = blockingStub.requestMessage(baseInfo);
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                for (Wechatwebsite.Message message : response.getMessageListList()) {
                    if (message.getText() != null && message.getText() != "") {
                        boolean success = NaturaliBot.this.sendMsg(NaturaliBot.this.getUserIdByNick(message.getChatNickName()), message.getText());
                        FrameController.instance().showTips("发送给 [" + message.getChatNickName() + "] 的消息: {" + message.getText() + "}," + success);
                    }
                    DateUtils.sleep(250);
                }
                try {
                    shutdown(channel);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    public Wechatwebsite.BaseInfo getOrgId() {
        Wechatwebsite.BaseInfo baseInfo = Wechatwebsite.BaseInfo.newBuilder()
                .setMineNickName(this.session().getNickName())
                .setOrgId("")
                .setUnionId("")
                .setAuthCode(this.authApi().getAuthCode()).build();
        ManagedChannel channel = ManagedChannelBuilder.forAddress(GATEWAY_HOST, GATEWAY_PORT)
                .usePlaintext(true)
                .build();
        ChatbotGatewayGrpc.ChatbotGatewayBlockingStub blockingStub = ChatbotGatewayGrpc.newBlockingStub(channel);
        try {
            Wechatwebsite.BaseInfo reply = blockingStub.getOrgId(baseInfo);
            shutdown(channel);
            return reply;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        Config config = Config.me();
        NaturaliBot helloBot = new NaturaliBot(config.autoLogin(true).showTerminal(true));
        FrameController.instance().init(helloBot, config);
        FrameController.instance().showTips("initializing");
        helloBot.start();
    }

}
