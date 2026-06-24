package org.temochko.NetworkLayer;



import org.temochko.Business.DTOs.KeyExchanges.EncryptedAesKeyExchange;
import org.temochko.Business.DTOs.KeyExchanges.RsaPublicKeyExchange;
import org.temochko.Business.DTOs.Login.LoginRequestDto;
import org.temochko.Business.DTOs.Login.LoginResponseDto;
import org.temochko.Business.DTOs.Register.RegisterRequestDto;
import org.temochko.Business.DTOs.Register.RegisterResponseDto;
import org.temochko.Business.DTOs.User.SearchUserRequestDto;
import org.temochko.Business.DTOs.User.SearchUserResponseDto;
import org.temochko.Business.Utils.CryptoUtils;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.PublicKey;


// Client side of network, works with server
public class NetworkClient {
    private final String serverAddress;
    private final int serverPort;

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private SecretKey aesSessionKey;

    public NetworkClient(String serverAddress, int serverPort) {
        this.serverAddress = serverAddress;
        this.serverPort = serverPort;
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

    public SearchUserResponseDto sendSearchUserRequest(String username) throws Exception {
        out.writeObject(new SearchUserRequestDto(username));
        out.flush();

        return (SearchUserResponseDto) in.readObject();
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
}