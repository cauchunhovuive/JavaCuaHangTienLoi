package entities;

import java.math.BigDecimal;
import java.util.Date;

/** Hóa đơn bán hàng tại quầy (POS). */
public class HoaDon {
    private int maHoaDon;
    private Date ngayLap;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private BigDecimal tongTien;
    private BigDecimal tienKhachDua;
    private BigDecimal tienThua;
    private int diemDuocTang;
    private String trangThai;       // HOAN_TAT, DA_HUY
    private String hinhThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN

    public HoaDon() {
    }

    public HoaDon(int maHoaDon, Date ngayLap, NhanVien nhanVien, KhachHang khachHang,
                  BigDecimal tongTien, BigDecimal tienKhachDua, BigDecimal tienThua,
                  int diemDuocTang, String trangThai) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
        this.tongTien = tongTien;
        this.tienKhachDua = tienKhachDua;
        this.tienThua = tienThua;
        this.diemDuocTang = diemDuocTang;
        this.trangThai = trangThai;
    }

    public int getMaHoaDon() { return maHoaDon; }
    public void setMaHoaDon(int maHoaDon) { this.maHoaDon = maHoaDon; }

    public Date getNgayLap() { return ngayLap; }
    public void setNgayLap(Date ngayLap) { this.ngayLap = ngayLap; }

    public NhanVien getNhanVien() { return nhanVien; }
    public void setNhanVien(NhanVien nhanVien) { this.nhanVien = nhanVien; }

    public KhachHang getKhachHang() { return khachHang; }
    public void setKhachHang(KhachHang khachHang) { this.khachHang = khachHang; }

    public BigDecimal getTongTien() { return tongTien; }
    public void setTongTien(BigDecimal tongTien) { this.tongTien = tongTien; }

    public BigDecimal getTienKhachDua() { return tienKhachDua; }
    public void setTienKhachDua(BigDecimal tienKhachDua) { this.tienKhachDua = tienKhachDua; }

    public BigDecimal getTienThua() { return tienThua; }
    public void setTienThua(BigDecimal tienThua) { this.tienThua = tienThua; }

    public int getDiemDuocTang() { return diemDuocTang; }
    public void setDiemDuocTang(int diemDuocTang) { this.diemDuocTang = diemDuocTang; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public String getHinhThucThanhToan() { return hinhThucThanhToan; }
    public void setHinhThucThanhToan(String hinhThucThanhToan) { this.hinhThucThanhToan = hinhThucThanhToan; }
}
