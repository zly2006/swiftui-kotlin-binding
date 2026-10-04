#!/bin/zsh
set -eu
cd "${0:A:h:h}"
app_executable="$PWD/samples/native-macos/build/NativeDemo.app/Contents/MacOS/NativeDemo"
[[ -x "$app_executable" ]] || { print -u2 '先执行 ./scripts/build-native.sh'; exit 1; }
mkdir -p evidence/native
for appearance in light dark; do
  theme_args=()
  [[ "$appearance" == dark ]] && theme_args+=(--dark)
  for scenario in initial updated unmounted; do
    scenario_args=()
    [[ "$scenario" == updated ]] && scenario_args+=(--updated)
    [[ "$scenario" == unmounted ]] && scenario_args+=(--unmounted)
    "$app_executable" "--snapshot=$PWD/evidence/native/$scenario-$appearance.png" "${theme_args[@]}" "${scenario_args[@]}" > "evidence/native/$scenario-$appearance.log" 2>&1
  done
done
print '六组原生窗口、受控回调、静止调度和作用域释放检查通过。'
