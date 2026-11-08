package dao;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import connectDB.DatabaseConnection;
import entities.ChiTietNhapHang;
import entities.PhieuNhapHang;
import entities.SanPham;

public class PhieuNhapHangDAO {

    private static final String SELECT_COLS =
            "SELECT MaPN, NgayNhap, MaNCC, MaNV, TongTien, GhiChu, TrangThai FROM PhieuNhapHang";

    private PhieuNhapHang map(ResultSet rs) throws SQLException {
        PhieuNhapHang pn = new PhieuNhapHang();
        pn.setMaPN(rs.getInt("MaPN"));
        pn.setNgayNhap(rs.getTimestamp("NgayNhap"));

        int maNCC = rs.getInt("MaNCC");
        pn.setMaNCC(rs.wasNull() ? null : maNCC);
        int maNV = rs.getInt("MaNV");
        pn.setMaNV(rs.wasNull() ? null : maNV);

        pn.setTongTien(rs.getBigDecimal("TongTien"));
        pn.setGhiChu(rs.getString("GhiChu"));
        pn.setTrangThai(rs.getString("TrangThai"));
        return pn;
    }

    /**
     * Tạo phiếu nhập: INSERT phiếu + chi tiết, cộng tồn kho, cập nhật giá vốn.
     * Toàn bộ nằm trong 1 transaction — trả về mã phiếu, -1 nếu lỗi (đã rollback).
     */
    public int taoPhieuNhap(PhieuNhapHang pn, List<ChiTietNhapHang> dsChiTiet) {
        String sqlPN = "INSERT INTO PhieuNhapHang (NgayNhap, MaNCC, MaNV, TongTien, GhiChu, TrangThai) " +
                       "VALUES (?, ?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietNhapHang (MaPN, MaSP, SoLuong, DonGiaNhap) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            try {
                int maPN;
                try (PreparedStatement ps = conn.prepareStatement(sqlPN, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, pn.getNgayNhap() == null
                            ? new java.sql.Timestamp(System.currentTimeMillis()) : pn.getNgayNhap());
                    if (pn.getMaNCC() == null) ps.setNull(2, java.sql.Types.INTEGER);
                    else ps.setInt(2, pn.getMaNCC());
                    if (pn.getMaNV() == null) ps.setNull(3, java.sql.Types.INTEGER);
                    else ps.setInt(3, pn.getMaNV());
                    ps.setBigDecimal(4, pn.getTongTien() == null ? BigDecimal.ZERO : pn.getTongTien());
                    ps.setString(5, pn.getGhiChu());
                    ps.setString(6, pn.getTrangThai() == null ? "HOAN_TAT" : pn.getTrangThai());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được mã phiếu nhập");
                        maPN = keys.getInt(1);
                    }
                }

                // Lấy dữ liệu sản phẩm trước khi update để tránh lock conflict
                for (ChiTietNhapHang ct : dsChiTiet) {
                    // Insert chi tiết
                    try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                        ps.setInt(1, maPN);
                        ps.setInt(2, ct.getMaSP());
                        ps.setInt(3, ct.getSoLuong());
                        ps.setBigDecimal(4, ct.getDonGiaNhap());
                        ps.executeUpdate();
                    }

                    // Đọc tonKho và giaVon hiện tại từ SanPham
                    BigDecimal tonKhoHienTai = null;
                    BigDecimal giaVonHienTai = null;
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT TonKho, GiaVon FROM SanPham WHERE MaSP = ?")) {
                        ps.setInt(1, ct.getMaSP());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                tonKhoHienTai = rs.getBigDecimal("TonKho");
                                giaVonHienTai = rs.getBigDecimal("GiaVon");
                            }
                        }
                    }

                    // Cộng tồn kho
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE SanPham SET TonKho = TonKho + ? WHERE MaSP = ?")) {
                        ps.setInt(1, ct.getSoLuong());
                        ps.setInt(2, ct.getMaSP());
                        ps.executeUpdate();
                    }

                    // Cập nhật giá vốn trung bình có trọng số
                    if (tonKhoHienTai != null) {
                        BigDecimal tonCu = tonKhoHienTai;
                        BigDecimal giaVonCu = giaVonHienTai == null ? BigDecimal.ZERO : giaVonHienTai;
                        BigDecimal tongSau = tonCu.add(BigDecimal.valueOf(ct.getSoLuong()));
                        if (tongSau.compareTo(BigDecimal.ZERO) > 0) {
                            BigDecimal giaVonMoi = giaVonCu.multiply(tonCu)
                                    .add(ct.getDonGiaNhap().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                                    .divide(tongSau, 0, RoundingMode.HALF_UP);
                            try (PreparedStatement ps = conn.prepareStatement(
                                    "UPDATE SanPham SET GiaVon = ? WHERE MaSP = ?")) {
                                ps.setBigDecimal(1, giaVonMoi);
                                ps.setInt(2, ct.getMaSP());
                                ps.executeUpdate();
                            }
                        }
                    }
                }

                conn.commit();
                return maPN;
            } catch (SQLException e) {
                try { conn.rollback(); } catch (SQLException ex) {
                    System.err.println("Lỗi rollback: " + ex.getMessage());
                }
                System.err.println("Lỗi tạo phiếu nhập: " + e.getMessage());
                return -1;
            }
        } catch (SQLException e) {
            System.err.println("Lỗi kết nối DB: " + e.getMessage());
            return -1;
        }
    }

    public List<PhieuNhapHang> dsPhieuNhap() {
        List<PhieuNhapHang> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COLS + " ORDER BY NgayNhap DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phiếu nhập: " + e.getMessage());
        }
        return list;
    }

    public PhieuNhapHang getByMaPN(int maPN) {
        String sql = SELECT_COLS + " WHERE MaPN = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy phiếu nhập: " + e.getMessage());
        }
        return null;
    }

    public List<ChiTietNhapHang> getChiTietByMaPN(int maPN) {
        List<ChiTietNhapHang> list = new ArrayList<>();
        String sql = "SELECT MaPN, MaSP, SoLuong, DonGiaNhap FROM ChiTietNhapHang WHERE MaPN = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new ChiTietNhapHang(rs.getInt("MaPN"), rs.getInt("MaSP"),
                            rs.getInt("SoLuong"), rs.getBigDecimal("DonGiaNhap")));
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy chi tiết phiếu nhập: " + e.getMessage());
        }
        return list;
    }

    public BigDecimal tongNhapTheoNgay(Date tuNgay, Date denNgay) {
        String sql = "SELECT ISNULL(SUM(TongTien), 0) FROM PhieuNhapHang " +
                     "WHERE NgayNhap >= ? AND NgayNhap < DATEADD(DAY, 1, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính tổng tiền nhập: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
