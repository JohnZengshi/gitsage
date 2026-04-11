# SETTINGS MODULE

**Generated:** 2026-04-12
**Location:** src/main/kotlin/com/example/gitsage/settings/

## OVERVIEW
Handles all plugin configuration, user preferences, and credential management.

## STRUCTURE
```
settings/
├── GitSageSettingsState.kt     # Data class for settings storage
├── GitSageSettings.kt          # Settings service implementation
├── GitSageConfigurable.kt      # Settings UI panel (largest file)
└── CredentialsManager.kt       # Secure credential handling
```

## WHERE TO LOOK
| Component | File | Purpose |
|-----------|------|---------|
| Settings State | GitSageSettingsState.kt | Serializable data class storing the settings |
| Settings Service | GitSageSettings.kt | Application-level service managing settings |
| Settings UI | GitSageConfigurable.kt | Implements Configurable interface for settings dialog |
| Credentials | CredentialsManager.kt | Secure credential storage using IntelliJ keyring |

## CONVENTIONS
- Uses IntelliJ's persistentStateService for settings storage
- Settings state is a simple data class with defaults
- Credentials stored separately from other settings for security
- UI components map directly to settings fields

## ANTI-PATTERNS
- GitSageConfigurable.kt is quite large (629 lines) - consider splitting UI logic