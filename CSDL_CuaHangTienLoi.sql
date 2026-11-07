-- =====================================================================
-- CƠ SỞ DỮ LIỆU HỆ THỐNG QUẢN LÝ CỬA HÀNG TIỆN LỢI
-- Chạy trên SQL Server (localhost:1433), database: QuanLyCuaHangTienLoi
-- File này chạy được nhiều lần (idempotent): nếu DB đã tạo rồi thì
-- chỉ phần thiếu được thêm vào (vd: cột HinhAnh, sản phẩm mới).
-- =====================================================================

IF DB_ID('QuanLyCuaHangTienLoi') IS NULL
    CREATE DATABASE QuanLyCuaHangTienLoi;
GO
USE QuanLyCuaHangTienLoi;
GO

-- ---------------------------------------------------------------------
-- Phân quyền: 1 = Quản lý (admin), 2 = Nhân viên bán hàng
-- ---------------------------------------------------------------------
IF OBJECT_ID('PhanQuyen') IS NULL
CREATE TABLE PhanQuyen (
    MaQuyen  INT PRIMARY KEY,
    TenQuyen NVARCHAR(50) NOT NULL
);
GO

-- ---------------------------------------------------------------------
-- Địa chỉ
-- ---------------------------------------------------------------------
IF OBJECT_ID('DiaChi') IS NULL
CREATE TABLE DiaChi (
    MaDiaChi  INT IDENTITY(1,1) PRIMARY KEY,
    SoNha     NVARCHAR(100),
    Duong     NVARCHAR(100),
    Quan      NVARCHAR(100),
    ThanhPho  NVARCHAR(100)
);
GO

-- ---------------------------------------------------------------------
-- Tài khoản đăng nhập
-- ---------------------------------------------------------------------
IF OBJECT_ID('TaiKhoan') IS NULL
CREATE TABLE TaiKhoan (
    TenTaiKhoan VARCHAR(50) PRIMARY KEY,
    MatKhau     VARCHAR(100) NOT NULL,
    Email       VARCHAR(100),
    MaQuyen     INT NOT NULL REFERENCES PhanQuyen(MaQuyen),
    NgayTao     DATETIME DEFAULT GETDATE(),
    TrangThai   NVARCHAR(20) DEFAULT N'Active'
);
GO

-- ---------------------------------------------------------------------
-- Nhân viên
-- ---------------------------------------------------------------------
IF OBJECT_ID('NhanVien') IS NULL
CREATE TABLE NhanVien (
    MaNV         INT IDENTITY(1,1) PRIMARY KEY,
    Ho           NVARCHAR(50),
    Ten          NVARCHAR(50) NOT NULL,
    NgaySinh     DATE,
    GioiTinh     NVARCHAR(10),
    CMND         VARCHAR(20),
    SoDienThoai  VARCHAR(20),
    MaDiaChi     INT REFERENCES DiaChi(MaDiaChi),
    TenTaiKhoan  VARCHAR(50) REFERENCES TaiKhoan(TenTaiKhoan),
    LoaiNV       NVARCHAR(50),
    TrangThai    NVARCHAR(20) DEFAULT N'Active',
    NgayVaoLam  DATE,
    LuongCoBan   DECIMAL(15,2)
);
GO

-- ---------------------------------------------------------------------
-- Loại sản phẩm
-- ---------------------------------------------------------------------
IF OBJECT_ID('LoaiSanPham') IS NULL
CREATE TABLE LoaiSanPham (
    MaLoai INT IDENTITY(1,1) PRIMARY KEY,
    TenLoai NVARCHAR(100) NOT NULL,
    MoTa    NVARCHAR(255),
    Icon    NVARCHAR(255)
);
GO

-- ---------------------------------------------------------------------
-- Sản phẩm (theo mã vạch, có tồn kho, ngưỡng cảnh báo và hình ảnh)
-- ---------------------------------------------------------------------
IF OBJECT_ID('SanPham') IS NULL
CREATE TABLE SanPham (
    MaSP       INT IDENTITY(1,1) PRIMARY KEY,
    MaVach     VARCHAR(50) UNIQUE,
    TenSP      NVARCHAR(150) NOT NULL,
    MaLoai     INT REFERENCES LoaiSanPham(MaLoai),
    DonViTinh  NVARCHAR(20) DEFAULT N'Cái',
    GiaBan     DECIMAL(15,2) NOT NULL,
    GiaVon     DECIMAL(15,2) DEFAULT 0,
    TonKho     INT DEFAULT 0,
    TonToiThieu INT DEFAULT 5,
    HinhAnh    NVARCHAR(255),
    TrangThai  NVARCHAR(20) DEFAULT N'DANG_BAN'
);
GO

