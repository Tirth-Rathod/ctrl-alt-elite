package com.example.sentinel.util

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.example.sentinel.model.AuditSummary
import com.example.sentinel.model.InstalledApp
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ReportExporter {

    fun generateMarkdownReport(
        summary: AuditSummary?,
        apps: List<InstalledApp>
    ): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val timestamp = sdf.format(Date(summary?.scanTimestamp ?: System.currentTimeMillis()))

        val sb = StringBuilder()
        sb.append("# Sentinel — On-Device App Risk Audit Report\n\n")
        sb.append("**Scan Date:** $timestamp\n")
        sb.append("**Total Applications Audited:** ${summary?.totalScanned ?: apps.size}\n")
        sb.append("**User Installed Apps:** ${summary?.userAppsCount ?: 0}\n")
        sb.append("**System Framework Apps:** ${summary?.systemAppsCount ?: 0}\n")
        sb.append("**Device Posture:** 100% On-Device Evaluation (Zero Cloud Upload)\n\n")

        sb.append("## Risk Level Distribution\n")
        sb.append("- **Critical Risk:** ${summary?.criticalCount ?: 0}\n")
        sb.append("- **High Risk:** ${summary?.highCount ?: 0}\n")
        sb.append("- **Medium Risk:** ${summary?.mediumCount ?: 0}\n")
        sb.append("- **Low Risk:** ${summary?.lowCount ?: 0}\n")
        sb.append("- **Average Fleet Score:** ${String.format(Locale.US, "%.1f", summary?.averageScore ?: 0.0)} / 100\n\n")

        sb.append("## Highest Risk Applications Flagged\n\n")
        val risky = apps.filter { it.riskScore >= 50 }
        if (risky.isEmpty()) {
            sb.append("No applications exceeded the High Risk threshold (Score >= 50).\n\n")
        } else {
            risky.forEach { app ->
                sb.append("### ${app.appLabel} (`${app.packageName}`)\n")
                sb.append("- **Risk Score:** ${app.riskScore} (${app.riskLevel.label})\n")
                sb.append("- **Version:** ${app.versionName} | Target SDK: ${app.targetSdk}\n")
                sb.append("- **Installer:** ${app.installerPackageName ?: "Sideloaded / Unverified"}\n")
                sb.append("- **Granted Sensitive Permissions:** ${app.dangerousGrantedCount}\n")
                if (app.contextMismatchWarning != null) {
                    sb.append("- **Context Mismatch:** ${app.contextMismatchWarning}\n")
                }
                sb.append("#### Key Findings:\n")
                app.findings.forEach { f ->
                    sb.append("  - **${f.title}**: ${f.evidence}\n")
                    sb.append("    *Impact:* ${f.whyItMatters}\n")
                    sb.append("    *Recommended Action:* ${f.recommendation}\n")
                }
                sb.append("\n")
            }
        }

        sb.append("## Scoring Methodology & Rubric\n")
        sb.append("Scores are calculated using deterministic on-device evaluation:\n")
        sb.append("1. **Category Base Weights:** Special Access (30), SMS (20), Contacts/Call Logs (12), Camera/Mic (10), Location (10), Phone Identity (8), Storage (8), Background/Boot (6), Network (2).\n")
        sb.append("2. **Combination Bonuses:** SMS+Internet+Boot (+15), Accessibility+Overlay (+18), Active Device Admin (+20), Context Mismatch (+12), Sideloaded (+10), Background Location (+8), Notification Listener (+12).\n")
        sb.append("3. **Bands:** Low (0–24), Medium (25–49), High (50–74), Critical (75–100).\n\n")
        sb.append("Generated offline by Sentinel for Android Mobile Security Hackathon.\n")

        return sb.toString()
    }

    fun shareAuditReport(context: Context, summary: AuditSummary?, apps: List<InstalledApp>) {
        val report = generateMarkdownReport(summary, apps)
        try {
            val file = File(context.cacheDir, "sentinel_audit_report.md")
            file.writeText(report)

            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/markdown"
                putExtra(Intent.EXTRA_STREAM, uri)
                putExtra(Intent.EXTRA_SUBJECT, "Sentinel Device Security Audit Report")
                putExtra(Intent.EXTRA_TEXT, report)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Security Audit Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        } catch (_: Exception) {
            // Fallback to pure text share
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_SUBJECT, "Sentinel Device Security Audit Report")
                putExtra(Intent.EXTRA_TEXT, report)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share Security Audit Report").apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            })
        }
    }
}
