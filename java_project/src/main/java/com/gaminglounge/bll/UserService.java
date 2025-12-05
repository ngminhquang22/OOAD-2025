package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.UserDAL;
import com.gaminglounge.model.User;

public class UserService {
    private UserDAL userDAL = new UserDAL();

    public List<User> getAllUsers() {
        return userDAL.getAllUsers();
    }

    public boolean addUser(String username, String password, String email, String roleName) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return false;
        }
        // Check if username exists
        if (userDAL.getByUsername(username) != null) {
            return false;
        }

        int roleId = userDAL.getRoleIdByName(roleName);
        if (roleId == -1) return false;

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(password); // In real app, hash this
        u.setEmail(email);
        u.setRoleId(roleId);
        
        return userDAL.addUser(u);
    }

    public boolean updateUser(User user, String roleName) {
        int roleId = userDAL.getRoleIdByName(roleName);
        if (roleId != -1) {
            user.setRoleId(roleId);
        }
        return userDAL.updateUser(user);
    }

    public boolean changePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.isEmpty()) return false;
        return userDAL.changePassword(userId, newPassword);
    }

    public boolean deleteUser(int userId) {
        return userDAL.deleteUser(userId);
    }

    public boolean toggleStatus(int userId, boolean currentStatus) {
        return userDAL.updateStatus(userId, !currentStatus);
    }

    public List<String> getAllRoles() {
        return userDAL.getAllRoles();
    }
}
