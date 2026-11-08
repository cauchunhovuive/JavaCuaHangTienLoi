package entities;

import java.math.BigDecimal;

public class SanPham {
    private int maSP;
    private String maVach;
    private String tenSP;
    private Integer maLoai;
    private String donViTinh;
    private BigDecimal giaBan;
    private BigDecimal giaVon;
    private int tonKho;
    private int tonToiThieu;
    private String hinhAnh;
    private String trangThai;
    private Integer maNCC;

    public SanPham() {
    }

    public SanPham(int maSP, String maVach, String tenSP, Integer maLoai, String donViTinh,
                   BigDecimal giaBan, BigDecimal giaVon, int tonKho, int tonToiThieu,
                   String hinhAnh, String trangThai, Integer maNCC) {
        this.maSP = maSP;
        this.maVach = maVach;
        this.tenSP = tenSP;
        this.maLoai = maLoai;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
        this.giaVon = giaVon;
        this.tonKho = tonKho;
        this.tonToiThieu = tonToiThieu;
        this.hinhAnh = hinhAnh;
        this.trangThai = trangThai;
        this.maNCC = maNCC;
    }

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public String getMaVach() {
        return maVach;
    }

    public void setMaVach(String maVach) {
        this.maVach = maVach;
    }

    public String getTenSP() {
        return tenSP;
    }

    public void setTenSP(String tenSP) {
        this.tenSP = tenSP;
    }

    public Integer getMaLoai() {
        return maLoai;
    }

    public void setMaLoai(Integer maLoai) {
        this.maLoai = maLoai;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public BigDecimal getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(BigDecimal giaBan) {
        this.giaBan = giaBan;
    }

    public BigDecimal getGiaVon() {
        return giaVon;
    }

    public void setGiaVon(BigDecimal giaVon) {
        this.giaVon = giaVon;
    }

    public int getTonKho() {
        return tonKho;
    }

    public void setTonKho(int tonKho) {
        this.tonKho = tonKho;
    }

    public int getTonToiThieu() {
        return tonToiThieu;
    }

    public void setTonToiThieu(int tonToiThieu) {
        this.tonToiThieu = tonToiThieu;
    }

    public String getHinhAnh() {
        return hinhAnh;
    }

    public void setHinhAnh(String hinhAnh) {
        this.hinhAnh = hinhAnh;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Integer getMaNCC() {
        return maNCC;
    }

    public void setMaNCC(Integer maNCC) {
        this.maNCC = maNCC;
    }

    /** Thuộc tính dẫn xuất: cảnh báo tồn kho thấp (không phải cột trong CSDL). */
    public boolean isSapHet() {
        return tonKho <= tonToiThieu;
    }

    @Override
    public String toString() {
        return tenSP + " [" + maVach + "]";
    }
}
