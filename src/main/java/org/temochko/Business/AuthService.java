package org.temochko.Business;

import org.mindrot.jbcrypt.BCrypt;
import org.temochko.Business.DTOs.Login.LoginResponseDto;
import org.temochko.Business.DTOs.Register.RegisterRequestDto;
import org.temochko.Business.DTOs.Register.RegisterResponseDto;
import org.temochko.Business.DTOs.User.SearchUserRequestDto;
import org.temochko.Business.DTOs.User.SearchUserResponseDto;
import org.temochko.Business.DTOs.User.UserSummaryDto;
import org.temochko.DataAccess.Models.User;
import org.temochko.DataAccess.Repositories.User.IUserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AuthService {

    private final IUserRepository userRepository;

    // login
    public AuthService(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponseDto authenticate(String username, String rawPassword) {
        try {
            Optional<String> storedHashOpt = userRepository.findPasswordHash(username);
            if (storedHashOpt.isPresent()) {
                String storedHash = storedHashOpt.get();

                // compare
                boolean passwordMatches = BCrypt.checkpw(rawPassword, storedHash);
                if (passwordMatches) {
                    String token = UUID.randomUUID().toString();
                    return new LoginResponseDto(true, "Login successful", token);
                }
            }

            return new LoginResponseDto(false, "Invalid username or password", null);

        } catch (Exception e) {
            e.printStackTrace();
            return new LoginResponseDto(false, "Server error during authentication", null);
        }
    }

    public RegisterResponseDto register(String username, String rawPassword, String email) {
        try {
            if (userRepository.findByUsername(username).isPresent()) {
                return new RegisterResponseDto(false, "Username is already taken", null);
            }
            String hashedPassword = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
            userRepository.save(username, hashedPassword, email);

            String token = UUID.randomUUID().toString();
            return new RegisterResponseDto(true, "Registration successful", token);

        } catch (Exception e) {
            e.printStackTrace();
            return new RegisterResponseDto(false, "Server error during registration", null);
        }
    }
    public RegisterResponseDto register(RegisterRequestDto request) throws Exception {
        return register(request.username, request.password, request.email);
    }


    public SearchUserResponseDto searchUsers(String query) {
        try {
            List<User> dbUsers = userRepository.searchByUsername(query);

            List<UserSummaryDto> safeUsers = new ArrayList<>();
            for (User u : dbUsers) {
                safeUsers.add(new UserSummaryDto(u.getId(), u.getUsername(), u.isOnline()));
            }

            return new SearchUserResponseDto(true, "Users found", safeUsers);
        } catch (Exception e) {
            return new SearchUserResponseDto(false, "Search failed", new ArrayList<>());
        }
    }
}
