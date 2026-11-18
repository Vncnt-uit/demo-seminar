"""
Architecture Governance Report Generator
Generates:
1. Terminal colored output for CLI/local demo
2. GitHub Actions Markdown Report (Step Summary / PR Comment)
3. Standalone Interactive HTML Report with live Mermaid.js rendering
"""

import os
from typing import Dict, Any, List

class ReportGenerator:
    def __init__(self, scan_result: Dict[str, Any], evaluation: Dict[str, Any]):
        self.scan_result = scan_result
        self.eval = evaluation

    def print_terminal(self):
        """Prints high-impact colored output for CLI presentation."""
        status = self.eval["status"]
        score = self.eval["score"]
        ai_mode = self.eval["ai_mode"]

        # ANSI Colors
        CYAN = "\033[96m"
        GREEN = "\033[92m"
        RED = "\033[91m"
        YELLOW = "\033[93m"
        BOLD = "\033[1m"
        RESET = "\033[0m"

        status_color = GREEN if status == "PASSED" else RED

        print(f"\n{BOLD}{CYAN}========================================================================{RESET}")
        print(f"{BOLD}{CYAN}           🤖 AI-DRIVEN ARCHITECTURE SENTINEL & CI/CD GATE             {RESET}")
        print(f"{BOLD}{CYAN}========================================================================{RESET}")
        print(f"Target:        {self.scan_result['root_dir']}")
        print(f"Scanned Files: {self.scan_result['total_files']} files across {len(self.scan_result['contexts'])} Bounded Contexts")
        print(f"AI Engine:     {ai_mode}")
        print(f"Arch Score:    {status_color}{score}/100{RESET}")
        print(f"CI/CD Gate:    {status_color}{BOLD}[ {status} ]{RESET}")
        print(f"Summary:       {self.eval['summary']}\n")

        print(f"{BOLD}Architectural Insights:{RESET}")
        for insight in self.eval.get("insights", []):
            bullet = f"{GREEN}✓{RESET}" if status == "PASSED" else f"{RED}✗{RESET}"
            print(f"  {bullet} {insight}")

        violations = self.eval.get("violations", [])
        if violations:
            print(f"\n{BOLD}{RED}🚨 CRITICAL BOUNDED CONTEXT VIOLATIONS ({len(violations)}):{RESET}")
            for idx, v in enumerate(violations, 1):
                print(f"\n  {BOLD}[{idx}] {v['title']} ({v['severity']}){RESET}")
                print(f"      Location: {YELLOW}{v['location']}{RESET}")
                print(f"      Evidence: {RED}{v['evidence']}{RESET}")
                print(f"      DDD Root Cause: {v['ddd_evaluation']}")
                print(f"      {BOLD}Remediation:{RESET}")
                for rem in v["remediation_plan"]:
                    print(f"        • {rem}")

        print(f"\n{BOLD}{CYAN}========================================================================{RESET}\n")

    def generate_markdown(self, output_path: str = "architecture-report.md") -> str:
        """Generates standard GitHub Markdown report."""
        status = self.eval["status"]
        status_badge = "🟢 **PASSED**" if status == "PASSED" else "🔴 **FAILED**"
        
        md = []
        md.append("# 🏛️ Architecture Governance & AI Bounded Context Report\n")
        md.append(f"> **CI/CD Quality Gate**: {status_badge} | **Architecture Health Score**: `{self.eval['score']}/100` | **AI Mode**: `{self.eval['ai_mode']}`\n")
        
        md.append("## 1. Executive Summary")
        md.append(self.eval["summary"])
        md.append("\n### Key Architectural Insights:")
        for ins in self.eval.get("insights", []):
            md.append(f"- {ins}")
        
        md.append("\n## 2. Reverse-Engineered Dependency Graph (Actual Architecture)")
        md.append("```mermaid")
        md.append(self.eval.get("annotated_mermaid", self.scan_result["mermaid"]))
        md.append("```\n")

        violations = self.eval.get("violations", [])
        if violations:
            md.append("## 3. 🚨 Detected Bounded Context Violations")
            for idx, v in enumerate(violations, 1):
                md.append(f"### Violation #{idx}: {v['title']}")
                md.append(f"- **Severity**: `{v['severity']}`")
                md.append(f"- **Location**: `{v['location']}`")
                md.append(f"- **Evidence**: `{v['evidence']}`")
                md.append(f"\n**DDD Architectural Analysis**:\n{v['ddd_evaluation']}\n")
                md.append("**Architectural Impacts:**")
                for imp in v["architectural_impact"]:
                    md.append(f"- ⚠️ {imp}")
                md.append("\n**Recommended Remediation Plan:**")
                for rem in v["remediation_plan"]:
                    md.append(f"- {rem}")
                md.append("\n**Refactoring Code Example:**")
                md.append("```java")
                md.append(v.get("refactor_code_example", "// Refactor to domain event or DTO"))
                md.append("```\n")
        else:
            md.append("## 3. ✅ Ranh giới Bounded Context hoàn toàn sạch sẽ!")
            md.append("Mọi dịch vụ tuân thủ nguyên tắc **Database-per-Service** và không có hiện tượng rò rỉ mã nguồn giữa các ngữ cảnh.")

        content = "\n".join(md)
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(content)
        return content

    def generate_html(self, output_path: str = "architecture-report.html") -> str:
        """Generates interactive presentation-ready HTML report with live Mermaid."""
        status = self.eval["status"]
        is_passed = status == "PASSED"
        status_color = "#10b981" if is_passed else "#ef4444"
        badge_bg = "#ecfdf5" if is_passed else "#fef2f2"
        badge_text = "PASSED (Clean Architecture)" if is_passed else "FAILED (Bounded Context Violation)"

        mermaid_code = self.eval.get("annotated_mermaid", self.scan_result["mermaid"])

        violations_html = ""
        violations = self.eval.get("violations", [])
        if violations:
            for idx, v in enumerate(violations, 1):
                remediation_list = "".join(f"<li>{r}</li>" for r in v["remediation_plan"])
                impact_list = "".join(f"<li>{imp}</li>" for imp in v["architectural_impact"])
                violations_html += f"""
                <div class="violation-card">
                    <div class="violation-header">
                        <span class="badge badge-danger">CRITICAL #{idx}</span>
                        <h3>{v['title']}</h3>
                    </div>
                    <p><b>📍 Vị trí:</b> <code>{v['location']}</code> | <b>Bằng chứng:</b> <code class="evidence">{v['evidence']}</code></p>
                    <div class="analysis-box">
                        <b>Phân tích lý thuyết DDD (Evans & Fowler):</b>
                        <p>{v['ddd_evaluation']}</p>
                    </div>
                    <div class="impact-box">
                        <b>Hậu quả kiến trúc (Architectural Drift):</b>
                        <ul>{impact_list}</ul>
                    </div>
                    <div class="remediation-box">
                        <b>Giải pháp khắc phục chuẩn:</b>
                        <ul>{remediation_list}</ul>
                        <pre><code>{v.get('refactor_code_example', '')}</code></pre>
                    </div>
                </div>
                """
        else:
            violations_html = """
            <div class="clean-card">
                <div class="check-icon">✓</div>
                <h3>Tuyệt vời! Kiến trúc hoàn toàn tuân thủ ranh giới Bounded Context.</h3>
                <p>Mọi phụ thuộc giữa các dịch vụ đều thông qua giao thức phân tán (REST API hoặc RabbitMQ AMQP Events). Không có hiện tượng rò rỉ cơ sở dữ liệu hay mô hình thực thể.</p>
            </div>
            """

        insights_html = "".join(f"<li>{ins}</li>" for ins in self.eval.get("insights", []))

        html = f"""<!DOCTYPE html>
<html lang="vi">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>AI Architecture Sentinel Report</title>
    <script src="https://cdn.jsdelivr.net/npm/mermaid@10/dist/mermaid.min.js"></script>
    <script>mermaid.initialize({{ startOnLoad: true, theme: 'default' }});</script>
    <style>
        :root {{
            --primary: #2563eb;
            --success: #10b981;
            --danger: #ef4444;
            --bg: #f8fafc;
            --card-bg: #ffffff;
            --text: #1e293b;
        }}
        body {{
            font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
            background: var(--bg);
            color: var(--text);
            margin: 0;
            padding: 30px 20px;
        }}
        .container {{
            max-width: 1100px;
            margin: 0 auto;
        }}
        .header {{
            background: var(--card-bg);
            padding: 30px;
            border-radius: 12px;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
            margin-bottom: 25px;
            display: flex;
            justify-content: space-between;
            align-items: center;
        }}
        .badge {{
            padding: 8px 16px;
            border-radius: 9999px;
            font-weight: 700;
            font-size: 0.9rem;
            display: inline-block;
        }}
        .badge-status {{
            background: {badge_bg};
            color: {status_color};
            border: 2px solid {status_color};
        }}
        .badge-danger {{
            background: #fef2f2;
            color: #ef4444;
            border: 1px solid #ef4444;
        }}
        .card {{
            background: var(--card-bg);
            padding: 25px;
            border-radius: 12px;
            box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
            margin-bottom: 25px;
        }}
        .card h2 {{
            margin-top: 0;
            border-bottom: 2px solid #e2e8f0;
            padding-bottom: 12px;
            color: #0f172a;
        }}
        .mermaid {{
            background: #ffffff;
            padding: 20px;
            border-radius: 8px;
            text-align: center;
            overflow-x: auto;
        }}
        .violation-card {{
            background: #fff5f5;
            border: 1px solid #fecaca;
            border-left: 6px solid #ef4444;
            padding: 20px;
            border-radius: 8px;
            margin-bottom: 20px;
        }}
        .violation-header {{
            display: flex;
            align-items: center;
            gap: 12px;
        }}
        .evidence {{
            color: #dc2626;
            background: #fee2e2;
            padding: 2px 6px;
            border-radius: 4px;
        }}
        .analysis-box {{
            background: #ffffff;
            padding: 15px;
            border-radius: 6px;
            margin-top: 10px;
            border: 1px solid #e2e8f0;
        }}
        .impact-box {{
            background: #fffbeb;
            padding: 15px;
            border-radius: 6px;
            margin-top: 10px;
            border: 1px solid #fef3c7;
        }}
        .remediation-box {{
            background: #f0fdf4;
            padding: 15px;
            border-radius: 6px;
            margin-top: 10px;
            border: 1px solid #dcfce7;
        }}
        pre {{
            background: #1e293b;
            color: #f8fafc;
            padding: 15px;
            border-radius: 6px;
            overflow-x: auto;
        }}
        .clean-card {{
            text-align: center;
            padding: 40px;
            color: #065f46;
        }}
        .check-icon {{
            font-size: 3rem;
            color: #10b981;
            margin-bottom: 10px;
        }}
    </style>
</head>
<body>
    <div class="container">
        <div class="header">
            <div>
                <h1 style="margin:0 0 8px 0;">🤖 AI-Driven Architecture Sentinel</h1>
                <p style="margin:0;color:#64748b;">Automated Reverse-Engineering & Bounded Context Fitness Gate</p>
            </div>
            <div>
                <span class="badge badge-status">{badge_text}</span>
            </div>
        </div>

        <div class="card">
            <h2>📊 Tổng quan Kiến trúc & Điểm đánh giá (Score: {self.eval['score']}/100)</h2>
            <p><b>Đánh giá tổng quát:</b> {self.eval['summary']}</p>
            <ul>{insights_html}</ul>
        </div>

        <div class="card">
            <h2>🗺️ Biểu đồ Phụ thuộc Thực tế (Reverse-Engineered Dependency Graph)</h2>
            <p style="color:#64748b; font-size: 0.9rem;">Biểu đồ được trích xuất tĩnh trực tiếp từ mã nguồn Java (Imports, HTTP REST Clients, RabbitMQ Events).</p>
            <div class="mermaid">
{mermaid_code}
            </div>
        </div>

        <div class="card">
            <h2>🔍 Chi tiết Kiểm soát Ranh giới (Architecture Conformance)</h2>
            {violations_html}
        </div>
    </div>
</body>
</html>
        """
        with open(output_path, "w", encoding="utf-8") as f:
            f.write(html)
        return html
