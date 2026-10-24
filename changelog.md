# Changelog - Blocky Outline v1.1.3-new

### 🛠️ Các Lỗi Đã Sửa (Bug Fixes)
- **Fix Crash Menu (M) trên các phiên bản 1.21.x & 26.x**: Xóa bỏ hoàn toàn lỗi crash khi mở menu trên Minecraft 1.21 cũ (1.21.0 – 1.21.4) và các bản mới (1.21.11+ / 26.x) nhờ bộ chuyển đổi giao diện động (Dual-Runtime Bridge).
- **Fix lỗi đăng ký Render Event**: Sửa lỗi `IllegalAccessException` khi đăng ký sự kiện `BEFORE_BLOCK_OUTLINE` với Fabric API.
- **Fix lỗi không nhận diện phiên bản Minecraft 26.1.x – 26.3**: Khai báo đầy đủ dải phiên bản trong `fabric.mod.json`, cho phép mod khởi chạy trên mọi launcher Fabric 1.21.x và 26.x.
- **Fix hiển thị sai phiên bản**: Đổi tag phiên bản hiển thị trong menu từ `v1.1.7` thành `v1.1.3-new`.
- **Fix tràn giao diện menu**: Sửa lỗi các nút bấm và thẻ cài đặt đè lên thanh footer khi chơi ở màn hình nhỏ hoặc GUI Scale lớn (Scale 3, 4, Auto).
- **Fix khựng giật khung hình (Micro-stutter GC)**: Loại bỏ triệt để việc cấp phát rác bộ nhớ (Zero-GC) mỗi frame khi render viền khối và di chuột.

---

### 🔄 Các Thay Đổi & Cải Tiến (Changes & Improvements)
- **Thêm lệnh `/report`**: Gõ `/report` hoặc `/blockyoutline report` trong chat để mở trực tiếp trang báo lỗi GitHub trên trình duyệt.
- **Thêm nút "Report Bug" trong Menu [M]**: Bổ sung nút màu ngọc lam cạnh nút Done để gửi phản hồi và báo lỗi nhanh chóng.
- **Tối ưu tốc độ Render (Fast-Path)**: Khối lập phương chuẩn (>95% khối trong game) được vẽ trực tiếp qua fast-path, bỏ qua phân tích hình học phức tạp.
- **Tối ưu Reflection sang `MethodHandle`**: Tăng tốc độ truy xuất ma trận và camera lên 20–50x, loại bỏ hoàn toàn chi phí tạo mảng tham số.
- **Tối ưu CPU khi đứng yên**: Thuật toán vật lý tự động ngắt tính toán khi tâm ngắm đã ổn định trên khối mục tiêu.
