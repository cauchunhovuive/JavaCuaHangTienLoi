package entities;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class HoaDon {
    private int maHD;
    private Timestamp ngayLap;
    private Integer maNV;
    private Integer maKH;
    private BigDecimal tienKhachDua;
    private BigDecimal tienThua;
    private Integer diemTang;
    private String trangThai;
    private String hinhThucThanhToan;
    private List<ChiTietHoaDon> chiTiet;
    private NhanVien nhanVien;
    private KhachHang khachHang;

    public HoaDon() {
        this.chiTiet = new ArrayList<>();
        this.nhanVien = new NhanVien();
        this.khachHang = new KhachHang();
    }

    public HoaDon(int maHD, Timestamp ngayLap, Integer maNV, Integer maKH,
                  BigDecimal tienKhachDua, BigDecimal tienThua, Integer diemTang,
                  String trangThai, String hinhThucThanhToan) {
        this.maHD = maHD;
        this.ngayLap = ngayLap;
        this.maNV = maNV;
        this.maKH = maKH;
        this.tienKhachDua = tienKhachDua;
        this.tienThua = tienThua;
        this.diemTang = diemTang;
        this.trangThai = trangThai;
        this.hinhThucThanhToan = hinhThucThanhToan;
        this.chiTiet = new ArrayList<>();
        this.nhanVien = new NhanVien();
        this.khachHang = new KhachHang();
    }

    public int getMaHD() {
        return maHD;
    }

    public void setMaHD(int maHD) {
        this.maHD = maHD;
    }

    public Timestamp getNgayLap() {
        return ngayLap;
    }

    public void setNgayLap(Timestamp ngayLap) {
        this.ngayLap = ngayLap;
    }

    public Integer getMaNV() {
        return maNV;
    }

    public void setMaNV(Integer maNV) {
        this.maNV = maNV;
    }

    public Integer getMaKH() {
        return maKH;
    }

    public void setMaKH(Integer maKH) {
        this.maKH = maKH;
    }

    public BigDecimal getTienKhachDua() {
        return tienKhachDua;
    }

    public void setTienKhachDua(BigDecimal tienKhachDua) {
        this.tienKhachDua = tienKhachDua;
    }

    public BigDecimal getTienThua() {
        return tienThua;
    }

    public void setTienThua(BigDecimal tienThua) {
        this.tienThua = tienThua;
    }

    public Integer getDiemTang() {
        return diemTang;
    }

    public void setDiemTang(Integer diemTang) {
        this.diemTang = diemTang;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public int getMaHoaDon() {
        return maHD;
    }

    public List<ChiTietHoaDon> getChiTiet() {
        return chiTiet;
    }

    public void setChiTiet(List<ChiTietHoaDon> chiTiet) {
        this.chiTiet = chiTiet;
    }

    public NhanVien getNhanVien() {
        return nhanVien;
    }

    public void setNhanVien(NhanVien nhanVien) {
        this.nhanVien = nhanVien;
    }

    public KhachHang getKhachHang() {
        return khachHang;
    }

    public void setKhachHang(KhachHang khachHang) {
        this.khachHang = khachHang;
    }

    public int getDiemDuocTang() {
        return diemTang;
    }

    public void setDiemDuocTang(int diemTang) {
        this.diemTang = diemTang;
    }
}
