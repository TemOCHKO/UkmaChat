package org.temochko.NetworkLayer;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;

import org.temochko.Business.AuthService;
import org.temochko.Business.DTOs.*;
import org.temochko.Business.Utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class ClientHandler extends Thread{

    private final Socket socket;
    private final AuthService authService;
    public ClientHandler(Socket socket, AuthService authService) {
        this.socket = socket;
        this.authService = authService;
    }

    public void run() {
        try {
            handleClientSocket();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleClientSocket() throws IOException {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream())) {
            out.flush();
            try (ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                // handshake
                KeyPair rsaKeys = CryptoUtils.generateRSAKeyPair();
                out.writeObject(new RsaPublicKeyExchange(rsaKeys.getPublic()));
                out.flush();

                EncryptedAesKeyExchange aesExchange = (EncryptedAesKeyExchange) in.readObject();

                Cipher rsaCipher = Cipher.getInstance("RSA");
                rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeys.getPrivate());
                byte[] decryptedAesBytes = rsaCipher.doFinal(aesExchange.encryptedAesKey);
                SecretKey aesSessionKey = new SecretKeySpec(decryptedAesBytes, "AES");

                System.out.println("Сервер: Handshake з клієнтом завершено.");

                // handling packets
                while (true) {
                    Object request = in.readObject();

                    if (request instanceof LoginRequestDto) {
                        LoginRequestDto loginReq = (LoginRequestDto) request;

                        String decryptedPassword = CryptoUtils.decryptString(loginReq.password, aesSessionKey);
                        LoginResponseDto response = authService.authenticate(loginReq.username, decryptedPassword);

                        out.writeObject(response);
                        out.flush();
                    }
                    else if (request instanceof RegisterRequestDto) {
                        RegisterRequestDto regReq = (RegisterRequestDto) request;

                        String decryptedPassword = CryptoUtils.decryptString(regReq.password, aesSessionKey);
                        RegisterResponseDto response = authService.register(regReq.username, decryptedPassword, regReq.email);

                        out.writeObject(response);
                        out.flush();
                    }
                }
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("Client Disconnected.");
        } catch (Exception e) {
            System.err.println("Error handling the client: " + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
            } catch (Exception ex) {
            }
        }

    }
}
