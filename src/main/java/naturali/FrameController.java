package naturali;

import io.github.biezhi.wechat.api.constant.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;

public class FrameController {

    NaturaliBot naturaliBot;
    Config config;
    JFrame frame;
    JLabel label;
    JButton button;

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
        frame.setLayout(null);

        label = new JLabel("", null, JLabel.CENTER);
        label.setVerticalTextPosition(JLabel.TOP);
        label.setHorizontalTextPosition(JLabel.CENTER);
        label.setBounds(0, 0, 600, 600);
        frame.add(label, BorderLayout.CENTER);

        button = new JButton("ClearCache:");
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String file = naturaliBot.config().assetsDir() + "/loginAuth.json";
                new File(file).delete();
                String file1 = naturaliBot.config().assetsDir() + "/login.json";
                new File(file1).delete();
                System.exit(0);
            }
        });
        button.setBounds(350, 0, 250, 30);
        frame.add(button);

        show();
    }

    public void showQRCode(String path, String text) {
        ImageIcon img = null;// 创建图片对象
        try {
            img = new ImageIcon(ImageIO.read(new File(config.assetsDir() + "/" + path)));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        ImageIcon img = new ImageIcon(config.assetsDir() + "/" + path);// 创建图片对象,这种方法，二维码改变的时候图片不刷新
        label.setText(text);
        label.setIcon(img);
        label.repaint();
    }

    public void showTips(String text) {
        label.setText(text);
        label.setIcon(null);
    }

    private void show() {
        frame.setSize(600, 600);
        frame.setBackground(Color.WHITE);
        frame.setLocation(200, 200);
        frame.setVisible(true);
    }

    public void resetOrg(String orgID,String nickName){
        button.setText("ClearCache:" + orgID+","+nickName);
    }

}
