package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.ChiTietHoaDonDAO;
import dao.HoaDonDAO;
import dao.KhachHangDAO;
import dao.NhanVienDAO;
import dao.SanPhamDAO;
import entities.ChiTietHoaDon;
import entities.HoaDon;
import entities.KhachHang;
import entities.NhanVien;
import entities.SanPham;

/** Quản lý hóa đơn: danh sách theo ngày, xem chi tiết. */
public class frmQuanLyHoaDon extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();
    private final NhanVienDAO nhanVienDAO = new NhanVienDAO();
    private final KhachHangDAO khachHangDAO = new KhachHangDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
    private DefaultTableModel model;
    private JTable bang;
    private List<HoaDon> dsHienTai;
    private final java.text.SimpleDateFormat fmtDay = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final Map<Integer, String> tenNhanVien = new HashMap<>();
    private final Map<Integer, String> tenKhachHang = new HashMap<>();
    private final Map<Integer, String> tenSanPham = new HashMap<>();

    public frmQuanLyHoaDon() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel lblTieuDe = new JLabel("QUẢN LÝ HÓA ĐƠN");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        DateTimeFormatter fmtNgay = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        txtTuNgay = new JTextField(fmtNgay.format(java.time.LocalDate.now().minusDays(7)), 10);
        txtDenNgay = new JTextField(fmtNgay.format(java.time.LocalDate.now()), 10);

        JButton btnLoc = new JButton("Lọc");
        btnLoc.setBackground(new Color(0, 102, 51));
        btnLoc.setForeground(Color.WHITE);
        btnLoc.setFocusPainted(false);
        btnLoc.addActionListener(e -> taiDuLieu());

        JButton btnXemTatCa = new JButton("Xem tất cả");
        btnXemTatCa.addActionListener(e -> {
            txtTuNgay.setText("");
            txtDenNgay.setText("");
            hienThi(hoaDonDAO.dsTatCaHoaDon());
        });

        JButton btnChiTiet = new JButton("Xem chi tiết");
        btnChiTiet.setBackground(new Color(0, 90, 160));
        btnChiTiet.setForeground(Color.WHITE);
        btnChiTiet.setFocusPainted(false);
        btnChiTiet.addActionListener(e -> xemChiTiet());

        JPanel pnlTren = new JPanel(new BorderLayout(10, 0));
        pnlTren.setOpaque(false);
        pnlTren.add(lblTieuDe, BorderLayout.WEST);

        JPanel pnlLoc = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
        pnlLoc.setOpaque(false);
        pnlLoc.add(new JLabel("Từ ngày (dd/MM/yyyy):"));
        pnlLoc.add(txtTuNgay);
        pnlLoc.add(new JLabel("Đến:"));
        pnlLoc.add(txtDenNgay);
        pnlLoc.add(btnLoc);
        pnlLoc.add(btnXemTatCa);
        pnlTren.add(pnlLoc, BorderLayout.CENTER);

        JPanel pnlNut = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlNut.setOpaque(false);
        pnlNut.add(btnChiTiet);
        pnlTren.add(pnlNut, BorderLayout.EAST);

        model = new DefaultTableModel(
                new Object[]{"Mã HĐ", "Thời gian", "Nhân viên", "Khách hàng", "Tổng tiền", "Trả", "Thừa"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        bang = new JTable(model);
        bang.setRowHeight(26);
        bang.getColumnModel().getColumn(0).setMaxWidth(70);
        JScrollPane scr = new JScrollPane(bang);
        scr.setBorder(BorderFactory.createTitledBorder("Danh sách hóa đơn"));

        add(pnlTren, BorderLayout.NORTH);
        add(scr, BorderLayout.CENTER);
    }

    /** Nạp dữ liệu (gọi khi vào màn). */
    public void taiDuLieu() {
        try {
            Date tu = txtTuNgay.getText().trim().isEmpty() ? null
                    : Date.valueOf(parse(txtTuNgay.getText().trim()));
            Date den = txtDenNgay.getText().trim().isEmpty() ? null
                    : Date.valueOf(parse(txtDenNgay.getText().trim()));
            if (tu == null || den == null) {
                hienThi(hoaDonDAO.dsTatCaHoaDon());
                return;
            }
            if (tu.after(den)) {
                JOptionPane.showMessageDialog(this, "Từ ngày phải trước đến ngày.");
                return;
            }
            hienThi(hoaDonDAO.dsHoaDonTheoNgay(tu, den));
        } catch (java.time.format.DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this, "Sai định dạng ngày. Dùng dd/MM/yyyy.",
                    "Lỗi định dạng", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** dd/MM/yyyy → LocalDate, hỗ trợ cả yyyy-MM-dd. */
    private java.time.LocalDate parse(String s) {
        String[] parts = s.split("/");
        if (parts.length == 3) {
            return java.time.LocalDate.of(Integer.parseInt(parts[2]),
                    Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
        }
        return java.time.LocalDate.parse(s);
    }

    /** Nạp sẵn tên nhân viên / khách hàng / sản phẩm để hiển thị thay cho mã. */
    private void napTen() {
        tenNhanVien.clear();
        for (NhanVien nv : nhanVienDAO.getAllNhanVien()) {
            tenNhanVien.put(nv.getMaNV(), (nv.getHo() + " " + nv.getTen()).trim());
        }
        tenKhachHang.clear();
        for (KhachHang kh : khachHangDAO.getAll()) {
            tenKhachHang.put(kh.getMaKH(), kh.getTenKH());
        }
        tenSanPham.clear();
        for (SanPham sp : sanPhamDAO.getAll()) {
            tenSanPham.put(sp.getMaSP(), sp.getTenSP());
        }
    }

    private void hienThi(List<HoaDon> ds) {
        dsHienTai = ds;
        napTen();
        Map<Integer, java.math.BigDecimal> tongTien = hoaDonDAO.tongTienTheoHoaDon(ds);
        model.setRowCount(0);
        for (HoaDon hd : ds) {
            java.math.BigDecimal tong = tongTien.getOrDefault(hd.getMaHD(), java.math.BigDecimal.ZERO);
            model.addRow(new Object[]{
                    hd.getMaHD(),
                    hd.getNgayLap() == null ? "" : fmtDay.format(hd.getNgayLap()),
                    tenNhanVien.getOrDefault(hd.getMaNV(), ""),
                    (hd.getMaKH() != null && hd.getMaKH() > 0) ? tenKhachHang.getOrDefault(hd.getMaKH(), "Khách lẻ") : "Khách lẻ",
                    String.format("%,.0f", tong),
                    String.format("%,.0f", hd.getTienKhachDua() == null ? java.math.BigDecimal.ZERO : hd.getTienKhachDua()),
                    String.format("%,.0f", hd.getTienThua() == null ? java.math.BigDecimal.ZERO : hd.getTienThua())
            });
        }
    }

    private void xemChiTiet() {
        int row = bangDangChon();
        if (row < 0) return;
        HoaDon hd = dsHienTai.get(row);
        List<ChiTietHoaDon> ds = chiTietDAO.getChiTietByMaHD(hd.getMaHD());

        StringBuilder sb = new StringBuilder();
        sb.append("HÓA ĐƠN #").append(hd.getMaHD()).append("\n");
        sb.append("Thời gian: ")
          .append(hd.getNgayLap() == null ? "" : fmtDay.format(hd.getNgayLap()))
          .append("\n");
        sb.append("Nhân viên: ").append(tenNhanVien.getOrDefault(hd.getMaNV(), "?")).append("\n");        sb.append("Khách: ").append((hd.getMaKH() != null && hd.getMaKH() > 0) ? tenKhachHang.getOrDefault(hd.getMaKH(), "Khách lẻ") : "Khách lẻ").append("\n");
        sb.append("----------------------------------------\n");
        java.math.BigDecimal tong = java.math.BigDecimal.ZERO;
        for (ChiTietHoaDon ct : ds) {
            tong = tong.add(ct.getThanhTien());
            sb.append(String.format("%-25s x%-3d %,.0f%n",
                    tenSanPham.getOrDefault(ct.getMaSP(), "SP #" + ct.getMaSP()),
                    ct.getSoLuong(), ct.getThanhTien()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("TỔNG CỘNG: %,.0f đ%n", tong));
        sb.append(String.format("Trả: %,.0f đ  Thừa: %,.0f đ",
                hd.getTienKhachDua() == null ? java.math.BigDecimal.ZERO : hd.getTienKhachDua(),
                hd.getTienThua() == null ? java.math.BigDecimal.ZERO : hd.getTienThua()));

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Chi tiết hóa đơn #" + hd.getMaHD(), JOptionPane.INFORMATION_MESSAGE);
    }

    private int bangDangChon() {
        int row = bang.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Chọn 1 hóa đơn trước.");
            return -1;
        }
        return row;
    }
}
