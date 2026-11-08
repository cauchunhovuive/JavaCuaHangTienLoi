package gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

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

import dao.NhaCungCapDAO;
import dao.NhanVienDAO;
import entities.NhaCungCap;
import entities.NhanVien;

public class frmNhaCungCap extends JPanel {
    private JTable table;
    private DefaultTableModel model;
    private NhaCungCapDAO nhaCungCapDAO;
    private List<NhaCungCap> dsNCC;
    private JButton btnThem, btnCapNhat, btnXoa, btnTimKiem;
    private JTextField txtTimKiem;
    
    public frmNhaCungCap() {
        try {
			setup();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        setLayout(new BorderLayout());

        // Panel tiêu đề
        JPanel panelTitle = new JPanel();
        JLabel titleLabel = new JLabel("Quản lý Nhà Cung Cấp", JLabel.CENTER);
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
        btnTimKiem.addActionListener(e -> timKiemNCC());
        panelTimKiem.add(btnTimKiem);
         
        
        // Panel nút chức năng
        JPanel panelButtons = new JPanel();
        
        panelButtons.add(panelTimKiem, BorderLayout.SOUTH);
        panelButtons.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        btnThem = new JButton("Thêm Nhà Cung Cấp");
        btnCapNhat = new JButton("Cập nhật");
        btnXoa = new JButton("Xóa Nhà Cung Cấp");
        
        // Thiết lập style cho các nút
        styleButton(btnThem, new Color(59, 89, 182));
        styleButton(btnCapNhat, new Color(76, 175, 80));
        styleButton(btnXoa, new Color(244, 67, 54));
        
        // Thêm sự kiện
        btnThem.addActionListener(e -> moMoDalAddNCC());
        btnCapNhat.addActionListener(e -> moModalCapNhatNCC());
        btnXoa.addActionListener(e -> xoaNCC());
        
        panelButtons.add(btnThem);
        panelButtons.add(btnCapNhat);
        panelButtons.add(btnXoa);
        
        add(panelButtons, BorderLayout.SOUTH);
    }
    
    private void timKiemNCC() {
        String idStr = txtTimKiem.getText().trim();
        model.setRowCount(0); // Xóa hết nội dung cũ
        if (idStr.isEmpty()) {
            // Nếu ô tìm kiếm trống, hiển thị lại tất cả
        	capNhatBangNCC();
        } else {
            try {
                int maNCC = Integer.parseInt(idStr);
                NhaCungCap found = nhaCungCapDAO.getNhaCungCapByMa(maNCC);
                if (found != null) {
                    model.addRow(new Object[]{
                        found.getMaNCC(),
                        found.getTenNCC(),
                        found.getSoDienThoai(),
                        found.getEmail(),
                        found.getMaDiaChi(),
                        found.getTrangThai(),
                        dinhDangNgay(found.getNgayTao())
                    });
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "Không tìm thấy Nhà Cung Cấp với ID: " + maNCC, 
                        "Kết quả tìm kiếm", JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, 
                    "Vui lòng nhập ID hợp lệ (số nguyên).", 
                    "Lỗi định dạng", JOptionPane.ERROR_MESSAGE);
            }
        }
    }


    private void xoaNCC() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần xóa.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maNCC = (Integer) model.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Bạn có chắc chắn muốn xóa nhà cung cấp này?", "Xác nhận", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            if (nhaCungCapDAO.updateTrangThaiNCC(maNCC, "Tạm dừng")) {
                JOptionPane.showMessageDialog(this, "Đã chuyển trạng thái nhà cung cấp sang 'Tạm dừng'.", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                capNhatBangNCC();
            } else {
                JOptionPane.showMessageDialog(this, "Xóa thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        }
    }



    private void moModalCapNhatNCC() {
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vui lòng chọn nhà cung cấp cần cập nhật.", "Thông báo", JOptionPane.WARNING_MESSAGE);
            return;
        }

        int maNCC = (Integer) model.getValueAt(selectedRow, 0);
        NhaCungCap ncc = nhaCungCapDAO.getNhaCungCapByMa(maNCC);

        JDialog dialog = new JDialog();
        dialog.setTitle("Cập Nhật Nhà Cung Cấp");
        dialog.setSize(450, 350);
        dialog.setLocationRelativeTo(this);
        dialog.setModal(true);

        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel formPanel = new JPanel(new GridLayout(6, 2, 10, 10));

        JLabel lblTenNCC = new JLabel("Tên Nhà Cung Cấp:");
        JTextField txtTenNCC = new JTextField(ncc.getTenNCC());

        JLabel lblSoDienThoai = new JLabel("Số Điện Thoại:");
        JTextField txtSoDienThoai = new JTextField(ncc.getSoDienThoai());

        JLabel lblEmail = new JLabel("Email:");
        JTextField txtEmail = new JTextField(ncc.getEmail());

        JLabel lblMaDiaChi = new JLabel("Mã Địa Chỉ:");
        JTextField txtMaDiaChi = new JTextField(ncc.getMaDiaChi() == null ? "" : String.valueOf(ncc.getMaDiaChi()));

        JLabel lblTrangThai = new JLabel("Trạng Thái:");
        JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Hoạt động", "Tạm dừng"});
        cbTrangThai.setSelectedItem(ncc.getTrangThai());

        JLabel lblNgayTao = new JLabel("Ngày Tạo:");
        JTextField txtNgayTao = new JTextField(dinhDangNgay(ncc.getNgayTao()));
        txtNgayTao.setEditable(false);
        txtNgayTao.setBackground(Color.LIGHT_GRAY);

        formPanel.add(lblTenNCC);      formPanel.add(txtTenNCC);
        formPanel.add(lblSoDienThoai); formPanel.add(txtSoDienThoai);
        formPanel.add(lblEmail);       formPanel.add(txtEmail);
        formPanel.add(lblMaDiaChi);    formPanel.add(txtMaDiaChi);
        formPanel.add(lblTrangThai);   formPanel.add(cbTrangThai);
        formPanel.add(lblNgayTao);     formPanel.add(txtNgayTao);

        JButton btnUpdate = new JButton("Cập nhật");
        btnUpdate.setPreferredSize(new Dimension(120, 35));
        btnUpdate.setFocusPainted(false);
        btnUpdate.setBackground(new Color(34, 139, 34));
        btnUpdate.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel();
        buttonPanel.add(btnUpdate);

        btnUpdate.addActionListener(e -> {
            try {
                ncc.setTenNCC(txtTenNCC.getText());
                ncc.setSoDienThoai(txtSoDienThoai.getText());
                ncc.setEmail(txtEmail.getText());
                String maDiaChiSua = txtMaDiaChi.getText().trim();
                ncc.setMaDiaChi(maDiaChiSua.isEmpty() ? null : Integer.parseInt(maDiaChiSua));
                ncc.setTrangThai((String) cbTrangThai.getSelectedItem());
                ncc.setNgayTao(new Timestamp(System.currentTimeMillis()));

                if (nhaCungCapDAO.updateNhaCungCap(ncc)) {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
                    capNhatBangNCC();
                    dialog.dispose();
                } else {
                    JOptionPane.showMessageDialog(dialog, "Cập nhật thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Vui lòng nhập đúng định dạng số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });

        contentPanel.add(formPanel, BorderLayout.CENTER);
        contentPanel.add(buttonPanel, BorderLayout.SOUTH);

        dialog.setContentPane(contentPanel);
        dialog.setVisible(true);
    }


	private void moMoDalAddNCC() {
    // Tạo một JDialog cho modal Thêm Nhà Cung Cấp
    JDialog dialog = new JDialog();
    dialog.setTitle("Thêm Nhà Cung Cấp");
    dialog.setSize(400, 350);
    dialog.setLocationRelativeTo(this); // Đặt vị trí của dialog gần với frmNhaCungCap
    
    // Tạo một JPanel với GridLayout để chứa các thành phần nhập liệu
    JPanel panel = new JPanel(new GridLayout(7, 2, 10, 10));
    
    // Tạo các JLabel và JTextField cho từng thông tin nhà cung cấp
    JLabel lblTenNCC = new JLabel("Tên Nhà Cung Cấp:");
    JTextField txtTenNCC = new JTextField();
    
    JLabel lblSoDienThoai = new JLabel("Số Điện Thoại:");
    JTextField txtSoDienThoai = new JTextField();
    
    JLabel lblEmail = new JLabel("Email:");
    JTextField txtEmail = new JTextField();
    
    JLabel lblMaDiaChi = new JLabel("Mã Địa Chỉ:");
    JTextField txtMaDiaChi = new JTextField();
    
    JLabel lblTrangThai = new JLabel("Trạng Thái:");
    JComboBox<String> cbTrangThai = new JComboBox<>(new String[]{"Hoạt động", "Tạm dừng"});
    
    JLabel lblNgayTao = new JLabel("Ngày Tạo:");
    JTextField txtNgayTao = new JTextField(
            new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Timestamp(System.currentTimeMillis())));
    // Thêm các label và textfield vào panel
    panel.add(lblTenNCC);
    panel.add(txtTenNCC);
    panel.add(lblSoDienThoai);
    panel.add(txtSoDienThoai);
    panel.add(lblEmail);
    panel.add(txtEmail);
    panel.add(lblMaDiaChi);
    panel.add(txtMaDiaChi);
    panel.add(lblTrangThai);
    panel.add(cbTrangThai);
    panel.add(lblNgayTao);
    panel.add(txtNgayTao);
    
    // Thêm nút "Thêm" vào panel
    JButton btnAdd = new JButton("Thêm");
    btnAdd.addActionListener(e -> {
        // Lấy dữ liệu từ các JTextField và JComboBox
        String tenNCC = txtTenNCC.getText().trim();
        String soDienThoai = txtSoDienThoai.getText().trim();
        String email = txtEmail.getText().trim();
        String maDiaChi = txtMaDiaChi.getText().trim();
        String trangThai = (String) cbTrangThai.getSelectedItem();
        String ngayTaoStr = txtNgayTao.getText().trim();
        
        // Kiểm tra tính hợp lệ của dữ liệu đầu vào
        if (tenNCC.isEmpty() || soDienThoai.isEmpty() || email.isEmpty() || ngayTaoStr.isEmpty()) {
            JOptionPane.showMessageDialog(dialog, "Vui lòng điền đầy đủ thông tin!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Kiểm tra số điện thoại có hợp lệ không
        if (!soDienThoai.matches("\\d{10,11}")) {
            JOptionPane.showMessageDialog(dialog, "Số điện thoại không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Kiểm tra định dạng email
        if (!email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$")) {
            JOptionPane.showMessageDialog(dialog, "Email không hợp lệ!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Mã địa chỉ là tùy chọn (cột MaDiaChi cho phép NULL)
        Integer maDiaChiInt = null;
        if (!maDiaChi.isEmpty()) {
            try {
                maDiaChiInt = Integer.parseInt(maDiaChi);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, "Mã địa chỉ phải là số!", "Lỗi", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }
        
        // Kiểm tra định dạng ngày tạo
        Timestamp ngayTao;
        try {
            ngayTao = Timestamp.valueOf(ngayTaoStr);
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(dialog, "Ngày tạo không hợp lệ. Vui lòng nhập theo định dạng yyyy-MM-dd!", "Lỗi", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Tạo đối tượng NhaCungCap mới
        NhaCungCap ncc = new NhaCungCap();
        ncc.setTenNCC(tenNCC);
        ncc.setSoDienThoai(soDienThoai);
        ncc.setEmail(email);
        ncc.setMaDiaChi(maDiaChiInt);
        ncc.setTrangThai(trangThai);
        ncc.setNgayTao(ngayTao);
        
        if (nhaCungCapDAO.addNhaCungCap(ncc)) {
		    JOptionPane.showMessageDialog(dialog, "Nhà cung cấp đã được thêm thành công!", "Thông báo", JOptionPane.INFORMATION_MESSAGE);
		    
		    // Cập nhật lại bảng sau khi thêm thành công
		    dsNCC.add(ncc);
		    capNhatBangNCC();
		    dialog.dispose(); // Đóng modal
		} else {
		    JOptionPane.showMessageDialog(dialog, "Thêm nhà cung cấp thất bại!", "Lỗi", JOptionPane.ERROR_MESSAGE);
		}
    });
    
    // Thêm nút Thêm vào panel
    panel.add(new JLabel()); // Cột trống cho GridLayout
    panel.add(btnAdd);
    
    // Thêm panel vào JDialog
    dialog.add(panel);
    dialog.setModal(true); // Đảm bảo modal không thể bị bỏ qua
    dialog.setVisible(true); // Hiển thị modal
}

	
	private void setup() throws SQLException {
        nhaCungCapDAO = new NhaCungCapDAO();
        dsNCC = nhaCungCapDAO.getAllNhaCungCap();

        String[] columnNames = {"Mã Nhà Cung Cấp", "Tên Nhà Cung Cấp", "Số Điện Thoại", "Email", "Trạng Thái", "Ngày Tạo"};

        Object[][] data = dsNCC.stream()
            .map(ncc-> new Object[] {
            		ncc.getMaNCC(),
            		ncc.getTenNCC(),
            		ncc.getSoDienThoai(),
            		ncc.getEmail(),
            		ncc.getTrangThai(),
            		dinhDangNgay(ncc.getNgayTao())
            })
            .toArray(Object[][]::new);

        model = new DefaultTableModel(data, columnNames);
        table = new JTable(model);
    }
	
	private void capNhatBangNCC() {
	    List<NhaCungCap> dsNhaCungCap = nhaCungCapDAO.getAllNhaCungCap(); // Lấy danh sách nhà cung cấp
	    
	    String[] columnNames = {"Mã Nhà Cung Cấp", "Tên Nhà Cung Cấp", "Số Điện Thoại", 
	                            "Email", "Mã Địa Chỉ", "Trạng Thái", "Ngày Tạo"};
	    
	    // Chuyển đổi danh sách nhà cung cấp thành dữ liệu cho JTable
	    Object[][] data = dsNhaCungCap.stream()
	        .map(ncc -> new Object[] {
	            ncc.getMaNCC(),
	            ncc.getTenNCC(),
	            ncc.getSoDienThoai(),
	            ncc.getEmail(),
	            ncc.getMaDiaChi(),
	            ncc.getTrangThai(),
	            dinhDangNgay(ncc.getNgayTao())
	        })
	        .toArray(Object[][]::new);
	    
	    model.setDataVector(data, columnNames);  // Cập nhật dữ liệu cho JTable
	}

    
    /** Hiển thị ngày tạo dạng dd/MM/yyyy thay vì Timestamp.toString(). */
    private String dinhDangNgay(Timestamp t) {
        return t == null ? "" : new java.text.SimpleDateFormat("dd/MM/yyyy").format(t);
    }

    private void styleButton(JButton button, Color color) {
        button.setFont(new Font("Arial", Font.BOLD, 14));
        button.setBackground(color);
        button.setForeground(Color.WHITE);
        button.setFocusPainted(false);
    }
    
}