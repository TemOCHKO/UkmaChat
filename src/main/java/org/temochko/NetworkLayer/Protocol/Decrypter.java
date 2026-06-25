package org.temochko.NetworkLayer.Protocol;

import org.temochko.Business.DTOs.Message.ChatMessage;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.ByteBuffer;

public class Decrypter {

    private final static String ALGORITHM = "AES";
    private final static String ENCRYPTION_KEY_STRING = "thisisa128bitkey";

    public Decrypter() {}

    public ChatMessage decrypt(byte[] messageToDecrypt) throws Exception {
        ByteBuffer buffer = ByteBuffer.wrap(messageToDecrypt);

        byte magicByte = buffer.get();
        byte uniqueIdentifierByte = buffer.get();
        long messageNumber = buffer.getLong();
        int wlen = buffer.getInt();

        // 1st Crc
        short firstCrc = buffer.getShort();
        short checksum = Crc16.calculateCrc(messageToDecrypt, 0, 14);
        validateChecksum(checksum, firstCrc);

        byte[] messageToBeDecrypted = new byte[wlen];
        buffer.get(messageToBeDecrypted);

        // 2nd Crc
        short secondCrc = buffer.getShort();
        short checksum2 = Crc16.calculateCrc(messageToDecrypt, 16, wlen);
        validateChecksum(checksum2, secondCrc);

        Cipher cipher = Cipher.getInstance(ALGORITHM);
        SecretKey key = new SecretKeySpec(ENCRYPTION_KEY_STRING.getBytes(), ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedBytes = cipher.doFinal(messageToBeDecrypted);

        ByteArrayInputStream bais = new ByteArrayInputStream(decryptedBytes);
        ObjectInputStream ois = new ObjectInputStream(bais);

        return (ChatMessage) ois.readObject();
    }

    private void validateChecksum(short expectedChecksum, short actualChecksum) {
        if (actualChecksum != expectedChecksum) {
            throw new IllegalArgumentException("Checksum does not match");
        }
    }
}