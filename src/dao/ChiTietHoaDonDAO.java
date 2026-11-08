package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connectDB.DatabaseConnection;
import entities.ChiTietHoaDon;

/** DAO cho chi tiết hóa đơn bán hàng. */
public class ChiTietHoaDonDAO {

    /** Ghi 1 dòng chi tiết (dùng chung connection để nằm trong transaction). */
    public void them(Connection conn, ChiTietHoaDon ct) throws SQLException {
        String sql = "INSERT INTO ChiTietHoaDon (MaHD, MaSP, SoLuong, DonGia) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, ct.getMaHD());
            ps.setInt(2, ct.getMaSP());
            ps.setInt(3, ct.getSoLuong());
            ps.setBigDecimal(4, ct.getDonGia());
            ps.executeUpdate();
        }
    }

    public List<ChiTietHoaDon> getChiTietByMaHD(int maHD) {
        List<ChiTietHoaDon> list = new ArrayList<>();
        String sql = "SELECT MaHD, MaSP, SoLuong, DonGia FROM ChiTietHoaDon WHERE MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietHoaDon(rs.getInt("MaHD"), rs.getInt("MaSP"),
                            rs.getInt("SoLuong"), rs.getBigDecimal("DonGia")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy chi tiết hóa đơn: " + e.getMessage());
        }
        return list;
    }

    /** Tổng tiền thực thu của 1 hóa đơn (phục vụ đối chiếu). */
    public BigDecimal tongTienHoaDon(int maHD) {
        BigDecimal tong = BigDecimal.ZERO;
        String sql = "SELECT ISNULL(SUM(SoLuong * DonGia), 0) FROM ChiTietHoaDon WHERE MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) tong = rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính tổng tiền hóa đơn: " + e.getMessage());
        }
        return tong;
    }
}
