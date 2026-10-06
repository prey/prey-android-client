#!/bin/sh
# Runs the instrumented suite inside the emulator-runner's script step.
#
# This lives in a file rather than inline in ci.yml because
# reactivecircus/android-emulator-runner executes each line of an inline script in
# its own shell. Any multi-line construct therefore breaks: an inline `if` fails
# with "Syntax error: end of file unexpected (expecting fi)" because only the first
# line reaches the shell. Invoking one script keeps it to a single line.
#
# Usage: run-instrumented-tests.sh <api-level>
set -e

API_LEVEL="$1"
if [ -z "$API_LEVEL" ]; then
    echo "usage: $0 <api-level>" >&2
    exit 2
fi

if [ "$API_LEVEL" -ge 36 ]; then
    # The runner's AVD is phone-sized, and the API 36 orientation change only
    # applies from 600dp of smallest width up. Without this the large-screen tests
    # report as skipped and the job goes green having covered nothing.
    # 1600dp / (240/160) = 1066dp of smallest width, in landscape.
    echo "API $API_LEVEL: forcing a large-screen configuration"
    adb shell wm size 2560x1600
    adb shell wm density 240
    adb shell wm set-ignore-orientation-request true
    # Informational, and must not fail the run if the line moves between releases.
    adb shell dumpsys window displays | grep -m1 ignoreOrientationRequest || true
else
    # Left phone-sized on purpose: this level is here to cover the code that only
    # runs below Android 13, where BackNavigationCompat falls back to onBackPressed.
    echo "API $API_LEVEL: keeping the default phone configuration"
fi

./gradlew connectedDebugAndroidTest
