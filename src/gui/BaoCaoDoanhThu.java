package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;

import dao.BaoCaoDAO;
import dao.HoaDonDAO;

/** Báo cáo doanh thu: theo ngày trong tháng, top sản phẩm, lợi nhuận. */
public class BaoCaoDoanhThu extends JPanel {
    private static final long serialVersionUID = 1L;

    private final BaoCaoDAO baoCaoDAO = new BaoCaoDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();

    private JComboBox<Integer> cbThang;
    private JComboBox<Integer> cbNam;
    private DefaultTableModel modelNgay;
    private DefaultTableModel modelTop;
    private JLabel lblDoanhThu;
    private JLabel lblLoiNhuan;
    private JLabel lblSoHoaDon;
    private final List<Object[]> duLieuBieuDo = new ArrayList<>();
    private BieuDoPanel bieuDo;

    public BaoCaoDoanhThu() {
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        JLabel lblTieuDe = new JLabel("BÁO CÁO DOANH THU");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        cbThang = new JComboBox<>();
        for (int m = 1; m <= 12; m++) cbThang.addItem(m);
        cbThang.setSelectedItem(LocalDate.now().getMonthValue());
        cbNam = new JComboBox<>();
        int nam = LocalDate.now().getYear();
        for (int y = nam - 4; y <= nam + 1; y++) cbNam.addItem(y);
        cbNam.setSelectedItem(nam);

        JButton btnXem = new JButton("Xem báo cáo");
        btnXem.setBackground(new Color(0, 102, 51));
        btnXem.setForeground(Color.WHITE);
        btnXem.setFocusPainted(false);
        btnXem.addActionListener(e -> taiDuLieu());

        JPanel pnlChon = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        pnlChon.setOpaque(false);
        pnlChon.add(new JLabel("Tháng:"));
        pnlChon.add(cbThang);
        pnlChon.add(new JLabel("Năm:"));
        pnlChon.add(cbNam);
        pnlChon.add(btnXem);

        JPanel pnlTren = new JPanel(new BorderLayout());
        pnlTren.setOpaque(false);
        pnlTren.add(lblTieuDe, BorderLayout.WEST);
        pnlTren.add(pnlChon, BorderLayout.CENTER);

        // 3 thẻ tổng
        JPanel pnlThe = new JPanel(new java.awt.GridLayout(1, 3, 12, 0));
        pnlThe.setOpaque(false);
        lblDoanhThu = taoThe(pnlThe, "Doanh thu tháng");
        lblLoiNhuan = taoThe(pnlThe, "Lợi nhuận gộp");
        lblSoHoaDon = taoThe(pnlThe, "Số hóa đơn");

        // Bảng doanh thu theo ngày
        modelNgay = new DefaultTableModel(
                new Object[]{"Ngày", "Số HĐ", "Doanh thu (đ)"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable bangNgay = new JTable(modelNgay);
        bangNgay.setRowHeight(22);
        JScrollPane scrNgay = new JScrollPane(bangNgay);
        scrNgay.setBorder(BorderFactory.createTitledBorder("Doanh thu theo ngày"));

        // Biểu đồ cột
        bieuDo = new BieuDoPanel();
        bieuDo.setBorder(BorderFactory.createTitledBorder("Biểu đồ doanh thu theo ngày"));
        bieuDo.setPreferredSize(new Dimension(520, 240));

        // Top sản phẩm
        modelTop = new DefaultTableModel(
                new Object[]{"Sản phẩm", "Đã bán", "Doanh thu (đ)"}, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        JTable bangTop = new JTable(modelTop);
        bangTop.setRowHeight(22);
        JScrollPane scrTop = new JScrollPane(bangTop);
        scrTop.setBorder(BorderFactory.createTitledBorder("Top sản phẩm bán chạy (tháng này)"));

        JPanel pnlGiua = new JPanel(new BorderLayout(10, 10));
        pnlGiua.setOpaque(false);
        pnlGiua.add(bieuDo, BorderLayout.CENTER);
        pnlGiua.add(scrNgay, BorderLayout.EAST);
        scrNgay.setPreferredSize(new Dimension(260, 0));

        JPanel pnlDuoi = new JPanel(new BorderLayout(10, 10));
        pnlDuoi.setOpaque(false);
        pnlDuoi.add(pnlGiua, BorderLayout.CENTER);
        pnlDuoi.add(scrTop, BorderLayout.SOUTH);
        scrTop.setPreferredSize(new Dimension(0, 150));

        JPanel pnlTong = new JPanel(new BorderLayout(0, 12));
        pnlTong.setOpaque(false);
        pnlTong.add(pnlTren, BorderLayout.NORTH);

        JPanel pnlGiuaTong = new JPanel(new BorderLayout(0, 12));
        pnlGiuaTong.setOpaque(false);
        pnlGiuaTong.add(pnlThe, BorderLayout.NORTH);
        pnlGiuaTong.add(pnlDuoi, BorderLayout.CENTER);
        pnlTong.add(pnlGiuaTong, BorderLayout.CENTER);

        add(pnlTong, BorderLayout.CENTER);
    }

    private JLabel taoThe(JPanel parent, String tieuDe) {
        JPanel the = new JPanel(new BorderLayout());
        the.setBackground(new Color(245, 248, 245));
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(220, 230, 220)),
                new EmptyBorder(12, 10, 12, 10)));
        JLabel giaTri = new JLabel("—", JLabel.CENTER);
        giaTri.setFont(new Font("SansSerif", Font.BOLD, 20));
        giaTri.setForeground(new Color(0, 102, 51));
        JLabel nhan = new JLabel(tieuDe, JLabel.CENTER);
        nhan.setFont(new Font("SansSerif", Font.PLAIN, 12));
        the.add(giaTri, BorderLayout.CENTER);
        the.add(nhan, BorderLayout.SOUTH);
        parent.add(the);
        return giaTri;
    }

