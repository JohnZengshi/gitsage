# GITSAGE PLUGIN ROOT DIRECTORY

**Generated:** 2026-04-12
**Location:** src/main/kotlin/com/example/gitsage/

## OVERVIEW
Main package for the GitSage plugin containing core functionality and entry point.

## STRUCTURE
```
src/main/kotlin/com/example/gitsage/
├── AICommitPlugin.kt          # Plugin entry point (class named GitSagePlugin)
├── ai/                        # AI provider implementations
├── settings/                  # Settings management and UI
├── git/                       # Git utilities and diff extraction
├── ui/                        # UI integration and actions
└── notifications/             # Notification helpers
```

## WHERE TO LOOK
| Component | File | Purpose |
|-----------|------|---------|
| Plugin initialization | AICommitPlugin.kt | Entry point that registers the plugin |
| UI Actions | ui/GenerateCommitAction.kt | Handles the "Generate Commit Message" button |
| Settings | settings/ | All configuration and credential management |
| AI Services | ai/ | All AI provider integrations and message generation |
| Git Utilities | git/ | Git diff extraction and repository utilities |
| Notifications | notifications/ | User notification management |

## CONVENTIONS
- Each functional area is grouped in its own subdirectory
- Plugin follows IntelliJ Platform extension patterns
- Uses service pattern for settings management
- Singleton pattern for AI providers

## ANTI-PATTERNS
- Naming mismatch: AICommitPlugin.kt file has GitSagePlugin class (should match filename)