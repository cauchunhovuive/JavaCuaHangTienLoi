package entities;

import java.math.BigDecimal;

/** Khách hàng: khách lẻ (maKhachHang = 0) hoặc thành viên tích điểm. */
public class KhachHang {
    private int maKhachHang;
    private String tenKhachHang;
    private String soDienThoai;
    private int diemTichLuy;
    private BigDecimal tongChiTieu;

    public KhachHang() {
        this.tenKhachHang = "Khách lẻ";
    }

    public KhachHang(int maKhachHang, String tenKhachHang, String soDienThoai, int diemTichLuy, BigDecimal tongChiTieu) {
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.soDienThoai = soDienThoai;
        this.diemTichLuy = diemTichLuy;
        this.tongChiTieu = tongChiTieu;
    }

    public int getMaKhachHang() { return maKhachHang; }
    public void setMaKhachHang(int maKhachHang) { this.maKhachHang = maKhachHang; }

    public String getTenKhachHang() { return tenKhachHang; }
    public void setTenKhachHang(String tenKhachHang) { this.tenKhachHang = tenKhachHang; }

    public String getSoDienThoai() { return soDienThoai; }
    public void setSoDienThoai(String soDienThoai) { this.soDienThoai = soDienThoai; }

    public int getDiemTichLuy() { return diemTichLuy; }
    public void setDiemTichLuy(int diemTichLuy) { this.diemTichLuy = diemTichLuy; }

    public BigDecimal getTongChiTieu() { return tongChiTieu; }
    public void setTongChiTieu(BigDecimal tongChiTieu) { this.tongChiTieu = tongChiTieu; }

    @Override
    public String toString() {
        return tenKhachHang + (soDienThoai == null || soDienThoai.isEmpty() ? "" : " - " + soDienThoai);
    }
}
