# GIT UTILITIES MODULE

**Generated:** 2026-04-12
**Location:** src/main/kotlin/com/example/gitsage/git/

## OVERVIEW
Handles Git repository interactions, diff extraction, and change analysis for commit message generation.

## STRUCTURE
```
git/
├── GitDiffExtractor.kt        # Extracts and formats Git diffs
└── GitRepositoryUtils.kt      # Git repository utilities
```

## WHERE TO LOOK
| Component | File | Purpose |
|-----------|------|---------|
| Diff Extraction | GitDiffExtractor.kt | Extracts differences from Git working directory |
| Repository Utils | GitRepositoryUtils.kt | Helper functions for Git repository operations |

## CONVENTIONS
- Uses IntelliJ's Git4Idea plugin integration
- Formats Git diffs appropriately for AI consumption
- Handles encoding issues with proper charset handling
- Provides utilities for common Git repository operations

## ANTI-PATTERNS
- No anti-patterns detected in this module