package io.github.biezhi.wechat.utils;

import com.google.zxing.*;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.util.EnumMap;
import java.util.Map;

/**
 * 二维码工具类
 *
 * @author biezhi
 * @date 2018/1/18
 */
@Slf4j
public class QRCodeUtils {

    /**
     * 显示二维码，Linux会显示在终端
     *
     * @param qrCode
     * @param terminal
     */
    public static void showQrCode(File qrCode, boolean terminal) throws WriterException {
        if (!terminal) {
            String os = System.getProperty("os.name").toLowerCase();
            try {
                if (os.contains("mac") || os.contains("win")) {
                    Desktop.getDesktop().open(qrCode);
                    return;
                }
            } catch (Exception e) {
                System.out.print("在 {"+os+"} 下打开文件 {"+qrCode.getPath()+"} 失败"+e);
            }
        }
        Map<EncodeHintType, Object> hintMapEncode = new EnumMap<EncodeHintType, Object>(EncodeHintType.class);
        hintMapEncode.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        // Now with zxing version 3.2.1 you could change border size (white border size to just 1)
        // default = 4
        hintMapEncode.put(EncodeHintType.MARGIN, 1);
        hintMapEncode.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);

        Map<DecodeHintType, Object> hintMap = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        // 解码设置编码方式为：utf-8，
        hintMap.put(DecodeHintType.CHARACTER_SET, "UTF-8");
        //优化精度
        hintMap.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        //复杂模式，开启PURE_BARCODE模式
        hintMap.put(DecodeHintType.PURE_BARCODE, Boolean.TRUE);

        String qrContent = QRCodeUtils.readQRCode(qrCode, hintMap);
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix;
        bitMatrix = qrCodeWriter.encode(qrContent, BarcodeFormat.QR_CODE, 10, 10, hintMapEncode);
        System.out.println(toAscii(bitMatrix));
    }

    /**
     * 将二维码输出为 ASCII
     *
     * @param bitMatrix
     * @return
     */
    private static String toAscii(BitMatrix bitMatrix) {
        StringBuilder sb = new StringBuilder();
        for (int rows = 0; rows < bitMatrix.getHeight(); rows++) {
            for (int cols = 0; cols < bitMatrix.getWidth(); cols++) {
                boolean x = bitMatrix.get(rows, cols);
                if (!x) {
                    // white
                    sb.append("\033[47m  \033[0m");
                } else {
                    sb.append("\033[30m  \033[0;39m");
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    /**
     * 读取二维码信息
     *
     * @param filePath 文件路径
     * @param hintMap  hintMap
     * @return 二维码内容
     */
    private static String readQRCode(File filePath, Map hintMap) {
        try {
            BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(
                    new BufferedImageLuminanceSource(ImageIO.read(new FileInputStream(filePath)))));

            Result qrCodeResult = new MultiFormatReader().decode(binaryBitmap, hintMap);
            return qrCodeResult.getText();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
