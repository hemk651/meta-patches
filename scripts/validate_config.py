#!/usr/bin/env python3
"""Validate the canonical MetaConfig override payload."""

import json
from pathlib import Path

CONFIG = Path(__file__).resolve().parents[1] / "config" / "mc_overrides.json"
EXPECTED = {
    "56474:": ["0: : true"],
    "28538:": ["0: : true"],
    "32432:": ["1: : true"],
    "_qe_overrides_": [],
}


def main() -> int:
    try:
        data = json.loads(CONFIG.read_text(encoding="utf-8"))
    except (OSError, json.JSONDecodeError) as exc:
        print(f"FAIL: cannot read valid JSON: {exc}")
        return 1

    if data != EXPECTED:
        print("FAIL: payload differs from the reviewed canonical values")
        return 1

    print("OK: mc_overrides.json is valid and matches the canonical payload")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
