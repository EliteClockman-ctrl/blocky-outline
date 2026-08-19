# Blocky Outline 1.0.7 — Mã nguồn và tài liệu

Thư mục này là bản khôi phục mã nguồn của `blocky-outline-1.0.7.jar` (Fabric client mod), kèm theo toàn bộ tài nguyên có trong JAR gốc.

## Chức năng

- Thay thế viền chọn block mặc định bằng viền có màu, độ mờ và độ dày tùy chỉnh.
- Tô phần khối đang được chọn bằng một lớp màu trong suốt tùy chọn.
- Màu tĩnh theo HSV hoặc hiệu ứng cầu vồng chuyển động độc lập cho viền và phần tô.
- Mở giao diện tùy chỉnh bằng phím **M**; nhấn M lần nữa để đóng.
- Chỉnh màu bằng thanh hue, bảng saturation/brightness, hoặc nhập mã màu hex.
- Năm preset: Minimalist Silver, Classic Executive, Vibrant Gold, Rainbow Corporate và Dark Slate.
- Tự lưu cấu hình dạng JSON tại thư mục `config` của instance Minecraft: `blocky-outline.json`.

## Cấu trúc

- `src/main/java`: sáu lớp Java đã khôi phục toàn bộ logic mod.
- `src/main/resources`: `fabric.mod.json`, mixin descriptor, icon, bản dịch và giấy phép gốc.
- `FEATURES.md`: tài liệu tính năng, cấu hình và kiến trúc.
- `RECOVERY_NOTES.md`: phạm vi và lưu ý của mã dịch ngược.

## Yêu cầu theo manifest gốc

- Fabric Loader >= 0.19.3
- Fabric API
- Minecraft >= 26.2
- Java >= 25

Đây là mã Java được dịch ngược trực tiếp từ JAR phát hành, vì vậy tên biến cục bộ và một vài biểu thức có thể khác mã tác giả ban đầu; hành vi logic được giữ theo bytecode của bản 1.0.7.
