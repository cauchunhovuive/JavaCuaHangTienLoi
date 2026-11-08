package entities;

import java.math.BigDecimal;

public class ChiTietNhapHang {
    private int maPN;
    private int maSP;
    private int soLuong;
    private BigDecimal donGiaNhap;

    public ChiTietNhapHang() {
    }

    public ChiTietNhapHang(int maPN, int maSP, int soLuong, BigDecimal donGiaNhap) {
        this.maPN = maPN;
        this.maSP = maSP;
        this.soLuong = soLuong;
        this.donGiaNhap = donGiaNhap;
    }

    public int getMaPN() {
        return maPN;
    }

    public void setMaPN(int maPN) {
        this.maPN = maPN;
    }

    public int getMaSP() {
        return maSP;
    }

    public void setMaSP(int maSP) {
        this.maSP = maSP;
    }

    public int getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(int soLuong) {
        this.soLuong = soLuong;
    }

    public BigDecimal getDonGiaNhap() {
        return donGiaNhap;
    }

    public void setDonGiaNhap(BigDecimal donGiaNhap) {
        this.donGiaNhap = donGiaNhap;
    }

    /** Thuộc tính dẫn xuất: số lượng × đơn giá nhập (không phải cột trong CSDL). */
    public BigDecimal getThanhTien() {
        if (donGiaNhap == null) return BigDecimal.ZERO;
        return donGiaNhap.multiply(BigDecimal.valueOf(soLuong));
    }
}
