#!/bin/bash
pwd=${PWD}

buildit()
{
    arch=$1
    platform=$2

    export LDFLAGS="-arch ${target} -isysroot $PLATFORMPATH/$platform.platform/Developer/SDKs/$platform$SDKVERSION.sdk"
		export TARGET_ARCH=${arch}
		export TARGET_PLATFORM=${platform}
    make clean
    TARGET_ARCH=${arch} TARGET_PLATFORM=${platform} BIN_DIR_ENV=bin/${arch} make iOS
}

buildit armv7 iPhoneOS
buildit armv7s iPhoneOS
buildit arm64 iPhoneOS
buildit i386 iPhoneSimulator
buildit x86_64 iPhoneSimulator

LIPO=$(xcrun -sdk iphoneos -find lipo)
$LIPO -create $pwd/bin/armv7/libmlm.so.1.0.0  $pwd/bin/armv7s/libmlm.so.1.0.0 $pwd/bin/arm64/libmlm.so.1.0.0 $pwd/bin/x86_64/libmlm.so.1.0.0 $pwd/bin/i386/libmlm.so.1.0.0 -output $pwd/libmlm.so.1.0.0
