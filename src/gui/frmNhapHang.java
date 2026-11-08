package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import dao.NhanVienDAO;
import dao.PhieuNhapHangDAO;
import dao.SanPhamDAO;
import entities.ChiTietNhapHang;
import entities.NhaCungCap;
import entities.NhanVien;
import entities.PhieuNhapHang;
import entities.SanPham;
import entities.TaiKhoan;

public class frmNhapHang extends JPanel {
    private static final long serialVersionUID = 1L;

    private final TaiKhoan taiKhoan;
    private final NhanVien nhanVien;
    private final PhieuNhapHangDAO phieuDAO = new PhieuNhapHangDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final NhaCungCapDAO nccDAO = new NhaCungCapDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();

    private final List<Object[]> chiTiet = new ArrayList<>();

    private JComboBox<NhaCungCap> cbNhaCungCap;
    private JComboBox<SanPham> cbSanPham;
    private JTextField txtSoLuong;
    private JTextField txtDonGia;
    private JTextField txtGhiChu;
    private DefaultTableModel modelChiTiet;
    private DefaultTableModel modelPhieu;
    private JLabel lblTong;
    private List<PhieuNhapHang> dsPhieu;

    private final Map<Integer, String> tenNCC = new HashMap<>();
    private final Map<Integer, String> tenNhanVien = new HashMap<>();
    private final Map<Integer, String> tenSanPham = new HashMap<>();

    private static final java.text.SimpleDateFormat FMT_NGAY = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

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

