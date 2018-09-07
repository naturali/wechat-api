package io.github.biezhi.wechat.api;

import io.github.biezhi.wechat.WeChatBot;
import io.github.biezhi.wechat.api.client.BotClient;
import io.github.biezhi.wechat.api.constant.AuthStateCode;
import io.github.biezhi.wechat.api.model.*;
import io.github.biezhi.wechat.api.request.FileRequest;
import io.github.biezhi.wechat.api.request.JsonRequest;
import io.github.biezhi.wechat.api.request.StringRequest;
import io.github.biezhi.wechat.api.response.*;
import io.github.biezhi.wechat.utils.*;
import lombok.extern.slf4j.Slf4j;
import naturali.FrameController;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 微信OAuth API实现
 *
 * @author wkj
 * @date 2018/09/04
 */
@Slf4j
public class WeChatAuthApiImpl implements WeChatAuthApi {

    private static final Pattern CHECK_LOGIN_PATTERN = Pattern.compile("window.wx_errcode=(\\d+);window.wx_code=\'(\\S*)\';");
    private String authUuid;
    private String authCode = "";
    private String orgId = "";
    private boolean authLogging;
    private WeChatBot bot;
    private BotClient client;

    //    private String appID="wxb82cc0701446acde";
//    private String redirectUri="https%3A%2F%2Fdeveloper.naturali.io%2Fwechat-auth";
    private String appID = "wx28fca7046cf95cad";
    private String appSecret = "897d31c95985ec2e08c0f767244d1937";
    private String redirectUri = "https%3A%2F%2Fni-skill-lab-dev.naturali.io";

    public WeChatAuthApiImpl(WeChatBot bot) {
        this.bot = bot;
        this.client = bot.client();
    }

    /**
     * 自动登录
     */
    private void autoLogin() {
        String file = bot.config().assetsDir() + "/loginAuth.json";
        try {
            HotReloadAuth hotReloadAuth = WeChatUtils.fromJson(new FileReader(file), HotReloadAuth.class);
            if (!hotReloadAuth.reLogin(bot)) {
                this.login(false);
            }
        } catch (FileNotFoundException e) {
            this.login(false);
        }
    }


    @Override
    public void login(boolean autoLogin) {
        if (authLogging) {
            log.warn("微信Auth已经登录");
            return;
        }
        if (autoLogin) {
            this.autoLogin();
        } else {
            this.authLogging = true;
            while (authLogging) {
                while (null == this.getAuthUUID()) {
                    DateUtils.sleep(10);
                }
                log.info("开始下载Auth二维码");
                this.getAuthQrImage(this.authUuid, bot.config().showTerminal());
                FrameController.instance().showQRCode("qrcodeAuth.png","OAuth login");
                log.info("请使用手机扫描屏幕二维码");
                Boolean isLoggedIn = false;
                Boolean isLast404 = false;
                while (null == isLoggedIn || !isLoggedIn) {
                    String status = this.checkLogin(this.authUuid, isLast404);
                    if (AuthStateCode.SUCCESS.equals(status)) {
                        isLoggedIn = true;
                    } else if (AuthStateCode.WAITSCAN.equals(status)) {
                        isLast404 = false;
                        if (null != isLoggedIn) {
                            log.info("请在手机上确认登录");
                            isLoggedIn = null;
                        }
                    } else if (AuthStateCode.FAIL.equals(status)) {
                        isLast404 = true;
                        if (null != isLoggedIn) {
                            log.info("请在手机上确认登录");
                            isLoggedIn = null;
                        }
                    } else if (AuthStateCode.TIMEOUT.equals(status)) {
                        break;
                    }
                    DateUtils.sleep(1000);
                }
                if (null != isLoggedIn && isLoggedIn) {
                    break;
                }
                if (authLogging) {
                    log.info("登录超时，重新加载Auth二维码");
                }
            }
        }
        this.authLogging = false;
    }

    /**
     * 获取UUID
     *
     * @return 返回uuid
     */
    private String getAuthUUID() {
        log.info("获取Auth二维码UUID");
        // 登录
        String url = "https://open.weixin.qq.com/connect/qrconnect";
        ApiResponse response = this.client.send(new StringRequest(url)
                .add("appid", appID)
                .add("scope", "snsapi_login")
                .add("redirect_uri", redirectUri)
                .add("state", "gcfvks2tot")
                .add("login_type", "jssdk")
                .add("self_redirect", "false")
                .add("style", "white")
                .add("href", "https://ni-web.oss-cn-beijing.aliyuncs.com/static/wx_reset.css")
                .timeout(30));
        String body = response.getRawBody();
        int k = body.indexOf("/connect/qrcode/");
        if (k < 10) {
            return null;
        }
        String uuid = body.substring(k + 16, k + 32);
        this.authUuid = uuid;
        return this.authUuid;
    }

    /**
     * 读取Auth二维码图片
     *
     * @param uuid         二维码uuid
     * @param terminalShow 是否在终端显示输出
     */
    private void getAuthQrImage(String uuid, boolean terminalShow) {
        String uid = null != uuid ? uuid : this.authUuid;
        String imgDir = bot.config().assetsDir();

        FileResponse fileResponse = this.client.download(
                new FileRequest(String.format("https://open.weixin.qq.com/connect/qrcode/%s", uid)));

        InputStream inputStream = fileResponse.getInputStream();
        File qrCode = WeChatUtils.saveFile(inputStream, imgDir, "qrcodeAuth.png");
        DateUtils.sleep(200);
        try {
            QRCodeUtils.showQrCode(qrCode, terminalShow);
        } catch (Exception e) {
            this.getAuthQrImage(uid, terminalShow);
        }
    }

    /**
     * 检查是否登录
     *
     * @param authUuid 二维码uuid
     * @return 返回登录状态码
     */
    private String checkLogin(String authUuid, boolean isLast404) {
        String baseUrl = "https://long.open.weixin.qq.com/connect/l/qrconnect";
        Long time = System.currentTimeMillis();
        StringRequest request = new StringRequest(baseUrl)
                .add("uuid", authUuid)
                .add("_", time)
                .timeout(50);
        if (isLast404) {
            request.add("last", "404");
        }
        ApiResponse apiResponse = this.client.send(request);
        Matcher matcher = CHECK_LOGIN_PATTERN.matcher(apiResponse.getRawBody());
        if (matcher.find()) {
            String code = matcher.group(1);
            if (AuthStateCode.SUCCESS.equals(code)) {
                this.authCode = matcher.group(2);
            }
            return code;
        }
        return AuthStateCode.FAIL;
    }

    private String pushAuthLogin(String authCode) {
        String baseUrl = "https://api.weixin.qq.com/sns/oauth2/access_token";
        Long time = System.currentTimeMillis();
        JsonResponse apiResponse = this.client.send(new JsonRequest(baseUrl)
                .add("appid", appID)
                .add("secret", appSecret)
                .add("code", authCode)
                .add("grant_type", "authorization_code"));
        System.out.println("^^^^^^^^^^^" + apiResponse.getRawBody());
        return apiResponse.getString("unionid");
    }

    @Override
    public String getAuthCode() {
        return authCode;
    }

    @Override
    public String getOrgId() {
        return this.orgId;
    }

    @Override
    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    @Override
    public void logout() {
    }


}