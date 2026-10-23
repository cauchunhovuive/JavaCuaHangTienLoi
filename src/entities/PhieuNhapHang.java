package entities;

import java.math.BigDecimal;
import java.util.Date;

/** Phiếu nhập hàng từ nhà cung cấp. */
public class PhieuNhapHang {
    private int maPhieuNhap;
    private Date ngayNhap;
    private NhaCungCap nhaCungCap;
    private NhanVien nhanVien;
    private BigDecimal tongTienNhap;
    private String ghiChu;

    public PhieuNhapHang() {
    }

    public PhieuNhapHang(int maPhieuNhap, Date ngayNhap, NhaCungCap nhaCungCap,
                         NhanVien nhanVien, BigDecimal tongTienNhap, String ghiChu) {
        this.maPhieuNhap = maPhieuNhap;
        this.ngayNhap = ngayNhap;
        this.nhaCungCap = nhaCungCap;
        this.nhanVien = nhanVien;
        this.tongTienNhap = tongTienNhap;
        this.ghiChu = ghiChu;
    }

    public int getMaPhieuNhap() { return maPhieuNhap; }
    public void setMaPhieuNhap(int maPhieuNhap) { this.maPhieuNhap = maPhieuNhap; }

    public Date getNgayNhap() { return ngayNhap; }
    public void setNgayNhap(Date ngayNhap) { this.ngayNhap = ngayNhap; }

    public NhaCungCap getNhaCungCap() { return nhaCungCap; }
    public void setNhaCungCap(NhaCungCap nhaCungCap) { this.nhaCungCap = nhaCungCap; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public BigDecimal getTongTienNhap() { return tongTienNhap; }
    public void setTongTienNhap(BigDecimal tongTienNhap) { this.tongTienNhap = tongTienNhap; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }
}