-- Bổ sung cột HinhAnh nếu DB được tạo từ phiên bản trước
IF OBJECT_ID('SanPham') IS NOT NULL AND COL_LENGTH('SanPham', 'HinhAnh') IS NULL
    ALTER TABLE SanPham ADD HinhAnh NVARCHAR(255);
GO

-- ---------------------------------------------------------------------
-- Khách hàng thành viên (khách lẻ không cần dòng trong bảng này)
-- ---------------------------------------------------------------------
IF OBJECT_ID('KhachHang') IS NULL
CREATE TABLE KhachHang (
    MaKH       INT IDENTITY(1,1) PRIMARY KEY,
    TenKH      NVARCHAR(100) NOT NULL,
    SoDienThoai VARCHAR(20),
    DiemTichLuy INT DEFAULT 0,
    TongChiTieu DECIMAL(15,2) DEFAULT 0
);
GO

-- ---------------------------------------------------------------------
-- Hóa đơn bán hàng
-- ---------------------------------------------------------------------
IF OBJECT_ID('HoaDon') IS NULL
CREATE TABLE HoaDon (
    MaHD        INT IDENTITY(1,1) PRIMARY KEY,
    NgayLap     DATETIME NOT NULL DEFAULT GETDATE(),
    MaNV        INT REFERENCES NhanVien(MaNV),
    MaKH        INT REFERENCES KhachHang(MaKH),
    -- TongTien là thuộc tính dẫn xuất: = SUM(ChiTietHoaDon.SoLuong * DonGia), không lưu trữ
    TienKhachDua DECIMAL(15,2),
    TienThua    DECIMAL(15,2),
    DiemTang    INT DEFAULT 0,
    TrangThai   NVARCHAR(20) DEFAULT N'HOAN_TAT',
    HinhThucThanhToan NVARCHAR(20) DEFAULT N'TIEN_MAT' -- TIEN_MAT hoac CHUYEN_KHOAN
);
GO

-- Bổ sung cột HinhThucThanhToan nếu DB tạo từ phiên bản trước
IF OBJECT_ID('HoaDon') IS NOT NULL AND COL_LENGTH('HoaDon', 'HinhThucThanhToan') IS NULL
    ALTER TABLE HoaDon ADD HinhThucThanhToan NVARCHAR(20) DEFAULT N'TIEN_MAT';
GO

-- Bổ sung cột mới nếu DB được tạo từ phiên bản trước (tránh lỗi khi thanh toán)
IF OBJECT_ID('HoaDon') IS NOT NULL
BEGIN
    IF COL_LENGTH('HoaDon', 'MaKH') IS NULL ALTER TABLE HoaDon ADD MaKH INT REFERENCES KhachHang(MaKH);
    IF COL_LENGTH('HoaDon', 'TienKhachDua') IS NULL ALTER TABLE HoaDon ADD TienKhachDua DECIMAL(15,2);
    IF COL_LENGTH('HoaDon', 'TienThua') IS NULL ALTER TABLE HoaDon ADD TienThua DECIMAL(15,2);
    IF COL_LENGTH('HoaDon', 'DiemTang') IS NULL ALTER TABLE HoaDon ADD DiemTang INT DEFAULT 0;
    IF COL_LENGTH('HoaDon', 'TrangThai') IS NULL ALTER TABLE HoaDon ADD TrangThai NVARCHAR(20) DEFAULT N'HOAN_TAT';
END
GO

