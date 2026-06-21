package org.temochko.Business;

import org.temochko.DataAccess.Repositories.User.UserRepository;

public class AuthService {

    private final UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
