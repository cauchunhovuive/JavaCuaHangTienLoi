package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import connectDB.DatabaseConnection;
import entities.ChiTietNhapHang;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.PhieuNhapHang;
import entities.SanPham;

/** DAO cho phiếu nhập hàng: tạo phiếu + cập nhật tồn kho/giá vốn trong 1 transaction. */
public class PhieuNhapHangDAO {

    private PhieuNhapHang map(ResultSet rs) throws SQLException {
        PhieuNhapHang pn = new PhieuNhapHang();
        pn.setMaPhieuNhap(rs.getInt("MaPN"));
        pn.setNgayNhap(rs.getTimestamp("NgayNhap"));
        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNCC(rs.getInt("MaNCC"));
        ncc.setTenNCC(rs.getString("TenNCC"));
        pn.setNhaCungCap(ncc);
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setTen(rs.getString("TenNV"));
        pn.setNhanVien(nv);
        pn.setTongTienNhap(rs.getBigDecimal("TongTien"));
        pn.setGhiChu(rs.getString("GhiChu"));
        return pn;
    }

    private static final String SELECT_JOIN =
            "SELECT pn.MaPN, pn.NgayNhap, pn.MaNCC, ncc.TenNCC, pn.MaNV, " +
            "nv.Ho + ' ' + nv.Ten AS TenNV, pn.TongTien, pn.GhiChu " +
            "FROM PhieuNhapHang pn " +
            "LEFT JOIN NhaCungCap ncc ON pn.MaNCC = ncc.MaNCC " +
            "LEFT JOIN NhanVien nv ON pn.MaNV = nv.MaNV";

    /**
     * Tạo phiếu nhập: INSERT phiếu + chi tiết, cộng tồn kho, cập nhật giá vốn.
     * Toàn bộ nằm trong 1 transaction — trả về mã phiếu, -1 nếu lỗi (đã rollback).
     */
    public int taoPhieuNhap(PhieuNhapHang pn, List<ChiTietNhapHang> dsChiTiet) {
        String sqlPN = "INSERT INTO PhieuNhapHang (NgayNhap, MaNCC, MaNV, TongTien, GhiChu) VALUES (?, ?, ?, ?, ?)";
        String sqlCT = "INSERT INTO ChiTietNhapHang (MaPN, MaSP, SoLuong, DonGiaNhap) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);
            try {
                int maPN;
                try (PreparedStatement ps = conn.prepareStatement(sqlPN, Statement.RETURN_GENERATED_KEYS)) {
                    ps.setTimestamp(1, new Timestamp(pn.getNgayNhap().getTime()));
                    ps.setInt(2, pn.getNhaCungCap().getMaNCC());
                    ps.setInt(3, pn.getNhanVien().getMaNV());
                    ps.setBigDecimal(4, pn.getTongTienNhap());
                    ps.setString(5, pn.getGhiChu());
                    ps.executeUpdate();
                    try (ResultSet keys = ps.getGeneratedKeys()) {
                        if (!keys.next()) throw new SQLException("Không lấy được mã phiếu nhập");
                        maPN = keys.getInt(1);
                    }
                }

                SanPhamDAO sanPhamDAO = new SanPhamDAO();
                for (ChiTietNhapHang ct : dsChiTiet) {
                    try (PreparedStatement ps = conn.prepareStatement(sqlCT)) {
                        ps.setInt(1, maPN);
                        ps.setInt(2, ct.getSanPham().getMaSanPham());
                        ps.setInt(3, ct.getSoLuong());
                        ps.setBigDecimal(4, ct.getDonGiaNhap());
                        ps.executeUpdate();
                    }

                    // Cộng tồn kho
                    try (PreparedStatement ps = conn.prepareStatement(
                            "UPDATE SanPham SET TonKho = TonKho + ? WHERE MaSP = ?")) {
                        ps.setInt(1, ct.getSoLuong());
                        ps.setInt(2, ct.getSanPham().getMaSanPham());
                        ps.executeUpdate();
                    }

                    // Cập nhật giá vốn = trung bình có trọng số
                    SanPham sp = sanPhamDAO.getByMaSP(ct.getSanPham().getMaSanPham());
                    if (sp != null) {
                        BigDecimal tonCu = BigDecimal.valueOf(Math.max(sp.getTonKho() - ct.getSoLuong(), 0));
                        BigDecimal giaVonCu = sp.getGiaVon() == null ? BigDecimal.ZERO : sp.getGiaVon();
                        BigDecimal giaVonMoi = giaVonCu.multiply(tonCu)
                                .add(ct.getDonGiaNhap().multiply(BigDecimal.valueOf(ct.getSoLuong())))
                                .divide(tonCu.add(BigDecimal.valueOf(ct.getSoLuong())), 0, java.math.RoundingMode.HALF_UP);
                        try (PreparedStatement ps = conn.prepareStatement(
                                "UPDATE SanPham SET GiaVon = ? WHERE MaSP = ?")) {
                            ps.setBigDecimal(1, giaVonMoi);
                            ps.setInt(2, ct.getSanPham().getMaSanPham());
                            ps.executeUpdate();
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
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + " ORDER BY pn.NgayNhap DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách phiếu nhập: " + e.getMessage());
        }
        return list;
    }

    public PhieuNhapHang getByMaPN(int maPN) {
        String sql = SELECT_JOIN + " WHERE pn.MaPN = ?";
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
        String sql = "SELECT ct.MaPN, ct.MaSP, ct.SoLuong, ct.DonGiaNhap, sp.TenSP, sp.MaVach " +
                     "FROM ChiTietNhapHang ct JOIN SanPham sp ON ct.MaSP = sp.MaSP WHERE ct.MaPN = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maPN);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    PhieuNhapHang pn = new PhieuNhapHang();
                    pn.setMaPhieuNhap(rs.getInt("MaPN"));
                    SanPham sp = new SanPham();
                    sp.setMaSanPham(rs.getInt("MaSP"));
                    sp.setTenSanPham(rs.getString("TenSP"));
                    sp.setMaVach(rs.getString("MaVach"));
                    ChiTietNhapHang ct = new ChiTietNhapHang();
                    ct.setPhieuNhap(pn);
                    ct.setSanPham(sp);
                    ct.setSoLuong(rs.getInt("SoLuong"));
                    ct.setDonGiaNhap(rs.getBigDecimal("DonGiaNhap"));
                    list.add(ct);
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
