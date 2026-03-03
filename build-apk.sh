#!/bin/bash
# Manual APK build for Vaishnavi
# Usage: ./build-apk.sh

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
BUILD_DIR="$PROJECT_DIR/build"
DIST_DIR="$PROJECT_DIR/dist"
APP_NAME="vaishnavi-voice-app"

echo "🔨 Building $APP_NAME..."
echo "Project: $PROJECT_DIR"

mkdir -p "$BUILD_DIR" "$DIST_DIR"

# Try gradle first
if command -v gradle &> /dev/null; then
    echo "✅ Using gradle..."
    cd "$PROJECT_DIR"
    gradle clean assembleDebug || {
        echo "❌ Gradle build failed"
        exit 1
    }
    
    if [ -f "$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk" ]; then
        cp "$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk" "$DIST_DIR/$APP_NAME-debug.apk"
        echo "✅ APK built: $DIST_DIR/$APP_NAME-debug.apk"
        exit 0
    fi
fi

# Fallback: Use Android SDK directly
if [ -d "$ANDROID_SDK_ROOT" ]; then
    echo "✅ Using Android SDK from $ANDROID_SDK_ROOT"
    
    # This is simplified - full Android build requires many steps
    echo "⚠️  Android SDK manual build is complex - use gradle instead"
    exit 1
else
    echo "❌ Neither gradle nor Android SDK found"
    echo "Install with: sudo apt-get install -y android-sdk gradle"
    exit 1
fi
