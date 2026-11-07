package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import connectDB.DatabaseConnection;
import entities.LoaiSanPham;
import entities.SanPham;

/** DAO cho sản phẩm tiện lợi (theo mã vạch, có tồn kho). */
public class SanPhamDAO {

    private static final String SELECT_COLS =
            "SELECT sp.MaSP, sp.MaVach, sp.TenSP, sp.MaLoai, lsp.TenLoai, sp.DonViTinh, " +
            "sp.GiaBan, sp.GiaVon, sp.TonKho, sp.TonToiThieu, sp.TrangThai, sp.HinhAnh, " +
            "sp.MaNCC, ncc.TenNCC " +
            "FROM SanPham sp LEFT JOIN LoaiSanPham lsp ON sp.MaLoai = lsp.MaLoai " +
            "LEFT JOIN NhaCungCap ncc ON sp.MaNCC = ncc.MaNCC";

    private SanPham map(ResultSet rs) throws SQLException {
        SanPham sp = new SanPham();
        sp.setMaSanPham(rs.getInt("MaSP"));
        sp.setMaVach(rs.getString("MaVach"));
        sp.setTenSanPham(rs.getString("TenSP"));
        LoaiSanPham loai = new LoaiSanPham();
        int maLoai = rs.getInt("MaLoai");
        if (maLoai > 0) {
            loai.setMaLoai(maLoai);
            loai.setTenLoai(rs.getString("TenLoai"));
        }
        sp.setLoaiSanPham(loai);
        sp.setDonViTinh(rs.getString("DonViTinh"));
        sp.setGiaBan(rs.getBigDecimal("GiaBan"));
        sp.setGiaVon(rs.getBigDecimal("GiaVon"));
        sp.setTonKho(rs.getInt("TonKho"));
        sp.setTonToiThieu(rs.getInt("TonToiThieu"));
        sp.setTrangThai(rs.getString("TrangThai"));
        sp.setHinhAnh(rs.getString("HinhAnh"));
        sp.setMaNhaCungCap(rs.getInt("MaNCC"));
        sp.setTenNhaCungCap(rs.getString("TenNCC"));
        return sp;
    }

