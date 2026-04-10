#!/bin/bash
#
# Syncs dependencies from app/build.gradle (source of truth) into
# build.gradle.internal and build.gradle.dev.
#
# Usage: ./scripts/sync-gradle-deps.sh [--dry-run]
#

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"
SOURCE="$PROJECT_DIR/app/build.gradle"
TARGETS=("$PROJECT_DIR/app/build.gradle.internal" "$PROJECT_DIR/app/build.gradle.dev")
DRY_RUN=false

if [[ "${1:-}" == "--dry-run" ]]; then
    DRY_RUN=true
fi

if [[ ! -f "$SOURCE" ]]; then
    echo "ERROR: Source file not found: $SOURCE"
    exit 1
fi

# Extracts dependencies from a build.gradle file.
# Returns lines like: "androidx.appcompat:appcompat:1.7.1"
extract_deps() {
    local file="$1"
    # Matches implementation/testImplementation/androidTestImplementation lines with group:artifact:version
    grep -E "^\s+(implementation|testImplementation|androidTestImplementation)\s+" "$file" \
        | grep -oE "'[^']+:[^']+:[^']+'" \
        | tr -d "'" \
        | sort
}

# Given group:artifact:version, returns group:artifact
get_artifact() {
    echo "$1" | rev | cut -d: -f2- | rev
}

# Given group:artifact:version, returns version
get_version() {
    echo "$1" | rev | cut -d: -f1 | rev
}

echo "Source: $SOURCE"
echo ""

source_deps=$(extract_deps "$SOURCE")

for target in "${TARGETS[@]}"; do
    if [[ ! -f "$target" ]]; then
        echo "SKIP: $target not found"
        echo ""
        continue
    fi

    target_name=$(basename "$target")
    echo "--- Syncing $target_name ---"

    changes=0
    target_content=$(cat "$target")

    while IFS= read -r source_dep; do
        artifact=$(get_artifact "$source_dep")
        source_version=$(get_version "$source_dep")

        # Check if this artifact exists in the target
        target_line=$(grep -E "'${artifact}:[^']+'" "$target" 2>/dev/null || true)
        if [[ -z "$target_line" ]]; then
            continue
        fi

        # Extract the current version from the target
        target_dep=$(echo "$target_line" | grep -oE "'${artifact}:[^']+'" | tr -d "'" | head -1)
        target_version=$(get_version "$target_dep")

        if [[ "$source_version" != "$target_version" ]]; then
            echo "  $artifact: $target_version -> $source_version"
            if [[ "$DRY_RUN" == false ]]; then
                target_content=$(echo "$target_content" | sed "s|'${artifact}:${target_version}'|'${artifact}:${source_version}'|g")
            fi
            changes=$((changes + 1))
        fi
    done <<< "$source_deps"

    # Check for dependencies missing in the target
    missing=0
    while IFS= read -r source_dep; do
        artifact=$(get_artifact "$source_dep")
        if ! grep -q "'${artifact}:" "$target" 2>/dev/null; then
            echo "  MISSING: $source_dep (present in build.gradle but not in $target_name)"
            missing=$((missing + 1))
        fi
    done <<< "$source_deps"

    if [[ $changes -eq 0 && $missing -eq 0 ]]; then
        echo "  Already up to date."
    elif [[ "$DRY_RUN" == false && $changes -gt 0 ]]; then
        echo "$target_content" > "$target"
        echo "  $changes dependency(ies) updated."
    elif [[ "$DRY_RUN" == true ]]; then
        echo "  (dry-run: no changes written)"
    fi

    if [[ $missing -gt 0 ]]; then
        echo "  $missing dependency(ies) missing - add manually if needed."
    fi

    echo ""
done

echo "Done."
