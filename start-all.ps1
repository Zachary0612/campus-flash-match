# 校园闪配 - 一键启动脚本

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  校园闪配 - 一键启动" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# 获取脚本所在目录
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Path

# 启动后端
Write-Host "正在启动后端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectRoot'; .\mvnw.cmd spring-boot:run"

# 等待后端启动
Write-Host "等待后端启动（5秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 5

# 启动前端
Write-Host "正在启动前端服务..." -ForegroundColor Green
Start-Process powershell -ArgumentList "-NoExit", "-Command", "cd '$projectRoot\frontend'; npm run dev"

# 等待前端启动
Write-Host "等待前端启动（3秒）..." -ForegroundColor Yellow
Start-Sleep -Seconds 3

# 打开浏览器
Write-Host "正在打开浏览器..." -ForegroundColor Green
Start-Process "http://localhost:3000"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "  启动完成！" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
Write-Host "后端地址: http://localhost:8080" -ForegroundColor Yellow
Write-Host "前端地址: http://localhost:3000" -ForegroundColor Yellow
Write-Host ""
Write-Host "按任意键退出..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
