package io.github.biezhi.wechat.api;

import io.github.biezhi.wechat.api.model.*;
import io.github.biezhi.wechat.api.response.MediaResponse;
import io.github.biezhi.wechat.api.response.WebSyncResponse;

import java.util.List;

/**
 * 微信API
 *
 * @author biezhi
 * @date 2018/1/21
 */
public interface WeChatAuthApi {

    /**
     * 扫码登录
     *
     * @return
     */
    void login(boolean autoLogin);

    /**
     * 退出登录
     */
    void logout();

}