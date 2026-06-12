# OpenSpec & SpecKit trên IntelliJ IDEA

> **Ngày cập nhật:** 2026-06-12  
> **Verified:** Windows 11, IntelliJ IDEA 2024+

---

## Thực tế cần biết

**IntelliJ IDEA** không phải AI tool — nó là Java IDE. OpenSpec và SpecKit tích hợp thông qua **AI plugin bạn cài trong IntelliJ**, không phải trực tiếp vào IDE.

Có 3 lựa chọn AI trong IntelliJ được cả hai tool hỗ trợ:

| AI Plugin trong IntelliJ | OpenSpec | SpecKit |
|--------------------------|----------|---------|
| **JetBrains Junie** | ✅ `--tools junie` | ❌ không hỗ trợ |
| **GitHub Copilot** | ✅ `--tools github-copilot` | ✅ `--integration copilot` |
| **Continue.dev** | ✅ `--tools continue` | ❌ không hỗ trợ |

> **Khuyến nghị:** Dùng **GitHub Copilot plugin** nếu muốn cả OpenSpec lẫn SpecKit.  
> Hoặc dùng **JetBrains Junie** nếu chỉ cần OpenSpec.

---

## Phần A: OpenSpec trên IntelliJ

### A1. Cài AI plugin trong IntelliJ

**Chọn một trong hai:**

**Option 1 — JetBrains Junie (native, miễn phí thử)**
```
IntelliJ IDEA → Settings → Plugins → Marketplace
→ Tìm "Junie" → Install → Restart
```

**Option 2 — GitHub Copilot (cần subscription)**
```
IntelliJ IDEA → Settings → Plugins → Marketplace
→ Tìm "GitHub Copilot" → Install → Restart
→ Settings → GitHub Copilot → Sign in
```

### A2. Cài OpenSpec CLI (một lần toàn máy)

```powershell
npm install -g @fission-ai/openspec@latest
openspec --version
# → 1.4.1
```

### A3. Init trong workspace

**Nếu dùng Junie:**
```powershell
cd <workspace>
openspec init --tools junie --force
```

Tạo ra:
```
.junie/
├── commands/     ← slash commands (opsx-propose.md, opsx-apply.md…)
└── skills/       ← skill files cho Junie
```

**Nếu dùng GitHub Copilot:**
```powershell
cd <workspace>
openspec init --tools github-copilot --force
```

Tạo ra:
```
.github/
├── prompts/      ← prompt files (opsx-propose.prompt.md…)
└── skills/       ← skill files
```

### A4. Cách dùng

**Với Junie trong IntelliJ:**

1. Mở panel Junie (icon AI bên phải hoặc `Alt+J`)
2. Gõ trong chat:
```
/opsx:propose Thêm endpoint export nhân viên ra Excel
```

**Với GitHub Copilot trong IntelliJ:**

1. Mở Copilot Chat panel
2. Gõ:
```
/opsx:propose Thêm endpoint export nhân viên ra Excel
```

### A5. Verify OpenSpec đã setup đúng

```powershell
# CLI hoạt động
openspec --version       # → 1.4.1

# Project hợp lệ
openspec list            # → No active changes found.
openspec validate --all  # → không lỗi

# Files được tạo đúng
# Junie:
ls .junie\skills\        # → openspec-propose, openspec-apply-change…

# GitHub Copilot:
ls .github\prompts\      # → opsx-propose.prompt.md…
ls .github\skills\       # → openspec-propose, openspec-apply-change…
```

---

## Phần B: SpecKit trên IntelliJ

> SpecKit chỉ hỗ trợ **GitHub Copilot** trong IntelliJ. Không hỗ trợ Junie.

### B1. Yêu cầu

- Python 3.8+
- `uv` (Python package manager)
- GitHub Copilot plugin đã cài trong IntelliJ

### B2. Cài uv

```powershell
pip install uv
python -m uv --version
# → uv 0.11.21
```

### B3. Cài specify-cli

```powershell
python -m uv tool install specify-cli --from "git+https://github.com/github/spec-kit.git"
```

Fix PATH (Windows):
```powershell
$env:PATH = "C:\Users\$env:USERNAME\.local\bin;$env:PATH"
specify --version
# → specify 0.10.3.dev0
```

Thêm vào PowerShell Profile để không cần chạy lại:
```powershell
Add-Content -Path $PROFILE -Value "`n`$env:PATH = `"C:\Users\$env:USERNAME\.local\bin;`$env:PATH`""
```

### B4. Init SpecKit cho GitHub Copilot

```powershell
cd <workspace>
specify init --here --integration copilot --ignore-agent-tools --force
```

Tạo ra:
```
.github/
└── copilot-instructions.md   ← SpecKit context cho Copilot

.specify/
├── templates/
├── scripts/
└── workflows/

