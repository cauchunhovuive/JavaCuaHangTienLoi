package gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import dao.NhanVienDAO;
import entities.NhanVien;
import entities.TaiKhoan;

/** Khung chính: menu bên trái + CardLayout chuyển màn hình. */
public class HeThongQuanLyCuaHangTienLoi extends JFrame {
    private static final long serialVersionUID = 1L;

    public static final String CARD_TRANG_CHU = "TrangChu";
    public static final String CARD_BAN_HANG = "BanHang";
    public static final String CARD_SAN_PHAM = "SanPham";
    public static final String CARD_HOA_DON = "HoaDon";
    public static final String CARD_NHAP_HANG = "NhapHang";
    public static final String CARD_BAO_CAO = "BaoCao";
    public static final String CARD_NHAN_VIEN = "NhanVien";
    public static final String CARD_NHA_CUNG_CAP = "NhaCungCap";
    public static final String CARD_CA_NHAN = "CaNhan";

    private static final Color MAU_MENU = new Color(0, 102, 51);
    private static final Color MAU_MENU_NHAN = new Color(0, 82, 41);

    private final TaiKhoan taiKhoan;
    private final NhanVien nhanVien;
    private final boolean laQuanLy;

    private JPanel panelNoiDung;
    private CardLayout cardLayout;
    private final Map<String, Runnable> truocChuyenMan = new LinkedHashMap<>();

    public HeThongQuanLyCuaHangTienLoi(TaiKhoan tk, NhanVien nv) {
        this.taiKhoan = tk;
        this.nhanVien = nv;
        this.laQuanLy = tk.getMaQuyen() == 1;

        setTitle("Hệ thống quản lý cửa hàng tiện lợi");
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        cardLayout = new CardLayout();
        panelNoiDung = new JPanel(cardLayout);

        frmBanHang banHang = new frmBanHang(tk, nv);
        ManHinhTrangChu trangChu = new ManHinhTrangChu(this, banHang);
        QuanLySanPham sanPham = new QuanLySanPham();
        frmQuanLyHoaDon hoaDon = new frmQuanLyHoaDon();
        frmNhapHang nhapHang = new frmNhapHang(tk, nv);
        BaoCaoDoanhThu baoCao = new BaoCaoDoanhThu();
        frmNhanVien nhanVienForm = new frmNhanVien();
        frmNhaCungCap nhaCungCapForm = new frmNhaCungCap();
        ThongTinCaNhan caNhan = new ThongTinCaNhan(
        nhanVien != null ? nhanVien : new NhanVienDAO().getNhanVienByTaiKhoan(tk.getTenTaiKhoan()));

        panelNoiDung.add(trangChu, CARD_TRANG_CHU);
        panelNoiDung.add(sanPham, CARD_SAN_PHAM);
        panelNoiDung.add(hoaDon, CARD_HOA_DON);
        panelNoiDung.add(nhapHang, CARD_NHAP_HANG);
        panelNoiDung.add(baoCao, CARD_BAO_CAO);
        panelNoiDung.add(nhanVienForm, CARD_NHAN_VIEN);
        panelNoiDung.add(nhaCungCapForm, CARD_NHA_CUNG_CAP);
        panelNoiDung.add(caNhan, CARD_CA_NHAN);

        truocChuyenMan.put(CARD_TRANG_CHU, trangChu::taiDuLieu);
        truocChuyenMan.put(CARD_SAN_PHAM, sanPham::taiDuLieu);
        truocChuyenMan.put(CARD_HOA_DON, hoaDon::taiDuLieu);
        truocChuyenMan.put(CARD_NHAP_HANG, nhapHang::taiDuLieu);
        truocChuyenMan.put(CARD_BAO_CAO, baoCao::taiDuLieu);

        JPanel panelMenu = new JPanel(new GridLayout(0, 1, 0, 4));
        panelMenu.setBackground(MAU_MENU);
        panelMenu.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panelMenu.setPreferredSize(new Dimension(170, 0));

        panelMenu.add(taoNutMenu("Trang chủ", CARD_TRANG_CHU, "home.png"));
        panelMenu.add(taoNutMenu("Sản phẩm", CARD_SAN_PHAM, "product.png"));
        panelMenu.add(taoNutMenu("Hóa đơn", CARD_HOA_DON, "rece.png"));
        panelMenu.add(taoNutMenu("Nhập hàng", CARD_NHAP_HANG, "supplier.png"));
        panelMenu.add(taoNutMenu("Thống kê", CARD_BAO_CAO, "sta.png"));
        panelMenu.add(taoNutMenu("Nhân viên", CARD_NHAN_VIEN, "nhanvien.png", laQuanLy));
        panelMenu.add(taoNutMenu("Nhà cung cấp", CARD_NHA_CUNG_CAP, "supplier.png", laQuanLy));
        panelMenu.add(taoNutMenu("Cá nhân", CARD_CA_NHAN, "user.png"));

        JButton btnDangXuat = taoNutMenuBase("Đăng xuất", "logout.png");
        btnDangXuat.addActionListener(e -> {
            int chon = JOptionPane.showConfirmDialog(this, "Bạn chắc chắn muốn đăng xuất?",
                    "Đăng xuất", JOptionPane.YES_NO_OPTION);
            if (chon == JOptionPane.YES_OPTION) {
                dispose();
                new Login().setVisible(true);
            }
        });
        panelMenu.add(btnDangXuat);

        add(panelMenu, BorderLayout.WEST);
        add(panelNoiDung, BorderLayout.CENTER);

        hienThiMan(CARD_TRANG_CHU);
        setVisible(true);
    }

    public void hienThiMan(String card) {
        Runnable truoc = truocChuyenMan.get(card);
        if (truoc != null) {
            try {
                truoc.run();
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        cardLayout.show(panelNoiDung, card);
    }

    private JButton taoNutMenu(String text, String card, String iconPath) {
        return taoNutMenu(text, card, iconPath, true);
    }

    private JButton taoNutMenu(String text, String card, String iconPath, boolean duocPhep) {
        JButton nut = taoNutMenuBase(text, iconPath);
        if (duocPhep) {
            nut.addActionListener(e -> hienThiMan(card));
        } else {
            nut.addActionListener(e -> JOptionPane.showMessageDialog(this,
                    "Chức năng này chỉ dành cho quản lý.", "Không có quyền",
                    JOptionPane.WARNING_MESSAGE));
        }
        return nut;
    }

    private JButton taoNutMenuBase(String text, String iconPath) {
        JButton nut = new JButton(text);
        try {
            ImageIcon icon = new ImageIcon(new ImageIcon("src/images/" + iconPath)
                    .getImage().getScaledInstance(28, 28, Image.SCALE_SMOOTH));
            nut.setIcon(icon);
        } catch (Exception ignored) {
        }
        nut.setHorizontalTextPosition(SwingConstants.CENTER);
        nut.setVerticalTextPosition(SwingConstants.BOTTOM);
        nut.setForeground(Color.WHITE);
        nut.setFont(new Font("SansSerif", Font.BOLD, 13));
        nut.setFocusPainted(false);
        nut.setBorderPainted(false);
        nut.setContentAreaFilled(false);
        nut.setOpaque(false);
        nut.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        nut.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                nut.setBackground(MAU_MENU_NHAN);
                nut.setOpaque(true);
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                nut.setOpaque(false);
            }
        });
        return nut;
    }
}
