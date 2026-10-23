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

import ConnectDB.DatabaseConnection;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;

/** DAO cho hóa đơn bán hàng. */
public class HoaDonDAO {

    private HoaDon map(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(rs.getInt("MaHD"));
        Timestamp ts = rs.getTimestamp("NgayLap");
        hd.setNgayLap(ts);
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setHo("");
        nv.setTen(rs.getString("TenNV"));
        hd.setNhanVien(nv);
        KhachHang kh = new KhachHang();
        kh.setMaKhachHang(rs.getInt("MaKH"));
        String tenKH = rs.getString("TenKH");
        kh.setTenKhachHang(tenKH == null ? "Khách lẻ" : tenKH);
        hd.setKhachHang(kh);
        hd.setTongTien(rs.getBigDecimal("TongTien"));
        hd.setTienKhachDua(rs.getBigDecimal("TienKhachDua"));
        hd.setTienThua(rs.getBigDecimal("TienThua"));
        hd.setDiemDuocTang(rs.getInt("DiemTang"));
        hd.setTrangThai(rs.getString("TrangThai"));
        hd.setHinhThucThanhToan(rs.getString("HinhThucThanhToan"));
        return hd;
    }

    private static final String SELECT_JOIN =
            "SELECT hd.MaHD, hd.NgayLap, hd.MaNV, nv.Ho + ' ' + nv.Ten AS TenNV, " +
            "hd.MaKH, kh.TenKH, hd.TongTien, hd.TienKhachDua, hd.TienThua, hd.DiemTang, hd.TrangThai, " +
            "hd.HinhThucThanhToan " +
            "FROM HoaDon hd " +
            "LEFT JOIN NhanVien nv ON hd.MaNV = nv.MaNV " +
            "LEFT JOIN KhachHang kh ON hd.MaKH = kh.MaKH";

    /**
     * Tạo hóa đơn mới: trả về mã hóa đơn vừa tạo, -1 nếu lỗi.
     * Gọi bởi transaction từ HoaDonService (GUI) để ghi kèm chi tiết + trừ kho.
     */
    public int taoHoaDon(Connection conn, HoaDon hd) throws SQLException {
        String sql = "INSERT INTO HoaDon (NgayLap, MaNV, MaKH, TongTien, TienKhachDua, TienThua, DiemTang, TrangThai, HinhThucThanhToan) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(hd.getNgayLap().getTime()));
            ps.setInt(2, hd.getNhanVien().getMaNV());
            if (hd.getKhachHang() != null && hd.getKhachHang().getMaKhachHang() > 0)
                ps.setInt(3, hd.getKhachHang().getMaKhachHang());
            else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setBigDecimal(4, hd.getTongTien());
            ps.setBigDecimal(5, hd.getTienKhachDua());
            ps.setBigDecimal(6, hd.getTienThua());
            ps.setInt(7, hd.getDiemDuocTang());
            ps.setString(8, hd.getTrangThai());
            ps.setString(9, hd.getHinhThucThanhToan() == null ? "TIEN_MAT" : hd.getHinhThucThanhToan());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<HoaDon> dsHoaDonTheoNgay(Date tuNgay, Date denNgay) {
        List<HoaDon> list = new ArrayList<>();
        String sql = SELECT_JOIN + " WHERE hd.NgayLap >= ? AND hd.NgayLap < DATEADD(DAY, 1, ?) ORDER BY hd.NgayLap DESC";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy hóa đơn theo ngày: " + e.getMessage());
        }
        return list;
    }

    public List<HoaDon> dsTatCaHoaDon() {
        List<HoaDon> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + " ORDER BY hd.NgayLap DESC");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách hóa đơn: " + e.getMessage());
        }
        return list;
    }

    public HoaDon getByMaHD(int maHD) {
        String sql = SELECT_JOIN + " WHERE hd.MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maHD);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return map(rs);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy hóa đơn theo mã: " + e.getMessage());
        }
        return null;
    }

    public BigDecimal tongDoanhThu(Date tuNgay, Date denNgay) {
        String sql = "SELECT ISNULL(SUM(TongTien), 0) FROM HoaDon WHERE TrangThai = 'HOAN_TAT' " +
                     "AND NgayLap >= ? AND NgayLap < DATEADD(DAY, 1, ?)";
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, tuNgay);
            ps.setDate(2, denNgay);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getBigDecimal(1);
            }
        } catch (SQLException e) {
            System.err.println("Lỗi tính doanh thu: " + e.getMessage());
        }
        return BigDecimal.ZERO;
    }
}
