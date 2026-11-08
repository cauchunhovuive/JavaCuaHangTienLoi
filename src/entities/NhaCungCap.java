package entities;

import java.sql.Timestamp;

public class NhaCungCap {
    private int maNCC;
    private String tenNCC;
    private String soDienThoai;
    private String email;
    private Integer maDiaChi;
    private String trangThai;
    private Timestamp ngayTao;

    public NhaCungCap() {
    }

    public NhaCungCap(int maNCC, String tenNCC, String soDienThoai, String email,
                      Integer maDiaChi, String trangThai, Timestamp ngayTao) {
        this.maNCC = maNCC;
        this.tenNCC = tenNCC;
        this.soDienThoai = soDienThoai;
        this.email = email;
        this.maDiaChi = maDiaChi;
        this.trangThai = trangThai;
        this.ngayTao = ngayTao;
    }

    public int getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(int maNCC) {
        this.maNCC = maNCC;
    }

    public String getTenNCC() {
        return tenNCC;
    }

    public void setTenNCC(String tenNCC) {
        this.tenNCC = tenNCC;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Integer getMaDiaChi() {
        return maDiaChi;
    }

    public void setMaDiaChi(Integer maDiaChi) {
        this.maDiaChi = maDiaChi;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Timestamp getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(Timestamp ngayTao) {
        this.ngayTao = ngayTao;
    }

    @Override
    public String toString() {
        return tenNCC;
    }
}
