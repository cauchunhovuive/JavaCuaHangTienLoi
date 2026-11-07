package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.NhaCungCapDAO;
import dao.PhieuNhapHangDAO;
import dao.SanPhamDAO;
import entities.ChiTietNhapHang;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.PhieuNhapHang;
import entities.SanPham;
import entities.TaiKhoan;

/** Màn nhập hàng: tạo phiếu nhập từ nhà cung cấp → cộng tồn kho, cập nhật giá vốn. */
public class frmNhapHang extends JPanel {
    private static final long serialVersionUID = 1L;

    private final TaiKhoan taiKhoan;
    private final NhanVien nhanVien;
    private final PhieuNhapHangDAO phieuDAO = new PhieuNhapHangDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final NhaCungCapDAO nccDAO = new NhaCungCapDAO();

    // Chi tiết phiếu nhập đang soạn: [TenSP, DonViTinh, SoLuong, DonGiaNhap, ThanhTien, MaSP, GiaVonHienTai]
    private final List<Object[]> chiTiet = new ArrayList<>();

    private JComboBox<NhaCungCap> cbNhaCungCap;
    private JComboBox<SanPham> cbSanPham;
    private JTextField txtSoLuong;
    private JTextField txtDonGia;
    private DefaultTableModel modelChiTiet;
    private DefaultTableModel modelPhieu;
    private JLabel lblTong;
    private JTextField txtGhiChu;
    private List<PhieuNhapHang> dsPhieu;

    public frmNhapHang(TaiKhoan tk, NhanVien nv) {
        this.taiKhoan = tk;
        this.nhanVien = nv;
        khoiTao();
    }

    private void khoiTao() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel lblTieuDe = new JLabel("NHẬP HÀNG");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        // ----- Thêm dòng chi tiết -----
        cbNhaCungCap = new JComboBox<>();
        cbSanPham = new JComboBox<>();
        txtSoLuong = new JTextField(5);
        txtDonGia = new JTextField(7);
        JButton btnThemDong = new JButton("Thêm dòng");
        btnThemDong.setBackground(new Color(0, 102, 51));
        btnThemDong.setForeground(Color.WHITE);
        btnThemDong.setFocusPainted(false);
        btnThemDong.addActionListener(e -> themDong());

        // Chọn NCC → combo sản phẩm chỉ hiện SP của NCC đó
        cbNhaCungCap.addActionListener(e -> locSanPhamTheoNCC());

        JPanel pnlThem = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlThem.setOpaque(false);
        pnlThem.add(new JLabel("Nhà cung cấp:"));
        pnlThem.add(cbNhaCungCap);
        pnlThem.add(new JLabel("Sản phẩm:"));
        pnlThem.add(cbSanPham);
        pnlThem.add(new JLabel("SL:"));
        pnlThem.add(txtSoLuong);
        pnlThem.add(new JLabel("Giá nhập:"));
        pnlThem.add(txtDonGia);
        pnlThem.add(btnThemDong);

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setOpaque(false);
        pnlTren.add(lblTieuDe, BorderLayout.NORTH);
        pnlTren.add(pnlThem, BorderLayout.CENTER);

        // ----- Bảng chi tiết phiếu -----
        modelChiTiet = new DefaultTableModel(
                new Object[]{"Tên sản phẩm", "ĐVT", "SL", "Giá nhập", "Thành tiền", "MãSP"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable bangChiTiet = new JTable(modelChiTiet);
        bangChiTiet.setRowHeight(24);
        bangChiTiet.getColumnModel().getColumn(5).setMaxWidth(0);
        bangChiTiet.getColumnModel().getColumn(5).setMinWidth(0);
        JScrollPane scrChiTiet = new JScrollPane(bangChiTiet);
        scrChiTiet.setBorder(BorderFactory.createTitledBorder("Chi tiết phiếu nhập"));

        JButton btnXoaDong = new JButton("Xóa dòng đã chọn");
        btnXoaDong.addActionListener(e -> {
            int row = bangChiTiet.getSelectedRow();
            if (row < 0) {
                JOptionPane.showMessageDialog(this, "Chọn dòng cần xóa.");
                return;
            }
            chiTiet.remove(row);
            modelChiTiet.removeRow(row);
            tongCong();
        });

        lblTong = new JLabel("0 đ");
        lblTong.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTong.setForeground(new Color(180, 60, 0));

        txtGhiChu = new JTextField(20);

        JButton btnTaoPhieu = new JButton("LẬP PHIẾU NHẬP");
        btnTaoPhieu.setBackground(new Color(230, 92, 0));
        btnTaoPhieu.setForeground(Color.WHITE);
        btnTaoPhieu.setFont(new Font("SansSerif", Font.BOLD, 14));
        btnTaoPhieu.setFocusPainted(false);
        btnTaoPhieu.addActionListener(e -> taoPhieuNhap());

        JButton btnXoaGiua = new JButton("Dọn phiếu đang soạn");
        btnXoaGiua.addActionListener(e -> donPhieu());

        JPanel pnlThong = new JPanel(new BorderLayout(10, 0));
        pnlThong.setOpaque(false);
        JPanel pnlTrai = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        pnlTrai.setOpaque(false);
        pnlTrai.add(btnXoaDong);
        pnlTrai.add(btnXoaGiua);
        pnlTrai.add(new JLabel("Ghi chú:"));
        pnlTrai.add(txtGhiChu);
        JPanel pnlPhai = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlPhai.setOpaque(false);
        pnlPhai.add(lblTong);
        pnlPhai.add(btnTaoPhieu);
        pnlThong.add(pnlTrai, BorderLayout.WEST);
        pnlThong.add(pnlPhai, BorderLayout.EAST);

        JPanel pnlGiua = new JPanel(new BorderLayout(0, 6));
        pnlGiua.setOpaque(false);
        pnlGiua.add(scrChiTiet, BorderLayout.CENTER);
        pnlGiua.add(pnlThong, BorderLayout.SOUTH);

        // ----- Lịch sử phiếu nhập -----
        modelPhieu = new DefaultTableModel(
                new Object[]{"Mã phiếu", "Ngày nhập", "Nhà cung cấp", "Người nhập", "Tổng tiền", "Ghi chú"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable bangPhieu = new JTable(modelPhieu);
        bangPhieu.setRowHeight(24);
        JScrollPane scrPhieu = new JScrollPane(bangPhieu);
        scrPhieu.setBorder(BorderFactory.createTitledBorder("Lịch sử phiếu nhập (bấm đúp để xem chi tiết)"));
        bangPhieu.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2 && bangPhieu.getSelectedRow() >= 0) {
                    xemChiTiet(dsPhieu.get(bangPhieu.getSelectedRow()).getMaPhieuNhap());
                }
            }
        });

