package io.github.biezhi.wechat.api.model;

import io.github.biezhi.wechat.WeChatBot;
import io.github.biezhi.wechat.api.client.BotClient;
import lombok.Data;
import okhttp3.Cookie;

import java.util.List;
import java.util.Map;

/**
 * 自动登录字段
 *
 * @author biezhi
 * @date 2018/1/21
 */
@Data
public class HotReloadAuth {

    private String nikcName;
    private String orgId;

    public static HotReloadAuth build(String nikcName, String orgId) {
        HotReloadAuth hotReloadAuth = new HotReloadAuth();
        hotReloadAuth.nikcName = nikcName;
        hotReloadAuth.orgId = orgId;
        return hotReloadAuth;
    }

    /**
     * 重新登录
     */
    public boolean reLogin(WeChatBot bot) {
        if (null != this.nikcName && !"".equals(this.nikcName)
                && null != this.orgId && !"".equals(this.orgId)) {
            bot.authApi().setOrgId((this.orgId));
            return true;
        }
        return false;
    }
}
