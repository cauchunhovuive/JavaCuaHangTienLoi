package dao;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import connectDB.DatabaseConnection;
import entities.TaiKhoan;

public class TaiKhoanDAO {

    public ArrayList<TaiKhoan> dsTaiKhoan() {
        String sql = "SELECT TenTaiKhoan, MatKhau, Email, MaQuyen, NgayTao, TrangThai FROM TaiKhoan";
        ArrayList<TaiKhoan> dsTk = new ArrayList<>();

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                dsTk.add(new TaiKhoan(
                        rs.getString("TenTaiKhoan"),
                        rs.getString("MatKhau"),
                        rs.getString("Email"),
                        rs.getInt("MaQuyen"),
                        rs.getTimestamp("NgayTao"),
                        rs.getString("TrangThai")));
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy danh sách tài khoản: " + e.getMessage());
        }

        return dsTk;
    }
}
