# Sentinel — On-Device App Risk Auditor

**Sentinel** is a complete, offline-first mobile security application for Android built for the Mobile Security Hackathon. It audits installed applications by analyzing their declared permissions, granted permissions, special access capabilities, and security-sensitive combination rules.

Sentinel helps users answer three fundamental security questions:
1. **Which installed applications have the most potentially dangerous capabilities?**
2. **Why has an application been flagged as risky?**
3. **What actions can the user take to investigate or reduce its risk?**

---

## Architecture & Codebase Structure

```
app/
├── src/
│   ├── main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/
│   │   │   ├── com/example/
│   │   │   │   ├── MainActivity.kt
│   │   │   │   ├── sentinel/
│   │   │   │   │   ├── model/
│   │   │   │   │   │   ├── RiskLevel.kt
│   │   │   │   │   │   ├── PermissionCategory.kt
│   │   │   │   │   │   ├── SpecialAccessType.kt
│   │   │   │   │   │   ├── AppPermission.kt
│   │   │   │   │   │   ├── RiskFinding.kt
│   │   │   │   │   │   └── InstalledApp.kt
│   │   │   │   │   ├── scanner/
│   │   │   │   │   │   ├── PermissionMapper.kt
│   │   │   │   │   │   ├── SpecialAccessScanner.kt
│   │   │   │   │   │   └── AppScanner.kt
│   │   │   │   │   ├── risk/
│   │   │   │   │   │   ├── ContextMismatchHeuristics.kt
│   │   │   │   │   │   └── RiskEngine.kt
│   │   │   │   │   ├── ui/
│   │   │   │   │   │   ├── Components.kt
│   │   │   │   │   │   ├── DashboardScreen.kt
│   │   │   │   │   │   ├── AppInventoryScreen.kt
│   │   │   │   │   │   ├── AppDetailsScreen.kt
│   │   │   │   │   │   ├── PermissionsExplorerScreen.kt
│   │   │   │   │   │   ├── SettingsScreen.kt
│   │   │   │   │   │   ├── SentinelViewModel.kt
│   │   │   │   │   │   └── SentinelMainScreen.kt
│   │   │   │   │   └── util/
│   │   │   │   │       ├── AppSettingsNavigator.kt
│   │   │   │   │       ├── ReportExporter.kt
│   │   │   │   │       └── DemoFixture.kt
│   │   │   │   └── ui/theme/
│   │   │   │       ├── Color.kt
│   │   │   │       ├── Theme.kt
│   │   │   │       └── Type.kt
│   │   └── res/
│   └── test/
│       └── java/com/example/sentinel/
│           └── SentinelRiskEngineTest.kt
├── docs/
│   ├── SCORING_RUBRIC.md
│   └── DEMO_GUIDE.md
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Core Features

- **Real Application Inventory:** Discovers all installed user applications using Android `PackageManager` APIs, with an optional toggle to audit system packages.
- **Nine Permission Categories:** Maps all standard and vendor Android permissions into 9 domains: Special Access, SMS, Contacts & Call Logs, Camera & Mic, Location, Phone Identity, Storage & Media, Background Persistence, and Network.
- **Declared vs. Granted Distinction:** Clearly separates permissions declared in the APK manifest from permissions actually granted by the user at runtime.
- **Special Access Probing:** Detects enabled Accessibility services, active Device Administrators, Notification Listeners, Overlay (`SYSTEM_ALERT_WINDOW`) permissions, Package Install requests, and Usage Access.
- **Deterministic 0–100 Scoring Engine:** Normalized category weights combined with calibrated multi-permission bonuses (SMS + Net + Boot, Accessibility + Overlay, Context Mismatch, Sideloading, etc.).
- **Explainable Findings:** Every High and Critical app includes at least 3 distinct findings detailing the capability detected, concrete evidence, security implications, and practical user actions.
- **Context Mismatch Heuristics:** Flags simple utilities (flashlights, calculators, wallpapers) holding disproportionate sensitive permissions.
- **Direct System Navigation:** Safely navigates users to Android Application Details, Special Access management, and system uninstall confirmation dialogs.
- **Offline Report Export:** Generates formatted on-device audit reports in Markdown shareable via Android's native share sheet.
- **Demo Fixtures Mode:** Enables judges to test high-risk combination rules safely on clean emulator environments without installing malware.

---

## Privacy & Security Stance

- **100% Offline-First:** Sentinel operates strictly inside the local device sandbox. No network calls are made. Zero telemetry, package inventories, or identifiers are uploaded.
- **Safe Remediation:** Sentinel never silently revokes permissions or uninstalls applications; it routes users directly to native Android confirmation dialogs.
- **Package Visibility:** Uses `android.permission.QUERY_ALL_PACKAGES` as permitted for security auditing and device inventory hackathon applications.

---

## Build & Test Instructions

### Building the Debug APK
```bash
gradle :app:assembleDebug
```
The resulting debug APK is output to:
`app/build/outputs/apk/debug/app-debug.apk`

### Running Unit Tests
```bash
gradle :app:testDebugUnitTest
```
Tests all 10 mandatory scoring scenarios:
1. Low-risk internet-only app baseline
2. Sensitive SMS access elevation
3. Accessibility + Overlay combination rule
4. SMS + Internet + Boot persistence exfiltration vector
5. Incomplete/missing metadata resilience
6. Duplicate permission deduplication
7. Score boundaries (0–100 bounds and risk bands)
8. Declared vs. granted weight scaling
9. System application filtering
10. Special access active vs. declared distinction

---

## Demonstration
Refer to `docs/DEMO_GUIDE.md` for a timed 3-minute live demonstration flow.
Detailed scoring parameters are documented in `docs/SCORING_RUBRIC.md`.
