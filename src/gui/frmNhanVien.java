package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GridLayout;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
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

import dao.NhanVienDAO;
import entities.NhanVien;

public class frmNhanVien extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private NhanVienDAO nhanVienDAO;
    private List<NhanVien> dsNhanVien;
    private JButton btnThem, btnCapNhat, btnXoa, btnTimKiem;
    private JTextField txtTimKiem;
    
    public frmNhanVien() {
        setup();
        setLayout(new BorderLayout());

        // Panel tiêu đề
        JPanel panelTitle = new JPanel();
        JLabel titleLabel = new JLabel("Quản lý nhân viên", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 16));
        panelTitle.add(titleLabel);
        add(panelTitle, BorderLayout.NORTH);

        // Bảng nhân viên
        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);
        
        // Panel tìm kiếm
        JPanel panelTimKiem = new JPanel();
        panelTimKiem.setBorder(new EmptyBorder(10, 10, 10, 10));
        panelTimKiem.add(new JLabel("Tìm kiếm theo ID:"));
        txtTimKiem = new JTextField(15);
        panelTimKiem.add(txtTimKiem);
        btnTimKiem = new JButton("Tìm kiếm");
        styleButton(btnTimKiem, new Color(52, 152, 219));
        btnTimKiem.addActionListener(e -> timKiemNhanVien());
        panelTimKiem.add(btnTimKiem);
         
        
        // Panel nút chức năng
        JPanel panelButtons = new JPanel();
        
        panelButtons.add(panelTimKiem, BorderLayout.SOUTH);
        panelButtons.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        btnThem = new JButton("Thêm nhân viên");
        btnCapNhat = new JButton("Cập nhật");
        btnXoa = new JButton("Xóa nhân viên");
        
        // Thiết lập style cho các nút
        styleButton(btnThem, new Color(59, 89, 182));
        styleButton(btnCapNhat, new Color(76, 175, 80));
        styleButton(btnXoa, new Color(244, 67, 54));
        
        // Thêm sự kiện
        btnThem.addActionListener(e -> moModalThemNhanVien());
        btnCapNhat.addActionListener(e -> moModalCapNhatNhanVien());
        btnXoa.addActionListener(e -> xoaNhanVien());
        
        panelButtons.add(btnThem);
        panelButtons.add(btnCapNhat);
        panelButtons.add(btnXoa);
        
        add(panelButtons, BorderLayout.SOUTH);
    }
    
    private void setup() {
        nhanVienDAO = new NhanVienDAO();
        dsNhanVien = nhanVienDAO.getAllNhanVien();

        String[] columnNames = {"Mã Nhân Viên", "Họ", "Tên", "Ngày Sinh", 
                              "Giới Tính", "CMND", "Số Điện Thoại", "Loại Nhân Viên", 
                              "Trạng Thái", "Ngày Vào Làm", "Lương Cơ Bản"};

        Object[][] data = dsNhanVien.stream()
            .map(nv -> new Object[] {
                nv.getMaNV(),
                nv.getHo(),
                nv.getTen(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),
                nv.getCmnd(),
                nv.getSoDienThoai(),
                nv.getLoaiNV(),
                nv.getTrangThai(),
                nv.getNgayVaoLam(),
                nv.getLuongCoBan() == null ? "" : String.format("%,.0f", nv.getLuongCoBan())
            })
            .toArray(Object[][]::new);

        model = new DefaultTableModel(data, columnNames);
        table = new JTable(model);
    }
    
    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
    
    private void moModalThemNhanVien() {
        // Tạo dialog thêm nhân viên
        JDialog dialog = new JDialog();
        dialog.setTitle("Thêm nhân viên mới");
        dialog.setSize(500, 400);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        
        // Panel form nhập liệu
        JPanel panelForm = new JPanel(new GridLayout(0, 2, 10, 10));
        panelForm.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Các trường nhập liệu (trừ mã NV)
        JTextField txtHo = new JTextField();
        JTextField txtTen = new JTextField();
        JTextField txtNgaySinh = new JTextField();
        JComboBox<String> comboGioiTinh = new JComboBox<>(new String[]{"Nam", "Nữ"});  // ComboBox cho giới tính
        JTextField txtCMND = new JTextField();
        JTextField txtSoDienThoai = new JTextField();
        JComboBox<String> comboLoaiNhanVien = new JComboBox<String>(new String[] {"Quản lý", "Nhân viên"});
        JTextField txtNgayVaoLam = new JTextField();
        JTextField txtLuongCoBan = new JTextField();
        
        // Thêm các trường vào form
        panelForm.add(new JLabel("Họ:"));
        panelForm.add(txtHo);
        panelForm.add(new JLabel("Tên:"));
        panelForm.add(txtTen);
        panelForm.add(new JLabel("Ngày sinh (yyyy-mm-dd):"));
        panelForm.add(txtNgaySinh);
        panelForm.add(new JLabel("Giới tính:"));
        panelForm.add(comboGioiTinh);  // Sử dụng combo box cho giới tính
        panelForm.add(new JLabel("CMND (9 số):"));
        panelForm.add(txtCMND);
        panelForm.add(new JLabel("Số điện thoại (10 số, bắt đầu bằng 0):"));
        panelForm.add(txtSoDienThoai);
        panelForm.add(new JLabel("Loại NV:"));
        panelForm.add(comboLoaiNhanVien);
        panelForm.add(new JLabel("Ngày vào làm (yyyy-mm-dd):"));
        panelForm.add(txtNgayVaoLam);
        panelForm.add(new JLabel("Lương cơ bản:"));
        panelForm.add(txtLuongCoBan);
        
        // Panel nút
        JPanel panelButtons = new JPanel();
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        
        btnLuu.addActionListener(e -> {
            try {
                // Kiểm tra ràng buộc
                if (!kiemTraCMND(txtCMND.getText())) {
                    JOptionPane.showMessageDialog(dialog, "CMND phải là 9 số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!kiemTraSoDienThoai(txtSoDienThoai.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Số điện thoại phải là 10 số và bắt đầu bằng 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!kiemTraNgaySinh(txtNgaySinh.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Ngày sinh phải trước ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                NhanVien nv = new NhanVien();
                nv.setHo(txtHo.getText());
                nv.setTen(txtTen.getText());
                nv.setNgaySinh(java.sql.Date.valueOf(txtNgaySinh.getText()).toLocalDate());
                nv.setGioiTinh((String) comboGioiTinh.getSelectedItem());  // Lấy giá trị từ comboBox
                nv.setCmnd(txtCMND.getText());
                nv.setSoDienThoai(txtSoDienThoai.getText());
                nv.setLoaiNV((String) comboLoaiNhanVien.getSelectedItem());
                nv.setTrangThai("Đang làm");
                nv.setNgayVaoLam(java.sql.Date.valueOf(txtNgayVaoLam.getText()).toLocalDate());
                nv.setLuongCoBan(new java.math.BigDecimal(txtLuongCoBan.getText()));
                nv.setMaDiaChi(2);
                
                if (nhanVienDAO.addNhanVien(nv)) {
                    JOptionPane.showMessageDialog(dialog, "Thêm nhân viên thành công!");
                    dialog.dispose();
                    capNhatBangNhanVien();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Thêm nhân viên thất bại!", 
                                                    "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), 
                                            "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnHuy.addActionListener(e -> dialog.dispose());
        
        panelButtons.add(btnLuu);
        panelButtons.add(btnHuy);
        
        dialog.add(panelForm, BorderLayout.CENTER);
        dialog.add(panelButtons, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }

    
    private void moModalCapNhatNhanVien() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần cập nhật", 
                                          "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        int maNV = (int) table.getModel().getValueAt(selectedRow, 0);
        NhanVien nv = nhanVienDAO.getNhanVienByMa(maNV);
        
        if (nv == null) {
            JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên", 
                                          "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo dialog cập nhật nhân viên
        JDialog dialog = new JDialog();
        dialog.setTitle("Cập nhật nhân viên");
        dialog.setSize(500, 400);
        dialog.setModal(true);
        dialog.setLayout(new BorderLayout());
        
        // Panel form nhập liệu
        JPanel panelForm = new JPanel(new GridLayout(0, 2, 10, 10));
        panelForm.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        // Các trường nhập liệu
        JTextField txtMaNV = new JTextField(String.valueOf(nv.getMaNV()));
        txtMaNV.setEditable(false);
        JTextField txtHo = new JTextField(nv.getHo());
        JTextField txtTen = new JTextField(nv.getTen());
        JTextField txtNgaySinh = new JTextField(nv.getNgaySinh().toString());
        JComboBox<String> cmbGioiTinh = new JComboBox<>(new String[] {"Nam", "Nữ"});
        cmbGioiTinh.setSelectedItem(nv.getGioiTinh());  // Set giá trị hiện tại
        JTextField txtCMND = new JTextField(nv.getCmnd());
        JTextField txtSoDienThoai = new JTextField(nv.getSoDienThoai());
        JTextField txtLoaiNV = new JTextField(nv.getLoaiNV());
        
        // Trạng thái làm việc
        JComboBox<String> cmbTrangThai = new JComboBox<>(new String[] {"Đang làm", "Tạm nghỉ", "Nghỉ việc"});
        cmbTrangThai.setSelectedItem(nv.getTrangThai()); // Set giá trị hiện tại
        
        JTextField txtNgayVaoLam = new JTextField(nv.getNgayVaoLam().toString());
        JTextField txtLuongCoBan = new JTextField(nv.getLuongCoBan() == null ? "" : nv.getLuongCoBan().toPlainString());
        
        // Thêm các trường vào form
        panelForm.add(new JLabel("Mã NV:"));
        panelForm.add(txtMaNV);
        panelForm.add(new JLabel("Họ:"));
        panelForm.add(txtHo);
        panelForm.add(new JLabel("Tên:"));
        panelForm.add(txtTen);
        panelForm.add(new JLabel("Ngày sinh (yyyy-mm-dd):"));
        panelForm.add(txtNgaySinh);
        panelForm.add(new JLabel("Giới tính:"));
        panelForm.add(cmbGioiTinh);
        panelForm.add(new JLabel("CMND (9 số):"));
        panelForm.add(txtCMND);
        panelForm.add(new JLabel("Số điện thoại (10 số, bắt đầu bằng 0):"));
        panelForm.add(txtSoDienThoai);
        panelForm.add(new JLabel("Loại NV:"));
        panelForm.add(txtLoaiNV);
        panelForm.add(new JLabel("Trạng thái:"));
        panelForm.add(cmbTrangThai);
        panelForm.add(new JLabel("Ngày vào làm (yyyy-mm-dd):"));
        panelForm.add(txtNgayVaoLam);
        panelForm.add(new JLabel("Lương cơ bản:"));
        panelForm.add(txtLuongCoBan);
        
        // Panel nút
        JPanel panelButtons = new JPanel();
        JButton btnLuu = new JButton("Lưu");
        JButton btnHuy = new JButton("Hủy");
        
        btnLuu.addActionListener(e -> {
            try {
                // Kiểm tra ràng buộc
                if (!kiemTraCMND(txtCMND.getText())) {
                    JOptionPane.showMessageDialog(dialog, "CMND phải là 9 số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!kiemTraSoDienThoai(txtSoDienThoai.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Số điện thoại phải là 10 số và bắt đầu bằng 0!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                if (!kiemTraNgaySinh(txtNgaySinh.getText())) {
                    JOptionPane.showMessageDialog(dialog, "Ngày sinh phải trước ngày hiện tại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                nv.setHo(txtHo.getText());
                nv.setTen(txtTen.getText());
                nv.setNgaySinh(java.sql.Date.valueOf(txtNgaySinh.getText()).toLocalDate());
                nv.setGioiTinh((String) cmbGioiTinh.getSelectedItem()); // Lấy giá trị từ JComboBox
                nv.setCmnd(txtCMND.getText());
                nv.setSoDienThoai(txtSoDienThoai.getText());
                nv.setLoaiNV(txtLoaiNV.getText());
                nv.setTrangThai((String) cmbTrangThai.getSelectedItem()); // Lấy giá trị từ JComboBox
                nv.setNgayVaoLam(java.sql.Date.valueOf(txtNgayVaoLam.getText()).toLocalDate());
                nv.setLuongCoBan(new java.math.BigDecimal(txtLuongCoBan.getText()));
                
                if (nhanVienDAO.updateNhanVien(nv)) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!");
                    dialog.dispose();
                    capNhatBangNhanVien();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!", 
                                                  "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Lỗi: " + ex.getMessage(), 
                                              "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        btnHuy.addActionListener(e -> dialog.dispose());
        
        panelButtons.add(btnLuu);
        panelButtons.add(btnHuy);
        
        dialog.add(panelForm, BorderLayout.CENTER);
        dialog.add(panelButtons, BorderLayout.SOUTH);
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
    

    private void xoaNhanVien() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhân viên cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maNV = (Integer) model.getValueAt(selectedRow, 0); // Cột 0 là MaNV
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn cho nhân viên này nghỉ việc?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (nhanVienDAO.updateTrangThaiNhanVien(maNV, "Nghỉ việc")) {
                JOptionPane.showMessageDialog(this, "Đã chuyển trạng thái nhân viên sang 'Nghỉ việc'.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                model.setValueAt("Nghỉ việc", selectedRow, 10);// Cột 10 là TrangThai nếu đúng thứ tự
                capNhatBangNhanVien();
            } else {
                JOptionPane.showMessageDialog(this, "Cập nhật trạng thái thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    
    private void capNhatBangNhanVien() {
        dsNhanVien = nhanVienDAO.getAllNhanVien();
        
        String[] columnNames = {"Mã Nhân Viên", "Họ", "Tên", "Ngày Sinh", 
                              "Giới Tính", "CMND", "Số Điện Thoại", "Loại Nhân Viên", 
                              "Trạng Thái", "Ngày Vào Làm", "Lương Cơ Bản"};
        
        Object[][] data = dsNhanVien.stream()
            .map(nv -> new Object[] {
                nv.getMaNV(),
                nv.getHo(),
                nv.getTen(),
                nv.getNgaySinh(),
                nv.getGioiTinh(),
                nv.getCmnd(),
                nv.getSoDienThoai(),
                nv.getLoaiNV(),
                nv.getTrangThai(),
                nv.getNgayVaoLam(),
                nv.getLuongCoBan() == null ? "" : String.format("%,.0f", nv.getLuongCoBan())
            })
            .toArray(Object[][]::new);
        
        model.setDataVector(data, columnNames);
    }
    
    private boolean kiemTraCMND(String cmnd) {
        return cmnd.matches("\\d{9}"); // CMND phải là 9 số
    }
    
    private boolean kiemTraSoDienThoai(String sdt) {
        return sdt.matches("0\\d{9}"); // SĐT phải bắt đầu bằng 0 và có 10 số
    }
    
    private boolean kiemTraNgaySinh(String ngaySinhStr) {
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
            LocalDate ngaySinh = LocalDate.parse(ngaySinhStr, formatter);
            return ngaySinh.isBefore(LocalDate.now()); // Ngày sinh phải trước ngày hiện tại
        } catch (DateTimeParseException e) {
            return false;
        }
    }
    
    private void timKiemNhanVien() {
        String idStr = txtTimKiem.getText().trim();
        if (idStr.isEmpty()) {
            capNhatBangNhanVien(); // Nếu trống thì hiển thị tất cả
            return;
        }
        
        try {
            int maNV = Integer.parseInt(idStr);
            NhanVien nv = nhanVienDAO.getNhanVienByMa(maNV);
            
            if (nv != null) {
                Object[][] data = {
                    {
                        nv.getMaNV(),
                        nv.getHo(),
                        nv.getTen(),
                        nv.getNgaySinh(),
                        nv.getGioiTinh(),
                        nv.getCmnd(),
                        nv.getSoDienThoai(),
                        nv.getLoaiNV(),
                        nv.getTrangThai(),
                        nv.getNgayVaoLam(),
                        nv.getLuongCoBan() == null ? "" : String.format("%,.0f", nv.getLuongCoBan())
                    }
                };
                
                String[] columnNames = {"Mã Nhân Viên", "Họ", "Tên", "Ngày Sinh", 
                                      "Giới Tính", "CMND", "Số Điện Thoại", "Loại Nhân Viên", 
                                      "Trạng Thái", "Ngày Vào Làm", "Lương Cơ Bản"};
                
                model.setDataVector(data, columnNames);
            } else {
                JOptionPane.showMessageDialog(this, "Không tìm thấy nhân viên với ID: " + maNV, 
                                            "Thông báo", JOptionPane.INFORMATION_MESSAGE);
            }
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "ID phải là số nguyên!", 
                                        "Lỗi", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    
}