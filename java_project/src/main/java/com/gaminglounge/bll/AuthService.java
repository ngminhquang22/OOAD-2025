package com.gaminglounge.bll;

import com.gaminglounge.dal.UserDAL;
import com.gaminglounge.model.User;

public class AuthService {
    private UserDAL userDAL = new UserDAL();

    public User login(String username, String password) {
        User user = userDAL.getByUsername(username);
        if (user != null && user.getPasswordHash().equals(password)) {
            return user;
        }
        return null;
    }
}
