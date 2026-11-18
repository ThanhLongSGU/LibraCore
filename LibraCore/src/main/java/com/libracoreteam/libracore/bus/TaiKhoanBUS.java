package com.libracoreteam.libracore.bus;

import com.libracoreteam.libracore.dao.TaiKhoanDAO;
import com.libracoreteam.libracore.dao.VaiTroDAO;
import com.libracoreteam.libracore.model.TaiKhoan;
import com.libracoreteam.libracore.model.VaiTro;
import com.libracoreteam.libracore.util.DatabaseConfigurationException;
import com.libracoreteam.libracore.util.UserSession;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TaiKhoanBUS {

    private static final Logger LOGGER = Logger.getLogger(TaiKhoanBUS.class.getName());

    public enum LoginStatus {
        SUCCESS,
        INVALID_CREDENTIALS,
        ACCOUNT_LOCKED,
        EMPLOYEE_INACTIVE,
        PROFILE_INCOMPLETE,
        DATABASE_CONFIGURATION_ERROR,
        DATABASE_UNAVAILABLE,
        DATABASE_AUTHENTICATION_ERROR,
        DATABASE_QUERY_ERROR,
        INTERNAL_ERROR
    }

    private final TaiKhoanDAO taiKhoanDAO;
    private final VaiTroDAO vaiTroDAO;

    public TaiKhoanBUS() {
        this.taiKhoanDAO = new TaiKhoanDAO();
        this.vaiTroDAO = new VaiTroDAO();
    }

    public List<TaiKhoan> getAll() {
        return taiKhoanDAO.getAll();
    }

    public TaiKhoan getById(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID tài khoản không hợp lệ.");
        return taiKhoanDAO.getById(id);
    }

    public boolean add(TaiKhoan tk) {
        validate(tk);
        return taiKhoanDAO.insert(tk);
    }

    public boolean update(TaiKhoan tk) {
        if (tk == null || tk.getIdTaiKhoan() <= 0)
            throw new IllegalArgumentException("Tài khoản không hợp lệ.");
        validate(tk);
        return taiKhoanDAO.update(tk);
    }

    public boolean delete(int id) {
        if (id <= 0) throw new IllegalArgumentException("ID tài khoản không hợp lệ.");
        return taiKhoanDAO.delete(id);
    }

    public List<TaiKhoan> search(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) return getAll();
        return taiKhoanDAO.search(keyword);
    }

    public Map<Integer, String> getRoleMap() {
        Map<Integer, String> roleMap = new HashMap<>();
        List<VaiTro> dsVaiTro = vaiTroDAO.getAll();
        if (dsVaiTro == null) return roleMap;
        for (VaiTro vt : dsVaiTro) {
            if (vt != null) roleMap.put(vt.getIdVaiTro(), vt.getTenVaiTro());
        }
        return roleMap;
    }

    private void validate(TaiKhoan tk) {
        if (tk == null)
            throw new IllegalArgumentException("Tài khoản không được null.");
        if (tk.getTaiKhoan() == null || tk.getTaiKhoan().trim().isEmpty())
            throw new IllegalArgumentException("Tên đăng nhập không được để trống.");
        if (tk.getTaiKhoan().trim().length() < 3)
            throw new IllegalArgumentException("Tên đăng nhập phải có ít nhất 3 ký tự.");
        if (tk.getMatKhau() == null || tk.getMatKhau().trim().isEmpty())
            throw new IllegalArgumentException("Mật khẩu không được để trống.");
        if (tk.getMatKhau().length() < 6)
            throw new IllegalArgumentException("Mật khẩu phải có ít nhất 6 ký tự.");
    }

    public LoginStatus checkLogin(String username, char[] passwordInput) {
        if (passwordInput == null) {
            return LoginStatus.INVALID_CREDENTIALS;
        }
        try {
            if (username == null || username.isBlank() || passwordInput.length == 0) {
                return LoginStatus.INVALID_CREDENTIALS;
            }

            TaiKhoanDAO.LoginInfo userInfo = taiKhoanDAO.getLoginInfo(username);
            if (userInfo == null) {
                return LoginStatus.INVALID_CREDENTIALS;
            }

            String inputPassword = new String(passwordInput);
            if (userInfo.matKhau() == null || !inputPassword.equals(userInfo.matKhau())) {
                return LoginStatus.INVALID_CREDENTIALS;
            }
            if (!userInfo.taiKhoanHoatDong()) {
                return LoginStatus.ACCOUNT_LOCKED;
            }
            if (userInfo.idNhanVien() == null || userInfo.nhanVienHoatDong() == null
                || userInfo.tenNhanVien() == null || userInfo.tenVaiTro() == null) {
                return LoginStatus.PROFILE_INCOMPLETE;
            }
            if (!userInfo.nhanVienHoatDong()) {
                return LoginStatus.EMPLOYEE_INACTIVE;
            }

            UserSession.getInstance().login(
                userInfo.idTaiKhoan(),
                userInfo.tenNhanVien(),
                userInfo.tenVaiTro()
            );
            return LoginStatus.SUCCESS;
        } catch (DatabaseConfigurationException e) {
            LOGGER.log(Level.SEVERE, "Database configuration error during login.", e);
            return LoginStatus.DATABASE_CONFIGURATION_ERROR;
        } catch (SQLException e) {
            LoginStatus status = classifyDatabaseError(e);
            LOGGER.log(Level.SEVERE, "Database error during login: " + status, e);
            return status;
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error during login.", e);
            return LoginStatus.INTERNAL_ERROR;
        } finally {
            Arrays.fill(passwordInput, '0');
        }
    }

    private LoginStatus classifyDatabaseError(SQLException exception) {
        for (SQLException current = exception; current != null; current = current.getNextException()) {
            String sqlState = current.getSQLState();
            if (sqlState != null && sqlState.startsWith("08")) {
                return LoginStatus.DATABASE_UNAVAILABLE;
            }
            if (sqlState != null && sqlState.startsWith("28")) {
                return LoginStatus.DATABASE_AUTHENTICATION_ERROR;
            }
        }
        return LoginStatus.DATABASE_QUERY_ERROR;
    }
}