package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import ConnectDB.DatabaseConnection;
import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.SanPhamDAO;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;
import entities.SanPham;
import entities.TaiKhoan;

/** Màn hình bán hàng (POS): quét mã vạch / tìm sản phẩm, giỏ hàng, thanh toán. */
public class frmBanHang extends JPanel {
    private static final long serialVersionUID = 1L;

    private final TaiKhoan taiKhoan;
    private final NhanVien nhanVien;
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();

    // Giỏ hàng: [TenSP, DonViTinh, DonGia, SoLuong, ThanhTien, MaSP(hidden)]
    private final List<Object[]> gioHang = new ArrayList<>();

    private JTextField txtMaVach;
    private DefaultTableModel modelGioHang;
    private JLabel lblTongTien;
    private JLabel lblSoMatHang;
    private KhachHang khachHang = new KhachHang(); // mặc định khách lẻ

    public frmBanHang(TaiKhoan tk, NhanVien nv) {
        this.taiKhoan = tk;
        this.nhanVien = nv;
        khoiTao();
    }

    private void khoiTao() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // ====== Tren: ô quét mã vạch ======
        JPanel pnlTren = new JPanel(new BorderLayout(10, 0));
        pnlTren.setOpaque(false);
        JLabel lblTieuDe = new JLabel("BÁN HÀNG TẠI QUẦY");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        txtMaVach = new JTextField();
        txtMaVach.setFont(new Font("SansSerif", Font.PLAIN, 16));
        txtMaVach.setToolTipText("Quét mã vạch hoặc nhập tên, rồi bấm Enter");
        txtMaVach.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    xuLyNhapMaVach();
                }
            }
        });

        JButton btnTim = new JButton("Thêm vào giỏ");
        btnTim.setBackground(new Color(0, 102, 51));
        btnTim.setForeground(Color.WHITE);
        btnTim.setFocusPainted(false);
        btnTim.addActionListener(e -> xuLyNhapMaVach());

        JPanel pnlTim = new JPanel(new BorderLayout(8, 0));
        pnlTim.setOpaque(false);
        pnlTim.add(txtMaVach, BorderLayout.CENTER);
        pnlTim.add(btnTim, BorderLayout.EAST);

        JPanel pnlTieu = new JPanel(new BorderLayout());
        pnlTieu.setOpaque(false);
        pnlTieu.add(lblTieuDe, BorderLayout.WEST);
        pnlTieu.add(pnlTim, BorderLayout.CENTER);

        pnlTren.add(pnlTieu, BorderLayout.CENTER);
        add(pnlTren, BorderLayout.NORTH);

        // ====== Giua: giỏ hàng ======
        modelGioHang = new DefaultTableModel(
                new Object[]{"Tên sản phẩm", "ĐVT", "Đơn giá", "SL", "Thành tiền", "MãSP"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return c == 3; // chỉ sửa được số lượng
            }

            @Override
            public void setValueAt(Object val, int row, int col) {
                if (col == 3) {
                    try {
                        int sl = Integer.parseInt(val.toString().trim());
                        if (sl <= 0) throw new NumberFormatException();
                        gioHang.get(row)[3] = sl;
                        BigDecimal thanhTien = ((BigDecimal) gioHang.get(row)[2]).multiply(BigDecimal.valueOf(sl));
                        gioHang.get(row)[4] = thanhTien;
                        super.setValueAt(sl, row, 3);
                        super.setValueAt(thanhTien, row, 4);
                        tongCong();
                    } catch (NumberFormatException ex) {
                        JOptionPane.showMessageDialog(frmBanHang.this,
                                "Số lượng phải là số nguyên dương.", "Sai dữ liệu", JOptionPane.WARNING_MESSAGE);
                    }
                    return;
                }
                super.setValueAt(val, row, col);
            }
        };
        JTable bangGio = new JTable(modelGioHang);
        bangGio.setRowHeight(26);
        bangGio.getColumnModel().getColumn(5).setMinWidth(0);
        bangGio.getColumnModel().getColumn(5).setMaxWidth(0);
        JScrollPane scrGio = new JScrollPane(bangGio);
        scrGio.setBorder(BorderFactory.createTitledBorder("Giỏ hàng"));
        add(scrGio, BorderLayout.CENTER);

        // ====== Duoi: nút thao tác giỏ + tổng tiền ======
        JButton btnXoaDong = new JButton("Xóa dòng");
        btnXoaDong.addActionListener(e -> {
            int row = bangGio.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa trong giỏ hàng.");
                return;
            }
            gioHang.remove(row);
            modelGioHang.removeRow(row);
            tongCong();
        });

        JButton btnXoaGio = new JButton("Xóa giỏ");
        btnXoaGio.addActionListener(e -> {
            gioHang.clear();
            modelGioHang.setRowCount(0);
            khachHang = new KhachHang();
            lblKhachHang.setText("Khách lẻ");
            tongCong();
        });

        JButton btnChonKhach = new JButton("Khách hàng");
        btnChonKhach.addActionListener(e -> chonKhachHang());

        lblKhachHang = new JLabel("Khách lẻ");
        lblKhachHang.setFont(new Font("SansSerif", Font.ITALIC, 13));

        JPanel pnlThaoTac = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        pnlThaoTac.setOpaque(false);
        pnlThaoTac.add(btnXoaDong);
        pnlThaoTac.add(btnXoaGio);
        pnlThaoTac.add(btnChonKhach);
        pnlThaoTac.add(lblKhachHang);

        lblSoMatHang = new JLabel("0 mặt hàng");
        lblTongTien = new JLabel("0 đ");
        lblTongTien.setFont(new Font("SansSerif", Font.BOLD, 24));
        lblTongTien.setForeground(new Color(180, 60, 0));

        JButton btnThanhToan = new JButton("THANH TOÁN (F2)");
        btnThanhToan.setBackground(new Color(230, 92, 0));
        btnThanhToan.setForeground(Color.WHITE);
        btnThanhToan.setFont(new Font("SansSerif", Font.BOLD, 15));
        btnThanhToan.setFocusPainted(false);
        btnThanhToan.addActionListener(e -> thanhToan());

        JPanel pnlThanhToan = new JPanel(new GridBagLayout());
        pnlThanhToan.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(2, 6, 2, 6);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        pnlThanhToan.add(lblSoMatHang, gbc);
        gbc.gridx = 1;
        pnlThanhToan.add(lblTongTien, gbc);
        gbc.gridx = 2;
        pnlThanhToan.add(btnThanhToan, gbc);

        JPanel pnlDuoi = new JPanel(new BorderLayout(10, 0));
        pnlDuoi.setOpaque(false);
        pnlDuoi.add(pnlThaoTac, BorderLayout.WEST);
        pnlDuoi.add(pnlThanhToan, BorderLayout.EAST);
        add(pnlDuoi, BorderLayout.SOUTH);

        // ====== Phím tắt F2 = thanh toán ======
        bangGio.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_F2) thanhToan();
            }
        });
    }

    private JLabel lblKhachHang;

    /** Nạp lại dữ liệu (gọi khi chuyển màn). */
    public void taiDuLieu() {
        txtMaVach.setText("");
        txtMaVach.requestFocusInWindow();
    }

    private void xuLyNhapMaVach() {
        String tuKhoa = txtMaVach.getText().trim();
        if (tuKhoa.isEmpty()) return;

        // 1) Thử tìm chính xác theo mã vạch
        SanPham sp = sanPhamDAO.getByMaVach(tuKhoa);
        if (sp != null) {
            themVaoGio(sp.getMaSanPham());
            txtMaVach.setText("");
            return;
        }

        // 2) Không có → tìm theo tên: 1 kết quả thì thêm thẳng, nhiều kết quả thì cho chọn
        List<SanPham> ds = sanPhamDAO.timKiem(tuKhoa);
        if (ds.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy sản phẩm với từ khóa: " + tuKhoa,
                    "Không tìm thấy", JOptionPane.WARNING_MESSAGE);
        } else if (ds.size() == 1) {
            themVaoGio(ds.get(0).getMaSanPham());
            txtMaVach.setText("");
        } else {
            SanPham chon = (SanPham) JOptionPane.showInputDialog(this,
                    "Chọn sản phẩm cần thêm vào giỏ:", "Nhiều kết quả",
                    JOptionPane.QUESTION_MESSAGE, null, ds.toArray(), ds.get(0));
            if (chon != null) {
                themVaoGio(chon.getMaSanPham());
                txtMaVach.setText("");
            }
        }
    }

    /** Gọi từ Trang chủ: bấm thẻ sản phẩm là thêm thẳng vào giỏ. */
    public void themSanPhamVaoGio(SanPham sp) {
        if (sp != null) themVaoGio(sp.getMaSanPham());
    }

    private void themVaoGio(int maSP) {
        if (maSP < 0) return;
        SanPham sp = sanPhamDAO.getByMaSP(maSP);
        if (sp == null) return;
        if (!"DANG_BAN".equals(sp.getTrangThai())) {
            JOptionPane.showMessageDialog(this, "Sản phẩm đang ngưng bán.");
            return;
        }
        if (sp.getTonKho() <= 0) {
            JOptionPane.showMessageDialog(this, "Sản phẩm đã hết hàng.");
            return;
        }

        // Đã có trong giỏ → +1 (không vượt tồn kho)
        for (int i = 0; i < gioHang.size(); i++) {
            if ((int) gioHang.get(i)[5] == maSP) {
                int sl = (int) gioHang.get(i)[3];
                if (sl + 1 > sp.getTonKho()) {
                    JOptionPane.showMessageDialog(this,
                            "Chỉ còn " + sp.getTonKho() + " " + sp.getDonViTinh() + " trong kho.");
                    return;
                }
                // setValueAt(3) tự đồng bộ số lượng, thành tiền và tổng cộng
                modelGioHang.setValueAt(sl + 1, i, 3);
                return;
            }
        }

        gioHang.add(new Object[]{ sp.getTenSanPham(), sp.getDonViTinh(),
                sp.getGiaBan(), 1, sp.getGiaBan(), sp.getMaSanPham() });
        modelGioHang.addRow(new Object[]{ sp.getTenSanPham(), sp.getDonViTinh(),
                sp.getGiaBan(), 1, sp.getGiaBan(), sp.getMaSanPham() });
        tongCong();
    }

    private BigDecimal tongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (Object[] d : gioHang) {
            tong = tong.add((BigDecimal) d[4]);
        }
        return tong;
    }

    private void tongCong() {
        lblTongTien.setText(String.format("%,.0f đ", tongTien()));
        lblSoMatHang.setText(gioHang.size() + " mặt hàng");
    }

    private void chonKhachHang() {
        String sdt = JOptionPane.showInputDialog(this,
                "Nhập SĐT thành viên (bỏ trống = khách lẻ):", "Chọn khách hàng",
                JOptionPane.QUESTION_MESSAGE);
        if (sdt == null) return;
        sdt = sdt.trim();
        if (sdt.isEmpty()) {
            khachHang = new KhachHang();
            lblKhachHang.setText("Khách lẻ");
            return;
        }
        KhachHang kh = khachHangDAO.timTheoSdt(sdt);
        if (kh == null) {
            int chon = JOptionPane.showConfirmDialog(this,
                    "Chưa có khách hàng với SĐT " + sdt + ". Tạo mới?", "Khách mới",
                    JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {
                kh = new KhachHang(0, "Khách " + sdt, sdt, 0, BigDecimal.ZERO);
                khachHangDAO.them(kh);
                kh = khachHangDAO.timTheoSdt(sdt);
            } else {
                return;
            }
        }
        khachHang = kh;
        lblKhachHang.setText(kh.getTenKhachHang() + " (" + kh.getDiemTichLuy() + " điểm)");
    }

    /** Thanh toán: hiện dialog nhập tiền, ghi hóa đơn + chi tiết + trừ kho (1 transaction). */
    private void thanhToan() {
        if (gioHang.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Giỏ hàng đang trống.");
            return;
        }
        if (nhanVien == null) {
            JOptionPane.showMessageDialog(this,
                    "Tài khoản này chưa gắn với nhân viên nào. Liên hệ quản lý.",
                    "Không thể bán", JOptionPane.ERROR_MESSAGE);
            return;
        }

        BigDecimal tong = tongTien();

        // Chọn hình thức thanh toán
        Object[] hinhThuc = {"Tiền mặt", "Chuyển khoản"};
        int chonHT = JOptionPane.showOptionDialog(this,
                String.format("Tổng tiền: %,.0f đ\nChọn hình thức thanh toán:", tong),
                "Thanh toán", JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE,
                null, hinhThuc, hinhThuc[0]);
        if (chonHT < 0) return; // hủy
        boolean chuyenKhoan = chonHT == 1;

        BigDecimal tienKhachDua;
        BigDecimal tienThua;
        if (chuyenKhoan) {
            // Chuyển khoản: khách đưa đúng tổng, không có tiền thừa
            tienKhachDua = tong;
            tienThua = BigDecimal.ZERO;
        } else {
            String input = JOptionPane.showInputDialog(this,
                    String.format("Tổng tiền: %,.0f đ\nNhập tiền khách đưa:", tong));
            if (input == null) return;

            try {
                tienKhachDua = new BigDecimal(input.trim().replace(",", ""));
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Tiền khách đưa không hợp lệ.");
                return;
            }
            if (tienKhachDua.compareTo(tong) < 0) {
                JOptionPane.showMessageDialog(this, "Tiền khách đưa không đủ.");
                return;
            }
            tienThua = tienKhachDua.subtract(tong);
        }

        // Tính điểm tích (1 điểm / 10.000đ) cho khách thành viên
        int diemTang = 0;
        boolean khThanhVien = khachHang != null && khachHang.getMaKhachHang() > 0;
        if (khThanhVien) {
            diemTang = tong.divideToIntegralValue(new BigDecimal(10000)).intValue();
        }

        HoaDon hd = new HoaDon();
        hd.setNgayLap(java.util.Date.from(LocalDateTime.now().atZone(java.time.ZoneId.systemDefault()).toInstant()));
        hd.setNhanVien(nhanVien);
        hd.setKhachHang(khachHang);
        hd.setTongTien(tong);
        hd.setTienKhachDua(tienKhachDua);
        hd.setTienThua(tienThua);
        hd.setDiemDuocTang(diemTang);
        hd.setTrangThai("HOAN_TAT");
        hd.setHinhThucThanhToan(chuyenKhoan ? "CHUYEN_KHOAN" : "TIEN_MAT");

        Connection conn = null;
        boolean autoTruoc = true;
        try {
            conn = DatabaseConnection.getInstance().getConnection();
            autoTruoc = conn.getAutoCommit();
            conn.setAutoCommit(false);

            int maHD = hoaDonDAO.taoHoaDon(conn, hd);
            if (maHD < 0) throw new IllegalStateException("Không tạo được hóa đơn");
            hd.setMaHoaDon(maHD);

            for (int i = 0; i < gioHang.size(); i++) {
                Object[] d = gioHang.get(i);
                int maSP = (int) d[5];
                int sl = (int) d[3];

                if (!sanPhamDAO.truTonKho(conn, maSP, sl)) {
                    throw new IllegalStateException("Sản phẩm không đủ tồn kho: " + d[0]);
                }

                ChiTietHoaDon ct = new ChiTietHoaDon();
                ct.setHoaDon(hd);
                SanPham sp = new SanPham();
                sp.setMaSanPham(maSP);
                ct.setSanPham(sp);
                ct.setSoLuong(sl);
                ct.setDonGia((BigDecimal) d[2]);
                chiTietDAO.them(conn, ct);
            }

            if (khThanhVien) {
                khachHangDAO.congDiem(conn, khachHang.getMaKhachHang(), diemTang, tong);
            }

            conn.commit();
        } catch (Exception ex) {
            if (conn != null) {
                try { conn.rollback(); } catch (Exception ignore) { }
            }
            JOptionPane.showMessageDialog(this, "Thanh toán thất bại: " + ex.getMessage(),
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(autoTruoc); } catch (Exception ignore) { }
                try { conn.close(); } catch (Exception ignore) { }
            }
        }

        // Thành công → snapshot giỏ hàng (trước khi xóa) rồi mở bill
        List<Object[]> snapshot = new ArrayList<>();
        for (Object[] d : gioHang) snapshot.add(d.clone());

        gioHang.clear();
        modelGioHang.setRowCount(0);
        khachHang = new KhachHang();
        lblKhachHang.setText("Khách lẻ");
        tongCong();
        taiDuLieu(); // nạp lại tồn kho

        java.awt.Window owner = javax.swing.SwingUtilities.getWindowAncestor(this);
        frmInHoaDon bill = new frmInHoaDon(owner, hd, snapshot);
        bill.setVisible(true);
    }
}