-- TongTien là thuộc tính dẫn xuất nên không còn cột lưu trữ.
-- Với DB tạo từ phiên bản trước: bỏ cột TongTien (dữ liệu vẫn tính lại được từ ChiTietHoaDon).
IF OBJECT_ID('HoaDon') IS NOT NULL AND COL_LENGTH('HoaDon', 'TongTien') IS NOT NULL
BEGIN
    DECLARE @dfTongTien NVARCHAR(200);
    SELECT @dfTongTien = dc.name
      FROM sys.default_constraints dc
      JOIN sys.columns c ON c.default_object_id = dc.object_id
     WHERE dc.parent_object_id = OBJECT_ID('HoaDon') AND c.name = 'TongTien';
    IF @dfTongTien IS NOT NULL EXEC('ALTER TABLE HoaDon DROP CONSTRAINT ' + @dfTongTien);
    ALTER TABLE HoaDon DROP COLUMN TongTien;
END
GO

IF OBJECT_ID('KhachHang') IS NOT NULL
BEGIN
    IF COL_LENGTH('KhachHang', 'DiemTichLuy') IS NULL ALTER TABLE KhachHang ADD DiemTichLuy INT DEFAULT 0;
    IF COL_LENGTH('KhachHang', 'TongChiTieu') IS NULL ALTER TABLE KhachHang ADD TongChiTieu DECIMAL(15,2) DEFAULT 0;
END
GO

IF OBJECT_ID('ChiTietHoaDon') IS NULL
CREATE TABLE ChiTietHoaDon (
    MaHD   INT NOT NULL REFERENCES HoaDon(MaHD),
    MaSP   INT NOT NULL REFERENCES SanPham(MaSP),
    SoLuong INT NOT NULL,
    DonGia DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (MaHD, MaSP)
);
GO

-- ---------------------------------------------------------------------
-- Nhà cung cấp
-- ---------------------------------------------------------------------
IF OBJECT_ID('NhaCungCap') IS NULL
CREATE TABLE NhaCungCap (
    MaNCC      INT IDENTITY(1,1) PRIMARY KEY,
    TenNCC     NVARCHAR(150) NOT NULL,
    SoDienThoai VARCHAR(20),
    Email      VARCHAR(100),
    MaDiaChi   INT REFERENCES DiaChi(MaDiaChi),
    TrangThai  NVARCHAR(20) DEFAULT N'Active',
    NgayTao    DATETIME DEFAULT GETDATE()
);
GO

-- Bổ sung cột MaNCC (nhà cung cấp của sản phẩm) nếu DB tạo từ phiên bản trước.
-- Đặt SAU khi bảng NhaCungCap đã tồn tại để FK hợp lệ.
IF OBJECT_ID('SanPham') IS NOT NULL AND COL_LENGTH('SanPham', 'MaNCC') IS NULL
    ALTER TABLE SanPham ADD MaNCC INT NULL REFERENCES NhaCungCap(MaNCC);
GO

-- ---------------------------------------------------------------------
-- Phiếu nhập hàng
-- ---------------------------------------------------------------------
IF OBJECT_ID('PhieuNhapHang') IS NULL
CREATE TABLE PhieuNhapHang (
    MaPN      INT IDENTITY(1,1) PRIMARY KEY,
    NgayNhap  DATETIME NOT NULL DEFAULT GETDATE(),
    MaNCC     INT REFERENCES NhaCungCap(MaNCC),
    MaNV      INT REFERENCES NhanVien(MaNV),
    TongTien  DECIMAL(15,2) DEFAULT 0,
    GhiChu    NVARCHAR(255)
);
GO

IF OBJECT_ID('ChiTietNhapHang') IS NULL
CREATE TABLE ChiTietNhapHang (
    MaPN      INT NOT NULL REFERENCES PhieuNhapHang(MaPN),
    MaSP      INT NOT NULL REFERENCES SanPham(MaSP),
    SoLuong   INT NOT NULL,
    DonGiaNhap DECIMAL(15,2) NOT NULL,
    PRIMARY KEY (MaPN, MaSP)
);
GO

