package entities;

import java.math.BigDecimal;

public class KhachHang {
    private int maKH;
    private String tenKH;
    private String soDienThoai;
    private Integer diemTichLuy;
    private BigDecimal tongChiTieu;

    public KhachHang() {
    }

    public KhachHang(int maKH, String tenKH, String soDienThoai, Integer diemTichLuy, BigDecimal tongChiTieu) {
        this.maKH = maKH;
        this.tenKH = tenKH;
        this.soDienThoai = soDienThoai;
        this.diemTichLuy = diemTichLuy;
        this.tongChiTieu = tongChiTieu;
    }

    public int getMaKH() {
        return maKH;
    }

    public void setMaKH(int maKH) {
        this.maKH = maKH;
    }

    public String getTenKH() {
        return tenKH;
    }

    public void setTenKH(String tenKH) {
        this.tenKH = tenKH;
    }

    public String getSoDienThoai() {
        return soDienThoai;
    }

    public void setSoDienThoai(String soDienThoai) {
        this.soDienThoai = soDienThoai;
    }

    public Integer getDiemTichLuy() {
        return diemTichLuy;
    }

    public void setDiemTichLuy(Integer diemTichLuy) {
        this.diemTichLuy = diemTichLuy;
    }

    public BigDecimal getTongChiTieu() {
        return tongChiTieu;
    }

    public void setTongChiTieu(BigDecimal tongChiTieu) {
        this.tongChiTieu = tongChiTieu;
    }

    @Override
    public String toString() {
        return tenKH;
    }
}
