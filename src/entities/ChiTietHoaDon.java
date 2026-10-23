package entities;

import java.math.BigDecimal;

/** Chi tiết hóa đơn: 1 dòng = 1 sản phẩm + số lượng + đơn giá. */
public class ChiTietHoaDon {
    private HoaDon hoaDon;
    private SanPham sanPham;
    private int soLuong;
    private BigDecimal donGia;

    public ChiTietHoaDon() {
    }

    public ChiTietHoaDon(HoaDon hoaDon, SanPham sanPham, int soLuong, BigDecimal donGia) {
        this.hoaDon = hoaDon;
        this.sanPham = sanPham;
        this.soLuong = soLuong;
        this.donGia = donGia;
    }

    public HoaDon getHoaDon() { return hoaDon; }
    public void setHoaDon(HoaDon hoaDon) { this.hoaDon = hoaDon; }

    public SanPham getSanPham() { return sanPham; }
    public void setSanPham(SanPham sanPham) { this.sanPham = sanPham; }

    public int getSoLuong() { return soLuong; }
    public void setSoLuong(int soLuong) { this.soLuong = soLuong; }

    public BigDecimal getDonGia() { return donGia; }
    public void setDonGia(BigDecimal donGia) { this.donGia = donGia; }

    public BigDecimal getThanhTien() {
        return donGia.multiply(BigDecimal.valueOf(soLuong));
    }
}
