"""
Static Code Scanner & Actual Dependency Graph Extractor
Parses Java source files, extracts packages, imports, annotations, and communication patterns.
Constructs the actual dependency graph between Bounded Contexts.
"""

import os
import re
import json
from pathlib import Path
from typing import Dict, List, Any, Optional

class JavaFileMetadata:
    def __init__(self, file_path: Path, rel_path: str):
        self.file_path = file_path
        self.rel_path = rel_path.replace("\\", "/")
        self.package_name: str = ""
        self.class_name: str = ""
        self.imports: List[Dict[str, Any]] = [] # {"package": str, "line": int, "raw": str}
        self.annotations: List[str] = []
        self.fields: List[Dict[str, Any]] = [] # {"type": str, "name": str, "line": int}
        self.context: str = "unknown"
        self.is_entity: bool = False
        self.is_repository: bool = False
        self.is_controller: bool = False
        self.is_service: bool = False
        self.is_listener: bool = False
        self.is_client: bool = False
        self.event_publishes: List[str] = []
        self.event_listens: List[str] = []
        self.rest_calls: List[str] = []

class DependencyScanner:
    def __init__(self, root_dir: str, spec_path: Optional[str] = None):
        self.root_dir = Path(root_dir)
        self.spec: Dict[str, Any] = {}
        if spec_path and os.path.exists(spec_path):
            with open(spec_path, "r", encoding="utf-8") as f:
                self.spec = json.load(f)

    def scan(self) -> Dict[str, Any]:
        """Scans all Java files in root_dir and constructs the dependency model."""
        java_files: List[JavaFileMetadata] = []
        for path in self.root_dir.rglob("*.java"):
            rel_path = str(path.relative_to(self.root_dir))
            # Skip build/target directories if any
            if "target" in path.parts or ".git" in path.parts:
                continue
            meta = self._parse_java_file(path, rel_path)
            java_files.append(meta)

        # Build Context-level graph
        contexts = self._detect_contexts(java_files)
        edges = self._extract_dependencies(java_files, contexts)
        mermaid_code = self._generate_mermaid(contexts, edges)

        return {
            "root_dir": str(self.root_dir),
            "total_files": len(java_files),
            "contexts": contexts,
            "edges": edges,
            "mermaid": mermaid_code,
            "raw_files": [
                {
                    "file": f.rel_path,
                    "package": f.package_name,
                    "class": f.class_name,
                    "context": f.context,
                    "imports": f.imports,
                    "annotations": f.annotations,
                    "event_publishes": f.event_publishes,
                    "event_listens": f.event_listens
                }
                for f in java_files
            ]
        }

    def _parse_java_file(self, file_path: Path, rel_path: str) -> JavaFileMetadata:
        meta = JavaFileMetadata(file_path, rel_path)
        try:
            with open(file_path, "r", encoding="utf-8", errors="replace") as f:
                lines = f.readlines()
        except Exception:
            return meta

        for idx, line in enumerate(lines, start=1):
            stripped = line.strip()

            # Package
            pkg_match = re.match(r"^package\s+([\w\.]+);", stripped)
            if pkg_match:
                meta.package_name = pkg_match.group(1)

            # Imports
            imp_match = re.match(r"^import\s+(static\s+)?([\w\.\*]+);", stripped)
            if imp_match:
                import_target = imp_match.group(2)
                meta.imports.append({
                    "target": import_target,
                    "line": idx,
                    "raw": stripped
                })

            # Class / Interface
            cls_match = re.search(r"\b(class|interface|enum|record)\s+(\w+)", stripped)
            if cls_match and not meta.class_name:
                meta.class_name = cls_match.group(2)

            # Annotations
            ann_matches = re.findall(r"@(\w+)", stripped)
            for ann in ann_matches:
                meta.annotations.append(ann)
                if ann in ["Entity", "Table"]:
                    meta.is_entity = True
                elif ann in ["Repository"]:
                    meta.is_repository = True
                elif ann in ["RestController", "Controller"]:
                    meta.is_controller = True
                elif ann in ["Service"]:
                    meta.is_service = True
                elif "Listener" in ann or "Handler" in ann:
                    meta.is_listener = True

            # Injected Fields / Repository / Client dependencies
            field_match = re.search(r"private\s+final\s+([A-Z]\w+)\s+(\w+);", stripped)
            if field_match:
                meta.fields.append({
                    "type": field_match.group(1),
                    "name": field_match.group(2),
                    "line": idx
                })

            # Event Publishing detection
            if "publish" in stripped.lower() or "converandsend" in stripped.lower():
                ev_match = re.search(r"publish(\w+)\((.*?)\)", stripped)
                if ev_match:
                    meta.event_publishes.append(ev_match.group(1))

            # Event Listening detection
            if "@RabbitListener" in stripped or "onPayment" in stripped or "onOrder" in stripped:
                handler_match = re.search(r"void\s+(on\w+)\((.*?)\)", stripped)
                if handler_match:
                    meta.event_listens.append(handler_match.group(1))

            # REST / HTTP calls detection
            if "restTemplate" in stripped or "webClient" in stripped or "feign" in stripped.lower():
                meta.rest_calls.append(stripped)

        # Detect Context
        meta.context = self._determine_context(rel_path, meta.package_name)
        return meta

    def _determine_context(self, rel_path: str, package_name: str) -> str:
        # Match from spec if available
        spec_contexts = self.spec.get("contexts", {})
        for ctx_id, ctx_data in spec_contexts.items():
            if ctx_id in rel_path:
                return ctx_id
            pkg_pref = ctx_data.get("package_prefix", "")
            if pkg_pref and package_name.startswith(pkg_pref):
                return ctx_id

        # Fallback to directory heuristic
        parts = rel_path.split("/")
        if len(parts) > 1 and "service" in parts[0]:
            return parts[0]
        if "api-gateway" in parts:
            return "api-gateway"
        if "ecommerce" in package_name:
            # Monolith
            for sub in ["controller", "service", "repository", "model"]:
                if f".{sub}" in package_name:
                    return f"monolith-{sub}"
            return "monolith-core"

        return "shared"

    def _detect_contexts(self, files: List[JavaFileMetadata]) -> Dict[str, Dict[str, Any]]:
        contexts: Dict[str, Dict[str, Any]] = {}
        for f in files:
            ctx = f.context
            if ctx not in contexts:
                spec_info = self.spec.get("contexts", {}).get(ctx, {})
                contexts[ctx] = {
                    "id": ctx,
                    "name": spec_info.get("name", ctx.replace("-", " ").title()),
                    "package_prefix": spec_info.get("package_prefix", f.package_name),
                    "files_count": 0,
                    "classes": [],
                    "entities": [],
                    "repositories": [],
                    "services": [],
                    "controllers": []
                }
            contexts[ctx]["files_count"] += 1
            if f.class_name:
                contexts[ctx]["classes"].append(f.class_name)
                if f.is_entity:
                    contexts[ctx]["entities"].append(f.class_name)
                if f.is_repository:
                    contexts[ctx]["repositories"].append(f.class_name)
                if f.is_service:
                    contexts[ctx]["services"].append(f.class_name)
                if f.is_controller:
                    contexts[ctx]["controllers"].append(f.class_name)

        return contexts

    def _extract_dependencies(self, files: List[JavaFileMetadata], contexts: Dict[str, Any]) -> List[Dict[str, Any]]:
        edges: List[Dict[str, Any]] = []
        edge_set = set()

        for f in files:
            source_ctx = f.context

            # 1. Analyze Imports
            for imp in f.imports:
                target_pkg = imp["target"]
                # Determine if this import crosses context
                for target_ctx, ctx_data in contexts.items():
                    if target_ctx == source_ctx:
                        continue
                    prefix = ctx_data.get("package_prefix", "")
                    if prefix and target_pkg.startswith(prefix):
                        # Cross-context direct import found!
                        edge_key = (source_ctx, target_ctx, "DIRECT_IMPORT", imp["target"])
                        if edge_key not in edge_set:
                            edge_set.add(edge_key)
                            edges.append({
                                "source": source_ctx,
                                "target": target_ctx,
                                "type": "DIRECT_IMPORT",
                                "detail": f"import {imp['target']}",
                                "file": f.rel_path,
                                "line": imp["line"],
                                "severity": "CRITICAL"
                            })

            # 2. Analyze REST / Client Calls
            # e.g., InventoryClient in order-service calling inventory-service
            if "client" in f.rel_path.lower():
                if "inventory" in f.rel_path.lower() and source_ctx != "inventory-service":
                    edge_key = (source_ctx, "inventory-service", "REST_HTTP", "InventoryClient")
                    if edge_key not in edge_set:
                        edge_set.add(edge_key)
                        edges.append({
                            "source": source_ctx,
                            "target": "inventory-service",
                            "type": "REST_HTTP",
                            "detail": "HTTP REST Client call (GET /api/inventory/{id})",
                            "file": f.rel_path,
                            "line": 1,
                            "severity": "ALLOWED"
                        })
                if "user" in f.rel_path.lower() and source_ctx != "user-service":
                    edge_key = (source_ctx, "user-service", "REST_HTTP", "UserClient")
                    if edge_key not in edge_set:
                        edge_set.add(edge_key)
                        edges.append({
                            "source": source_ctx,
                            "target": "user-service",
                            "type": "REST_HTTP",
                            "detail": "HTTP REST Client call (GET /api/users/{id})",
                            "file": f.rel_path,
                            "line": 1,
                            "severity": "ALLOWED"
                        })

            # 3. Analyze Event Publications & Listeners (AMQP Broker)
            if f.event_publishes:
                for ev in f.event_publishes:
                    edge_key = (source_ctx, "message-broker", "AMQP_PUBLISH", ev)
                    if edge_key not in edge_set:
                        edge_set.add(edge_key)
                        edges.append({
                            "source": source_ctx,
                            "target": "message-broker",
                            "type": "AMQP_PUBLISH",
                            "detail": f"Publishes event '{ev}' to exchange",
                            "file": f.rel_path,
                            "line": 1,
                            "severity": "ALLOWED"
                        })

            if f.event_listens or "listener" in f.rel_path.lower():
                edge_key = ("message-broker", source_ctx, "AMQP_CONSUME", "EventListener")
                if edge_key not in edge_set:
                    edge_set.add(edge_key)
                    edges.append({
                        "source": "message-broker",
                        "target": source_ctx,
                        "type": "AMQP_CONSUME",
                        "detail": f"Consumes domain events from queue ({f.class_name})",
                        "file": f.rel_path,
                        "line": 1,
                        "severity": "ALLOWED"
                    })

        # Add Gateway route edges if api-gateway exists
        if "api-gateway" in contexts:
            for target_ctx in contexts:
                if target_ctx != "api-gateway" and not target_ctx.startswith("monolith"):
                    edges.append({
                        "source": "api-gateway",
                        "target": target_ctx,
                        "type": "REST_HTTP",
                        "detail": "Reverse Proxy Route",
                        "file": "api-gateway/src/main/resources/application.yml",
                        "line": 1,
                        "severity": "ALLOWED"
                    })

        return edges

    def _generate_mermaid(self, contexts: Dict[str, Any], edges: List[Dict[str, Any]]) -> str:
        lines = [
            "flowchart TD",
            "    %% Architectural Dependency Graph (Reverse-Engineered from Source Code)"
        ]

        # Add Gateway & Client
        lines.append("    subgraph Ingress [Client & Gateway]")
        lines.append('        Client["📱 Web / Mobile Client"]')
        if "api-gateway" in contexts:
            lines.append('        Gateway["🚪 API Gateway (Spring Cloud)"]')
            lines.append("        Client --> Gateway")
        lines.append("    end")

        # Add Message Broker
        lines.append('    Broker[["🐰 RabbitMQ Message Broker (Async Events)"]]')

        # Add Contexts
        lines.append("    subgraph BoundedContexts [DDD Bounded Contexts]")
        for ctx_id, ctx in contexts.items():
            if ctx_id == "api-gateway":
                continue
            name = ctx.get("name", ctx_id)
            entity_str = ", ".join(ctx.get("entities", [])[:2])
            label = f'<b>{name}</b><br/><small>Entities: {entity_str or "None"}</small>'
            node_id = ctx_id.replace("-", "_")
            lines.append(f'        {node_id}["{label}"]')
        lines.append("    end")

        # Add Edges
        violation_indices = []
        edge_index = 0

        for edge in edges:
            src = edge["source"].replace("-", "_")
            tgt = edge["target"].replace("-", "_")
            if src == "api_gateway":
                src = "Gateway"
            if tgt == "message_broker":
                tgt = "Broker"
            if src == "message_broker":
                src = "Broker"

            edge_type = edge["type"]
            detail = edge["detail"]

            if edge_type == "DIRECT_IMPORT":
                # Critical violation!
                lines.append(f'    {src} == "❌ DIRECT IMPORT: {detail}" ==> {tgt}')
                violation_indices.append(edge_index)
            elif edge_type == "REST_HTTP":
                lines.append(f'    {src} -. "HTTP REST" .-> {tgt}')
            elif edge_type == "AMQP_PUBLISH":
                lines.append(f'    {src} -->|Publish Event| {tgt}')
            elif edge_type == "AMQP_CONSUME":
                lines.append(f'    {src} -->|Consume Event| {tgt}')
            else:
                lines.append(f'    {src} --> {tgt}')

            edge_index += 1

        # Style definitions
        lines.append("\n    %% Styling")
        lines.append("    classDef clean fill:#e1f5fe,stroke:#0288d1,stroke-width:2px;")
        lines.append("    classDef broker fill:#fff3e0,stroke:#f57c00,stroke-width:2px,stroke-dasharray: 3 3;")
        lines.append("    classDef violation fill:#ffebee,stroke:#d32f2f,stroke-width:3px;")
        lines.append("    class Broker broker;")

        return "\n".join(lines)


if __name__ == "__main__":
    import sys
    target = sys.argv[1] if len(sys.argv) > 1 else "demo-microservices"
    spec = "arch-guard/boundaries-spec.json"
    scanner = DependencyScanner(target, spec)
    result = scanner.scan()
    print(f"Scanned {result['total_files']} files across {len(result['contexts'])} contexts.")
    print(f"Detected {len(result['edges'])} dependencies.")
    print("\nMermaid:\n", result['mermaid'])
