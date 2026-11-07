package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import connectDB.DatabaseConnection;
import entities.PhanQuyen;

public class PhanQuyenDAO {

    public ArrayList<PhanQuyen> getDsQuyen() {
        String sql = "SELECT * FROM PhanQuyen";
        ArrayList<PhanQuyen> quyens = new ArrayList<>();
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            rs = stmt.executeQuery();
            System.out.println("Đang truy vấn danh sách quyền...");
            int count = 0;
            
            while (rs.next()) {
                count++;
                int maQuyen = rs.getInt("MaQuyen");
                String tenQuyen = rs.getString("TenQuyen");
                PhanQuyen pq = new PhanQuyen(maQuyen, tenQuyen);
                quyens.add(pq);
                System.out.println("Đã lấy quyền: " + maQuyen + " - " + tenQuyen);
            }
            
            System.out.println("Tổng số quyền lấy được: " + count);
        } catch (SQLException e) {
            System.out.println("Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy danh sách quyền: " + e.getMessage());
        } finally {
            try {
                if(rs != null) rs.close();
                if(stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return quyens;
    }

    public PhanQuyen getPhanQuyen(int maQuyen) {
        String sql = "SELECT MaQuyen, TenQuyen FROM PhanQuyen WHERE MaQuyen = ?";
        PhanQuyen pq = null;
        Connection con = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            con = DatabaseConnection.getInstance().getConnection();
            stmt = con.prepareStatement(sql);
            stmt.setInt(1, maQuyen);
            
            System.out.println("Truy vấn phân quyền với mã: " + maQuyen);
            System.out.println("SQL: " + stmt.toString());
            
            rs = stmt.executeQuery();

            if (rs.next()) {
                pq = new PhanQuyen();
                pq.setMaQuyen(rs.getInt("MaQuyen"));
                pq.setTenQuyen(rs.getString("TenQuyen"));
                System.out.println("Đã tìm thấy quyền: " + pq.getMaQuyen() + " - " + pq.getTenQuyen());
            } else {
                System.out.println("Không tìm thấy quyền với mã: " + maQuyen);
                pq = new PhanQuyen(0, "Không xác định");
            }
        } catch (SQLException e) {
            System.out.println("Lỗi SQL: " + e.getMessage());
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Lỗi khi lấy quyền theo mã: " + e.getMessage());
        } finally {
            try {
                if(rs != null) rs.close();
                if(stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return pq;
    }
    
    
}