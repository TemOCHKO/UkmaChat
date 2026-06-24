package org.temochko.NetworkLayer.ClientSide;

import org.temochko.Business.DTOs.KeyExchanges.EncryptedAesKeyExchange;
import org.temochko.Business.DTOs.KeyExchanges.RsaPublicKeyExchange;
import org.temochko.Business.DTOs.Login.LoginRequestDto;
import org.temochko.Business.DTOs.Login.LoginResponseDto;
import org.temochko.Business.DTOs.Message.ChatMessage;
import org.temochko.Business.DTOs.PingDto;
import org.temochko.Business.DTOs.Register.RegisterRequestDto;
import org.temochko.Business.DTOs.Register.RegisterResponseDto;
import org.temochko.Business.DTOs.User.SearchUserRequestDto;
import org.temochko.Business.DTOs.User.SearchUserResponseDto;
import org.temochko.Business.DTOs.User.UserSetOnlineRequestDto;
import org.temochko.Business.Utils.CryptoUtils;
import org.temochko.NetworkLayer.Protocol.Decrypter;
import org.temochko.NetworkLayer.Protocol.Encrypter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

// Client side of network, works with server
public class NetworkClient {
    private final String serverAddress;
    private final int serverPort;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SecretKey aesSessionKey;
    private ScheduledExecutorService heartbeatScheduler;

    private Thread listenerThread;
    private Consumer<ChatMessage> onMessageReceived;
    private Consumer<SearchUserResponseDto> onSearchResponseReceived;

    private Encrypter encrypter = new Encrypter();
    private Decrypter decrypter = new Decrypter();

    public NetworkClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
    }

    public void setOnSearchResponseReceived(Consumer<SearchUserResponseDto> callback) {
        this.onSearchResponseReceived = callback;
    }

    // get a connection with the server and exhcange keys
    public void connect() throws Exception {
        socket = new Socket(serverAddress, serverPort);

        out = new ObjectOutputStream(socket.getOutputStream());
        out.flush();
        in = new ObjectInputStream(socket.getInputStream());

        // get public rsa from server
        RsaPublicKeyExchange rsaExchange = (RsaPublicKeyExchange) in.readObject();
        PublicKey serverPublicKey = rsaExchange.publicKey;

        // generate ouur own key
        aesSessionKey = CryptoUtils.generateAESKey();

        // encipher aes key with our rsa key from server
        Cipher rsaCipher = Cipher.getInstance("RSA");
        rsaCipher.init(Cipher.ENCRYPT_MODE, serverPublicKey);
        byte[] encryptedAesKey = rsaCipher.doFinal(aesSessionKey.getEncoded());

        // send to the server
        out.writeObject(new EncryptedAesKeyExchange(encryptedAesKey));
        out.flush();

        startHeartbeat();
    }

    public LoginResponseDto sendLoginRequest(String username, String rawPassword) throws Exception {

        // only encipher the password
        String encryptedPassword = CryptoUtils.encryptString(rawPassword, aesSessionKey);
        LoginRequestDto request = new LoginRequestDto(username, encryptedPassword);

        out.writeObject(request);
        out.flush();

        return (LoginResponseDto) in.readObject();
    }

    public RegisterResponseDto sendRegisterRequest(String username, String rawPassword, String email) throws Exception {
        String encryptedPassword = CryptoUtils.encryptString(rawPassword, aesSessionKey);
        RegisterRequestDto request = new RegisterRequestDto(username, encryptedPassword, email);

        out.writeObject(request);
        out.flush();

        return (RegisterResponseDto) in.readObject();
    }

    public void sendSearchUserRequest(String username) throws Exception {
        out.writeObject(new SearchUserRequestDto(username));
        out.flush();

    }

    public void sendSetOnlineRequest(String username, boolean online) throws Exception {
        out.writeObject(new UserSetOnlineRequestDto(username, online));
        out.flush();

    }

    private void startHeartbeat() {
        heartbeatScheduler = Executors.newSingleThreadScheduledExecutor();
        // every 15 secs
        heartbeatScheduler.scheduleAtFixedRate(() -> {
            try {
                if (out != null) {
                    out.writeObject(new PingDto());
                    out.flush();
                }
            } catch (Exception e) {
                System.err.println("heartbeat failed");
                disconnect();
                System.exit(0);
            }
        }, 15, 15, TimeUnit.SECONDS);
    }

    public void disconnect() {
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(ChatMessage chatMessage) throws Exception {
        byte[] packet = encrypter.encrypt(chatMessage);
        out.writeObject(packet);
        out.flush();
    }

    public void setOnMessageReceived(Consumer<ChatMessage> callback) {
        this.onMessageReceived = callback;
    }

    // after successfully logged in
    public void startListening() {
        listenerThread = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    Object incoming = in.readObject();

                    if (incoming instanceof byte[]) {
                        byte[] packet = (byte[]) incoming;
                        try {
                            ChatMessage decryptedMsg = decrypter.decrypt(packet);
                            if (onMessageReceived != null) {
                                onMessageReceived.accept(decryptedMsg);
                            }
                        } catch (Exception e) {
                            System.err.println("Decrypter error " + e.getMessage());
                        }
                    }

                    if (incoming instanceof ChatMessage) {
                        ChatMessage msg = (ChatMessage) incoming;
                        if (onMessageReceived != null) {
                            onMessageReceived.accept(msg);
                        }
                    }
                    else if (incoming instanceof SearchUserResponseDto) {
                        SearchUserResponseDto response = (SearchUserResponseDto) incoming;
                        if (onSearchResponseReceived != null) {
                            onSearchResponseReceived.accept(response);
                        }
                    }
                }
            } catch (Exception e) {
            }
        });
        listenerThread.start();
    }
}