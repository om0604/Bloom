#!/usr/bin/env python3
"""
Bloom Font Setup Script
Downloads Lora and DM Sans font files from Google Fonts
and places them in the correct Android resource directory.

Usage:
    python setup_fonts.py

Requirements:
    pip install requests
"""

import os
import io
import zipfile
import urllib.request
import shutil

FONT_DIR = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "app", "src", "main", "res", "font"
)

FONTS = {
    # Lora — warm serif for editorial text
    "https://fonts.google.com/download?family=Lora": {
        "Lora-Regular.ttf":     "lora_regular.ttf",
        "Lora-Medium.ttf":      "lora_medium.ttf",
        "Lora-SemiBold.ttf":    "lora_semibold.ttf",
        "Lora-Bold.ttf":        "lora_bold.ttf",
        "Lora-Italic.ttf":      "lora_italic.ttf",
        "Lora-BoldItalic.ttf":  "lora_bold_italic.ttf",
    },
    # DM Sans — clean sans-serif for UI chrome
    "https://fonts.google.com/download?family=DM+Sans": {
        "DMSans-Regular.ttf":   "dmsans_regular.ttf",
        "DMSans-Medium.ttf":    "dmsans_medium.ttf",
        "DMSans-SemiBold.ttf":  "dmsans_semibold.ttf",
        "DMSans-Bold.ttf":      "dmsans_bold.ttf",
    },
}

def download_and_extract(url, name_map):
    print(f"\nDownloading {url.split('family=')[1]}...")
    req = urllib.request.Request(url, headers={"User-Agent": "Mozilla/5.0"})
    with urllib.request.urlopen(req) as response:
        data = response.read()

    with zipfile.ZipFile(io.BytesIO(data)) as zf:
        for member in zf.namelist():
            filename = os.path.basename(member)
            if filename in name_map:
                target = os.path.join(FONT_DIR, name_map[filename])
                with zf.open(member) as src, open(target, "wb") as dst:
                    shutil.copyfileobj(src, dst)
                print(f"  ✓ {filename} → {name_map[filename]}")

def main():
    os.makedirs(FONT_DIR, exist_ok=True)
    print(f"Font directory: {FONT_DIR}")

    for url, name_map in FONTS.items():
        try:
            download_and_extract(url, name_map)
        except Exception as e:
            print(f"  ✗ Failed to download: {e}")
            print(f"    Please download manually from: {url}")

    print("\n✅ Font setup complete! Build the project in Android Studio.")

if __name__ == "__main__":
    main()
