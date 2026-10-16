# Sentinel — Explainable Risk Scoring Rubric

## 1. Overview & Architectural Philosophy
**Sentinel** implements an explainable, deterministic, on-device risk scoring engine for Android applications. Unlike opaque heuristic engines or black-box classifiers, Sentinel produces a score from **0 to 100** where every single point is traceable back to declared permissions, granted permissions, active special access capabilities, and security-sensitive combination rules.

The system adheres strictly to the principle that requesting a sensitive capability does **not** automatically make an app malware. Rather, risk scoring reflects the potential blast radius, privilege escalation vectors, and data exfiltration pathways accessible to the package.

---

## 2. Risk Bands & Severity Thresholds

| Band | Score Range | Label | Color Indicator | Assessment & Security Meaning |
| :--- | :---: | :--- | :--- | :--- |
| **LOW** | `0 – 24` | Minimal Risk | Green (`#00E676`) | Standard or sandboxed footprint. No privileged sensors, telephony, or special access. Operates within standard Android sandbox boundaries. |
| **MEDIUM** | `25 – 49` | Moderate Risk | Yellow/Amber (`#FFD600`) | Standard multimedia, camera, audio, or storage access typical of communication, social media, or creation tools. |
| **HIGH** | `50 – 74` | Elevated Risk | Orange (`#FF9100`) | Privileged capabilities such as background geofencing, SMS inspection, or unusual permission-to-context profiles requiring user audit. |
| **CRITICAL** | `75 – 100` | Critical Risk | Crimson Red (`#FF3D00`) | Highly privileged system control such as active Device Administration, enabled Accessibility + Overlay tapjacking vectors, or SMS OTP interception pipelines. |

---

## 3. Base Category Weights (106 Points Total Pool)

Base points are allocated across nine mandatory security domains:

| Category # | Security Domain | Max Base Weight | Key Evaluated Permissions & Special Privileges |
| :---: | :--- | :---: | :--- |
| **1** | **Special Access** | **30 pts** | Active Accessibility Services (15), Device Administrator (15), Notification Listener (12), System Alert Window Overlay (10), Request Install Packages (8), Usage Stats (6). |
| **2** | **SMS & Telephony** | **20 pts** | `READ_SMS` (10), `RECEIVE_SMS` (8), `SEND_SMS` (8), `RECEIVE_MMS` (4). |
| **3** | **Contacts & Call Logs**| **12 pts** | `READ_CALL_LOG` (6), `READ_CONTACTS` (5), `WRITE_CALL_LOG` (4), `WRITE_CONTACTS` (3). |
| **4** | **Camera & Microphone** | **10 pts** | `CAMERA` (6), `RECORD_AUDIO` (6). |
| **5** | **Location Tracking** | **10 pts** | `ACCESS_BACKGROUND_LOCATION` (7), `ACCESS_FINE_LOCATION` (5), `ACCESS_COARSE_LOCATION` (3). |
| **6** | **Phone & Identity** | **8 pts** | `CALL_PHONE` (4), `READ_PHONE_NUMBERS` (4), `READ_PHONE_STATE` (3). |
| **7** | **Storage & Media** | **8 pts** | `MANAGE_EXTERNAL_STORAGE` (8), `READ_EXTERNAL_STORAGE` (4), Media collections (3). |
| **8** | **Background Persistence** | **6 pts** | `RECEIVE_BOOT_COMPLETED` (3), Battery optimization exemptions (3), `FOREGROUND_SERVICE` (2). |
| **9** | **Network Communication**| **2 pts** | `INTERNET` (2), `ACCESS_NETWORK_STATE` (1). |

---

## 4. Declared vs. Granted Evidence Scaling
- **Dangerous Runtime Permissions:**
  - **Granted:** 100% of individual permission weight.
  - **Declared in Manifest Only (Not Granted):** 25% of individual weight (reflects potential intent or request on future updates without current active data access).
- **Special Access Capabilities:**
  - **Active / Granted by User:** 100% of weight.
  - **Declared in Manifest (Not Active/Enabled):** ~20–25% of weight.
