package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.LoaiSanPhamDAO;
import dao.SanPhamDAO;
import entities.LoaiSanPham;
import entities.SanPham;

/** Quản lý sản phẩm: thêm/sửa/xóa, tìm theo mã vạch/tên, cảnh báo tồn thấp. */
public class QuanLySanPham extends JPanel {
    private static final long serialVersionUID = 1L;

    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();
    private final LoaiSanPhamDAO loaiDAO = new LoaiSanPhamDAO();

    private JTextField txtTimKiem;
    private DefaultTableModel model;
    private JTable bang;
    private List<SanPham> dsHienTai;
    private final Map<Integer, String> tenLoai = new HashMap<>();

    public QuanLySanPham() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel lblTieuDe = new JLabel("QUẢN LÝ SẢN PHẨM");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        txtTimKiem = new JTextField(20);
        txtTimKiem.setToolTipText("Nhập mã vạch hoặc tên sản phẩm");
        JButton btnTim = new JButton("Tìm");
        btnTim.addActionListener(e -> taiDuLieu(txtTimKiem.getText().trim()));
        txtTimKiem.addKeyListener(new java.awt.event.KeyAdapter() {
            @Override
            public void keyPressed(java.awt.event.KeyEvent e) {
                if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                    taiDuLieu(txtTimKiem.getText().trim());
                }
            }
        });

        JButton btnThem = taoNut("Thêm mới", new Color(0, 102, 51));
        btnThem.addActionListener(e -> moForm(null));
        JButton btnSua = taoNut("Sửa", new Color(0, 90, 160));
        btnSua.addActionListener(e -> {
            SanPham sp = sanPhamDaChon();
            if (sp != null) moForm(sp);
        });
        JButton btnXoa = taoNut("Xóa", new Color(180, 40, 40));
        btnXoa.addActionListener(e -> xoaSanPham());

        JPanel pnlTren = new JPanel(new BorderLayout(10, 0));
        pnlTren.setOpaque(false);
        pnlTren.add(lblTieuDe, BorderLayout.WEST);

        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pnlTim.setOpaque(false);
        pnlTim.add(new JLabel("Tìm:"));
        pnlTim.add(txtTimKiem);
        pnlTim.add(btnTim);
        pnlTren.add(pnlTim, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlNut.setOpaque(false);
        pnlNut.add(btnThem);
        pnlNut.add(btnSua);
        pnlNut.add(btnXoa);
        pnlTren.add(pnlNut, BorderLayout.EAST);

        model = new DefaultTableModel(
                new Object[]{"Mã SP", "Mã vạch", "Tên sản phẩm", "Loại", "ĐVT",
                        "Giá bán", "Giá vốn", "Tồn", "Ngưỡng", "Trạng thái"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bang = new JTable(model);
        bang.setRowHeight(26);
        bang.getColumnModel().getColumn(0).setMaxWidth(60);

        JScrollPane scr = new JScrollPane(bang);
        scr.setBorder(BorderFactory.createTitledBorder("Danh sách sản phẩm"));

        add(pnlTren, BorderLayout.NORTH);
        add(scr, BorderLayout.CENTER);
    }

    private JButton taoNut(String text, Color bg) {
        JButton nut = new JButton(text);
        nut.setBackground(bg);
        nut.setForeground(Color.WHITE);
        nut.setFocusPainted(false);
        nut.setBorderPainted(false);
        return nut;
    }

    /** Nạp lại danh sách (gọi khi vào màn). */
    public void taiDuLieu() {
        taiDuLieu(txtTimKiem == null ? "" : txtTimKiem.getText().trim());
    }

    public void taiDuLieu(String tuKhoa) {
        // Tra tên loại theo maLoai (sơ đồ dùng khóa ngoại int)
        tenLoai.clear();
        try {
            for (LoaiSanPham l : loaiDAO.getAllLoaiSanPham()) {
                tenLoai.put(l.getMaLoai(), l.getTenLoai());
            }
        } catch (Exception ex) {
            System.err.println("Lỗi tải loại sản phẩm: " + ex.getMessage());
        }

        dsHienTai = tuKhoa.isEmpty() ? sanPhamDAO.getAll() : sanPhamDAO.timKiem(tuKhoa);
        model.setRowCount(0);
        for (SanPham sp : dsHienTai) {
            model.addRow(new Object[]{
                    sp.getMaSP(),
                    sp.getMaVach(),
                    sp.getTenSP(),
                    tenLoai.getOrDefault(sp.getMaLoai(), ""),
                    sp.getDonViTinh(),
                    String.format("%,.0f", sp.getGiaBan()),
                    String.format("%,.0f", sp.getGiaVon()),
                    sp.getTonKho(),
                    sp.getTonToiThieu(),
                    sp.isSapHet() ? "SẮP HẾT" : sp.getTrangThai()
            });
        }
    }

    private SanPham sanPhamDaChon() {
        int row = bang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 sản phẩm trong danh sách trước.");
            return null;
        }
        return dsHienTai.get(row);
    }

    private void xoaSanPham() {
        SanPham sp = sanPhamDaChon();
        if (sp == null) return;
        int chon = JOptionPane.showConfirmDialog(this,
                "Xóa sản phẩm \"" + sp.getTenSP() + "\"?\n(Lưu ý: sản phẩm đã phát sinh hóa đơn sẽ không xóa được)",
                "Xác nhận xóa", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (chon != JOptionPane.YES_OPTION) return;
        boolean ok = sanPhamDAO.xoa(sp.getMaSP());
        if (ok) {
            JOptionPane.showMessageDialog(this, "Đã xóa.");
            taiDuLieu();
        } else {
            JOptionPane.showMessageDialog(this,
                    "Không xóa được (sản phẩm có thể đã phát sinh trong hóa đơn/phiếu nhập).",
                    "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Dialog thêm/sửa sản phẩm. sp = null → thêm mới. */
    private void moForm(SanPham sp) {
        JDialog dialog = new JDialog(SwingUtilitiesWindow(), sp == null ? "Thêm sản phẩm" : "Sửa sản phẩm");
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.setSize(420, 460);
        dialog.setLocationRelativeTo(this);

        JPanel form = new JPanel(new GridLayout(0, 2, 8, 10));
        form.setBorder(new EmptyBorder(15, 20, 15, 20));

        JTextField txtMaVach = new JTextField(sp == null ? "" : sp.getMaVach());
        JTextField txtTen = new JTextField(sp == null ? "" : sp.getTenSP());
        JComboBox<LoaiSanPham> cbLoai = new JComboBox<>();
        try {
            for (LoaiSanPham l : loaiDAO.getAllLoaiSanPham()) cbLoai.addItem(l);
        } catch (Exception ex) {
            System.err.println("Lỗi tải loại sản phẩm: " + ex.getMessage());
        }
        if (sp != null && sp.getMaLoai() > 0) {
            for (int i = 0; i < cbLoai.getItemCount(); i++) {
                if (cbLoai.getItemAt(i).getMaLoai() == sp.getMaLoai()) {
                    cbLoai.setSelectedIndex(i);
                    break;
                }
            }
        }
        String[] donVi = {"Cái", "Gói", "Chai", "Hộp", "Lon", "Thùng", "Kg"};
        JComboBox<String> cbDonVi = new JComboBox<>(donVi);
        if (sp != null && sp.getDonViTinh() != null) cbDonVi.setSelectedItem(sp.getDonViTinh());
        JTextField txtGiaBan = new JTextField(sp == null ? "" : String.valueOf(sp.getGiaBan()));
        JTextField txtGiaVon = new JTextField(sp == null ? "0" : String.valueOf(sp.getGiaVon()));
        JTextField txtTon = new JTextField(sp == null ? "0" : String.valueOf(sp.getTonKho()));
        JTextField txtNguong = new JTextField(sp == null ? "5" : String.valueOf(sp.getTonToiThieu()));
        String[] trangThai = {"DANG_BAN", "NGUNG_BAN"};
        JComboBox<String> cbTrangThai = new JComboBox<>(trangThai);
        if (sp != null) cbTrangThai.setSelectedItem(sp.getTrangThai());

        form.add(new JLabel("Mã vạch *"));
        form.add(txtMaVach);
        form.add(new JLabel("Tên sản phẩm *"));
        form.add(txtTen);
        form.add(new JLabel("Loại"));
        form.add(cbLoai);
        form.add(new JLabel("Đơn vị tính"));
        form.add(cbDonVi);
        form.add(new JLabel("Giá bán *"));
        form.add(txtGiaBan);
        form.add(new JLabel("Giá vốn"));
        form.add(txtGiaVon);
        form.add(new JLabel("Tồn kho"));
        form.add(txtTon);
        form.add(new JLabel("Ngưỡng cảnh báo"));
        form.add(txtNguong);
        form.add(new JLabel("Trạng thái"));
        form.add(cbTrangThai);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        pnlNut.add(btnHuy);
        pnlNut.add(btnLuu);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(pnlNut, BorderLayout.SOUTH);

        btnHuy.addActionListener(e -> dialog.dispose());
        btnLuu.addActionListener(e -> {
            try {
                String maVach = txtMaVach.getText().trim();
                String ten = txtTen.getText().trim();
                if (maVach.isEmpty() || ten.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, "Mã vạch và tên sản phẩm là bắt buộc.");
                    return;
                }
                java.math.BigDecimal giaBan = new java.math.BigDecimal(txtGiaBan.getText().trim());
                String giaVonStr = txtGiaVon.getText().trim();
                java.math.BigDecimal giaVon = giaVonStr.isEmpty()
                        ? java.math.BigDecimal.ZERO : new java.math.BigDecimal(giaVonStr);
                int nguong = Integer.parseInt(txtNguong.getText().trim());
                int ton = Integer.parseInt(txtTon.getText().trim());

                SanPham formSP = new SanPham();
                formSP.setMaSP(sp == null ? 0 : sp.getMaSP());
                formSP.setMaVach(maVach);
                formSP.setTenSP(ten);
                LoaiSanPham loaiChon = (LoaiSanPham) cbLoai.getSelectedItem();
                formSP.setMaLoai(loaiChon == null ? null : loaiChon.getMaLoai());
                formSP.setDonViTinh((String) cbDonVi.getSelectedItem());
                formSP.setGiaBan(giaBan);
                formSP.setGiaVon(giaVon);
                formSP.setTonKho(ton);
                formSP.setTonToiThieu(nguong);
                formSP.setTrangThai((String) cbTrangThai.getSelectedItem());

                boolean ok;
                if (sp == null) {
                    ok = sanPhamDAO.them(formSP);
                } else {
                    // Không sửa tồn kho qua form (tồn chỉ đổi qua bán/nhập)
                    formSP.setTonKho(sp.getTonKho());
                    formSP.setHinhAnh(sp.getHinhAnh());
                    ok = sanPhamDAO.sua(formSP);
                }
                if (ok) {
                    JOptionPane.showMessageDialog(dialog, "Lưu thành công!");
                    dialog.dispose();
                    taiDuLieu();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Lưu thất bại (kiểm tra mã vạch đã tồn tại?).",
                            "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Dữ liệu số không hợp lệ: " + ex.getMessage());
            }
        });

        dialog.setVisible(true);
    }

    private java.awt.Window SwingUtilitiesWindow() {
        return javax.swing.SwingUtilities.getWindowAncestor(this);
    }
}