    /** Nạp dữ liệu báo cáo (gọi khi vào màn / đổi kỳ). */
    public void taiDuLieu() {
        int thang = (int) cbThang.getSelectedItem();
        int nam = (int) cbNam.getSelectedItem();

        LocalDate dau = LocalDate.of(nam, thang, 1);
        LocalDate cuoi = dau.withDayOfMonth(dau.lengthOfMonth());
        Date sqlDau = Date.valueOf(dau);
        Date sqlCuoi = Date.valueOf(cuoi);

        // Doanh thu theo ngày
        List<Object[]> rows = baoCaoDAO.doanhThuTheoNgay(nam, thang);
        modelNgay.setRowCount(0);
        duLieuBieuDo.clear();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM");

        BigDecimal tong = BigDecimal.ZERO;
        for (LocalDate d = dau; !d.isAfter(cuoi); d = d.plusDays(1)) {
            Date sqlNgay = Date.valueOf(d);
            BigDecimal doanhThuNgay = hoaDonDAO.tongDoanhThu(sqlNgay, sqlNgay);
            int soHD = 0;
            for (Object[] r : rows) {
                if (sqlNgay.equals(r[0])) {
                    soHD = (int) r[1];
                    break;
                }
            }
            tong = tong.add(doanhThuNgay);
            modelNgay.addRow(new Object[]{ d.format(formatter), soHD, String.format("%,.0f", doanhThuNgay) });
            duLieuBieuDo.add(new Object[]{ d.format(formatter), doanhThuNgay });
        }
        bieuDo.repaint();

        // Top sản phẩm
        modelTop.setRowCount(0);
        for (Object[] r : baoCaoDAO.topSanPham(sqlDau, sqlCuoi, 10)) {
            modelTop.addRow(new Object[]{ r[0], r[1], String.format("%,.0f", r[2]) });
        }

        // Thẻ tổng
        BigDecimal loiNhuan = baoCaoDAO.loiNhuan(sqlDau, sqlCuoi);
        int soHD = baoCaoDAO.soHoaDon(sqlDau, sqlCuoi);
        lblDoanhThu.setText(String.format("%,.0f đ", tong));
        lblLoiNhuan.setText(String.format("%,.0f đ", loiNhuan));
        lblSoHoaDon.setText(String.valueOf(soHD));
    }

    /** Biểu đồ cột đơn giản vẽ tay. */
    private class BieuDoPanel extends JPanel {
        private static final long serialVersionUID = 1L;

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            int w = getWidth() - 40;
            int h = getHeight() - 50;
            if (duLieuBieuDo.isEmpty() || w <= 0 || h <= 0) {
                g.setColor(Color.GRAY);
                g.drawString("Chưa có dữ liệu", 20, 30);
                return;
            }

            BigDecimal lonNhat = BigDecimal.ZERO;
            for (Object[] d : duLieuBieuDo) {
                BigDecimal v = (BigDecimal) d[1];
                if (v.compareTo(lonNhat) > 0) lonNhat = v;
            }
            if (lonNhat.compareTo(BigDecimal.ZERO) == 0) lonNhat = BigDecimal.ONE;

            int soCot = duLieuBieuDo.size();
            int rongCot = Math.max(w / soCot - 2, 2);

            // Trục
            g.setColor(new Color(180, 180, 180));
            g.drawLine(20, 25, 20, 25 + h);
            g.drawLine(20, 25 + h, 20 + w, 25 + h);

            for (int i = 0; i < soCot; i++) {
                Object[] d = duLieuBieuDo.get(i);
                BigDecimal giaTri = (BigDecimal) d[1];
                int chieuCao = (int) (giaTri.doubleValue() / lonNhat.doubleValue() * (h - 10));
                int x = 24 + i * (rongCot + 2);
                int y = 25 + h - chieuCao;

                g.setColor(new Color(0, 130, 80));
                g.fillRect(x, y, rongCot, chieuCao);

                // Nhãn ngày: cách 3 cột cho đỡ dày
                if (i % 3 == 0) {
                    g.setColor(Color.DARK_GRAY);
                    g.setFont(new Font("SansSerif", Font.PLAIN, 9));
                    g.drawString((String) d[0], x - 4, 25 + h + 14);
                }
            }
        }
    }
}