        JPanel pnlDuoi = new JPanel(new GridLayout(2, 1, 0, 8));
        pnlDuoi.setOpaque(false);
        pnlDuoi.add(pnlGiua);
        pnlDuoi.add(scrPhieu);

        add(pnlTren, BorderLayout.NORTH);
        add(pnlDuoi, BorderLayout.CENTER);
    }

    /** Nạp dữ liệu (gọi khi vào màn). */
    public void taiDuLieu() {
        // Nhà cung cấp
        cbNhaCungCap.removeAllItems();
        for (NhaCungCap ncc : nccDAO.getAllNhaCungCap()) {
            cbNhaCungCap.addItem(ncc);
        }
        locSanPhamTheoNCC();
        taiLichSuPhieu();
    }

    /** Lọc combo sản phẩm theo nhà cung cấp đang chọn. */
    private void locSanPhamTheoNCC() {
        cbSanPham.removeAllItems();
        NhaCungCap ncc = (NhaCungCap) cbNhaCungCap.getSelectedItem();
        List<SanPham> ds = ncc == null ? new ArrayList<>() : sanPhamDAO.getTheoNhaCungCap(ncc.getMaNCC());
        for (SanPham sp : ds) {
            cbSanPham.addItem(sp);
        }
    }

    private void taiLichSuPhieu() {
        modelPhieu.setRowCount(0);
        dsPhieu = phieuDAO.dsPhieuNhap();
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        for (PhieuNhapHang pn : dsPhieu) {
            modelPhieu.addRow(new Object[]{
                    pn.getMaPhieuNhap(),
                    fmt.format(pn.getNgayNhap()),
                    pn.getNhaCungCap() == null ? "" : pn.getNhaCungCap().getTenNCC(),
                    pn.getNhanVien() == null ? "" : pn.getNhanVien().getTen(),
                    String.format("%,.0f", pn.getTongTienNhap()),
                    pn.getGhiChu() == null ? "" : pn.getGhiChu()
            });
        }
    }

    private void themDong() {
        NhaCungCap ncc = (NhaCungCap) cbNhaCungCap.getSelectedItem();
        SanPham sp = (SanPham) cbSanPham.getSelectedItem();
        if (ncc == null) {
            JOptionPane.showMessageDialog(this, "Chưa có nhà cung cấp. Hãy thêm ở màn Nhà cung cấp.");
            return;
        }
        if (sp == null) {
            JOptionPane.showMessageDialog(this, "Chọn sản phẩm cần nhập.");
            return;
        }
        int sl;
        BigDecimal giaNhap;
        try {
            sl = Integer.parseInt(txtSoLuong.getText().trim());
            giaNhap = new BigDecimal(txtDonGia.getText().trim().replace(",", ""));
            if (sl <= 0) throw new NumberFormatException("SL phải > 0");
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Số lượng / giá nhập không hợp lệ.");
            return;
        }

        // Cập nhật dòng đã có (cộng dồn) hoặc thêm mới
        for (int i = 0; i < chiTiet.size(); i++) {
            if ((int) chiTiet.get(i)[5] == sp.getMaSanPham()) {
                chiTiet.get(i)[2] = (int) chiTiet.get(i)[2] + sl;
                chiTiet.get(i)[3] = giaNhap;
                capNhatThanhTien(i);
                modelChiTiet.fireTableRowsUpdated(i, i);
                tongCong();
                txtSoLuong.setText("");
                txtDonGia.setText("");
                return;
            }
        }
        BigDecimal thanhTien = giaNhap.multiply(BigDecimal.valueOf(sl));
        chiTiet.add(new Object[]{ sp.getTenSanPham(), sp.getDonViTinh(), sl, giaNhap, thanhTien, sp.getMaSanPham() });
        modelChiTiet.addRow(new Object[]{ sp.getTenSanPham(), sp.getDonViTinh(), sl,
                String.format("%,.0f", giaNhap), String.format("%,.0f", thanhTien), sp.getMaSanPham() });
        tongCong();
        txtSoLuong.setText("");
        txtDonGia.setText("");
    }

    private void capNhatThanhTien(int row) {
        Object[] d = chiTiet.get(row);
        BigDecimal thanhTien = ((BigDecimal) d[3]).multiply(BigDecimal.valueOf((int) d[2]));
        d[4] = thanhTien;
        modelChiTiet.setValueAt(String.format("%,.0f", thanhTien), row, 4);
    }

    private BigDecimal tongTien() {
        BigDecimal tong = BigDecimal.ZERO;
        for (Object[] d : chiTiet) tong = tong.add((BigDecimal) d[4]);
        return tong;
    }

    private void tongCong() {
        lblTong.setText(String.format("Tổng: %,.0f đ", tongTien()));
    }

    private void donPhieu() {
        chiTiet.clear();
        modelChiTiet.setRowCount(0);
        txtGhiChu.setText("");
        tongCong();
    }

    private void taoPhieuNhap() {
        if (chiTiet.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Chưa có dòng hàng nào.");
            return;
        }
        NhaCungCap ncc = (NhaCungCap) cbNhaCungCap.getSelectedItem();
        if (ncc == null) {
            JOptionPane.showMessageDialog(this, "Chọn nhà cung cấp.");
            return;
        }
        if (nhanVien == null) {
            JOptionPane.showMessageDialog(this, "Tài khoản chưa gắn nhân viên. Liên hệ quản lý.",
                    "Không thể nhập", JOptionPane.ERROR_MESSAGE);
            return;
        }

        int chon = JOptionPane.showConfirmDialog(this,
                String.format("Lập phiếu nhập tổng %,.0f đ từ \"%s\"?\nTồn kho sẽ được cộng ngay.",
                        tongTien(), ncc.getTenNCC()),
                "Xác nhận nhập hàng", JOptionPane.YES_NO_OPTION);
        if (chon != JOptionPane.YES_OPTION) return;

        PhieuNhapHang pn = new PhieuNhapHang();
        pn.setNgayNhap(java.util.Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant()));
        pn.setNhaCungCap(ncc);
        pn.setNhanVien(nhanVien);
        pn.setTongTienNhap(tongTien());
        pn.setGhiChu(txtGhiChu.getText().trim());

        List<ChiTietNhapHang> dsCT = new ArrayList<>();
        for (Object[] d : chiTiet) {
            ChiTietNhapHang ct = new ChiTietNhapHang();
            PhieuNhapHang pnRef = new PhieuNhapHang();
            pnRef.setMaPhieuNhap(0);
            ct.setPhieuNhap(pnRef);
            SanPham sp = new SanPham();
            sp.setMaSanPham((int) d[5]);
            ct.setSanPham(sp);
            ct.setSoLuong((int) d[2]);
            ct.setDonGiaNhap((BigDecimal) d[3]);
            dsCT.add(ct);
        }

        int maPN = phieuDAO.taoPhieuNhap(pn, dsCT);
        if (maPN < 0) {
            JOptionPane.showMessageDialog(this, "Lập phiếu thất bại. Đã hủy toàn bộ thay đổi.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this, "Đã lập phiếu nhập #" + maPN + " thành công!");
        donPhieu();
        taiDuLieu();
    }

    private void xemChiTiet(int maPN) {
        List<ChiTietNhapHang> ds = phieuDAO.getChiTietByMaPN(maPN);
        StringBuilder sb = new StringBuilder("CHI TIẾT PHIẾU NHẬP #" + maPN + "\n\n");
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        PhieuNhapHang pn = phieuDAO.getByMaPN(maPN);
        if (pn != null) {
            sb.append("Ngày: ").append(fmt.format(pn.getNgayNhap())).append("\n");
            sb.append("Nhà cung cấp: ")
              .append(pn.getNhaCungCap() == null ? "?" : pn.getNhaCungCap().getTenNCC()).append("\n\n");
        }
        for (ChiTietNhapHang ct : ds) {
            sb.append(String.format("- %s x %d @ %,.0f = %,.0f đ%n",
                    ct.getSanPham().getTenSanPham(), ct.getSoLuong(),
                    ct.getDonGiaNhap(), ct.getThanhTien()));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }
}
