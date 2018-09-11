package naturali;

import io.github.biezhi.wechat.api.constant.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

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
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                naturaliBot.api().logout();
                System.exit(0);
            }
        });
    }

    public void showQRCode(String path, String text) {
        frame.getContentPane().removeAll();
        ImageIcon img = new ImageIcon(config.assetsDir() + "/" + path);// 创建图片对象
        JLabel label = new JLabel(text, img, JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.TOP);
        label.setHorizontalTextPosition(JLabel.CENTER);
        frame.add(label, BorderLayout.CENTER);
        show();
    }

    public void showTips(String text) {
        frame.getContentPane().removeAll();
        JLabel labelText = new JLabel(text, JLabel.CENTER);
        frame.add(labelText, BorderLayout.CENTER);
        show();
    }

    private void show() {
        frame.setSize(600, 600);
        frame.setBackground(Color.WHITE);
        frame.setLocation(300, 300);
        frame.setVisible(true);
    }

}
