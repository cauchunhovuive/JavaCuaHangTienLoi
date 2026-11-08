package gui;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import entities.HoaDon;

/**
 * Bill sau khi thanh toán — layout giống màn chi tiết hóa đơn cà phê cũ:
 * JTextArea in được + QR chuyển khoản (nếu khách chuyển khoản) + nút In hóa đơn.
 */
public class frmInHoaDon extends JDialog {
    private static final long serialVersionUID = 1L;

    /**
     * @param hoaDon  hóa đơn vừa ghi (đã có mã)
     * @param gioHang snapshot giỏ hàng: [TenSP, DonViTinh, DonGia, SoLuong, ThanhTien, MaSP]
     * @param tenNV   tên nhân viên lập hóa đơn
     * @param tenKH   tên khách hàng ("Khách lẻ" nếu không phải thành viên)
     */
    public frmInHoaDon(java.awt.Window owner, HoaDon hoaDon, List<Object[]> gioHang,
                       String tenNV, String tenKH) {
        super(owner, "Hóa đơn #" + hoaDon.getMaHD(), ModalityType.APPLICATION_MODAL);
        setSize(620, 780);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        JTextArea txtChiTiet = new JTextArea();
        txtChiTiet.setEditable(false);
        txtChiTiet.setFont(new Font("Monospaced", Font.PLAIN, 14));

        boolean chuyenKhoan = "CHUYEN_KHOAN".equals(hoaDon.getHinhThucThanhToan());
        java.text.SimpleDateFormat fmt = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm");
        StringBuilder sb = new StringBuilder();
        sb.append("=========== CHI TIẾT HÓA ĐƠN ===========\n");
        sb.append("Mã hóa đơn: ").append(hoaDon.getMaHD()).append("\n");
        sb.append("Ngày bán  : ").append(hoaDon.getNgayLap() == null ? "" : fmt.format(hoaDon.getNgayLap())).append("\n");
        sb.append("----------------------------------------\n");
        sb.append("Nhân viên : ").append(tenNV == null ? "" : tenNV).append("\n");
        sb.append("Khách hàng: ").append(tenKH == null ? "Khách lẻ" : tenKH).append("\n");
        sb.append("----------------------------------------\n")
          .append(String.format("%-25s %-6s %-10s %-12s\n", "Tên SP", "SL", "Đơn giá", "Thành tiền"))
          .append("------------------------------------------------------------------\n");

        java.math.BigDecimal tong = java.math.BigDecimal.ZERO;
        for (Object[] d : gioHang) {
            java.math.BigDecimal donGia = (java.math.BigDecimal) d[2];
            java.math.BigDecimal thanhTien = (java.math.BigDecimal) d[4];
            tong = tong.add(thanhTien);
            sb.append(String.format("%-25s %-6d %,-10.0f %,-12.0f\n",
                    d[0], (int) d[3], donGia, thanhTien));
        }

        sb.append("------------------------------------------------------------------\n")
          .append(String.format("Hình thức   : %s\n", chuyenKhoan ? "CHUYỂN KHOẢN" : "TIỀN MẶT"))
          .append(String.format("Tổng tiền   : %,.0f VNĐ\n", tong))
          .append(String.format("Khách đưa   : %,.0f VNĐ\n",
                  hoaDon.getTienKhachDua() == null ? java.math.BigDecimal.ZERO : hoaDon.getTienKhachDua()))
          .append(String.format("Tiền thối    : %,.0f VNĐ\n",
                  hoaDon.getTienThua() == null ? java.math.BigDecimal.ZERO : hoaDon.getTienThua()));
        if (hoaDon.getDiemTang() != null && hoaDon.getDiemTang() > 0) {
            sb.append("Tích điểm    : ").append(hoaDon.getDiemTang()).append(" điểm\n");
        }
        sb.append("========================================\n")
          .append("       CẢM ƠN QUÝ KHÁCH ĐÃ MUA HÀNG!\n");
        txtChiTiet.setText(sb.toString());
        txtChiTiet.setCaretPosition(0);

        JScrollPane scr = new JScrollPane(txtChiTiet);
        scr.setBorder(BorderFactory.createEmptyBorder(10, 15, 0, 15));
        add(scr, BorderLayout.CENTER);

        // QR chuyển khoản (như bill cũ: MB Bank)
        if (chuyenKhoan) {
            String thongTin = "MB Bank\nSo tai khoan: 1234567890\nTen: NGUYEN VAN A\nNoi dung: HD"
                    + hoaDon.getMaHD() + " " + String.format("%,.0f VND", tong).replace(",", ".");
            BufferedImage qr = taoQR(thongTin, 200, 200);
            if (qr != null) {
                JLabel lblQR = new JLabel(new ImageIcon(qr), JLabel.CENTER);
                lblQR.setBorder(BorderFactory.createTitledBorder("QR Chuyển khoản (MB Bank - 1234567890)"));
                JPanel pnlQR = new JPanel();
                pnlQR.add(lblQR);
                add(pnlQR, BorderLayout.SOUTH);
            }
        }

        JButton btnIn = new JButton("In hóa đơn");
        btnIn.addActionListener((ActionEvent e) -> {
            try {
                boolean done = txtChiTiet.print();
                JOptionPane.showMessageDialog(this, done ? "In hóa đơn thành công!" : "In hóa đơn bị hủy.",
                        "Thông báo", done ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.WARNING_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Lỗi khi in hóa đơn: " + ex.getMessage(),
                        "Lỗi", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton btnDong = new JButton("Đóng");
        btnDong.addActionListener(e -> dispose());

        JPanel pnlNut = new JPanel();
        pnlNut.add(btnIn);
        pnlNut.add(btnDong);
        // Nếu khung cửa sổ vượt quá chiều cao màn hình thì không chặn rê chuột
        if (getHeight() > GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds().height) {
            setAlwaysOnTop(true);
        }
        add(pnlNut, BorderLayout.NORTH);
    }

    private static BufferedImage taoQR(String duLieu, int rong, int cao) {
        try {
            BitMatrix matrix = new MultiFormatWriter().encode(duLieu, BarcodeFormat.QR_CODE, rong, cao);
            return MatrixToImageWriter.toBufferedImage(matrix);
        } catch (Exception e) {
            System.err.println("Lỗi tạo QR: " + e.getMessage());
            return null;
        }
    }
}
