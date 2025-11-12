@echo off
REM Script para compilar o projeto de Damas

echo ========================================
echo   COMPILANDO PROJETO DE DAMAS
echo ========================================
echo.

REM Cria diretório bin se não existir
if not exist "bin" mkdir bin

REM Compila os arquivos Java
echo Compilando arquivos...
javac -d bin -encoding UTF-8 src\network\*.java src\model\*.java src\server\*.java src\form\*.java

if %ERRORLEVEL% == 0 (
    echo.
    echo ========================================
    echo   COMPILACAO CONCLUIDA COM SUCESSO!
    echo ========================================
    echo.
    echo Para executar:
    echo   Servidor:   run-server.bat
    echo   Cliente:    run-client.bat
    echo.
) else (
    echo.
    echo ========================================
    echo   ERRO NA COMPILACAO!
    echo ========================================
    echo.
)

pause
