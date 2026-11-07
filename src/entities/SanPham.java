package entities;

import java.math.BigDecimal;

public class SanPham {
    private int maSanPham;
    private String maVach;         
    private String tenSanPham;
    private LoaiSanPham loaiSanPham;
    private String donViTinh;      
    private BigDecimal giaBan;
    private BigDecimal giaVon;
    private int tonKho;
    private int tonToiThieu;        
    private String trangThai;       
    private String hinhAnh;         
    private int maNhaCungCap;       
    private String tenNhaCungCap;  

    public SanPham() {
    }
    public SanPham(int maSanPham, String maVach, String tenSanPham, LoaiSanPham loaiSanPham,
                   String donViTinh, BigDecimal giaBan, BigDecimal giaVon,
                   int tonKho, int tonToiThieu, String trangThai) {
        this.maSanPham = maSanPham;
        this.maVach = maVach;
        this.tenSanPham = tenSanPham;
        this.loaiSanPham = loaiSanPham;
        this.donViTinh = donViTinh;
        this.giaBan = giaBan;
        this.giaVon = giaVon;
        this.tonKho = tonKho;
        this.tonToiThieu = tonToiThieu;
        this.trangThai = trangThai;
    }

    public int getMaSanPham() { return maSanPham; }
    public void setMaSanPham(int maSanPham) { this.maSanPham = maSanPham; }

    public String getMaVach() { return maVach; }
    public void setMaVach(String maVach) { this.maVach = maVach; }

    public String getTenSanPham() { return tenSanPham; }
    public void setTenSanPham(String tenSanPham) { this.tenSanPham = tenSanPham; }

    public LoaiSanPham getLoaiSanPham() { return loaiSanPham; }
    public void setLoaiSanPham(LoaiSanPham loaiSanPham) { this.loaiSanPham = loaiSanPham; }

    public String getDonViTinh() { return donViTinh; }
    public void setDonViTinh(String donViTinh) { this.donViTinh = donViTinh; }

    public BigDecimal getGiaBan() { return giaBan; }
    public void setGiaBan(BigDecimal giaBan) { this.giaBan = giaBan; }

    public BigDecimal getGiaVon() { return giaVon; }
    public void setGiaVon(BigDecimal giaVon) { this.giaVon = giaVon; }

    public int getTonKho() { return tonKho; }
    public void setTonKho(int tonKho) { this.tonKho = tonKho; }

    public int getTonToiThieu() { return tonToiThieu; }
    public void setTonToiThieu(int tonToiThieu) { this.tonToiThieu = tonToiThieu; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhAnh() { return hinhAnh; }
    public void setHinhAnh(String hinhAnh) { this.hinhAnh = hinhAnh; }

    public int getMaNhaCungCap() { return maNhaCungCap; }
    public void setMaNhaCungCap(int maNhaCungCap) { this.maNhaCungCap = maNhaCungCap; }

    public String getTenNhaCungCap() { return tenNhaCungCap; }
    public void setTenNhaCungCap(String tenNhaCungCap) { this.tenNhaCungCap = tenNhaCungCap; }

    public boolean isSapHet() {
        return tonKho <= tonToiThieu;
    }

    @Override
    public String toString() {
        return tenSanPham + " [" + maVach + "]";
    }
}