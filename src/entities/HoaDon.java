package entities;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/** Hóa đơn bán hàng tại quầy (POS). */
public class HoaDon {
    private int maHoaDon;
    private Date ngayLap;
    private NhanVien nhanVien;
    private KhachHang khachHang;
    private BigDecimal tienKhachDua;
    private BigDecimal tienThua;
    private int diemDuocTang;
    private String trangThai;       // HOAN_TAT, DA_HUY
    private String hinhThucThanhToan; // TIEN_MAT, CHUYEN_KHOAN

    /** Các dòng chi tiết — nguồn dữ liệu để suy ra tongTien. */
    private List<ChiTietHoaDon> chiTiet = new ArrayList<>();

    public HoaDon() {
    }

    public HoaDon(int maHoaDon, Date ngayLap, NhanVien nhanVien, KhachHang khachHang,
                  BigDecimal tienKhachDua, BigDecimal tienThua,
                  int diemDuocTang, String trangThai) {
        this.maHoaDon = maHoaDon;
        this.ngayLap = ngayLap;
        this.nhanVien = nhanVien;
        this.khachHang = khachHang;
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

    /** Thuộc tính dẫn xuất: tổng thành tiền của các dòng chi tiết (không lưu trữ). */
    public BigDecimal getTongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (ChiTietHoaDon ct : chiTiet) {
            if (ct != null && ct.getDonGia() != null) {
                tong = tong.add(ct.getThanhTien());
            }
        }
        return tong;
    }

    public List<ChiTietHoaDon> getChiTiet() { return chiTiet; }
    public void setChiTiet(List<ChiTietHoaDon> chiTiet) {
        this.chiTiet = chiTiet == null ? new ArrayList<>() : chiTiet;
    }

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
