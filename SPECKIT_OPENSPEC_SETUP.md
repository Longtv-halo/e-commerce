# 📋 Hướng Dẫn Cài Đặt SpecKit & OpenSpec

> **Workspace:** `d:\e-commerce` | **Cập nhật:** 2026-06-12

Tài liệu này hướng dẫn step-by-step để cài đặt và sử dụng hai công cụ **Spec-Driven Development (SDD)** trong project này:

| Công cụ | Package | Mục đích |
|---------|---------|----------|
| **OpenSpec** | `@fission-ai/openspec` (npm) | Quản lý workflow đặc tả API & feature changes |
| **SpecKit** | `specify-cli` (Python/uv) | Framework SDD: tạo spec → plan → implement |

---

## 🔍 Tổng Quan Hai Công Cụ

### OpenSpec (`openspec`)
- Công cụ **Node.js** của Fission AI
- Quản lý file spec trong thư mục `openspec/` của project
- Tích hợp slash commands với AI assistant để propose, apply, archive các thay đổi
- **Phù hợp cho:** dự án hiện có (brownfield), quản lý incremental changes

### SpecKit (`specify`)
- Công cụ **Python** của GitHub (open-source, 112k stars)
- Framework SDD đầy đủ: constitution → specify → plan → tasks → implement
- Tích hợp sâu với Antigravity IDE qua **`$speckit-*` skills**
- **Phù hợp cho:** đảm bảo AI coding agent follow đúng spec

---

## ✅ Prerequisites

Kiểm tra các công cụ đã có sẵn:

```powershell
# Kiểm tra Node.js (cần >= 20.x)
node --version     # → v22.17.0 ✓

# Kiểm tra Python (cần >= 3.8)
python --version   # → Python 3.13.5 ✓

# Kiểm tra npm
npm --version      # → 11.4.2 ✓
```

---

## 📦 Phần 1: Cài Đặt OpenSpec

### Bước 1.1 — Cài global package

```powershell
npm install -g @fission-ai/openspec@latest
```

### Bước 1.2 — Verify cài đặt

```powershell
openspec --version
# → 1.4.1
```

### Bước 1.3 — Kiểm tra project status

> Project này đã có sẵn cấu trúc `openspec/`. Chạy lệnh sau để verify:

```powershell
cd d:\e-commerce
openspec list
# → No active changes found.

openspec validate --all
# → No items found to validate.
```

### Bước 1.4 — Cấu trúc OpenSpec đã có sẵn

```
openspec/
├── config.yaml          # Cấu hình project context (tech stack, conventions)
├── specs/
│   └── api.yaml         # OpenAPI 3.0.3 specification
└── changes/
    └── archive/         # Các thay đổi đã archived
```

### Bước 1.5 — Sử dụng OpenSpec

Khi muốn propose một tính năng mới, dùng slash command trong AI assistant:

```
/opsx:propose Thêm endpoint GET /api/employee/{id}/history để xem lịch sử thay đổi
```

Khi muốn apply thay đổi đã được approve:
```
/opsx:apply
```

Khi hoàn thành một thay đổi:
```
/opsx:archive
```

---

## 🛠️ Phần 2: Cài Đặt SpecKit (`specify-cli`)

### Bước 2.1 — Cài `uv` (Python package manager)

`specify-cli` yêu cầu `uv`. Cài qua pip:

```powershell
pip install uv
```

> ⚠️ **Lưu ý trên Windows:** `uv` có thể không được thêm vào PATH tự động sau khi cài qua pip.  
> Thay vì dùng `uv`, dùng: `python -m uv`

Verify:
```powershell
python -m uv --version
# → uv 0.11.21
```

### Bước 2.2 — Cài `specify-cli` từ GitHub

```powershell
python -m uv tool install specify-cli --from "git+https://github.com/github/spec-kit.git"
```

Quá trình này tải về và build từ source (~30 giây). Kết quả thành công:
```
Installed 1 executable: specify
warning: `C:\Users\<username>\.local\bin` is not on your PATH
```

### Bước 2.3 — Fix PATH (Quan trọng!)

SpecKit cài `specify` vào `C:\Users\<username>\.local\bin` nhưng chưa thêm vào PATH.

**Thêm vào PATH cho session hiện tại:**
```powershell
$env:PATH = "C:\Users\longt\.local\bin;$env:PATH"
specify --version
# → specify 0.10.3.dev0
```

