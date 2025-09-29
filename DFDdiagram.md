
```mermaid
    graph TD
        QuanLyNet-->QuanLyNhanVien
        QuanLyNet-->QuanLyKhachHang
        QuanLyNet-->QuanLyGiaoDich
        QuanLyNet-->QuanLyKhoHang
        QuanLyNet-->QuanLyDichVu
        QuanLyNet-->QuanLyBaoCao&ThongKe
        
        QuanLyNhanVien-->PhanQuyen
        QuanLyNhanVien-->QuanLyCaLamViec
        QuanLyNhanVien-->QuanLyTaiKhoanNV

        QuanLyKhachHang-->QuanLyTaiKhoanKH
        QuanLyKhachHang-->DangNhap/DangKy
        QuanLyKhachHang-->QuanLyThoiGianChoi

        