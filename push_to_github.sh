#!/bin/bash
# macOS/Linux local GitHub sync helper

echo "======================================================="
echo "Kuaforum Projesi - GitHub Yukleme Yardimcisi (macOS/Linux)"
echo "======================================================="
echo ""

# Check if git is installed
if ! command -v git &> /dev/null
then
    echo "[HATA] Bilgisayarinizda Git yuklu degil!"
    echo "Lutfen once https://git-scm.com/ adresinden veya paket yoneticinizle Git kurun."
    exit 1
fi

echo "[*] Yerel git deposu baslatiliyor..."
if [ ! -d ".git" ]; then
    git init
fi

echo "[*] Uzak depo (Remote) ayarlaniyor..."
git remote remove origin 2>/dev/null
git remote add origin https://github.com/Hxexe/kuafor.git

echo "[*] Dosyalar git havuzuna ekleniyor..."
git add .

echo "[*] Degisiklikler commit ediliyor..."
git commit -m "Initial commit from Google AI Studio (Local Push)"

echo "[*] Varsayilan dal 'main' olarak ayarlaniyor..."
git branch -M main

echo "[*] Proje GitHub'a yukleniyor (Pushing)..."
echo "Lutfen acilan kimlik dogrulama pencerelerindeki adimlari takip edin."
git push -u origin main

if [ $? -eq 0 ]; then
    echo ""
    echo "======================================================="
    echo "[TEBRIKLER] Projeniz basariyla GitHub'a yuklendi!"
    echo "Depo url: https://github.com/Hxexe/kuafor"
    echo "======================================================="
else
    echo ""
    echo "======================================================="
    echo "[HATA] Yukleme sirasinda bir sorun olustu."
    echo "Lutfen terminalde kimlik bilgilerinizi veya SSH anahtarlarinizi kontrol edin."
    echo "======================================================="
fi