-- ---------------------------------------------------------------------
-- DỮ LIỆU MẪU: nhà cung cấp (cần cho màn nhập hàng)
-- ---------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM NhaCungCap)
BEGIN
    INSERT INTO NhaCungCap (TenNCC, SoDienThoai, Email, TrangThai) VALUES
    (N'Vinamilk', '02854155555', 'info@vinamilk.com.vn', N'Active'),
    (N'Tân Hiệp Phát (Red Bull)', '02743805116', 'thp@tan-hiep-phat.vn', N'Active'),
    (N'Acecook Việt Nam', '02838154021', 'info@acecook.vn', N'Active'),
    (N'Unilever Việt Nam', '02838234567', 'consumer@unilever.com.vn', N'Active'),
    (N'Masan Consumer', '02854132123', 'info@masan.com.vn', N'Active');
END
GO

-- ---------------------------------------------------------------------
-- DỮ LIỆU MẪU: phân quyền, tài khoản, nhân viên
-- ---------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM PhanQuyen)
BEGIN
    INSERT INTO PhanQuyen (MaQuyen, TenQuyen) VALUES (1, N'Quản lý');
    INSERT INTO PhanQuyen (MaQuyen, TenQuyen) VALUES (2, N'Nhân viên');
END
GO

IF NOT EXISTS (SELECT 1 FROM TaiKhoan)
BEGIN
    INSERT INTO TaiKhoan (TenTaiKhoan, MatKhau, Email, MaQuyen, TrangThai)
    VALUES ('admin', '123456', 'admin@cuahang.com', 1, N'Active'),
           ('nhanvien1', '123', 'nv1@cuahang.com', 2, N'Active');
END
GO

IF NOT EXISTS (SELECT 1 FROM NhanVien)
BEGIN
    INSERT INTO NhanVien (Ho, Ten, NgaySinh, GioiTinh, CMND, SoDienThoai, TenTaiKhoan, LoaiNV, TrangThai, NgayVaoLam, LuongCoBan)
    VALUES (N'Quản', N'Lý', '1990-01-01', N'Nam', '012345678901', '0901000001', 'admin', N'Quản lý', N'Active', '2024-01-01', 8000000),
           (N'Nhân', N'Viên', '2000-05-05', N'Nữ', '098765432109', '0902000002', 'nhanvien1', N'Bán hàng', N'Active', '2024-06-01', 5000000);
END
GO

-- ---------------------------------------------------------------------
-- DỮ LIỆU MẪU: loại sản phẩm
-- ---------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM LoaiSanPham)
BEGIN
    INSERT INTO LoaiSanPham (TenLoai, MoTa) VALUES
    (N'Đồ uống', N'Nước uống các loại'),
    (N'Bánh kẹo', N'Bánh, kẹo, snack'),
    (N'Đồ khô', N'Mì, gạo, gia vị, thực phẩm đóng hộp'),
    (N'Chăm sóc cá nhân', N'Nước gội, kem đánh răng, xà phòng'),
    (N'Đồ dùng gia đình', N'Nước giặt, dầu ăn, giấy vệ sinh');
END
GO

