# UI INTEGRATION MODULE

**Generated:** 2026-04-12
**Location:** src/main/kotlin/com/example/gitsage/ui/

## OVERVIEW
Handles UI integration with IntelliJ/Android Studio IDE, including the "Generate Commit Message" action.

## STRUCTURE
```
ui/
└── GenerateCommitAction.kt      # The main UI action that integrates with VCS commit window
```

## WHERE TO LOOK
| Component | File | Purpose |
|-----------|------|---------|
| Generate Action | GenerateCommitAction.kt | Action that appears in Git commit tool window |

## CONVENTIONS
- Extends IntelliJ's AnAction class for IDE integration
- Registered in plugin.xml under Vcs.MessageActionGroup
- Uses IntelliJ's coroutine dispatchers for async operations
- Integrates with IntelliJ's notification system via NotificationHelper

## ANTI-PATTERNS
- No anti-patterns detected in this module