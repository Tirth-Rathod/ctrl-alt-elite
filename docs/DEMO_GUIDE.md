# Sentinel — Hackathon Live Demo Guide (3-Minute Presentation)

## Presentation Overview
- **App Name:** Sentinel — On-Device App Risk Auditor
- **Hackathon Track:** Mobile Security & Privacy
- **Target Audience:** Hackathon Judges, Security Engineers, and Android Users
- **Primary Objective:** Demonstrate real, explainable, on-device application risk auditing with zero telemetry, zero root requirements, and direct system settings navigation.

---

## 3-Minute Presentation Script & Flow

### Minute 0:00 – 0:45: The Problem & The Sentinel Solution
1. **Launch Sentinel** on the Android device or emulator.
2. **Opening Pitch:**
   > *"Most Android users have dozens of apps installed, but have no way to know which ones hold dangerous capabilities, which permissions are actually granted versus merely declared, or when an app has enabled accessibility or overlay access. Sentinel is an offline-first on-device risk auditor that gives users full transparency without ever sending a single byte off the device."*
3. **Show Dashboard Header:**
   - Point out the cybersecurity dark mode aesthetic and the **100% Offline Analysis** badge.
   - Mention `android.permission.QUERY_ALL_PACKAGES` used strictly for full device inventory visibility.

---

### Minute 0:45 – 1:30: Live Scan & Fleet Distribution
1. **Tap "Scan Now":**
   - Show the live radar scanner animation and real-time progress counter processing packages on a background coroutine thread.
2. **Explain the Dashboard Metrics:**
   - Review real calculated totals: Total Scanned, Low, Medium, High, and Critical counts.
   - Explain the **Risk Level Distribution bar**, showing fleet proportions at a glance.
3. **Demo Fixtures Toggle:**
   - If presenting on an emulator with only clean default Google apps, tap **"Demo Fixtures"** in the header or Settings.
   - Point out the visual indicator clearly distinguishing demo fixtures from device findings, allowing judges to test all high-risk combination rules safely without installing real malware!

---

### Minute 1:30 – 2:15: Deep Dive into Risky Apps & Explainable Evidence
1. **Select a High/Critical Application:**
   - Select `StealthTracker Pro` (or a real high-risk package found on the device).
2. **Highlight Key Differentiators:**
   - **Score Banner:** 84 / 100 CRITICAL with clear status.
   - **Context Mismatch Warning:** Show the heuristic alert explaining why a simple utility requesting SMS or contacts is flagged.
   - **Explainable Findings:** Expand each finding card. Show that Sentinel answers *which* capabilities are present, *why* they matter, and *what practical action* the user should take.
   - **Declared vs. Granted:** Show the permission list toggle. Prove that Sentinel differentiates between permissions declared in the manifest and permissions actually granted by the user.

---

### Minute 2:15 – 2:45: Special Access Matrix & System Settings Navigation
1. **Review Special Access Matrix:**
   - Show the 6 special access probes: Accessibility, Overlay, Device Administrator, Notification Listener, Package Install, and Usage Stats.
   - Show how Sentinel distinguishes *declared* capability from *active/enabled* access.
2. **Execute System Actions:**
   - Tap **"Settings"**: opens the native Android Application Details page.
   - Tap **"Special Access"**: opens Android's native Special App Access / Overlay management screen.
   - Tap **"Uninstall"**: triggers Android's standard user-confirmed package deletion dialog (safe, non-silent).

---

### Minute 2:45 – 3:00: Capabilities Explorer, Rubric & Export
1. **Switch to "Permissions" Tab:**
   - Show the 9 capability cards (SMS, Location, Contacts, Camera, etc.).
   - Tap **"SMS & Messaging"** to instantly show every app on the device that can read text messages.
2. **Switch to "Settings" Tab:**
   - Tap **"Export Security Audit Report"**: generates an on-device Markdown audit report and triggers the native Android Share sheet.
   - Show the **Explainable Scoring Rubric** collapsible viewer.
3. **Closing Statement:**
   > *"Sentinel proves that modern Android devices have all the telemetry necessary to empower users with explainable, actionable security—completely on-device, privately, and deterministically. Thank you!"*