-- ---------------------------------------------------------------------
-- DỮ LIỆU MẪU: 20 sản phẩm (bỏ qua nếu MaVach đã tồn tại)
-- Ảnh nằm trong src/images/sp/ — nếu thiếu ảnh, phần mềm tự hiện
-- ảnh image-notfound.png thay thế.
-- ---------------------------------------------------------------------
IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938505972116')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938505972116', N'Sữa tươi Vinamilk 1000ml',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ uống'),
            N'Hộp', 22000, 18000, 40, 10, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938504567890')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938504567890', N'Nước ngọt Coca-Cola 330ml',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ uống'),
            N'Lon', 12000, 9000, 60, 12, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8934981000018')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8934981000018', N'Nước khoáng La Vie 500ml',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ uống'),
            N'Chai', 6000, 4500, 80, 15, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8934662744017')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8934662744017', N'Trà xanh C2 455ml',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ uống'),
            N'Chai', 10000, 7500, 50, 12, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938500599005')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938500599005', N'Cà phê G7 3in1 (10 gói)',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ uống'),
            N'Gói', 38000, 30000, 25, 6, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8934800000011')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8934800000011', N'Bia Sài Gòn 330ml',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ uống'),
            N'Lon', 16000, 13000, 48, 12, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8934662012012')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8934662012012', N'Mì Hảo Hảo tôm chua cay (vỉ 5)',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ khô'),
            N'Gói', 17500, 14000, 60, 15, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938402090015')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938402090015', N'Gạo ST25 5kg',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ khô'),
            N'Túi', 165000, 140000, 20, 5, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938506122007')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938506122007', N'Đường trắng Vina White 1kg',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ khô'),
            N'Túi', 22000, 18500, 30, 8, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938507330016')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938507330016', N'Cá hộp Crownblue 175g',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ khô'),
            N'Hộp', 18000, 14500, 36, 10, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938504000014')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938504000014', N'Trứng gà 10 quả',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ khô'),
            N'Vỉ', 36000, 31000, 20, 5, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938501234567')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938501234567', N'Bánh Oreo 169g',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Bánh kẹo'),
            N'Gói', 15000, 11000, 30, 8, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938501100017')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938501100017', N'Bánh quy Cosy 180g',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Bánh kẹo'),
            N'Gói', 25000, 20000, 24, 6, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938501100024')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938501100024', N'Bánh cracker mặn 170g',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Bánh kẹo'),
            N'Gói', 18000, 14500, 24, 6, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938500071016')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938500071016', N'Kem đánh răng Colgate 100g',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Chăm sóc cá nhân'),
            N'Tuýp', 32000, 26000, 25, 6, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938506100018')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938506100018', N'Dầu gội Head & Shoulders 650ml',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Chăm sóc cá nhân'),
            N'Chai', 145000, 118000, 15, 4, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938500011017')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938500011017', N'Xà phòng Lifebuoy 175g',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Chăm sóc cá nhân'),
            N'Bánh', 33000, 26000, 30, 8, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938509876543')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938509876543', N'Nước giặt OMO 3kg',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ dùng gia đình'),
            N'Túi', 85000, 70000, 15, 5, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938501122334')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938501122334', N'Dầu ăn Neptune 1L',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ dùng gia đình'),
            N'Chai', 48000, 40000, 20, 5, N'DANG_BAN');

IF NOT EXISTS (SELECT 1 FROM SanPham WHERE MaVach = '8938504440013')
    INSERT INTO SanPham (MaVach, TenSP, MaLoai, DonViTinh, GiaBan, GiaVon, TonKho, TonToiThieu, TrangThai)
    VALUES ('8938504440013', N'Giấy vệ sinh Bless You 10 cuộn',
            (SELECT MaLoai FROM LoaiSanPham WHERE TenLoai = N'Đồ dùng gia đình'),
            N'Lốc', 42000, 34000, 25, 6, N'DANG_BAN');
GO

-- ---------------------------------------------------------------------
-- GÁN NHÀ CUNG CẤP CHO SẢN PHẨM MẪU (chạy 1 lần, bỏ qua nếu đã gán)
-- ---------------------------------------------------------------------
IF EXISTS (SELECT 1 FROM SanPham WHERE MaNCC IS NULL) AND EXISTS (SELECT 1 FROM NhaCungCap)
BEGIN
    -- Vinamilk: sữa
    UPDATE SanPham SET MaNCC = (SELECT MaNCC FROM NhaCungCap WHERE TenNCC = N'Vinamilk')
    WHERE MaNCC IS NULL AND TenSP LIKE N'%Vinamilk%';

    -- Tân Hiệp Phát: nước uống đóng chai/lon (Coca, La Vie, C2, bia)
    UPDATE SanPham SET MaNCC = (SELECT MaNCC FROM NhaCungCap WHERE TenNCC LIKE N'Tân Hiệp Phát%')
    WHERE MaNCC IS NULL AND (TenSP LIKE N'%Coca%' OR TenSP LIKE N'%La Vie%'
                          OR TenSP LIKE N'%C2%' OR TenSP LIKE N'%Bia%');

    -- Acecook: mì gói
    UPDATE SanPham SET MaNCC = (SELECT MaNCC FROM NhaCungCap WHERE TenNCC LIKE N'Acecook%')
    WHERE MaNCC IS NULL AND TenSP LIKE N'%Hảo Hảo%';

    -- Unilever: chăm sóc cá nhân + nước giặt
    UPDATE SanPham SET MaNCC = (SELECT MaNCC FROM NhaCungCap WHERE TenNCC LIKE N'Unilever%')
    WHERE MaNCC IS NULL AND (TenSP LIKE N'%Head%' OR TenSP LIKE N'%Lifebuoy%' OR TenSP LIKE N'%OMO%');

    -- Masan Consumer: cà phê, bánh kẹo, đồ khô, dầu ăn
    UPDATE SanPham SET MaNCC = (SELECT MaNCC FROM NhaCungCap WHERE TenNCC LIKE N'Masan%')
    WHERE MaNCC IS NULL AND (TenSP LIKE N'%G7%' OR TenSP LIKE N'%Oreo%' OR TenSP LIKE N'%Cosy%'
                          OR TenSP LIKE N'%gạo%' OR TenSP LIKE N'%đường%' OR TenSP LIKE N'%cá hộp%'
                          OR TenSP LIKE N'%trứng%' OR TenSP LIKE N'%dầu ăn%');

    -- Các SP còn lại (kem đánh răng, cracker, giấy vệ sinh) → Unilever
    UPDATE SanPham SET MaNCC = (SELECT MaNCC FROM NhaCungCap WHERE TenNCC LIKE N'Unilever%')
    WHERE MaNCC IS NULL;