        cbNhaCungCap = new JComboBox<>();
        cbSanPham = new JComboBox<>();
        txtSoLuong = new JTextField(5);
        txtDonGia = new JTextField(7);
        JButton btnThemDong = new JButton("Thêm dòng");
        btnThemDong.setBackground(new Color(0, 102, 51));
        btnThemDong.setForeground(Color.WHITE);
        btnThemDong.setFocusPainted(false);
        btnThemDong.addActionListener(e -> themDong());

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
                new Object[]{"Mã phiếu", "Ngày nhập", "Nhà cung cấp", "Người nhập", "Tổng tiền", "Ghi chú", "Trạng thái"}, 0) {
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
                    xemChiTiet(dsPhieu.get(bangPhieu.getSelectedRow()).getMaPN());
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
        napTen();
        cbNhaCungCap.removeAllItems();
        for (NhaCungCap ncc : nccDAO.getAllNhaCungCap()) {
            cbNhaCungCap.addItem(ncc);
        }
        locSanPhamTheoNCC();
        taiLichSuPhieu();
    }

    /** Nạp tên NCC / nhân viên / sản phẩm để hiển thị thay cho mã. */
    private void napTen() {
        tenNCC.clear();
        for (NhaCungCap ncc : nccDAO.getAllNhaCungCap()) tenNCC.put(ncc.getMaNCC(), ncc.getTenNCC());
        tenNhanVien.clear();
        for (NhanVien nv : nhanVienDAO.getAllNhanVien()) {
            tenNhanVien.put(nv.getMaNV(), (nv.getHo() + " " + nv.getTen()).trim());
        }
        tenSanPham.clear();
        for (SanPham sp : sanPhamDAO.getAll()) tenSanPham.put(sp.getMaSP(), sp.getTenSP());
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
        for (PhieuNhapHang pn : dsPhieu) {
            modelPhieu.addRow(new Object[]{
                    pn.getMaPN(),
                    pn.getNgayNhap() == null ? "" : FMT_NGAY.format(pn.getNgayNhap()),
                    pn.getMaNCC() == null ? "" : tenNCC.getOrDefault(pn.getMaNCC(), ""),
                    pn.getMaNV() == null ? "" : tenNhanVien.getOrDefault(pn.getMaNV(), ""),
                    String.format("%,.0f", pn.getTongTien() == null ? BigDecimal.ZERO : pn.getTongTien()),
                    pn.getGhiChu() == null ? "" : pn.getGhiChu(),
                    pn.getTrangThai() == null ? "" : pn.getTrangThai()
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
            if ((int) chiTiet.get(i)[5] == sp.getMaSP()) {
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
        chiTiet.add(new Object[]{ sp.getTenSP(), sp.getDonViTinh(), sl, giaNhap, thanhTien, sp.getMaSP() });
        modelChiTiet.addRow(new Object[]{ sp.getTenSP(), sp.getDonViTinh(), sl,
                String.format("%,.0f", giaNhap), String.format("%,.0f", thanhTien), sp.getMaSP() });
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
        System.out.println("[frmNhapHang] taoPhieuNhap() called");
        if (chiTiet.isEmpty()) {
            System.out.println("[frmNhapHang] chiTiet.isEmpty() -> return");
            JOptionPane.showMessageDialog(this, "Chưa có dòng hàng nào.");
            return;
        }
        NhaCungCap ncc = (NhaCungCap) cbNhaCungCap.getSelectedItem();
        if (ncc == null) {
            System.out.println("[frmNhapHang] ncc == null -> return");
            JOptionPane.showMessageDialog(this, "Chọn nhà cung cấp.");
            return;
        }
        if (nhanVien == null) {
            System.out.println("[frmNhapHang] nhanVien == null -> return");
            JOptionPane.showMessageDialog(this, "Tài khoản chưa gắn nhân viên. Liên hệ quản lý.",
                    "Không thể nhập", JOptionPane.ERROR_MESSAGE);
            return;
        }

        System.out.println("[frmNhapHang] ncc=" + ncc.getMaNCC() + "/" + ncc.getTenNCC()
            + ", nhanVien=" + nhanVien.getMaNV()
            + ", tongTien=" + tongTien()
            + ", chiTiet size=" + chiTiet.size());

        int chon = JOptionPane.showConfirmDialog(this,
                String.format("Lập phiếu nhập tổng %,.0f đ từ \"%s\"?\nTồn kho sẽ được cộng ngay.",
                        tongTien(), ncc.getTenNCC()),
                "Xác nhận nhập hàng", JOptionPane.YES_NO_OPTION);
        System.out.println("[frmNhapHang] user selected: " + (chon == JOptionPane.YES_OPTION ? "YES" : "NO/CANCEL"));
        if (chon != JOptionPane.YES_OPTION) return;

        PhieuNhapHang pn = new PhieuNhapHang();
        pn.setNgayNhap(new Timestamp(System.currentTimeMillis()));
        pn.setMaNCC(ncc.getMaNCC());
        pn.setMaNV(nhanVien.getMaNV());
        pn.setTongTien(tongTien());
        pn.setGhiChu(txtGhiChu.getText().trim());
        pn.setTrangThai("HOAN_TAT");

        List<ChiTietNhapHang> dsCT = new ArrayList<>();
        for (Object[] d : chiTiet) {
            dsCT.add(new ChiTietNhapHang(0, (int) d[5], (int) d[2], (BigDecimal) d[3]));
        }

        try {
            System.out.println("[frmNhapHang] calling phieuDAO.taoPhieuNhap()...");
            int maPN = phieuDAO.taoPhieuNhap(pn, dsCT);
            System.out.println("[frmNhapHang] phieuDAO returned: " + maPN);
            if (maPN < 0) {
                JOptionPane.showMessageDialog(this, "Lập phiếu thất bại. Đã hủy toàn bộ thay đổi.",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
            JOptionPane.showMessageDialog(this, "Đã lập phiếu nhập #" + maPN + " thành công!");
            donPhieu();
            taiDuLieu();
        } catch (Exception e) {
            String msg = e.getMessage();
            if (msg == null || msg.isEmpty()) msg = e.getClass().getName();
            StringBuilder sb = new StringBuilder("Lập phiếu thất bại: " + msg + "\n\n");
            sb.append("Stack trace:\n");
            for (StackTraceElement ste : e.getStackTrace()) {
                sb.append(ste.toString()).append("\n");
            }
            JOptionPane.showMessageDialog(this, sb.toString(),
                "Lỗi", JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }

    private void xemChiTiet(int maPN) {
        List<ChiTietNhapHang> ds = phieuDAO.getChiTietByMaPN(maPN);
        StringBuilder sb = new StringBuilder("CHI TIẾT PHIẾU NHẬP #" + maPN + "\n\n");
        PhieuNhapHang pn = phieuDAO.getByMaPN(maPN);
        if (pn != null) {
            sb.append("Ngày: ").append(pn.getNgayNhap() == null ? "" : FMT_NGAY.format(pn.getNgayNhap())).append("\n");
            sb.append("Nhà cung cấp: ")
              .append(pn.getMaNCC() == null ? "?" : tenNCC.getOrDefault(pn.getMaNCC(), "?")).append("\n\n");
        }
        for (ChiTietNhapHang ct : ds) {
            sb.append(String.format("- %s x %d @ %,.0f = %,.0f đ%n",
                    tenSanPham.getOrDefault(ct.getMaSP(), "SP #" + ct.getMaSP()), ct.getSoLuong(),
                    ct.getDonGiaNhap(), ct.getThanhTien()));
        }
        JOptionPane.showMessageDialog(this, sb.toString());
    }
}
