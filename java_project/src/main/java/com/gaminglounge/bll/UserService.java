package com.gaminglounge.bll;

import java.util.List;

import com.gaminglounge.dal.UserDAL;
import com.gaminglounge.model.User;

public class UserService {
    private UserDAL userDAL = new UserDAL();

    public List<User> getAllUsers(boolean includeDeleted) {
        return userDAL.getAllUsers(includeDeleted);
    }

    public void addUser(String username, String password, String email, String roleName) throws Exception {
        // 1. Validate Username
        if (username == null || username.trim().isEmpty()) {
            throw new Exception("Tên đăng nhập không được để trống.");
        }
        if (username.length() < 3) {
            throw new Exception("Tên đăng nhập phải có ít nhất 3 ký tự.");
        }
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new Exception("Tên đăng nhập chỉ được chứa chữ cái, số và dấu gạch dưới.");
        }
        if (userDAL.getByUsername(username) != null) {
            throw new Exception("Tên đăng nhập đã tồn tại.");
        }

        // 2. Validate Password
        if (password == null || password.isEmpty()) {
            throw new Exception("Mật khẩu không được để trống.");
        }
        if (password.length() < 6) {
            throw new Exception("Mật khẩu phải có ít nhất 6 ký tự.");
        }

        // 3. Validate Email
        if (email == null || email.trim().isEmpty()) {
            throw new Exception("Email không được để trống.");
        }
        if (!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Email không đúng định dạng (ví dụ: user@example.com).");
        }

        int roleId = userDAL.getRoleIdByName(roleName);
        if (roleId == -1) {
            throw new Exception("Vai trò không hợp lệ.");
        }

        User u = new User();
        u.setUsername(username);
        u.setPasswordHash(password); // In real app, hash this
        u.setEmail(email);
        u.setRoleId(roleId);
        
        if (!userDAL.addUser(u)) {
            throw new Exception("Lỗi cơ sở dữ liệu khi thêm người dùng.");
        }
    }

    public void updateUser(User user, String roleName) throws Exception {
        // Validate Email
        if (user.getEmail() == null || user.getEmail().trim().isEmpty()) {
            throw new Exception("Email không được để trống.");
        }
        if (!user.getEmail().matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new Exception("Email không đúng định dạng.");
        }

        int roleId = userDAL.getRoleIdByName(roleName);
        if (roleId != -1) {
            user.setRoleId(roleId);
        }
        
        if (!userDAL.updateUser(user)) {
            throw new Exception("Lỗi cơ sở dữ liệu khi cập nhật.");
        }
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