END
GO

-- ---------------------------------------------------------------------
-- GÁN ẢNH CHO SẢN PHẨM (chạy lại được, không mất dữ liệu)
-- ---------------------------------------------------------------------
UPDATE SanPham SET HinhAnh = 'src/images/sp/sua-tuoi.jpg'     WHERE MaVach = '8938505972116';
UPDATE SanPham SET HinhAnh = 'src/images/sp/cocacola.jpg'      WHERE MaVach = '8938504567890';
UPDATE SanPham SET HinhAnh = 'src/images/sp/nuoc-khoang.jpg'   WHERE MaVach = '8934981000018';
UPDATE SanPham SET HinhAnh = 'src/images/sp/tra-xanh.jpg'      WHERE MaVach = '8934662744017';
UPDATE SanPham SET HinhAnh = 'src/images/sp/ca-phe.jpg'        WHERE MaVach = '8938500599005';
UPDATE SanPham SET HinhAnh = 'src/images/sp/bia.jpg'           WHERE MaVach = '8934800000011';
UPDATE SanPham SET HinhAnh = 'src/images/sp/mi-goi.jpg'        WHERE MaVach = '8934662012012';
UPDATE SanPham SET HinhAnh = 'src/images/sp/gao.jpg'           WHERE MaVach = '8938402090015';
UPDATE SanPham SET HinhAnh = 'src/images/sp/duong.jpg'         WHERE MaVach = '8938506122007';
UPDATE SanPham SET HinhAnh = 'src/images/sp/ca-hop.jpg'        WHERE MaVach = '8938507330016';
UPDATE SanPham SET HinhAnh = 'src/images/sp/trung.jpg'         WHERE MaVach = '8938504000014';
UPDATE SanPham SET HinhAnh = 'src/images/sp/banh-oreo.png'     WHERE MaVach = '8938501234567';
UPDATE SanPham SET HinhAnh = 'src/images/sp/banh-quy.jpg'      WHERE MaVach = '8938501100017';
UPDATE SanPham SET HinhAnh = 'src/images/sp/banh-cracker.jpg'  WHERE MaVach = '8938501100024';
UPDATE SanPham SET HinhAnh = 'src/images/sp/kem-dan-rang.jpg'  WHERE MaVach = '8938500071016';
UPDATE SanPham SET HinhAnh = 'src/images/sp/dau-goi.jpg'       WHERE MaVach = '8938506100018';
UPDATE SanPham SET HinhAnh = 'src/images/sp/xuat-khon.jpg'     WHERE MaVach = '8938500011017';
UPDATE SanPham SET HinhAnh = 'src/images/sp/nuoc-giat.jpg'     WHERE MaVach = '8938509876543';
UPDATE SanPham SET HinhAnh = 'src/images/sp/dau-an.jpg'        WHERE MaVach = '8938501122334';
UPDATE SanPham SET HinhAnh = 'src/images/sp/giay-ve-sinh.jpg'  WHERE MaVach = '8938504440013';
GO

PRINT N'Hoàn tất: cơ sở dữ liệu QuanLyCuaHangTienLoi với 20 sản phẩm mẫu!';
GO
