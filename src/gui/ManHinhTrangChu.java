package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;

import dao.BaoCaoDAO;
import dao.HoaDonDAO;
import dao.SanPhamDAO;
import entities.SanPham;

/**
 * Trang chủ: 4 thẻ thống kê + lưới sản phẩm có hình ảnh (giống bản cũ)
 * + thanh lối tắt. Có ô tìm lọc nhanh trên lưới.
 */
public class ManHinhTrangChu extends JPanel {
    private static final long serialVersionUID = 1L;

    private final HeThongQuanLyCuaHangTienLoi frame;
    private final frmBanHang banHang;
    private final BaoCaoDAO baoCaoDAO = new BaoCaoDAO();
    private final HoaDonDAO hoaDonDAO = new HoaDonDAO();
    private final SanPhamDAO sanPhamDAO = new SanPhamDAO();

    private final List<SanPham> dsSP = new ArrayList<>();
    private JLabel lblDoanhThu;
    private JLabel lblSoHoaDon;
    private JLabel lblSapHet;
    private JLabel lblSoSanPham;
    private JTextField txtTim;
    private JPanel pnlLuoi;

    public ManHinhTrangChu(HeThongQuanLyCuaHangTienLoi frame, frmBanHang banHang) {
        this.frame = frame;
        this.banHang = banHang;
        setLayout(new BorderLayout(10, 10));
        setBorder(new EmptyBorder(15, 15, 15, 15));
        setBackground(Color.WHITE);

        // ===== Tieu de + o tim =====
        JLabel lblTieuDe = new JLabel("TỔNG QUAN CỬA HÀNG");
        lblTieuDe.setFont(new Font("SansSerif", Font.BOLD, 20));
        lblTieuDe.setForeground(new Color(0, 102, 51));

        txtTim = new JTextField(22);
        txtTim.setToolTipText("Tìm nhanh sản phẩm theo tên hoặc mã vạch");
        txtTim.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                veLuoi();
            }
        });

        JPanel pnlTren = new JPanel(new BorderLayout(10, 0));
        pnlTren.setOpaque(false);
        pnlTren.add(lblTieuDe, BorderLayout.WEST);
        JPanel pnlTim = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        pnlTim.setOpaque(false);
        pnlTim.add(new JLabel("Tìm sản phẩm:"));
        pnlTim.add(txtTim);
        pnlTren.add(pnlTim, BorderLayout.EAST);

        // ===== 4 the thong ke =====
        JPanel pnlThe = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlThe.setOpaque(false);
        pnlThe.setPreferredSize(new Dimension(0, 90));
        lblDoanhThu = taoThe(pnlThe, "Doanh thu hôm nay");
        lblSoHoaDon = taoThe(pnlThe, "Hóa đơn hôm nay");
        lblSapHet = taoThe(pnlThe, "Sản phẩm sắp hết");
        lblSoSanPham = taoThe(pnlThe, "Sản phẩm đang bán");

        // ===== Luoi san pham co anh (bam la them vao giỏ) =====
        pnlLuoi = new JPanel(new GridLayout(0, 3, 12, 12));
        pnlLuoi.setBackground(new Color(245, 246, 247));
        JScrollPane scrLuoi = new JScrollPane(pnlLuoi);
        scrLuoi.setBorder(BorderFactory.createTitledBorder("Sản phẩm — bấm vào thẻ để thêm vào giỏ hàng bên phải"));
        scrLuoi.getVerticalScrollBar().setUnitIncrement(16);

        // Giỏ hàng + thanh toán (màn bán hàng) gộp thẳng vào trang chủ
        JScrollPane scrBanHang = new JScrollPane(banHang);
        scrBanHang.setBorder(BorderFactory.createTitledBorder("Giỏ hàng / Thanh toán"));
        scrBanHang.setPreferredSize(new Dimension(480, 0));

        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, scrLuoi, scrBanHang);
        split.setResizeWeight(0.62);
        split.setBorder(null);

        // ===== Thanh loi toat =====
        JPanel pnlLoiToat = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 4));
        pnlLoiToat.setOpaque(false);
        pnlLoiToat.add(taoNutLoiToat("Quản lý sản phẩm", HeThongQuanLyCuaHangTienLoi.CARD_SAN_PHAM));
        pnlLoiToat.add(taoNutLoiToat("Nhập hàng", HeThongQuanLyCuaHangTienLoi.CARD_NHAP_HANG));
        pnlLoiToat.add(taoNutLoiToat("Hóa đơn", HeThongQuanLyCuaHangTienLoi.CARD_HOA_DON));
        pnlLoiToat.add(taoNutLoiToat("Thống kê", HeThongQuanLyCuaHangTienLoi.CARD_BAO_CAO));

        JPanel pnlGiua = new JPanel(new BorderLayout(0, 10));
        pnlGiua.setOpaque(false);
        pnlGiua.add(pnlThe, BorderLayout.NORTH);
        pnlGiua.add(split, BorderLayout.CENTER);

        add(pnlTren, BorderLayout.NORTH);
        add(pnlGiua, BorderLayout.CENTER);
        add(pnlLoiToat, BorderLayout.SOUTH);
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
        nhan.setForeground(Color.DARK_GRAY);
        the.add(giaTri, BorderLayout.CENTER);
        the.add(nhan, BorderLayout.SOUTH);
        parent.add(the);
        return giaTri;
    }

    private JButton taoNutLoiToat(String text, String card) {
        JButton nut = new JButton(text);
        nut.setBackground(new Color(0, 102, 51));
        nut.setForeground(Color.WHITE);
        nut.setFocusPainted(false);
        nut.setBorderPainted(false);
        nut.setFont(new Font("SansSerif", Font.BOLD, 13));
        nut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nut.addActionListener(e -> frame.hienThiMan(card));
        return nut;
    }

    /** Nạp dữ liệu (gọi mỗi khi vào màn). */
    public void taiDuLieu() {
        banHang.taiDuLieu();
        LocalDate homNay = LocalDate.now();
        Date sqlHomNay = Date.valueOf(homNay);

        BigDecimal doanhThu = hoaDonDAO.tongDoanhThu(sqlHomNay, sqlHomNay);
        lblDoanhThu.setText(String.format("%,.0f đ", doanhThu));
        lblSoHoaDon.setText(String.valueOf(baoCaoDAO.soHoaDon(sqlHomNay, sqlHomNay)));

        dsSP.clear();
        dsSP.addAll(sanPhamDAO.getAll());
        int sapHet = 0;
        for (SanPham sp : dsSP) {
            if (sp.isSapHet()) sapHet++;
        }
        lblSapHet.setText(String.valueOf(sapHet));
        lblSoSanPham.setText(String.valueOf(dsSP.size()));

        if (txtTim != null) txtTim.setText("");
        veLuoi();
    }

    /** Vẽ lưới thẻ sản phẩm (lọc theo ô tìm). */
    private void veLuoi() {
        pnlLuoi.removeAll();
        String tuKhoa = txtTim.getText().trim().toLowerCase();
        int hien = 0;

        for (SanPham sp : dsSP) {
            if (!tuKhoa.isEmpty()
                    && !sp.getTenSanPham().toLowerCase().contains(tuKhoa)
                    && !(sp.getMaVach() != null && sp.getMaVach().contains(tuKhoa))) {
                continue;
            }
            pnlLuoi.add(taoTheSanPham(sp));
            hien++;
        }
        if (hien == 0) {
            JLabel rong = new JLabel("Không tìm thấy sản phẩm phù hợp", SwingConstants.CENTER);
            rong.setForeground(Color.GRAY);
            pnlLuoi.add(rong);
        }
        pnlLuoi.revalidate();
        pnlLuoi.repaint();
    }

    private JPanel taoTheSanPham(SanPham sp) {
        JPanel the = new JPanel(new BorderLayout(4, 4));
        the.setBackground(Color.WHITE);
        the.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(225, 228, 230)),
                new EmptyBorder(8, 8, 8, 8)));
        the.setPreferredSize(new Dimension(170, 220));
        the.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Anh san pham (thieu anh -> image-notfound.png)
        JLabel lblAnh = new JLabel("", SwingConstants.CENTER);
        lblAnh.setPreferredSize(new Dimension(150, 110));
        lblAnh.setIcon(tayAnh(sp.getHinhAnh(), 150, 110));
        the.add(lblAnh, BorderLayout.NORTH);

        // Ten san pham (2 dong)
        JLabel lblTen = new JLabel("<html><div style='text-align:center'>" + sp.getTenSanPham() + "</div></html>");
        lblTen.setFont(new Font("SansSerif", Font.BOLD, 12));
        lblTen.setHorizontalAlignment(SwingConstants.CENTER);
        the.add(lblTen, BorderLayout.CENTER);

        // Gia + ton kho
        JPanel pnlDuoi = new JPanel(new BorderLayout());
        pnlDuoi.setOpaque(false);
        JLabel lblGia = new JLabel(String.format("%,.0f đ", sp.getGiaBan()), SwingConstants.CENTER);
        lblGia.setFont(new Font("SansSerif", Font.BOLD, 14));
        lblGia.setForeground(new Color(200, 80, 0));

        String tonText = "Còn " + sp.getTonKho() + " " + (sp.getDonViTinh() == null ? "" : sp.getDonViTinh());
        JLabel lblTon = new JLabel(sp.isSapHet() ? "SẮP HẾT!" : tonText, SwingConstants.CENTER);
        lblTon.setFont(new Font("SansSerif", Font.PLAIN, 11));
        lblTon.setForeground(sp.isSapHet() ? new Color(200, 0, 0) : new Color(90, 90, 90));

        pnlDuoi.add(lblGia, BorderLayout.NORTH);
        pnlDuoi.add(lblTon, BorderLayout.SOUTH);
        the.add(pnlDuoi, BorderLayout.SOUTH);

        // Bam vao the -> them thang vao gio hang
        the.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                banHang.themSanPhamVaoGio(sp);
            }
        });
        return the;
    }

    /** Tai anh theo duong dan, keo co, thieu file thi dung anh mac dinh. */
    private ImageIcon tayAnh(String duongDan, int rong, int cao) {
        String duong = duongDan;
        if (duong == null || duong.trim().isEmpty() || !new File(duong).exists()) {
            duong = "src/images/image-notfound.png";
        }
        try {
            ImageIcon icon = new ImageIcon(duong);
            if (icon.getIconWidth() <= 0) {
                icon = new ImageIcon("src/images/image-notfound.png");
            }
            int w = icon.getIconWidth();
            int h = icon.getIconHeight();
            if (w <= 0 || h <= 0) return icon;
            int sw = rong, sh = cao;
            double tiLe = Math.min((double) rong / w, (double) cao / h);
            sw = (int) (w * tiLe);
            sh = (int) (h * tiLe);
            return new ImageIcon(icon.getImage().getScaledInstance(sw, sh, Image.SCALE_SMOOTH));
        } catch (Exception ex) {
            return new ImageIcon("src/images/image-notfound.png");
        }
    }
}