**Thêm vào PATH vĩnh viễn (PowerShell Profile):**
```powershell
# Mở PowerShell Profile
notepad $PROFILE

# Thêm dòng sau vào cuối file:
$env:PATH = "C:\Users\longt\.local\bin;$env:PATH"

# Hoặc dùng lệnh nhanh:
Add-Content -Path $PROFILE -Value "`n`$env:PATH = `"C:\Users\longt\.local\bin;`$env:PATH`""
```

**Verify sau khi fix PATH:**
```powershell
specify --version
# → specify 0.10.3.dev0 ✓
```

### Bước 2.4 — Khởi tạo SpecKit trong project

Project này đã được khởi tạo SpecKit. Nếu cần re-initialize:

```powershell
cd d:\e-commerce

# Khởi tạo với Antigravity IDE integration (agy)
specify init --here --integration agy --ignore-agent-tools --force
```

> **Tại sao dùng `--ignore-agent-tools`?**  
> SpecKit yêu cầu Gemini CLI hoặc Agent CLI nếu không dùng flag này.  
> Với Antigravity IDE, có thể bỏ qua check này vì skills được inject qua `.agents/`.

Kết quả thành công:
```
Project ready.
├── ● Install integration (Antigravity)  ✓
├── ● Install shared infrastructure     ✓
├── ● Install bundled workflow           ✓
└── ● Finalize (project ready)          ✓
```

### Bước 2.5 — Verify cài đặt

```powershell
# Kiểm tra tools available
specify check
# → ● Antigravity (available) ✓
# → ● Visual Studio Code (available) ✓

# Kiểm tra version là mới nhất
specify self check
# → Up to date: 0.10.3.dev0
```

### Bước 2.6 — Cấu trúc SpecKit đã tạo

```
d:\e-commerce\
├── .specify/
│   ├── extensions/       # Extensions đã cài
│   ├── integrations/     # Cấu hình tích hợp (agy)
│   ├── memory/           # AI memory context
│   ├── scripts/          # PowerShell scripts (ps)
│   ├── templates/        # Templates spec/plan/tasks
│   ├── workflows/        # Workflow definitions (speckit)
│   ├── extensions.yml    # Extension config
│   ├── init-options.json # Init options
│   └── integration.json  # Integration: agy + ps
├── .agents/
│   └── skills/           # Antigravity skills ($speckit-*)
└── constitution.md       # Project principles & guidelines
```

---

## 🚀 Phần 3: Workflow Sử Dụng Hàng Ngày

### Workflow SpecKit trong Antigravity IDE

SpecKit expose các **skills** có thể dùng trực tiếp trong chat với AI agent:

| Skill | Mục đích |
|-------|----------|
| `$speckit-constitution` | Thiết lập nguyên tắc và guidelines của project |
| `$speckit-specify` | Mô tả tính năng muốn build (what & why) |
| `$speckit-plan` | Tạo implementation plan kỹ thuật |
| `$speckit-tasks` | Break down plan thành danh sách tasks |
| `$speckit-implement` | AI thực thi tất cả tasks |
| `$speckit-clarify` | *(Optional)* Làm rõ ambiguous requirements |
| `$speckit-analyze` | *(Optional)* Kiểm tra consistency giữa các artifacts |
| `$speckit-checklist` | *(Optional)* Tạo checklist kiểm tra chất lượng |

### Ví dụ thực tế: Thêm tính năng mới

**1. Thiết lập principles (chỉ cần làm 1 lần):**
```
$speckit-constitution
```

**2. Mô tả tính năng:**
```
$speckit-specify Thêm chức năng export danh sách nhân viên ra file Excel/CSV, 
hỗ trợ filter theo phòng ban và ngày tạo. File download về máy người dùng.
```

**3. Lập kế hoạch kỹ thuật:**
```
$speckit-plan Stack: Spring Boot + Apache POI cho Excel. 
Endpoint: GET /api/employee/export?format=xlsx&departmentId=1
```

**4. Tạo danh sách tasks:**
```
$speckit-tasks
```

**5. Thực thi:**
```
$speckit-implement
```

### Workflow OpenSpec

**Propose thay đổi API:**
```
/opsx:propose Cập nhật endpoint POST /api/department/create 
để hỗ trợ tạo nhiều department cùng lúc (bulk create)
```

**Xem danh sách thay đổi:**
```powershell
openspec list
```

**Validate spec hiện tại:**
```powershell
openspec validate --specs
```

---

## 🔧 Phần 4: Nâng Cấp & Bảo Trì

### Nâng cấp OpenSpec

```powershell
npm update -g @fission-ai/openspec
```

### Nâng cấp SpecKit

```powershell
# Xem version hiện tại và version mới nhất
specify self check

