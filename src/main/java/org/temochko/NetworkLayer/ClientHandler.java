package org.temochko.NetworkLayer;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;

import org.temochko.Business.AuthService;
import org.temochko.Business.DTOs.KeyExchanges.EncryptedAesKeyExchange;
import org.temochko.Business.DTOs.KeyExchanges.RsaPublicKeyExchange;
import org.temochko.Business.DTOs.Login.LoginRequestDto;
import org.temochko.Business.DTOs.Login.LoginResponseDto;
import org.temochko.Business.DTOs.PingDto;
import org.temochko.Business.DTOs.Register.RegisterRequestDto;
import org.temochko.Business.DTOs.Register.RegisterResponseDto;
import org.temochko.Business.DTOs.User.SearchUserRequestDto;
import org.temochko.Business.DTOs.User.SearchUserResponseDto;
import org.temochko.Business.DTOs.User.UserSetOnlineRequestDto;
import org.temochko.Business.Utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class ClientHandler extends Thread{

    private final Socket socket;
    private final AuthService authService;

    private volatile long lastActivityTime;
    private String loggedInUsername = null;
    public ClientHandler(Socket socket, AuthService authService) {
        this.socket = socket;
        this.authService = authService;
        lastActivityTime = System.currentTimeMillis();
    }

    public long getLastActivityTime() { return lastActivityTime; }
    public String getUsername() { return loggedInUsername; }

    public void forceDisconnect() {
        try { socket.close(); } catch (Exception ignored) {}
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

                    // ping
                    lastActivityTime = System.currentTimeMillis();
                    if (request instanceof PingDto) {
                        continue;
                    }

                    if (request instanceof LoginRequestDto) {
                        LoginRequestDto loginReq = (LoginRequestDto) request;

                        String decryptedPassword = CryptoUtils.decryptString(loginReq.password, aesSessionKey);
                        LoginResponseDto response = authService.authenticate(loginReq.username, decryptedPassword);

                        if (response.success) {
                            this.loggedInUsername = loginReq.username;
                            authService.setOnline(loggedInUsername, true);
                        }

                        out.writeObject(response);
                        out.flush();
                    } else if (request instanceof RegisterRequestDto) {
                        RegisterRequestDto regReq = (RegisterRequestDto) request;

                        String decryptedPassword = CryptoUtils.decryptString(regReq.password, aesSessionKey);
                        RegisterResponseDto response = authService.register(regReq.username, decryptedPassword, regReq.email);

                        out.writeObject(response);
                        out.flush();
                    } else if (request instanceof SearchUserRequestDto) {
                        SearchUserRequestDto searchReq = (SearchUserRequestDto) request;

                        SearchUserResponseDto responseDto = authService.searchUsers(searchReq.searchQuery);

                        out.writeObject(responseDto);
                        out.flush();
                    } else if (request instanceof UserSetOnlineRequestDto) {

                        // update online status
                        authService.setOnline(((UserSetOnlineRequestDto) request).username, ((UserSetOnlineRequestDto) request).online);
                    }
                }
            }
        } catch (EOFException | java.net.SocketException e) {
            System.out.println("Client " + (loggedInUsername != null ? loggedInUsername : "anonymous ") + " disconnected.");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (loggedInUsername != null) {
                authService.setOnline(loggedInUsername, false);
            }

            // disconnect client
            SimpleServer.removeClient(this);
            try { if (socket != null) socket.close(); } catch (Exception ex) {}
        }

    }
}
