package com.gaminglounge.bll;

import com.gaminglounge.dal.UserDAL;
import com.gaminglounge.model.User;

public class AuthService {
    private UserDAL userDAL = new UserDAL();

    public User login(String username, String password) throws Exception {
        User user = userDAL.getByUsername(username);
        
        if (user == null) {
            throw new Exception("Tài khoản không tồn tại hoặc đã bị xóa.");
        }
        
        if (!user.getPasswordHash().equals(password)) {
            throw new Exception("Mật khẩu không đúng.");
        }
        
        if (!user.isActive()) {
            throw new Exception("Tài khoản đã bị khóa. Vui lòng liên hệ quản trị viên.");
        }
        
        return user;
    }
}