constitution.md
```

### B5. Cách dùng SpecKit với Copilot trong IntelliJ

Mở **Copilot Chat** panel trong IntelliJ và dùng slash commands:

| Command | Mục đích |
|---------|----------|
| `/speckit.specify` | Mô tả feature muốn build |
| `/speckit.plan` | Tạo technical implementation plan |
| `/speckit.tasks` | Break down thành task list |
| `/speckit.implement` | AI implement theo tasks |
| `/speckit.constitution` | Thiết lập project principles |

Ví dụ:
```
/speckit.specify Thêm chức năng export nhân viên ra Excel, 
filter theo phòng ban và date range
```

---

## Phần C: Dùng cả hai cùng lúc trên IntelliJ

Nếu muốn cả **OpenSpec** và **SpecKit** trong IntelliJ, cần dùng **GitHub Copilot**.

### Setup một lần

```powershell
cd <workspace>

# 1. OpenSpec cho Copilot
openspec init --tools github-copilot --force

# 2. SpecKit cho Copilot  
specify init --here --integration copilot --ignore-agent-tools --force
```

Sau đó trong IntelliJ Copilot Chat:

```
# OpenSpec workflow (feature proposal)
/opsx:propose Thêm feature mới

# SpecKit workflow (spec → plan → tasks → implement)
/speckit.specify Mô tả feature
/speckit.plan Tech stack lựa chọn
/speckit.tasks
/speckit.implement
```

---

## Phần D: Chỉ dùng CLI (không cần AI plugin)

Cả hai tool hoạt động hoàn toàn qua terminal IntelliJ ngay cả **không cần plugin AI**.

Mở **Terminal** trong IntelliJ (`Alt+F12`):

```powershell
# OpenSpec
openspec list                          # xem active changes
openspec new change "ten-feature"      # tạo change mới
openspec status --change "ten-feature" # xem trạng thái
openspec validate --all                # validate

# SpecKit
specify --version
specify check
```

Tạo change template thủ công để AI nào cũng dùng được:

```powershell
openspec new change "export-employee"
# → Tạo openspec/changes/export-employee/
#   với proposal.md, design.md, tasks.md template sẵn
```

Sau đó paste nội dung vào JetBrains AI chat bất kỳ để implement.

---

## So sánh các kịch bản

| Kịch bản | Cần cài | Lệnh init |
|----------|---------|-----------|
| Junie (IntelliJ native) + OpenSpec | OpenSpec CLI | `openspec init --tools junie` |
| Copilot + OpenSpec | Copilot plugin + OpenSpec CLI | `openspec init --tools github-copilot` |
| Copilot + SpecKit | Copilot plugin + specify-cli | `specify init --here --integration copilot` |
| Copilot + cả hai | Copilot plugin + cả hai CLI | Chạy cả hai lệnh init |
| Chỉ CLI (không AI plugin) | OpenSpec CLI | `openspec init --tools none` |

---

## Checklist setup cho IntelliJ

```
□ Cài AI plugin trong IntelliJ (Junie hoặc GitHub Copilot)
□ npm install -g @fission-ai/openspec@latest
□ openspec --version  →  trả về version

Nếu dùng Junie:
□ openspec init --tools junie --force
□ Verify: ls .junie\skills\  →  4 openspec skills

Nếu dùng GitHub Copilot:
□ openspec init --tools github-copilot --force  
□ Verify: ls .github\prompts\  →  4 .prompt.md files

Cả hai trường hợp:
□ Chỉnh sửa openspec/config.yaml (tech stack của project)
□ Restart IntelliJ
□ Test: mở AI chat, gõ /opsx:propose "test"  →  AI phản hồi
□ openspec list  →  "No active changes found."
```

---

## Troubleshooting IntelliJ

### Junie không nhận `/opsx:propose`

Kiểm tra `.junie/skills/` có files không:
```powershell
ls .junie\skills\
# Phải có: openspec-propose, openspec-apply-change, openspec-archive-change, openspec-explore
```

Nếu không có, chạy lại:
```powershell
openspec init --tools junie --force
```

Restart IntelliJ.

### Copilot không nhận `/opsx:*` commands

Kiểm tra `.github/prompts/`:
```powershell
ls .github\prompts\
# Phải có: opsx-propose.prompt.md, opsx-apply.prompt.md...
```

Đảm bảo **GitHub Copilot Chat** (không phải chỉ Copilot completion) đã được bật.

### SpecKit slash commands không có trong Copilot

Kiểm tra `.specify/` đã được tạo:
```powershell
ls .specify\
```

Chạy lại:
```powershell
specify init --here --integration copilot --ignore-agent-tools --force
```

### `specify` không tìm thấy

```powershell
$env:PATH = "C:\Users\$env:USERNAME\.local\bin;$env:PATH"
specify --version
```
