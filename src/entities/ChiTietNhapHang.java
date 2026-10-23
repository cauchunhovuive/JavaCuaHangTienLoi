package entities;

import java.math.BigDecimal;

/** Chi tiết phiếu nhập: 1 dòng = 1 sản phẩm + số lượng nhập + đơn giá nhập. */
public class ChiTietNhapHang {
    private PhieuNhapHang phieuNhap;
    private SanPham sanPham;
    private int soLuong;
    private BigDecimal donGiaNhap;

    public ChiTietNhapHang() {
    }

    public ChiTietNhapHang(PhieuNhapHang phieuNhap, SanPham sanPham, int soLuong, BigDecimal donGiaNhap) {
        this.phieuNhap = phieuNhap;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
    }

    public PhieuNhapHang getPhieuNhap() { return phieuNhap; }
    public void setPhieuNhap(PhieuNhapHang phieuNhap) { this.phieuNhap = phieuNhap; }

    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGiaNhap() { return donGiaNhap; }
    public void setDonGiaNhap(BigDecimal donGiaNhap) { this.donGiaNhap = donGiaNhap; }

    public BigDecimal getThanhTien() {
        return donGiaNhap.multiply(BigDecimal.valueOf(soLuong));
    }
}
