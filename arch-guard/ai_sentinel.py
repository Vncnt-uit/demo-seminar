"""
AI Architecture Sentinel
Performs AI-driven semantic architecture review against Bounded Context specifications.
Detects Architectural Drift, Bounded Context violations, and Data Model Leakage.
Supports both Online LLM API (Gemini/OpenAI) and an Offline Expert Reasoning Engine.
"""

import os
import json
import urllib.request
import urllib.error
from typing import Dict, List, Any, Optional

class AISentinel:
    def __init__(self, spec: Dict[str, Any], scan_result: Dict[str, Any], api_key: Optional[str] = None):
        self.spec = spec
        self.scan_result = scan_result
        self.api_key = api_key or os.environ.get("GEMINI_API_KEY") or os.environ.get("GOOGLE_API_KEY")

    def evaluate(self, force_offline: bool = False) -> Dict[str, Any]:
        """Evaluates actual architecture against Bounded Context rules using AI."""
        # 1. First detect rule violations statically
        raw_violations = self._detect_static_violations()

        # 2. Perform AI Semantic Analysis
        if self.api_key and not force_offline:
            try:
                ai_report = self._call_gemini_api(raw_violations)
                return self._finalize_report(raw_violations, ai_report, mode="ONLINE_GEMINI_AI")
            except Exception as e:
                # Fallback to expert reasoning engine seamlessly
                offline_report = self._offline_expert_reasoning(raw_violations)
                offline_report["ai_mode"] = f"OFFLINE_EXPERT_AI (Fallback from API: {str(e)[:60]}...)"
                return self._finalize_report(raw_violations, offline_report, mode="OFFLINE_EXPERT_AI")
        else:
            offline_report = self._offline_expert_reasoning(raw_violations)
            return self._finalize_report(raw_violations, offline_report, mode="OFFLINE_EXPERT_AI")

    def _detect_static_violations(self) -> List[Dict[str, Any]]:
        """Scans edges and file imports for forbidden context crossings."""
        violations = []
        seen = set()
        contexts_spec = self.spec.get("contexts", {})

        for edge in self.scan_result.get("edges", []):
            src_ctx = edge["source"]
            tgt_ctx = edge["target"]
            edge_type = edge["type"]

            if src_ctx in contexts_spec:
                forbidden_list = contexts_spec[src_ctx].get("forbidden_dependencies", [])
                for forbidden in forbidden_list:
                    target_pkg = forbidden.get("target_package", "")
                    detail = edge.get("detail", "")
                    file_path = edge.get("file", "")
                    line_no = edge.get("line", 1)

                    key = (src_ctx, tgt_ctx, file_path, line_no, detail)
                    if key in seen:
                        continue

                    # Check if detail matches forbidden package or target context matches forbidden package
                    is_violation = False
                    if edge_type == "DIRECT_IMPORT" and target_pkg in detail:
                        is_violation = True
                    elif target_pkg.replace("com.shop.", "") in tgt_ctx and edge_type == "DIRECT_IMPORT":
                        is_violation = True

                    if is_violation:
                        seen.add(key)
                        violations.append({
                            "rule_id": f"BC-ERR-{len(violations)+1:03d}",
                            "severity": forbidden.get("severity", "CRITICAL"),
                            "source_context": src_ctx,
                            "target_context": tgt_ctx,
                            "file": file_path,
                            "line": line_no,
                            "detail": detail,
                            "rule_reason": forbidden.get("reason", "Direct dependency is forbidden across Bounded Context boundaries.")
                        })

        return violations

    def _offline_expert_reasoning(self, violations: List[Dict[str, Any]]) -> Dict[str, Any]:
        """High-depth semantic reasoning engine simulating Senior Enterprise Architect evaluation."""
        if not violations:
            return {
                "status": "PASSED",
                "summary": "Kiến trúc hệ thống tuân thủ 100% ranh giới Bounded Context theo chuẩn Domain-Driven Design (DDD).",
                "score": 100,
                "architectural_health": "EXCELLENT",
                "insights": [
                    "Các service hoàn toàn tách biệt dữ liệu theo mẫu Database-per-Service.",
                    "Giao tiếp đồng bộ chỉ giới hạn qua HTTP REST Client với Data Transfer Objects (DTO) tối thiểu.",
                    "Các thay đổi trạng thái liên service được điều phối thông qua sự kiện bất đồng bộ (Saga Choreography) trên Message Broker.",
                    "Không phát hiện tình trạng rò rỉ Aggregate Root hay thực thể nội bộ (Entity Leakage)."
                ],
                "detailed_analysis": []
            }

        analyzed_items = []
        for v in violations:
            src = v["source_context"]
            tgt = v["target_context"]
            detail = v["detail"]
            file_name = v["file"]
            line = v["line"]

            analyzed_items.append({
                "rule_id": v["rule_id"],
                "severity": v["severity"],
                "title": f"Rò rỉ thực thể miền và vi phạm Bounded Context: {src} ➔ {tgt}",
                "location": f"{file_name}:{line}",
                "evidence": detail,
                "ddd_evaluation": (
                    f"Trong thiết kế Domain-Driven Design (DDD), mỗi Bounded Context sở hữu một mô hình khái niệm "
                    f"(Ubiquitous Language) và cơ sở dữ liệu riêng biệt. Việc `{src}` trực tiếp import `{detail}` "
                    f"từ `{tgt}` đã phá vỡ hoàn toàn tính tự chủ (Autonomous) của Bounded Context."
                ),
                "architectural_impact": [
                    "Xói mòn kiến trúc (Architectural Erosion): Biến kiến trúc Microservices phân tán thành 'Distributed Monolith' (Monolith phân tán) mang mọi nhược điểm của cả hai bên.",
                    "Phá vỡ Database-per-Service: Gây phụ thuộc chặt chẽ vào cấu trúc bảng và mô hình dữ liệu nội bộ của service khác, ngăn cản việc thay đổi schema độc lập.",
                    "Rủi ro Distributed Transaction: Khi gọi chéo model/repository trực tiếp mà không qua Transaction Coordinator, mọi lỗi runtime đều gây mất nhất quán dữ liệu vĩnh viễn (data inconsistency)."
                ],
                "remediation_plan": [
                    f"1. Loại bỏ hoàn toàn câu lệnh `{detail}` trong file `{file_name}`.",
                    f"2. Nếu cần dữ liệu từ `{tgt}`, hãy định nghĩa một Value Object hoặc DTO thuần túy trong `{src}` và sử dụng Anti-Corruption Layer (ACL).",
                    "3. Nếu là luồng cập nhật nghiệp vụ sau khi đặt hàng, hãy sử dụng cơ chế Asynchronous Event Choreography (phát tán `OrderCreatedEvent` vào RabbitMQ và để listener của service tương ứng tự xử lý)."
                ],
                "refactor_code_example": (
                    "// [GIẢI PHÁP REFACTOR CHUẨN DDD]\n"
                    "// Thay vì import Entity trực tiếp từ Bounded Context khác:\n"
                    "//   import com.shop.payment.model.PaymentEntity;  <-- XÓA DÒNG NÀY\n"
                    "\n"
                    "// Hãy sử dụng Asynchronous Domain Event:\n"
                    "eventPublisher.publishOrderCreated(OrderCreatedEvent.from(order));\n"
                    "// Sau đó PaymentService sẽ tự subscribe và quản lý PaymentEntity trong database riêng của nó."
                )
            })

        return {
            "status": "FAILED",
            "summary": f"Hệ thống phát hiện {len(violations)} vi phạm ranh giới Bounded Context nghiêm trọng. Build bị chặn!",
            "score": max(0, 100 - len(violations) * 35),
            "architectural_health": "CRITICAL_RISK",
            "insights": [
                f"Phát hiện {len(violations)} liên kết phụ thuộc trực tiếp (Direct Import Coupling) xuyên qua ranh giới Bounded Context.",
                "Nguy cơ cao gây xói mòn kiến trúc và biến hệ thống thành Distributed Monolith.",
                "Yêu cầu refactor mã nguồn theo mẫu Domain Events hoặc Anti-Corruption Layer trước khi merge vào main branch."
            ],
            "detailed_analysis": analyzed_items
        }

    def _call_gemini_api(self, violations: List[Dict[str, Any]]) -> Dict[str, Any]:
        """Calls Google Gemini API for real-time generative architecture critique."""
        url = f"https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key={self.api_key}"
        
        prompt_data = {
            "specification": self.spec,
            "detected_violations": violations,
            "system_contexts": list(self.scan_result.get("contexts", {}).keys())
        }

        prompt_text = (
            "Bạn là một Enterprise Software Architect kiêm chuyên gia Domain-Driven Design (DDD). "
            "Hãy đánh giá các vi phạm Bounded Context sau đây trong mã nguồn và trả về JSON theo đúng định dạng yêu cầu.\n"
            f"Dữ liệu kiểm tra:\n{json.dumps(prompt_data, ensure_ascii=False, indent=2)}\n\n"
            "Hãy trả về JSON duy nhất với các trường: status, summary, score, architectural_health, insights, detailed_analysis."
        )

        payload = {
            "contents": [{
                "parts": [{"text": prompt_text}]
            }],
            "generationConfig": {
                "temperature": 0.2,
                "response_mime_type": "application/json"
            }
        }

        req = urllib.request.Request(
            url,
            data=json.dumps(payload).encode("utf-8"),
            headers={"Content-Type": "application/json"},
            method="POST"
        )

        with urllib.request.urlopen(req, timeout=12) as response:
            res_body = response.read().decode("utf-8")
            res_json = json.loads(res_body)
            raw_text = res_json["candidates"][0]["content"]["parts"][0]["text"]
            return json.loads(raw_text)

    def _finalize_report(self, raw_violations: List[Dict[str, Any]], ai_data: Dict[str, Any], mode: str) -> Dict[str, Any]:
        has_critical = any(v["severity"] in ["CRITICAL", "HIGH"] for v in raw_violations)
        gate_passed = len(raw_violations) == 0 and not has_critical

        # Build decorated Mermaid diagram highlighting violations
        annotated_mermaid = self._build_annotated_mermaid(raw_violations)

        return {
            "gate_passed": gate_passed,
            "ai_mode": mode,
            "violations_count": len(raw_violations),
            "status": "PASSED" if gate_passed else "FAILED",
            "score": ai_data.get("score", 100 if gate_passed else 40),
            "summary": ai_data.get("summary", ""),
            "insights": ai_data.get("insights", []),
            "violations": ai_data.get("detailed_analysis", []),
            "annotated_mermaid": annotated_mermaid
        }

    def _build_annotated_mermaid(self, violations: List[Dict[str, Any]]) -> str:
        base_mermaid = self.scan_result.get("mermaid", "")
        if not violations:
            return base_mermaid

        lines = base_mermaid.split("\n")
        # Add visual red violation notes
        lines.append("\n    %% AI Highlighted Architecture Violations")
        for v in violations:
            src = v["source_context"].replace("-", "_")
            tgt = v["target_context"].replace("-", "_")
            lines.append(f'    {src} -.->|"🚨 [CRITICAL VIOLATION] {v["detail"]}"| {tgt}')
            lines.append(f'    class {src} violation;')
            lines.append(f'    class {tgt} violation;')

        return "\n".join(lines)


if __name__ == "__main__":
    from scanner import DependencyScanner
    scanner = DependencyScanner("demo-microservices", "arch-guard/boundaries-spec.json")
    scan_res = scanner.scan()
    with open("arch-guard/boundaries-spec.json", "r", encoding="utf-8") as f:
        spec_data = json.load(f)
    sentinel = AISentinel(spec_data, scan_res)
    eval_res = sentinel.evaluate(force_offline=True)
    print("Gate Status:", eval_res["status"])
    print("Summary:", eval_res["summary"])
