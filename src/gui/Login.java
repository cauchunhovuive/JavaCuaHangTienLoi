package gui;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

import dao.NhanVienDAO;
import dao.TaiKhoanDAO;
import entities.NhanVien;
import entities.TaiKhoan;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;
import java.util.ArrayList;

public class Login extends JFrame {
    private JPanel mainPanel;
    private JPanel leftPanel;
    private JPanel rightPanel;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private ImageIcon logoIcon;
    private TaiKhoan tk = new TaiKhoan();
    private TaiKhoanDAO taiKhoanDAO = new TaiKhoanDAO();

    public Login() {
        initComponents();
        setLocationRelativeTo(null); // Đặt cửa sổ ở giữa màn hình
    }

    private void initComponents() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Đăng Nhập - Hệ Thống Quản Lý Cửa Hàng Tiện Lợi");
        setSize(1200, 550);
        setResizable(false);

        mainPanel = new JPanel(new GridLayout(1, 2));
        leftPanel = createLeftPanel();
        rightPanel = createRightPanel();

        // Thêm các panel vào panel chính
        mainPanel.add(leftPanel);
        mainPanel.add(rightPanel);

        // Thêm panel chính vào frame
        getContentPane().add(mainPanel);
    }

    private JPanel createLeftPanel() {
        JPanel panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (logoIcon != null) {
                    Image img = logoIcon.getImage();
                    g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                }
            }
        };

        panel.setLayout(new BorderLayout());
        panel.setBackground(new Color(0, 102, 51));

        // Tim o ca 2 cho: src/images/ va src/images/sp/ (anh ban vua bo vao sp/)
        logoIcon = tayAnh("/images/background-store.jpg", "src/images/background-store.jpg");
        if (logoIcon == null) logoIcon = tayAnh(null, "src/images/sp/background-store.jpg");
        if (logoIcon == null) logoIcon = tayAnh(null, "src/images/background-cafe.png");
        if (logoIcon == null) System.out.println("Không tìm thấy ảnh!");

        return panel;
    }

    private JPanel createRightPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        panel.setBorder(new EmptyBorder(40, 50, 40, 50));

        // Tiêu đề đăng nhập
        JLabel loginTitle = new JLabel("ĐĂNG NHẬP");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 32));
        loginTitle.setForeground(new Color(51, 51, 51));
        loginTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Tiêu đề phụ
        JLabel subTitle = new JLabel("Vui lòng đăng nhập để tiếp tục");
        subTitle.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        subTitle.setForeground(new Color(102, 102, 102));
        subTitle.setAlignmentX(Component.CENTER_ALIGNMENT);

        // Panel đệm
        JPanel spacer1 = new JPanel();
        spacer1.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));
        spacer1.setOpaque(false);

        JPanel spacer2 = new JPanel();
        spacer2.setMaximumSize(new Dimension(Short.MAX_VALUE, 15));
        spacer2.setOpaque(false);

        // Tạo trường tên đăng nhập
        usernameField = new JTextField();
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 5, 5, 5)));
        JPanel usernamePanel = createInputPanel("Tài khoản", "/images/user_name-icon.png", usernameField);

        passwordField = new JPasswordField();
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(200, 200, 200)),
        BorderFactory.createEmptyBorder(5, 0, 5, 5)));
        JPanel passwordPanel = createInputPanel("Mật khẩu", "/images/password-icon.png", passwordField);

        JPanel optionsPanel = new JPanel();
        optionsPanel.setLayout(new BoxLayout(optionsPanel, BoxLayout.X_AXIS));
        optionsPanel.setOpaque(false);
        optionsPanel.setMaximumSize(new Dimension(Short.MAX_VALUE, 30));

        loginButton = new JButton("ĐĂNG NHẬP");
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        loginButton.setForeground(Color.WHITE);
        loginButton.setBackground(new Color(0, 102, 51));
        loginButton.setBorderPainted(false);
        loginButton.setFocusPainted(false);
        loginButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginButton.setMaximumSize(new Dimension(250, 40));

        loginButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                loginButton.setBackground(new Color(0, 82, 41));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                loginButton.setBackground(new Color(0, 102, 51));
            }
        });

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                handleLogin(evt);
            }
        });

        panel.add(loginTitle);
        panel.add(Box.createRigidArea(new Dimension(0, 10)));
        panel.add(subTitle);
        panel.add(spacer1);
        panel.add(Box.createRigidArea(new Dimension(0, 50)));
        panel.add(usernamePanel);
        panel.add(Box.createRigidArea(new Dimension(0, 20)));
        panel.add(passwordPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(optionsPanel);
        panel.add(Box.createRigidArea(new Dimension(0, 30)));
        panel.add(loginButton);
        panel.add(Box.createVerticalGlue());

        return panel;
    }

    private JPanel createInputPanel(String labelText, String iconPath, JComponent inputField) {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setOpaque(false);
        panel.setMaximumSize(new Dimension(Short.MAX_VALUE, 70));

        JPanel pn = new JPanel();
        pn.setLayout(new BoxLayout(pn, BoxLayout.X_AXIS));
        pn.setOpaque(false);
        JLabel label = new JLabel(labelText);
        label.setFont(new Font("Segoe UI", Font.BOLD, 14));
        label.setForeground(new Color(51, 51, 51));

        pn.add(label);

        JPanel inputContainer = new JPanel();
        inputContainer.setLayout(new BoxLayout(inputContainer, BoxLayout.X_AXIS));
        inputContainer.setOpaque(false);

        ImageIcon icon = tayAnh(iconPath, "src" + iconPath);
        if (icon != null && icon.getIconWidth() > 0) {
            Image img = icon.getImage().getScaledInstance(18, 18, Image.SCALE_SMOOTH);
            JLabel iconLabel = new JLabel(new ImageIcon(img));
            iconLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
            inputContainer.add(iconLabel);
        }

        inputContainer.add(inputField);

        panel.add(pn);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(inputContainer);

        return panel;
    }

    private ImageIcon tayAnh(String duongDanClasspath, String duongDanFile) {
        try {
            if (duongDanClasspath != null) {
                URL url = getClass().getResource(duongDanClasspath);
                if (url != null) {
                    ImageIcon icon = new ImageIcon(url);
                    if (icon.getIconWidth() > 0) return icon;
                }
            }
        } catch (Exception ignore) { }
        java.io.File f = new java.io.File(duongDanFile);
        if (f.exists()) {
            ImageIcon icon = new ImageIcon(f.getAbsolutePath());
            if (icon.getIconWidth() > 0) return icon;
        }
        return null;
    }

    private void handleLogin(ActionEvent evt) {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Vui lòng nhập đầy đủ tài khoản và mật khẩu.",
                    "Thiếu thông tin", JOptionPane.WARNING_MESSAGE);
            return;
        }

        ArrayList<TaiKhoan> dsTk = taiKhoanDAO.dsTaiKhoan();
        TaiKhoan tkDung = null;
        for (TaiKhoan x : dsTk) {
            if (x.getTenDangNhap().equalsIgnoreCase(username) && password.equals(x.getMatKhau())) {
                tkDung = x;
                break;
            }
        }

        if (tkDung == null) {
            JOptionPane.showMessageDialog(this, "Tên đăng nhập hoặc mật khẩu không đúng.",
                    "Lỗi đăng nhập", JOptionPane.ERROR_MESSAGE);
            passwordField.setText("");
            passwordField.requestFocus();
            return;
        }

        if (tkDung.getQuyen() == null) {
            JOptionPane.showMessageDialog(this, "Tài khoản chưa được gán quyền. Liên hệ quản lý.",
                    "Lỗi phân quyền", JOptionPane.ERROR_MESSAGE);
            return;
        }

        tk = tkDung;
        NhanVien nhanVien = new NhanVienDAO().getNhanVienByTaiKhoan(tk.getTenDangNhap());

        JOptionPane.showMessageDialog(this, "Đăng nhập thành công!");
        SwingUtilities.invokeLater(() -> {
            HeThongQuanLyCuaHangTienLoi frm = new HeThongQuanLyCuaHangTienLoi(tk, nhanVien);
            frm.setVisible(true);
            this.dispose();
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new Login().setVisible(true);
            }
        });
    }
}
