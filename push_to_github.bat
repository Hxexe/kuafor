@echo off
:: Windows local GitHub sync helper
echo =======================================================
echo Kuaforum Projesi - GitHub Yukleme Yardimcisi
echo =======================================================
echo.

:: Git kontrolü
where git >nul 2>nul
if %errorlevel% neq 0 (
    echo [HATA] Bilgisayarinizda Git yuklu degil! 
    echo Lutfen once https://git-scm.com/ adresinden Git indirin ve kurun.
    pause
    exit /b
)

echo [*] Yerel git deposu baslatiliyor...
if not exist .git (
    git init
)

echo [*] Uzak depo (Remote) ayarlaniyor...
git remote remove origin >nul 2>nul
git remote add origin https://github.com/Hxexe/kuafor.git

echo [*] Dosyalar git havuzuna ekleniyor...
git add .

echo [*] Degisiklikler commit ediliyor...
git commit -m "Initial commit from Google AI Studio (Local Push)"

echo [*] Varsayilan dal 'main' olarak ayarlaniyor...
git branch -M main

echo [*] Proje GitHub'a yukleniyor (Pushing)...
echo Lutfen acilan kimlik dogrulama pencerelerindeki adimlari takip edin.
git push -u origin main

if %errorlevel% equ 0 (
    echo.
    echo =======================================================
    echo [TEBRIKLER] Projeniz basariyla GitHub'a yuklendi!
    echo Depo url: https://github.com/Hxexe/kuafor
    echo =======================================================
) else (
    echo.
    echo =======================================================
    echo [HATA] Yukleme sirasinda bir sorun olustu. 
    echo Kimlik dogrulama basarisiz olmus olabilir veya yetkiniz olmayabilir.
    echo Lutfen tarayicinizda GitHub oturumunun acik oldugundan emin olun.
    echo =======================================================
)

pause
