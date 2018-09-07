package naturali;

import io.github.biezhi.wechat.api.constant.Config;

import javax.swing.*;
import java.awt.*;

public class FrameController {

    NaturaliBot naturaliBot;
    Config config;
    JFrame frame;

    private static FrameController frameController;

    public static FrameController instance() {
        if (null == frameController) {
            frameController = new FrameController();
        }
        return frameController;
    }

    public void init(NaturaliBot naturaliBot, Config config) {
        this.naturaliBot = naturaliBot;
        this.config = config;
        frame = new JFrame("ChatBot");
    }

    public void showQRCode(String path, String text) {
        frame = new JFrame("ChatBot_" + text);
        JLabel label = new JLabel();
        ImageIcon img = new ImageIcon(config.assetsDir() + "/" + path);// 创建图片对象
        label.setIcon(img);
        frame.add(label);
        show();
    }

    public void showTips(String text) {
        frame = new JFrame("ChatBot_" + text);
        JLabel labelText = new JLabel(text, JLabel.CENTER);
        frame.add(labelText);
        show();
    }

    private void show() {
        frame.setSize(500, 500);
        frame.setBackground(Color.WHITE);
        frame.setLocation(300, 300);
        frame.setVisible(true);
    }

}