# Nâng cấp
python -m uv tool install specify-cli --force --from "git+https://github.com/github/spec-kit.git"
```

### Refresh SpecKit project files sau khi nâng cấp

```powershell
$env:PATH = "C:\Users\longt\.local\bin;$env:PATH"
cd d:\e-commerce
specify init --here --integration agy --ignore-agent-tools --force
```

### Refresh OpenSpec project files

```powershell
cd d:\e-commerce
openspec update --force
```

---

## 🐛 Troubleshooting

### Lỗi: `specify` không tìm thấy sau khi cài

```powershell
# Fix: Thêm PATH tạm thời
$env:PATH = "C:\Users\longt\.local\bin;$env:PATH"

# Hoặc dùng trực tiếp qua uv
python -m uv run specify --version
```

### Lỗi: `openspec` không tìm thấy

```powershell
# Kiểm tra npm global bin
npm list -g --depth=0

# Reinstall nếu cần
npm install -g @fission-ai/openspec@latest
```

### Lỗi: `specify init` báo "Agent not found"

```powershell
# Thêm --ignore-agent-tools để bỏ qua kiểm tra
specify init --here --integration agy --ignore-agent-tools --force
```

### Lỗi: openspec `validate` trả về "Nothing to validate"

```powershell
# Dùng flag cụ thể
openspec validate --all      # Validate tất cả
openspec validate --specs    # Chỉ validate spec files
openspec validate --changes  # Chỉ validate active changes
```

### Lỗi: `uv` không nhận sau `pip install uv`

```powershell
# Dùng qua Python module thay vì trực tiếp
python -m uv <command>

# Ví dụ:
python -m uv tool install specify-cli --from "git+https://github.com/github/spec-kit.git"
python -m uv --version
```

---

## 📋 Tóm Tắt Quick Reference

### Các lệnh hay dùng nhất

```powershell
# ─── Setup PATH (cần mỗi session mới) ───────────────────
$env:PATH = "C:\Users\longt\.local\bin;$env:PATH"

# ─── SpecKit CLI ────────────────────────────────────────
specify --version         # Kiểm tra version
specify check             # Kiểm tra tools available
specify self check        # Kiểm tra cần update không
specify integration list  # Xem integrations đã cài

# ─── OpenSpec CLI ────────────────────────────────────────
openspec --version        # Kiểm tra version
openspec list             # Xem active changes
openspec validate --all   # Validate project
openspec update           # Refresh files

# ─── Antigravity Skills (trong chat) ────────────────────
# $speckit-constitution  → Thiết lập principles
# $speckit-specify       → Mô tả feature
# $speckit-plan          → Lập kế hoạch
# $speckit-tasks         → Tạo task list
# $speckit-implement     → Implement
```

---

## 📁 Cấu Trúc File Liên Quan

```
d:\e-commerce\
├── .specify/                        # SpecKit config & templates
│   ├── integration.json             # Cấu hình: agy (Antigravity) + ps
│   ├── extensions.yml               # Extensions đã enable
│   └── scripts/ templates/ ...      # Internal SpecKit files
├── .agents/
│   └── skills/                      # Antigravity $speckit-* skills
├── .gemini/                         # Antigravity IDE config (không commit)
├── openspec/
│   ├── config.yaml                  # Context project cho AI
│   ├── specs/api.yaml               # OpenAPI 3.0.3 spec
│   └── changes/archive/             # Archived changes
└── constitution.md                  # Project principles (SpecKit)
```

> [!TIP]
> Thêm `.agents/` vào `.gitignore` nếu chứa credentials hoặc tokens cá nhân.

> [!NOTE]
> File `openspec/config.yaml` đóng vai trò context quan trọng — AI sẽ đọc file này để hiểu tech stack và coding conventions khi làm việc với project.
