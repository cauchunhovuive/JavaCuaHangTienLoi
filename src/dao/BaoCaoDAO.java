package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import connectDB.DatabaseConnection;
import entities.HoaDon;

public class BaoCaoDAO {

    public List<Object[]> doanhThuTheoNgay(int nam, int thang) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT CAST(hd.NgayLap AS DATE) AS Ngay, " +
            "COUNT(DISTINCT hd.MaHD) AS SoHD, ISNULL(SUM(ct.SoLuong * ct.DonGia), 0) AS DoanhThu " +
            "FROM HoaDon hd LEFT JOIN ChiTietHoaDon ct ON ct.MaHD = hd.MaHD " +
            "WHERE hd.TrangThai = 'HOAN_TAT' AND YEAR(hd.NgayLap) = ? AND MONTH(hd.NgayLap) = ? " +
            "GROUP BY CAST(hd.NgayLap AS DATE) ORDER BY Ngay";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            ps.setInt(2, thang);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getDate("Ngay"), rs.getInt("SoHD"), rs.getBigDecimal("DoanhThu") });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi báo cáo doanh thu theo ngày: " + e.getMessage());
        }
        return list;
    }

    public List<Object[]> doanhThuTheoThang(int nam) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT MONTH(hd.NgayLap) AS Thang, COUNT(DISTINCT hd.MaHD) AS SoHD, " +
            "ISNULL(SUM(ct.SoLuong * ct.DonGia), 0) AS DoanhThu " +
            "FROM HoaDon hd LEFT JOIN ChiTietHoaDon ct ON ct.MaHD = hd.MaHD " +
            "WHERE hd.TrangThai = 'HOAN_TAT' AND YEAR(hd.NgayLap) = ? " +
            "GROUP BY MONTH(hd.NgayLap) ORDER BY Thang";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, nam);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getInt("Thang"), rs.getInt("SoHD"), rs.getBigDecimal("DoanhThu") });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi báo cáo doanh thu theo tháng: " + e.getMessage());
        }
        return list;
    }

    public List<Object[]> topSanPham(Date tuNgay, Date denNgay, int limit) {
        List<Object[]> list = new ArrayList<>();
        String sql =
            "SELECT TOP (?) sp.TenSP, SUM(ct.SoLuong) AS SoLuong, SUM(ct.SoLuong * ct.DonGia) AS DoanhThu " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON ct.MaHD = hd.MaHD " +
            "JOIN SanPham sp ON ct.MaSP = sp.MaSP " +
            "WHERE hd.TrangThai = 'HOAN_TAT' AND hd.NgayLap >= ? AND hd.NgayLap < DATEADD(DAY, 1, ?) " +
            "GROUP BY sp.TenSP ORDER BY SoLuong DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            ps.setDate(2, tuNgay);
            ps.setDate(3, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{ rs.getString("TenSP"), rs.getInt("SoLuong"), rs.getBigDecimal("DoanhThu") });
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi top sản phẩm: " + e.getMessage());
        }
        return list;
    }

    public BigDecimal loiNhuan(Date tuNgay, Date denNgay) {
        String sql =
            "SELECT ISNULL(SUM(ct.SoLuong * (ct.DonGia - ISNULL(sp.GiaVon, 0))), 0) " +
            "FROM ChiTietHoaDon ct " +
            "JOIN HoaDon hd ON ct.MaHD = hd.MaHD " +
            "JOIN SanPham sp ON ct.MaSP = sp.MaSP " +
            "WHERE hd.TrangThai = 'HOAN_TAT' AND hd.NgayLap >= ? AND hd.NgayLap < DATEADD(DAY, 1, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính lợi nhuận: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }

    public int soHoaDon(Date tuNgay, Date denNgay) {
        String sql = "SELECT COUNT(*) FROM HoaDon WHERE TrangThai = 'HOAN_TAT' " +
                     "AND NgayLap >= ? AND NgayLap < DATEADD(DAY, 1, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi đếm hóa đơn: " + e.getMessage());
        }
        return 0;
    }
}
