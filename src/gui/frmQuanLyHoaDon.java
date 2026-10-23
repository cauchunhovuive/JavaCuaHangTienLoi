package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.List;

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
import entities.ChiTietHoaDon;
import entities.HoaDon;

/** Quản lý hóa đơn: danh sách theo ngày, xem chi tiết. */
public class frmQuanLyHoaDon extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final ChiTietHoaDonDAO chiTietDAO = new ChiTietHoaDonDAO();

    private JTextField txtTuNgay;
    private JTextField txtDenNgay;
    private DefaultTableModel model;
    private JTable bang;
    private List<HoaDon> dsHienTai;
    private final SimpleDateFormat fmt = new SimpleDateFormat("dd/MM/yyyy");
    private final SimpleDateFormat fmtDay = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    public frmQuanLyHoaDon() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel lblTieuDe = new JLabel("QUẢN LÝ HÓA ĐƠN");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        txtTuNgay = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(
                java.sql.Date.valueOf(java.time.LocalDate.now().minusDays(7))), 10);
        txtDenNgay = new JTextField(new SimpleDateFormat("dd/MM/yyyy").format(
                java.sql.Date.valueOf(java.time.LocalDate.now())), 10);

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

    private void hienThi(List<HoaDon> ds) {
        dsHienTai = ds;
        model.setRowCount(0);
        for (HoaDon hd : ds) {
            model.addRow(new Object[]{
                    hd.getMaHoaDon(),
                    fmtDay.format(hd.getNgayLap()),
                    hd.getNhanVien() == null ? "" : hd.getNhanVien().getTen(),
                    hd.getKhachHang() == null ? "Khách lẻ" : hd.getKhachHang().getTenKhachHang(),
                    String.format("%,.0f", hd.getTongTien()),
                    hd.getTienKhachDua() == null ? "" : String.format("%,.0f", hd.getTienKhachDua()),
                    hd.getTienThua() == null ? "" : String.format("%,.0f", hd.getTienThua())
            });
        }
    }

    private void xemChiTiet() {
        int row = bangDangChon();
        if (row < 0) return;
        HoaDon hd = dsHienTai.get(row);
        List<ChiTietHoaDon> ds = chiTietDAO.getChiTietByMaHD(hd.getMaHoaDon());

        StringBuilder sb = new StringBuilder();
        sb.append("HÓA ĐƠN #").append(hd.getMaHoaDon()).append("\n");
        sb.append("Thời gian: ").append(fmtDay.format(hd.getNgayLap())).append("\n");
        sb.append("Nhân viên: ").append(hd.getNhanVien() == null ? "?" : hd.getNhanVien().getTen()).append("\n");
        sb.append("Khách: ").append(hd.getKhachHang() == null ? "Khách lẻ" : hd.getKhachHang().getTenKhachHang()).append("\n");
        sb.append("----------------------------------------\n");
        for (ChiTietHoaDon ct : ds) {
            sb.append(String.format("%-25s x%-3d %,.0f%n",
                    ct.getSanPham().getTenSanPham(), ct.getSoLuong(), ct.getThanhTien()));
        }
        sb.append("----------------------------------------\n");
        sb.append(String.format("TỔNG CỘNG: %,.0f đ%n", hd.getTongTien()));
        sb.append(String.format("Trả: %,.0f đ  Thừa: %,.0f đ",
                hd.getTienKhachDua() == null ? BigDecimal.ZERO : hd.getTienKhachDua(),
                hd.getTienThua() == null ? BigDecimal.ZERO : hd.getTienThua()));

        JOptionPane.showMessageDialog(this, sb.toString(),
                "Chi tiết hóa đơn #" + hd.getMaHoaDon(), JOptionPane.INFORMATION_MESSAGE);
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