    public List<SanPham> getAll() {
        List<SanPham> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_COLS + " ORDER BY sp.TenSP");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách sản phẩm: " + e.getMessage());
        }
        return list;
    }

    /** Tìm theo mã vạch hoặc tên (dùng cho ô quét mã ở POS). */
    public List<SanPham> timKiem(String tuKhoa) {
        List<SanPham> list = new ArrayList<>();
        String sql = SELECT_COLS + " WHERE sp.MaVach LIKE ? OR sp.TenSP LIKE ? ORDER BY sp.TenSP";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            String kw = "%" + tuKhoa + "%";
            ps.setString(1, kw);
            ps.setString(2, kw);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm sản phẩm: " + e.getMessage());
        }
        return list;
    }

    public SanPham getByMaVach(String maVach) {
        String sql = SELECT_COLS + " WHERE sp.MaVach = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maVach);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm sản phẩm theo mã vạch: " + e.getMessage());
        }
        return null;
    }

    public SanPham getByMaSP(int maSP) {
        String sql = SELECT_COLS + " WHERE sp.MaSP = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maSP);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm sản phẩm theo mã: " + e.getMessage());
        }
        return null;
    }

    private void setSanPhamParams(PreparedStatement ps, SanPham sp) throws SQLException {
        ps.setString(1, sp.getMaVach());
        ps.setString(2, sp.getTenSanPham());
        if (sp.getLoaiSanPham() != null && sp.getLoaiSanPham().getMaLoai() > 0)
            ps.setInt(3, sp.getLoaiSanPham().getMaLoai());
        else ps.setNull(3, Types.INTEGER);
        ps.setString(4, sp.getDonViTinh());
        ps.setBigDecimal(5, sp.getGiaBan());
        ps.setBigDecimal(6, sp.getGiaVon() == null ? BigDecimal.ZERO : sp.getGiaVon());
    }

    public boolean them(SanPham sp) {
        String sql = "INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai, HinhAnh, MaNCC) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            setSanPhamParams(ps, sp);
            ps.setInt(7, sp.getTonKho());
            ps.setInt(8, sp.getTonToiThieu());
            ps.setString(9, sp.getTrangThai());
            ps.setString(10, sp.getHinhAnh());
            if (sp.getMaNhaCungCap() > 0) ps.setInt(11, sp.getMaNhaCungCap());
            else ps.setNull(11, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm sản phẩm: " + e.getMessage());
            return false;
        }
    }

    public boolean sua(SanPham sp) {
        String sql = "UPDATE SanPham SET MaVach=?, TenSP=?, MaLoai=?, DonViTinh=?, GiaBan=?, GiaVon=?, TonToiThieu=?, TrangThai=?, HinhAnh=?, MaNCC=? WHERE MaSP=?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, sp.getMaVach());
            ps.setString(2, sp.getTenSanPham());
            if (sp.getLoaiSanPham() != null && sp.getLoaiSanPham().getMaLoai() > 0)
                ps.setInt(3, sp.getLoaiSanPham().getMaLoai());
            else ps.setNull(3, Types.INTEGER);
            ps.setString(4, sp.getDonViTinh());
            ps.setBigDecimal(5, sp.getGiaBan());
            ps.setBigDecimal(6, sp.getGiaVon() == null ? BigDecimal.ZERO : sp.getGiaVon());
            ps.setInt(7, sp.getTonToiThieu());
            ps.setString(8, sp.getTrangThai());
            ps.setString(9, sp.getHinhAnh());
            if (sp.getMaNhaCungCap() > 0) ps.setInt(10, sp.getMaNhaCungCap());
            else ps.setNull(10, Types.INTEGER);
            ps.setInt(11, sp.getMaSanPham());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi sửa sản phẩm: " + e.getMessage());
            return false;
        }
    }

    public boolean xoa(int maSP) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM SanPham WHERE MaSP=?")) {
            ps.setInt(1, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi xóa sản phẩm: " + e.getMessage());
            return false;
        }
    }

    /**
     * Trừ tồn kho trong transaction bán hàng: dùng chung connection đã truyền,
     * KHÔNG đóng connection (tránh đóng connection singleton giữa transaction).
     */
    public boolean truTonKho(Connection conn, int maSP, int soLuong) throws SQLException {
        String sql = "UPDATE SanPham SET TonKho = TonKho - ? WHERE MaSP = ? AND TonKho >= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuong);
            ps.setInt(2, maSP);
            ps.setInt(3, soLuong);
            return ps.executeUpdate() > 0;
        }
    }

    /** Trừ tồn kho khi bán. Trả về false nếu không đủ hàng. */
    public boolean truTonKho(int maSP, int soLuong) {
        String sql = "UPDATE SanPham SET TonKho = TonKho - ? WHERE MaSP = ? AND TonKho >= ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuong);
            ps.setInt(2, maSP);
            ps.setInt(3, soLuong);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi trừ tồn kho: " + e.getMessage());
            return false;
        }
    }

    /** Cộng tồn kho khi nhập hàng. */
    public boolean congTonKho(int maSP, int soLuong) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET TonKho = TonKho + ? WHERE MaSP=?")) {
            ps.setInt(1, soLuong);
            ps.setInt(2, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cộng tồn kho: " + e.getMessage());
            return false;
        }
    }

    /** Cập nhật giá vốn sau nhập hàng (trung bình có trọng số). */
    public boolean capNhatGiaVon(int maSP, BigDecimal giaVonMoi) {
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("UPDATE SanPham SET GiaVon = ? WHERE MaSP=?")) {
            ps.setBigDecimal(1, giaVonMoi);
            ps.setInt(2, maSP);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi cập nhật giá vốn: " + e.getMessage());
            return false;
        }
    }

    /** Sản phẩm của 1 nhà cung cấp (dùng ở màn nhập hàng). */
    public List<SanPham> getTheoNhaCungCap(int maNCC) {
        List<SanPham> list = new ArrayList<>();
        String sql = SELECT_COLS + " WHERE sp.MaNCC = ? ORDER BY sp.TenSP";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maNCC);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy sản phẩm theo NCC: " + e.getMessage());
        }
        return list;
    }

    public List<SanPham> getSapHet() {
        List<SanPham> list = new ArrayList<>();
        String sql = SELECT_COLS + " WHERE sp.TonKho <= sp.TonToiThieu ORDER BY sp.TonKho";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi lấy sản phẩm sắp hết: " + e.getMessage());
        }
        return list;
    }
}
