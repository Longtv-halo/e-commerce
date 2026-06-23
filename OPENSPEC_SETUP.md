# OpenSpec Setup Guide

> **Tool:** `@fission-ai/openspec` v1.4.1  
> **IDE:** Antigravity  
> **OS:** Windows  
> **Node.js:** >= 18.x

---

## Mục lục

1. [OpenSpec là gì?](#1-openspec-là-gì)
2. [Cài đặt CLI (một lần toàn máy)](#2-cài-đặt-cli)
3. [Init trong workspace](#3-init-trong-workspace)
4. [Fix: Copy skills vào đúng thư mục Antigravity](#4-fix-copy-skills-vào-đúng-thư-mục-antigravity)
5. [Cấu hình config.yaml](#5-cấu-hình-configyaml)
6. [Cách dùng trong Antigravity chat](#6-cách-dùng-trong-antigravity-chat)
7. [Kiểm tra hoạt động (checklist)](#7-kiểm-tra-hoạt-động)
8. [Nâng cấp](#8-nâng-cấp)
9. [Troubleshooting](#9-troubleshooting)
10. [Quick Reference](#10-quick-reference)

---

## 1. OpenSpec là gì?

OpenSpec là framework **Spec-Driven Development** giúp AI làm việc đúng spec thay vì đoán mò.

**Workflow:**
```
Propose → Apply → Archive
```

- **Propose**: AI tạo proposal + design + tasks cho feature
- **Apply**: AI implement code theo tasks đã định nghĩa
- **Archive**: Merge spec vào spec chính, đóng feature

---

## 2. Cài đặt CLI

> Chỉ cần làm **một lần** trên máy. Nếu đã cài, bỏ qua bước này.

```powershell
npm install -g @fission-ai/openspec@latest
```

Verify:
```powershell
openspec --version
# → 1.4.1
```

---

## 3. Init trong workspace

Chạy lệnh này **tại thư mục gốc** của workspace mới:

```powershell
cd <đường-dẫn-workspace>
openspec init --tools antigravity --force
```

> **Tại sao cần `--tools antigravity`?**  
> Nếu không chỉ định, OpenSpec sẽ hỏi interactive hoặc cài cho tool sai (Windsurf, Gemini…).  
> `--force` để overwrite nếu đã init trước đó.

Kết quả thành công:
```
- Setting up Antigravity...
√ Setup complete for Antigravity

OpenSpec Setup Complete
Created: Antigravity
4 skills and 4 commands in .agent/
Config: openspec/config.yaml (exists)
```

---

## 4. Fix: Copy skills vào đúng thư mục Antigravity

> ⚠️ **Đây là bước bắt buộc — không có bước này Antigravity sẽ không thấy skills.**

OpenSpec tạo skills vào `.agent/skills/` nhưng **Antigravity đọc từ `.agents/skills/`** (có chữ `s`).

Chạy lệnh sau để copy sang đúng vị trí:

```powershell
# Chạy từ thư mục gốc workspace
$src = ".agent\skills"
$dst = ".agents\skills"

foreach ($skill in Get-ChildItem -Path $src -Directory) {
    Copy-Item -Path $skill.FullName -Destination "$dst\$($skill.Name)" -Recurse -Force
    Write-Host "Copied: $($skill.Name)"
}

Write-Host "`nDone. Skills in .agents/skills/:"
Get-ChildItem -Path $dst | Select-Object Name
```

Kết quả mong đợi:
```
Copied: openspec-apply-change
Copied: openspec-archive-change
Copied: openspec-explore
Copied: openspec-propose

Done. Skills in .agents/skills/:
Name
----
openspec-apply-change
openspec-archive-change
openspec-explore
openspec-propose
```

### Restart IDE

> ⚠️ **Bắt buộc phải restart Antigravity IDE** để skills mới được load.

---

## 5. Cấu hình config.yaml

File `openspec/config.yaml` là nơi bạn mô tả project của mình. **AI sẽ đọc file này** trước khi propose/apply.

**Vị trí:** `<workspace>/openspec/config.yaml`

### Template

```yaml
schema: spec-driven

context: |
  ## Tech Stack
  - <ngôn ngữ và framework>
  - <database>
  - <build tool>

  ## Domain
  <mô tả ngắn project>

  ## Directory structure
  - src/controller/  → REST endpoints
  - src/service/     → Business logic
  - src/repository/  → Data access

  ## API conventions
  - Base URL: http://localhost:<port>
  - <format request/response>
  - <auth method>

  ## Coding conventions
  - <style, naming>
  - <error handling>

rules:
  proposal:
    - <rule 1>
    - <rule 2>
  tasks:
    - <rule 1>
    - <rule 2>
```

### Ví dụ (Java Spring Boot)

```yaml
schema: spec-driven

context: |
  ## Tech Stack
  - Java 17 + Spring Boot 3.x
  - Spring Data JPA + Spring Security (JWT)
  - PostgreSQL + Flyway migration
  - Lombok, Maven

  ## Domain
  Hệ thống quản lý nhân viên và phòng ban.

  ## Package structure
  - controller/  → REST endpoints
  - service/     → Business logic
  - repository/  → Spring Data JPA
  - entity/      → JPA entities
  - dto/         → Request/Response
  - security/    → JWT + SecurityConfig

  ## API conventions
  - Base URL: http://localhost:8080
  - Response: BaseResponse<T> { success, results, error }
  - Auth: JWT Bearer từ POST /api/auth/login

  ## Coding conventions
  - Lombok @Data, @Builder, @RequiredArgsConstructor
  - Validation: @NotBlank, @Email, @NotNull
  - Exception: ResourceNotFoundException, BadRequestException

rules:
  proposal:
    - Cập nhật openspec/specs/api.yaml khi thêm endpoint
    - Mô tả rõ request/response schema
  tasks:
    - Thứ tự: DTO → Entity → Repository → Service → Controller
    - Mỗi task không quá 1 file thay đổi
```

---

## 6. Cách dùng trong Antigravity chat

Sau khi restart IDE, gọi skills bằng cách nhắn trong chat:

### Propose một feature mới

```
openspec-propose

[mô tả feature muốn build]
```

Hoặc trực tiếp:
```
openspec-propose Thêm endpoint export danh sách nhân viên ra Excel
```

AI sẽ tạo folder `openspec/changes/<tên-feature>/` với:
- `proposal.md`
- `design.md`  
- `tasks.md`

### Implement feature

```
openspec-apply-change
```

AI đọc `tasks.md` và implement từng task.

### Hoàn thành và lưu vào spec chính

```
openspec-archive-change
```

### Tìm hiểu spec hiện có

```
openspec-explore
```

---

## 7. Kiểm tra hoạt động

Chạy từng lệnh, tất cả phải không báo lỗi:

```powershell
# 1. Version CLI
openspec --version
# Mong đợi: 1.4.1  ✅

# 2. List changes
openspec list
# Mong đợi: No active changes found.  ✅

# 3. Validate project
openspec validate --all
# Mong đợi: No items found to validate.  ✅

# 4. Config
openspec config list
# Mong đợi: profile: custom, workflows: propose, explore, apply, archive  ✅

# 5. Kiểm tra skills đã ở đúng chỗ
Get-ChildItem ".agents\skills" | Where-Object { $_.Name -like "openspec*" } | Select-Object Name
# Mong đợi: openspec-propose, openspec-apply-change, openspec-archive-change, openspec-explore  ✅
```

---

## 8. Nâng cấp

```powershell
# Nâng cấp CLI
npm update -g @fission-ai/openspec

# Refresh workspace files
cd <workspace>
openspec update --force

# Copy lại skills sau khi update
$src = ".agent\skills"
$dst = ".agents\skills"
foreach ($skill in Get-ChildItem -Path $src -Directory) {
    Copy-Item -Path $skill.FullName -Destination "$dst\$($skill.Name)" -Recurse -Force
}
```

---

## 9. Troubleshooting

### Skills không xuất hiện trong Antigravity

**Nguyên nhân:** OpenSpec tạo `.agent/` thay vì `.agents/`

**Fix:**
```powershell
foreach ($skill in Get-ChildItem -Path ".agent\skills" -Directory) {
    Copy-Item -Path $skill.FullName -Destination ".agents\skills\$($skill.Name)" -Recurse -Force
}
```
Sau đó **restart IDE**.

### `openspec` không tìm thấy sau khi cài

```powershell
# Kiểm tra npm global bin
npm bin -g

# Thêm vào PATH
$env:PATH = "$(npm bin -g);$env:PATH"
```

### `openspec init` không tạo file config.yaml

```powershell
# Kiểm tra xem thư mục openspec/ đã tồn tại chưa
ls openspec/

# Nếu chưa có, init lại
openspec init --tools antigravity --force
```

### Skills bị cũ sau khi update OpenSpec

```powershell
# Force refresh
openspec update --force

# Copy lại skills
foreach ($skill in Get-ChildItem -Path ".agent\skills" -Directory) {
    Copy-Item -Path $skill.FullName -Destination ".agents\skills\$($skill.Name)" -Recurse -Force
}
# Restart IDE
```

---

## 10. Quick Reference

### CLI commands

```powershell
openspec --version          # Kiểm tra version
openspec init --tools antigravity --force  # Init workspace
openspec update --force     # Refresh files
openspec list               # Xem active changes
openspec list --specs       # Xem spec files
openspec validate --all     # Validate toàn bộ
openspec config list        # Xem config
openspec schemas            # Xem schemas có sẵn
```

### Skills trong Antigravity chat

| Skill | Mục đích |
|-------|----------|
| `openspec-propose` | Tạo proposal + design + tasks cho feature mới |
| `openspec-apply-change` | Implement tasks từ change hiện tại |
| `openspec-archive-change` | Đóng feature, merge vào spec chính |
| `openspec-explore` | Tìm hiểu spec hiện có |

### Cấu trúc thư mục sau setup

```
<workspace>/
├── openspec/
│   ├── config.yaml          ← Sửa file này cho mỗi project
│   ├── specs/               ← Spec tổng hợp (OpenAPI…)
│   └── changes/             ← Active features
│       └── archive/         ← Features đã done
├── .agent/                  ← OpenSpec tạo ra (đừng xóa)
│   └── skills/              ← Nguồn gốc skills
└── .agents/                 ← Antigravity đọc từ đây
    └── skills/
        ├── openspec-propose/
        ├── openspec-apply-change/
        ├── openspec-archive-change/
        └── openspec-explore/
```

---

## Checklist setup mới (copy để dùng lại)

```
□ node --version  →  >= 18.x
□ npm install -g @fission-ai/openspec@latest
□ openspec --version  →  trả về version
□ cd <workspace>
□ openspec init --tools antigravity --force
□ Copy skills: .agent/skills/ → .agents/skills/
□ Chỉnh sửa openspec/config.yaml (tech stack + conventions)
□ Restart Antigravity IDE
□ openspec list  →  "No active changes found."
□ openspec validate --all  →  không lỗi
□ Test: gõ "openspec-propose" trong chat → AI phản hồi
```
