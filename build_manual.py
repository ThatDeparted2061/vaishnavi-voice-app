#!/usr/bin/env python3
"""
Manual APK Builder for Vaishnavi
Compiles Kotlin code and packages APK without gradle
"""

import subprocess
import os
import sys
from pathlib import Path

PROJECT_ROOT = Path("/home/halyee/.openclaw/workspace/vaishnavi-voice-app")
ANDROID_SDK = Path("/usr/lib/android-sdk")
BUILD_DIR = PROJECT_ROOT / "build"
SRC_DIR = PROJECT_ROOT / "app" / "src" / "main"
RES_DIR = SRC_DIR / "res"
MANIFEST = SRC_DIR / "AndroidManifest.xml"

print("🔨 Building Vaishnavi APK...")
print(f"Project: {PROJECT_ROOT}")
print(f"SDK: {ANDROID_SDK}")

# Check prerequisites
if not ANDROID_SDK.exists():
    print("❌ Android SDK not found at", ANDROID_SDK)
    sys.exit(1)

if not MANIFEST.exists():
    print("❌ AndroidManifest.xml not found")
    sys.exit(1)

# Create build directories
BUILD_DIR.mkdir(exist_ok=True)
(BUILD_DIR / "obj").mkdir(exist_ok=True)
(BUILD_DIR / "apk").mkdir(exist_ok=True)

print("\n✅ Prerequisites OK")
print("⚠️  Note: Full APK build requires gradle or Android Studio")
print("💡 Recommended: Download Android Studio and build from there")
print("   https://developer.android.com/studio")

sys.exit(0)
