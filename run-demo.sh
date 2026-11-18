#!/usr/bin/env bash
# ==============================================================================
# AI Architecture Sentinel - Interactive Seminar Demo Runner
# ==============================================================================

while true; do
    clear
    echo "========================================================================"
    echo "     🏛️  AI ARCHITECTURE SENTINEL & BOUNDED CONTEXT CI/CD DEMO          "
    echo "========================================================================"
    echo "1. [PASS] Kiểm tra kiến trúc Microservices SẠCH (Clean Architecture)"
    echo "2. [FAIL] Tự động Inject VI PHẠM Bounded Context & Chạy AI Sentinel"
    echo "3. [CLEAN] Khôi phục mã nguồn về trạng thái SẠCH ban đầu"
    echo "4. [VIEW] Mở Báo cáo Trực quan (Interactive HTML Report) trên Trình duyệt"
    echo "5. [TEST] Kiểm tra mã nguồn Monolith"
    echo "0. Thoát"
    echo "========================================================================"
    read -p "Nhập lựa chọn của bạn (0-5): " choice

    case $choice in
        1)
            echo -e "\nĐang chạy kiểm tra kiến trúc sạch..."
            python3 arch-guard/main.py --target demo-microservices
            read -p "Nhấn Enter để tiếp tục..."
            ;;
        2)
            echo -e "\nĐang inject lỗi vi phạm Bounded Context và phân tích AI..."
            python3 arch-guard/main.py --inject-violation --target demo-microservices
            read -p "Nhấn Enter để tiếp tục..."
            ;;
        3)
            echo -e "\nĐang khôi phục mã nguồn sạch..."
            python3 arch-guard/main.py --clean
            read -p "Nhấn Enter để tiếp tục..."
            ;;
        4)
            if [ -f "arch-guard/architecture-report.html" ]; then
                xdg-open arch-guard/architecture-report.html 2>/dev/null || open arch-guard/architecture-report.html 2>/dev/null || echo "File: arch-guard/architecture-report.html"
            else
                echo "Chưa có báo cáo HTML. Vui lòng chạy lựa chọn 1 hoặc 2 trước!"
                sleep 2
            fi
            ;;
        5)
            echo -e "\nĐang kiểm tra Monolith..."
            python3 arch-guard/main.py --target demo-monolith
            read -p "Nhấn Enter để tiếp tục..."
            ;;
        0)
            echo "Tạm biệt!"
            exit 0
            ;;
        *)
            echo "Lựa chọn không hợp lệ."
            sleep 1
            ;;
    esac
done
