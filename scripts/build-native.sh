#!/bin/zsh
set -eu
cd "${0:A:h:h}"
export JAVA_HOME="$(/usr/libexec/java_home -v 21 -a arm64)"
./gradlew :binding-generator:test :native-compose:jvmTest :samples:demo-ui:compileKotlinJvm :samples:native-macos:linkDebugExecutableMacosArm64 --console=plain
bundle_dir="$PWD/samples/native-macos/build/NativeDemo.app"
mkdir -p "$bundle_dir/Contents/MacOS" "$bundle_dir/Contents/Frameworks"
cp samples/native-macos/build/bin/macosArm64/debugExecutable/NativeDemo.kexe "$bundle_dir/Contents/MacOS/NativeDemo"
cp binding-core/build/swift/libSwiftUIBinding.dylib "$bundle_dir/Contents/Frameworks/"
cp samples/native-macos/src/macosMain/resources/Info.plist "$bundle_dir/Contents/Info.plist"
install_name_tool -delete_rpath "$PWD/binding-core/build/swift" "$bundle_dir/Contents/MacOS/NativeDemo"
codesign --force --sign - "$bundle_dir/Contents/Frameworks/libSwiftUIBinding.dylib"
codesign --force --sign - "$bundle_dir"
codesign --verify --deep --strict "$bundle_dir"
file "$bundle_dir/Contents/MacOS/NativeDemo"
print -r -- "App: $bundle_dir"
