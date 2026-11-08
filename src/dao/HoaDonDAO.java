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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import connectDB.DatabaseConnection;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;
import entities.SanPham;

public class HoaDonDAO {

    private HoaDon map(ResultSet rs) throws SQLException {
        HoaDon hd = new HoaDon();
        hd.setMaHD(rs.getInt("MaHD"));
        Timestamp ts = rs.getTimestamp("NgayLap");
        hd.setNgayLap(ts);
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setHo("");
        nv.setTen(rs.getString("TenNV"));
        hd.setNhanVien(nv);
        KhachHang kh = new KhachHang();
        kh.setMaKH(rs.getInt("MaKH"));
        String tenKH = rs.getString("TenKH");
        kh.setTenKH(tenKH == null ? "Khách lẻ" : tenKH);
        hd.setKhachHang(kh);
        hd.setTienKhachDua(rs.getBigDecimal("TienKhachDua"));
        hd.setTienThua(rs.getBigDecimal("TienThua"));
        hd.setDiemDuocTang(rs.getInt("DiemTang"));
        hd.setTrangThai(rs.getString("TrangThai"));
        hd.setHinhThucThanhToan(rs.getString("HinhThucThanhToan"));
        return hd;
    }

    private static final String SELECT_JOIN =
            "SELECT hd.MaHD, hd.NgayLap, hd.MaNV, nv.Ho + ' ' + nv.Ten AS TenNV, " +
            "hd.MaKH, kh.TenKH, hd.TienKhachDua, hd.TienThua, hd.DiemTang, hd.TrangThai, " +
            "hd.HinhThucThanhToan " +
            "FROM HoaDon hd " +
            "LEFT JOIN NhanVien nv ON hd.MaNV = nv.MaNV " +
            "LEFT JOIN KhachHang kh ON hd.MaKH = kh.MaKH";

    private void napChiTiet(Connection conn, List<HoaDon> ds) throws SQLException {
        if (ds.isEmpty()) return;

        StringBuilder sql = new StringBuilder(
                "SELECT MaHD, MaSP, SoLuong, DonGia FROM ChiTietHoaDon WHERE MaHD IN (");
        for (int i = 0; i < ds.size(); i++) {
            if (i > 0) sql.append(", ");
            sql.append("?");
        }
        sql.append(")");

        Map<Integer, HoaDon> theoMa = new HashMap<>();
        for (HoaDon hd : ds) theoMa.put(hd.getMaHoaDon(), hd);

        try (PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < ds.size(); i++) ps.setInt(i + 1, ds.get(i).getMaHoaDon());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    HoaDon hd = theoMa.get(rs.getInt("MaHD"));
                    if (hd == null) continue;
                    SanPham sp = new SanPham();
                    sp.setMaSP(rs.getInt("MaSP"));
                    hd.getChiTiet().add(new ChiTietHoaDon(hd.getMaHoaDon(), rs.getInt("MaSP"),
                            rs.getInt("SoLuong"), rs.getBigDecimal("DonGia")));
                }
            }
        }
    }

    public int taoHoaDon(Connection conn, HoaDon hd) throws SQLException {
        String sql = "INSERT INTO HoaDon (NgayLap, MaNV, MaKH, TienKhachDua, TienThua, DiemTang, TrangThai, HinhThucThanhToan) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setTimestamp(1, new Timestamp(hd.getNgayLap().getTime()));
            ps.setInt(2, hd.getMaNV() == null ? 0 : hd.getMaNV());
            if (hd.getKhachHang() != null && hd.getKhachHang().getMaKH() > 0)
                ps.setInt(3, hd.getKhachHang().getMaKH());
            else ps.setNull(3, java.sql.Types.INTEGER);
            ps.setBigDecimal(4, hd.getTienKhachDua());
            ps.setBigDecimal(5, hd.getTienThua());
            ps.setInt(6, hd.getDiemDuocTang());
            ps.setString(7, hd.getTrangThai());
            ps.setString(8, hd.getHinhThucThanhToan() == null ? "TIEN_MAT" : hd.getHinhThucThanhToan());
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
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setDate(1, tuNgay);
                ps.setDate(2, denNgay);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) list.add(map(rs));
                }
            }
            napChiTiet(conn, list);
        } catch (SQLException e) {
            System.err.println("Lỗi lấy hóa đơn theo ngày: " + e.getMessage());
        }
        return list;
    }

    public List<HoaDon> dsTatCaHoaDon() {
        List<HoaDon> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(SELECT_JOIN + " ORDER BY hd.NgayLap DESC");
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
            napChiTiet(conn, list);
        } catch (SQLException e) {
            System.err.println("Lỗi lấy danh sách hóa đơn: " + e.getMessage());
        }
        return list;
    }

    public HoaDon getByMaHD(int maHD) {
        String sql = SELECT_JOIN + " WHERE hd.MaHD = ?";
        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setInt(1, maHD);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        HoaDon hd = map(rs);
                        List<HoaDon> motHoaDon = new ArrayList<>();
                        motHoaDon.add(hd);
                        napChiTiet(conn, motHoaDon);
                        return hd;
                    }
                }
            }
        } catch (SQLException e) {
            System.err.println("Lỗi lấy hóa đơn theo mã: " + e.getMessage());
        }
        return null;
    }

    public BigDecimal tongDoanhThu(Date tuNgay, Date denNgay) {
        String sql = "SELECT ISNULL(SUM(ct.SoLuong * ct.DonGia), 0) FROM HoaDon hd " +
                     "LEFT JOIN ChiTietHoaDon ct ON ct.MaHD = hd.MaHD " +
                     "WHERE hd.TrangThai = 'HOAN_TAT' " +
                     "AND hd.NgayLap >= ? AND hd.NgayLap < DATEADD(DAY, 1, ?)";
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

    public Map<Integer, BigDecimal> tongTienTheoHoaDon(List<HoaDon> ds) {
        Map<Integer, BigDecimal> result = new HashMap<>();
        for (HoaDon hd : ds) {
            BigDecimal tong = BigDecimal.ZERO;
            if (hd.getChiTiet() != null) {
                for (ChiTietHoaDon ct : hd.getChiTiet()) {
                    tong = tong.add(ct.getThanhTien());
                }
            }
            result.put(hd.getMaHD(), tong);
        }
        return result;
    }
}
