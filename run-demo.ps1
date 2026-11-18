# ==============================================================================
# AI Architecture Sentinel - Interactive Seminar Demo Runner
# ==============================================================================

function Show-Header {
    Clear-Host
    Write-Host "========================================================================" -ForegroundColor Cyan
    Write-Host "     🏛️  AI ARCHITECTURE SENTINEL & BOUNDED CONTEXT CI/CD DEMO          " -ForegroundColor Yellow
    Write-Host "========================================================================" -ForegroundColor Cyan
    Write-Host "1. [PASS] Kiểm tra kiến trúc Microservices SẠCH (Clean Architecture)"
    Write-Host "2. [FAIL] Tự động Inject VI PHẠM Bounded Context & Chạy AI Sentinel"
    Write-Host "3. [CLEAN] Khôi phục mã nguồn về trạng thái SẠCH ban đầu"
    Write-Host "4. [VIEW] Mở Báo cáo Trực quan (Interactive HTML Report) trên Trình duyệt"
    Write-Host "5. [TEST] Kiểm tra mã nguồn Monolith"
    Write-Host "0. Thoát"
    Write-Host "========================================================================" -ForegroundColor Cyan
}

do {
    Show-Header
    $choice = Read-Host "Nhập lựa chọn của bạn (0-5)"
    switch ($choice) {
        "1" {
            Write-Host "`nĐang chạy kiểm tra kiến trúc sạch..." -ForegroundColor Green
            python arch-guard/main.py --target demo-microservices
            Write-Host "`nNhấn phím bất kỳ để tiếp tục..." -ForegroundColor Gray
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
        "2" {
            Write-Host "`nĐang inject lỗi vi phạm Bounded Context và phân tích AI..." -ForegroundColor Red
            python arch-guard/main.py --inject-violation --target demo-microservices
            Write-Host "`nNhấn phím bất kỳ để tiếp tục..." -ForegroundColor Gray
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
        "3" {
            Write-Host "`nĐang khôi phục mã nguồn sạch..." -ForegroundColor Cyan
            python arch-guard/main.py --clean
            Write-Host "`nNhấn phím bất kỳ để tiếp tục..." -ForegroundColor Gray
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
        "4" {
            $htmlPath = "$PSScriptRoot\arch-guard\architecture-report.html"
            if (Test-Path $htmlPath) {
                Write-Host "`nĐang mở báo cáo HTML trên trình duyệt..." -ForegroundColor Green
                Start-Process $htmlPath
            } else {
                Write-Host "`nChưa có báo cáo HTML. Vui lòng chạy lựa chọn 1 hoặc 2 trước!" -ForegroundColor Yellow
            }
            Start-Sleep -Seconds 2
        }
        "5" {
            Write-Host "`nĐang kiểm tra Monolith..." -ForegroundColor Yellow
            python arch-guard/main.py --target demo-monolith
            Write-Host "`nNhấn phím bất kỳ để tiếp tục..." -ForegroundColor Gray
            $null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
        }
        "0" {
            Write-Host "`nTạm biệt!" -ForegroundColor Cyan
            break
        }
        default {
            Write-Host "`nLựa chọn không hợp lệ." -ForegroundColor Red
            Start-Sleep -Seconds 1
        }
    }
} while ($choice -ne "0")
