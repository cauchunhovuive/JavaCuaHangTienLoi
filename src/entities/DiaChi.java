package entities;

public class DiaChi {
    private int maDiaChi;
    private String soNha;
    private String duong;
    private String quan;
    private String thanhPho;

    public DiaChi() {
    }

    public DiaChi(int maDiaChi, String soNha, String duong, String quan, String thanhPho) {
        this.maDiaChi = maDiaChi;
        this.soNha = soNha;
        this.duong = duong;
        this.quan = quan;
        this.thanhPho = thanhPho;
    }

    public int getMaDiaChi() {
        return maDiaChi;
    }

    public void setMaDiaChi(int maDiaChi) {
        this.maDiaChi = maDiaChi;
    }

    public String getSoNha() {
        return soNha;
    }

    public void setSoNha(String soNha) {
        this.soNha = soNha;
    }

    public String getDuong() {
        return duong;
    }

    public void setDuong(String duong) {
        this.duong = duong;
    }

    public String getQuan() {
        return quan;
    }

    public void setQuan(String quan) {
        this.quan = quan;
    }

    public String getThanhPho() {
        return thanhPho;
    }

    public void setThanhPho(String thanhPho) {
        this.thanhPho = thanhPho;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (soNha != null && !soNha.isEmpty()) sb.append(soNha).append(' ');
        if (duong != null && !duong.isEmpty()) sb.append(duong);
        if (quan != null && !quan.isEmpty()) sb.append(", ").append(quan);
        if (thanhPho != null && !thanhPho.isEmpty()) sb.append(", ").append(thanhPho);
        return sb.toString();
    }
}
