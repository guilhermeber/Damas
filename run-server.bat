@echo off
REM Script para executar o servidor de Damas

echo ========================================
echo   SERVIDOR DE DAMAS
echo ========================================
echo.
echo Iniciando servidor na porta 5000...
echo Pressione Ctrl+C para encerrar
echo.
echo ========================================
echo.

java -cp bin server.CheckersServer

pause
