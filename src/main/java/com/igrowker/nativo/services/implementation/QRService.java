package com.igrowker.nativo.services.implementation;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.qrcode.QRCodeWriter;
import com.igrowker.nativo.exceptions.QrGenerationException;
import org.springframework.stereotype.Service;

import java.awt.image.BufferedImage;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import java.io.ByteArrayOutputStream;

@Service
public class QRService {

    public String generateQrCode(String paymentId) {
        try{
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            Map<EncodeHintType, Object> hints = new HashMap<>();
            hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

            var bitMatrix = qrCodeWriter.encode("PagoID:" + paymentId, BarcodeFormat.QR_CODE, 250, 250, hints);
            BufferedImage qrImage = new BufferedImage(250, 250, BufferedImage.TYPE_INT_RGB);

            for (int x = 0; x < 250; x++) {
                for (int y = 0; y < 250; y++) {
                    qrImage.setRGB(x, y, bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            ImageIO.write(qrImage, "png", outputStream);

            byte[] qrImageBytes = outputStream.toByteArray();
            return Base64.getEncoder().encodeToString(qrImageBytes); // Devuelve el QR en Base64
        }catch (Exception e){
            throw new QrGenerationException("Ocurrio un error al generar el QR. Intentelo nuevamente.");
        }

    }
}
