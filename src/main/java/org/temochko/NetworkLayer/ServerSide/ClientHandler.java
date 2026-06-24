package org.temochko.NetworkLayer.ServerSide;

import java.io.*;
import java.net.Socket;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.temochko.Business.AuthService;
import org.temochko.Business.DTOs.KeyExchanges.EncryptedAesKeyExchange;
import org.temochko.Business.DTOs.KeyExchanges.RsaPublicKeyExchange;
import org.temochko.Business.DTOs.Login.LoginRequestDto;
import org.temochko.Business.DTOs.Login.LoginResponseDto;
import org.temochko.Business.DTOs.Message.ChatHistoryRequestDto;
import org.temochko.Business.DTOs.Message.ChatHistoryResponseDto;
import org.temochko.Business.DTOs.Message.ChatMessage;
import org.temochko.Business.DTOs.PingDto;
import org.temochko.Business.DTOs.Register.RegisterRequestDto;
import org.temochko.Business.DTOs.Register.RegisterResponseDto;
import org.temochko.Business.DTOs.User.SearchUserRequestDto;
import org.temochko.Business.DTOs.User.SearchUserResponseDto;
import org.temochko.Business.DTOs.User.UserSetOnlineRequestDto;
import org.temochko.Business.Utils.CryptoUtils;
import org.temochko.DataAccess.Repositories.MessageRepository;
import org.temochko.NetworkLayer.Protocol.Decrypter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;


public class ClientHandler extends Thread{

    private final Socket socket;
    private final AuthService authService;

    private volatile long lastActivityTime;
    private String loggedInUsername = null;

    ObjectInputStream in;
    ObjectOutputStream out;
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
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush();
            try {
                in = new ObjectInputStream(socket.getInputStream());
                // handshake
                KeyPair rsaKeys = CryptoUtils.generateRSAKeyPair();
                out.writeObject(new RsaPublicKeyExchange(rsaKeys.getPublic()));
                out.flush();

                EncryptedAesKeyExchange aesExchange = (EncryptedAesKeyExchange) in.readObject();

                Cipher rsaCipher = Cipher.getInstance("RSA");
                rsaCipher.init(Cipher.DECRYPT_MODE, rsaKeys.getPrivate());
                byte[] decryptedAesBytes = rsaCipher.doFinal(aesExchange.encryptedAesKey);
                SecretKey aesSessionKey = new SecretKeySpec(decryptedAesBytes, "AES");

                // handling packets
                while (true) {
                    if (in == null) { return; }
                    Object request = in.readObject();

                    // ping
                    lastActivityTime = System.currentTimeMillis();
                    if (request instanceof PingDto) {
                        continue;
                    }

                    else if (request instanceof byte[]) {
                        byte[] packet = (byte[]) request;

                        try {
                            Decrypter decrypter = new Decrypter();
                            ChatMessage msg = decrypter.decrypt(packet);

                            MessageRepository msgRepo = new MessageRepository();
                            msgRepo.saveMessage(new ChatMessage(msg.username, msg.from, msg.message, msg.timestamp));

                            ClientHandler recipient = SimpleServer.getClientByUsername(msg.username);
                            if (recipient != null) {
                                recipient.sendMessageToClient(packet);
                            }
                        } catch (Exception e) {
                            System.err.println("Error: " + e.getMessage());
                        }
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
                    } else if (request instanceof ChatMessage) {
                        MessageRepository messageRepository = new MessageRepository();

                        ChatMessage chatMsg = (ChatMessage) request;
                        messageRepository.saveMessage(new ChatMessage(chatMsg.username, chatMsg.from, chatMsg.message, chatMsg.timestamp));
                        ClientHandler recipient = SimpleServer.getClientByUsername(chatMsg.username);

                        if (recipient != null) {
                            recipient.sendMessageToClient(chatMsg);
                        }
                    } else if (request instanceof ChatHistoryRequestDto) {
                        MessageRepository messageRepository = new MessageRepository();
                        ChatHistoryRequestDto historyReq = (ChatHistoryRequestDto) request;

                        List<ChatMessage> dbHistory = messageRepository.getChatHistory(this.loggedInUsername, historyReq.targetUsername);
                        List<ChatMessage> dtoHistory = new java.util.ArrayList<>();

                        for (ChatMessage dbMsg : dbHistory) {
                            LocalDateTime dateTime = LocalDateTime.parse(dbMsg.timestamp);
                            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("HH:mm");
                            String timeStr = dateTime.format(outputFormatter);

                            dtoHistory.add(new ChatMessage(historyReq.targetUsername, dbMsg.from, dbMsg.message, timeStr));
                        }

                        out.writeObject(new ChatHistoryResponseDto(historyReq.targetUsername, dtoHistory));
                        out.flush();
                    }
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        } catch (EOFException | java.net.SocketException e) {

        } catch (Exception e) {
            System.err.println("Error handling client: " + e.getMessage());
            e.printStackTrace();

        } finally {
            if (loggedInUsername != null) {
                authService.setOnline(loggedInUsername, false);
            }

            // disconnect client
            SimpleServer.removeClient(this);
            try {
                if (socket != null) socket.close();
            } catch (Exception ignored) {}
        }

    }

    public void sendMessageToClient(Object dto) {
        try {
            synchronized (out) {
                out.writeObject(dto);
                out.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
