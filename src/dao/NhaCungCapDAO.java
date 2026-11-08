package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import connectDB.DatabaseConnection;
import entities.NhaCungCap;

public class NhaCungCapDAO {
    private static final Logger LOGGER = Logger.getLogger(NhaCungCapDAO.class.getName());

    public List<NhaCungCap> getAllNhaCungCap() {
        List<NhaCungCap> list = new ArrayList<>();
        String sql = "SELECT TOP (1000) MaNCC, TenNCC, SoDienThoai, Email, MaDiaChi, TrangThai, NgayTao FROM NhaCungCap";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                list.add(mapResultSetToNhaCungCap(rs));
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi lấy danh sách nhà cung cấp: " + e.getMessage());
        }

        return list;
    }

    public NhaCungCap getNhaCungCapByMa(int maNCC) {
        String sql = "SELECT * FROM NhaCungCap WHERE MaNCC = ?";
        NhaCungCap ncc = null;

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maNCC);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    ncc = mapResultSetToNhaCungCap(rs);
                }
            }
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi lấy nhà cung cấp theo mã: " + e.getMessage());
        }

        return ncc;
    }

    public boolean addNhaCungCap(NhaCungCap ncc) {
        String insertSQL = "INSERT INTO NhaCungCap (TenNCC, SoDienThoai, Email, MaDiaChi, TrangThai, NgayTao) " +
                           "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(insertSQL)) {

            stmt.setString(1, ncc.getTenNCC());
            stmt.setString(2, ncc.getSoDienThoai());
            stmt.setString(3, ncc.getEmail());
            stmt.setObject(4, ncc.getMaDiaChi(), java.sql.Types.INTEGER);
            stmt.setString(5, ncc.getTrangThai());
            stmt.setTimestamp(6, ncc.getNgayTao());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi thêm nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

    public boolean updateNhaCungCap(NhaCungCap ncc) {
        String sql = "UPDATE NhaCungCap SET TenNCC = ?, SoDienThoai = ?, Email = ?, MaDiaChi = ?, TrangThai = ?, NgayTao = ? " +
                     "WHERE MaNCC = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, ncc.getTenNCC());
            stmt.setString(2, ncc.getSoDienThoai());
            stmt.setString(3, ncc.getEmail());
            stmt.setObject(4, ncc.getMaDiaChi(), java.sql.Types.INTEGER);
            stmt.setString(5, ncc.getTrangThai());
            stmt.setTimestamp(6, ncc.getNgayTao());
            stmt.setInt(7, ncc.getMaNCC());

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi cập nhật nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteNhaCungCap(int maNCC) {
        String sql = "DELETE FROM NhaCungCap WHERE MaNCC = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, maNCC);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi xóa nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

    private NhaCungCap mapResultSetToNhaCungCap(ResultSet rs) throws SQLException {
        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNCC(rs.getInt("MaNCC"));
        ncc.setTenNCC(rs.getString("TenNCC"));
        ncc.setSoDienThoai(rs.getString("SoDienThoai"));
        ncc.setEmail(rs.getString("Email"));
        int maDiaChi = rs.getInt("MaDiaChi");
        ncc.setMaDiaChi(rs.wasNull() ? null : maDiaChi);
        ncc.setTrangThai(rs.getString("TrangThai"));
        ncc.setNgayTao(rs.getTimestamp("NgayTao"));
        return ncc;
    }
    
    public boolean updateTrangThaiNCC(int maNCC, String trangThai) {
        String sql = "UPDATE NhaCungCap SET TrangThai = ? WHERE MaNCC = ?";

        try (Connection conn = DatabaseConnection.getInstance().getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, trangThai);
            stmt.setInt(2, maNCC);

            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.severe("Lỗi khi cập nhật trạng thái nhà cung cấp: " + e.getMessage());
            return false;
        }
    }

}
