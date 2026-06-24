package org.temochko.NetworkLayer.Protocol;

import org.temochko.Business.DTOs.Message.ChatMessage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

public class Encrypter {

    private final static String ALGORITHM = "AES";
    private final static String ENCRYPTION_KEY_STRING = "thisisa128bitkey";

    public Encrypter() {}

    public byte[] encrypt(ChatMessage message) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(message);
        oos.flush();
        byte[] messageBytes = baos.toByteArray();

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = new SecretKeySpec(ENCRYPTION_KEY_STRING.getBytes(), ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedMessage = cipher.doFinal(messageBytes);

        int wlen = encryptedMessage.length;

        ByteBuffer buffer = ByteBuffer.allocate(16 + wlen + 2);

        // Magic byte
        buffer.put((byte) 0x13);
        // Unique identifier
        buffer.put((byte) 0x01);

        buffer.putLong(System.currentTimeMillis());

        buffer.putInt(wlen);

        // 1st crc
        short firstCrc = Crc16.calculateCrc(buffer.array(), 0, 14);
        buffer.putShort(firstCrc);

        buffer.put(encryptedMessage);

        // 2nd Crc
        short secondCrc = Crc16.calculateCrc(buffer.array(), 16, wlen);
        buffer.putShort(secondCrc);

        return buffer.array();
    }
}