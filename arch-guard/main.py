"""
Main CLI Entrypoint for AI Architecture Sentinel
Usage:
    python arch-guard/main.py [--target demo-microservices]
    python arch-guard/main.py --inject-violation   # Injects violation to test failure
    python arch-guard/main.py --clean              # Restores clean codebase
"""

import sys
import os
import argparse
import json
from pathlib import Path

# Add current dir to sys.path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from scanner import DependencyScanner
from ai_sentinel import AISentinel
from report_generator import ReportGenerator

VIOLATION_FILE = Path("demo-microservices/order-service/src/main/java/com/shop/order/service/OrderService.java")
VIOLATION_MARKER = "// [INJECTED_VIOLATION]"

def inject_violation():
    """Injects a cross-boundary Bounded Context violation into OrderService."""
    if not VIOLATION_FILE.exists():
        print(f"Error: Could not find {VIOLATION_FILE}")
        return False

    with open(VIOLATION_FILE, "r", encoding="utf-8") as f:
        content = f.read()

    if VIOLATION_MARKER in content:
        print("Violation already present in codebase.")
        return True

    # Inject forbidden imports
    injection = (
        f"\nimport com.shop.payment.model.PaymentEntity; {VIOLATION_MARKER}\n"
        f"import com.shop.payment.repository.PaymentRepository; {VIOLATION_MARKER}\n"
    )

    # Insert right after the package line
    package_idx = content.find("package com.shop.order.service;\n")
    if package_idx != -1:
        new_content = content[:package_idx + len("package com.shop.order.service;\n")] + injection + content[package_idx + len("package com.shop.order.service;\n"):]
        with open(VIOLATION_FILE, "w", encoding="utf-8") as f:
            f.write(new_content)
        print("Successfully injected Bounded Context violation into OrderService.java!")
        return True
    else:
        print("Could not locate package declaration in OrderService.java")
        return False

def clean_violation():
    """Removes all injected violations from OrderService."""
    if not VIOLATION_FILE.exists():
        return

    with open(VIOLATION_FILE, "r", encoding="utf-8") as f:
        lines = f.readlines()

    clean_lines = [l for l in lines if VIOLATION_MARKER not in l]

    with open(VIOLATION_FILE, "w", encoding="utf-8") as f:
        f.writelines(clean_lines)

    print("Cleaned all injected violations. Codebase restored to 100% clean state.")


def main():
    parser = argparse.ArgumentParser(description="AI-Driven Architecture Sentinel & CI/CD Gate")
    parser.add_argument("--target", default="demo-microservices", help="Target directory to analyze")
    parser.add_argument("--spec", default="arch-guard/boundaries-spec.json", help="Path to boundary spec JSON")
    parser.add_argument("--inject-violation", action="store_true", help="Inject sample violation into OrderService")
    parser.add_argument("--clean", action="store_true", help="Restore clean codebase")
    parser.add_argument("--offline", action="store_true", default=False, help="Force offline expert AI mode")
    parser.add_argument("--output-md", default="arch-guard/architecture-report.md", help="Markdown output path")
    parser.add_argument("--output-html", default="arch-guard/architecture-report.html", help="HTML output path")

    args = parser.parse_args()

    if args.clean:
        clean_violation()
        return 0

    if args.inject_violation:
        inject_violation()

    print(f"\nScanning architecture of: {args.target} ...")
    scanner = DependencyScanner(args.target, args.spec)
    scan_result = scanner.scan()

    spec_data = {}
    if os.path.exists(args.spec):
        with open(args.spec, "r", encoding="utf-8") as f:
            spec_data = json.load(f)

    sentinel = AISentinel(spec_data, scan_result)
    eval_result = sentinel.evaluate(force_offline=args.offline)

    # Generate Reports
    reporter = ReportGenerator(scan_result, eval_result)
    reporter.print_terminal()
    reporter.generate_markdown(args.output_md)
    reporter.generate_html(args.output_html)

    print(f"Generated Markdown report: {args.output_md}")
    print(f"Generated HTML visual report: {args.output_html}")

    # Return exit code based on gate
    return 0 if eval_result["gate_passed"] else 1


if __name__ == "__main__":
    sys.exit(main())
