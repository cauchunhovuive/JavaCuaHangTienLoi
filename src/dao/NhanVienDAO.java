package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

import connectDB.DatabaseConnection;

import java.math.BigDecimal;

import entities.NhanVien;

public class NhanVienDAO {
    private static final Logger LOGGER = Logger.getLogger(NhanVienDAO.class.getName());

    public List<NhanVien> getAllNhanVien() {
        List<NhanVien> list = new ArrayList<>();
        String sql = "SELECT * FROM NhanVien";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNhanVien(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi lấy danh sách nhân viên: " + e.getMessage());
        }

        return list;
    }
    
    public NhanVien getNhanVienByMa(int maNV) {
        String sql = "SELECT * FROM NhanVien WHERE MaNV = ?";
        NhanVien nv = null;
        
        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, maNV);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nv = mapResultSetToNhanVien(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi lấy nhân viên theo mã: " + e.getMessage());
        }
        
        return nv;
    }
    
    public NhanVien getNhanVienByTaiKhoan(String tenTaiKhoan) {
        String sql = "SELECT * FROM NhanVien WHERE TenTaiKhoan = ?";
        NhanVien nv = null;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, tenTaiKhoan);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    nv = mapResultSetToNhanVien(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi lấy nhân viên theo tài khoản: " + e.getMessage());
        }

        return nv;
    }

    

    public boolean addNhanVien(NhanVien nv) {
        // Tạo tài khoản mới cho nhân viên
        String randomDigits = String.format("%04d", new Random().nextInt(10000));
        String tenTaiKhoan = "nhanvien" + randomDigits;
        String matKhau = "123";
        String email = tenTaiKhoan + "@coffee.com";
        int maQuyen = 2;
        String trangThai = "Active";
        Timestamp  ngayTao = Timestamp.valueOf(LocalDateTime.now());

        String insertTaiKhoanSQL = "INSERT INTO TaiKhoan (TenTaiKhoan, MatKhau, Email, MaQuyen, NgayTao, TrangThai) " +
                                   "VALUES (?, ?, ?, ?, ?, ?)";

        String insertNhanVienSQL = "INSERT INTO NhanVien (Ho, Ten, NgaySinh, GioiTinh, CMND, SoDienThoai, MaDiaChi, TenTaiKhoan, LoaiNV, TrangThai, NgayVaoLam, LuongCoBan) " +
                                   "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection()) {
            conn.setAutoCommit(false);

            // 1. Thêm tài khoản
            try (PreparedStatement stmtTK = conn.prepareStatement(insertTaiKhoanSQL)) {
                stmtTK.setString(1, tenTaiKhoan);
                stmtTK.setString(2, matKhau);
                stmtTK.setString(3, email);
                stmtTK.setInt(4, maQuyen);
                stmtTK.setTimestamp(5, ngayTao);
                stmtTK.setString(6, trangThai);
                stmtTK.executeUpdate();
            }

            // 2. Cập nhật tên tài khoản cho nhân viên
            nv.setTenTaiKhoan(tenTaiKhoan);

            // 3. Thêm nhân viên
            try (PreparedStatement stmtNV = conn.prepareStatement(insertNhanVienSQL)) {
                stmtNV.setString(1, nv.getHo());
                stmtNV.setString(2, nv.getTen());
                stmtNV.setDate(3, Date.valueOf(nv.getNgaySinh()));
                stmtNV.setString(4, nv.getGioiTinh());
                stmtNV.setString(5, nv.getCmnd());
                stmtNV.setString(6, nv.getSoDienThoai());
                stmtNV.setObject(7, nv.getMaDiaChi(), java.sql.Types.INTEGER);
                stmtNV.setString(8, nv.getTenTaiKhoan());
                stmtNV.setString(9, nv.getLoaiNV());
                stmtNV.setString(10, nv.getTrangThai());
                stmtNV.setDate(11, Date.valueOf(nv.getNgayVaoLam()));
                stmtNV.setBigDecimal(12, nv.getLuongCoBan());

                stmtNV.executeUpdate();
            }

            conn.commit();
            return true;

        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi thêm nhân viên: " + e.getMessage());
            return false;
        }
    }


    public boolean updateNhanVien(NhanVien nv) {
        String sql = "UPDATE NhanVien SET Ho = ?, Ten = ?, NgaySinh = ?, GioiTinh = ?, CMND = ?, SoDienThoai = ?, " +
                     "MaDiaChi = ?, TenTaiKhoan = ?, LoaiNV = ?, TrangThai = ?, NgayVaoLam = ?, LuongCoBan = ? " +
                     "WHERE MaNV = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, nv.getHo());
            stmt.setString(2, nv.getTen());
            stmt.setDate(3, Date.valueOf(nv.getNgaySinh()));
            stmt.setString(4, nv.getGioiTinh());
            stmt.setString(5, nv.getCmnd());
            stmt.setString(6, nv.getSoDienThoai());
            stmt.setObject(7, nv.getMaDiaChi(), java.sql.Types.INTEGER);
            stmt.setString(8, nv.getTenTaiKhoan());
            stmt.setString(9, nv.getLoaiNV());
            stmt.setString(10, nv.getTrangThai());
            stmt.setDate(11, Date.valueOf(nv.getNgayVaoLam()));
            stmt.setBigDecimal(12, nv.getLuongCoBan());
            stmt.setInt(13, nv.getMaNV());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi cập nhật nhân viên: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteNhanVien(int maNV) {
        String sql = "DELETE FROM NhanVien WHERE MaNV = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maNV);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi xóa nhân viên: " + e.getMessage());
            return false;
        }
    }

    private NhanVien mapResultSetToNhanVien(ResultSet rs) throws SQLException {
        NhanVien nv = new NhanVien();
        nv.setMaNV(rs.getInt("MaNV"));
        nv.setHo(rs.getString("Ho"));
        nv.setTen(rs.getString("Ten"));
        nv.setNgaySinh(rs.getDate("NgaySinh").toLocalDate());
        nv.setGioiTinh(rs.getString("GioiTinh"));
        nv.setCmnd(rs.getString("CMND"));
        nv.setSoDienThoai(rs.getString("SoDienThoai"));
        int maDiaChi = rs.getInt("MaDiaChi");
        nv.setMaDiaChi(rs.wasNull() ? null : maDiaChi);
        nv.setTenTaiKhoan(rs.getString("TenTaiKhoan"));
        nv.setLoaiNV(rs.getString("LoaiNV"));
        nv.setTrangThai(rs.getString("TrangThai"));
        nv.setNgayVaoLam(rs.getDate("NgayVaoLam").toLocalDate());
        nv.setLuongCoBan(rs.getBigDecimal("LuongCoBan"));
        return nv;
    }
    
    public boolean updateTrangThaiNhanVien(int maNV, String trangThai) {
        String sql = "UPDATE NhanVien SET TrangThai = ? WHERE MaNV = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trangThai);
            stmt.setInt(2, maNV);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi cập nhật trạng thái nhân viên: " + e.getMessage());
            return false;
        }
    }

}
