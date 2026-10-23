package dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import ConnectDB.DatabaseConnection;
import entities.KhachHang;

/** DAO cho khách hàng thành viên (khách lẻ không cần lưu). */
public class KhachHangDAO {

    private KhachHang map(ResultSet rs) throws SQLException {
        return new KhachHang(
                rs.getInt("MaKH"),
                rs.getString("TenKH"),
                rs.getString("SoDienThoai"),
                rs.getInt("DiemTichLuy"),
                rs.getBigDecimal("TongChiTieu"));
    }

    public List<KhachHang> getAll() {
        List<KhachHang> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM KhachHang ORDER BY TenKH");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách khách hàng: " + e.getMessage());
        }
        return list;
    }

    public KhachHang timTheoSdt(String soDienThoai) {
        String sql = "SELECT * FROM KhachHang WHERE SoDienThoai = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, soDienThoai);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tìm khách hàng theo SĐT: " + e.getMessage());
        }
        return null;
    }

    public boolean them(KhachHang kh) {
        String sql = "INSERT INTO KhachHang (TenKH, SoDienThoai, DiemTichLuy, TongChiTieu) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, kh.getTenKhachHang());
            ps.setString(2, kh.getSoDienThoai());
            ps.setInt(3, kh.getDiemTichLuy());
            ps.setBigDecimal(4, kh.getTongChiTieu() == null ? BigDecimal.ZERO : kh.getTongChiTieu());
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Lỗi thêm khách hàng: " + e.getMessage());
            return false;
        }
    }

    /** Cộng điểm + tổng chi tiêu khi khách thành viên mua hàng (trong transaction bán hàng). */
    public void congDiem(Connection conn, int maKH, int diem, BigDecimal thanhTien) throws SQLException {
        String sql = "UPDATE KhachHang SET DiemTichLuy = DiemTichLuy + ?, TongChiTieu = TongChiTieu + ? WHERE MaKH = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, diem);
            ps.setBigDecimal(2, thanhTien);
            ps.setInt(3, maKH);
            ps.executeUpdate();
        }
    }
}