- **Duplicate Prevention:**
  - Duplicate declarations or duplicate array queries for identical permission strings are deduplicated before scoring.

---

## 5. Combination Rules & Bonuses

When specific multi-permission capabilities intersect to form high-probability exploit vectors or covert exfiltration pipelines, Sentinel applies calibrated combination bonuses:

1. **SMS Exfiltration Pipeline (+15 pts)**
   - *Condition:* Granted SMS access + Granted `INTERNET` + `RECEIVE_BOOT_COMPLETED`.
   - *Rationale:* Automated autostart on reboot coupled with SMS inspection and outbound web communication creates an active OTP/data exfiltration channel.
2. **Interface Cloaking & Tapjacking Potential (+18 pts)**
   - *Condition:* Accessibility Service declared/active + System Alert Window (`SYSTEM_ALERT_WINDOW`) overlay.
   - *Rationale:* Floating transparent/spoofed overlay surfaces combined with UI accessibility event interception allows observing credentials and injecting synthetic clicks.
3. **Active Device Administrator Authority (+20 pts)**
   - *Condition:* Package registered as an active Device Administrator in `DevicePolicyManager`.
   - *Rationale:* Grants hardware policy enforcement, screen lock overrides, remote data wipe, and hinders standard user package uninstallation.
4. **App Context Mismatch (+12 pts)**
   - *Condition:* Simple utilities (e.g. Flashlight, Calculator, Wallpaper, Compass, Notepad) holding granted permissions in unexpected domains (SMS, Contacts, Location, Camera).
   - *Rationale:* Disproportionate privilege requests outside the advertised functional scope represent prime characteristics of trojanized utilities.
5. **Sideloaded Source with Multiple Sensitive Capabilities (+10 pts)**
   - *Condition:* Installed outside official app store repositories (via package installer or unknown source) with ≥ 2 sensitive categories granted.
   - *Rationale:* Bypasses app store developer verification and Google Play Protect policy screening.
6. **Persistent Background Geolocation Tracking (+8 pts)**
   - *Condition:* `ACCESS_BACKGROUND_LOCATION` granted alongside Camera, Audio, Contacts, or SMS.
   - *Rationale:* Continuous 24/7 physical tracking correlated with private personal records.
7. **Notification Listener Interception Pipeline (+12 pts)**
   - *Condition:* Active Notification Listener access + Network connectivity or telephony access.
   - *Rationale:* Allows intercepting incoming notifications across all apps, including chat previews and bank 2FA SMS alerts.

---

## 6. Score Normalization & Boundaries
1. **Raw Base Score:** Sum of category scores (max 106).
2. **Normalized Base Score:** `(rawBaseScore / 106.0) * 100.0`.
3. **Combined Score:** `(normalizedBaseScore + totalCombinationBonus).coerceIn(0, 100)`.
4. **Guaranteed Range:** Every application score is guaranteed to remain strictly bounded between **0** and **100**.

---

## 7. Explainable Findings Requirement
Every application categorized as **HIGH** or **CRITICAL** produces at least **three distinct, plain-language explanations** whenever sufficient evidence exists. Each finding details:
- **Capability or Combination:** The specific privilege or vector matched.
- **Evidence Detected:** The concrete Android package metadata and permission states that triggered the finding.
- **Security Implications:** Why the capability presents risk to the user.
- **Practical Recommendation:** Concrete action the user can take in Android System Settings.

---

## 8. Limitations & Scope Constraints
- **Static Metadata Analysis:** Sentinel analyzes declared and granted permissions and system settings queries exposed via standard Android `PackageManager` and system APIs. It does not perform kernel-level bytecode decompilation or runtime dynamic network packet inspection.
- **User Intent:** Privileges required by legitimate specialized tools (e.g., an authentic corporate MDM or password manager) will naturally register higher capability scores; Sentinel provides actionable context and direct system navigation rather than silently labeling tools as malicious.
