package entities;

import java.util.Date;

public class TaiKhoan {
	private String tenTaiKhoan;
	private String matKhau;
	private String email;
	private PhanQuyen quyen;   
	private Date ngayTao;
	private String trangThai;  
	public TaiKhoan() {
	}

	public TaiKhoan(String tenTaiKhoan, String matKhau, PhanQuyen quyen) {
		this.tenTaiKhoan = tenTaiKhoan;
		this.matKhau = matKhau;
		this.quyen = quyen;
	}

	public String getTenTaiKhoan() {
		return tenTaiKhoan;
	}

	public void setTenTaiKhoan(String tenTaiKhoan) {
		this.tenTaiKhoan = tenTaiKhoan;
	}

	public String getMatKhau() {
		return matKhau;
	}

	public void setMatKhau(String matKhau) {
		this.matKhau = matKhau;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public PhanQuyen getQuyen() {
		return quyen;
	}

	public void setQuyen(PhanQuyen quyen) {
		this.quyen = quyen;
	}

	public Date getNgayTao() {
		return ngayTao;
	}

	public void setNgayTao(Date ngayTao) {
		this.ngayTao = ngayTao;
	}

	public String getTrangThai() {
		return trangThai;
	}

	public void setTrangThai(String trangThai) {
		this.trangThai = trangThai;
	}
}
