package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.entity.FraudReportEntity
import com.example.data.local.entity.FraudReportStatus
import com.example.data.local.entity.GrievanceStatus
import com.example.data.local.entity.GrievanceTicketEntity
import com.example.data.local.entity.InquiryActivityLogEntity
import com.example.data.local.entity.SellerSuspensionEntity
import com.example.data.local.entity.TrustAuditLogEntity
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class TrustAndSafetyRepository(
    private val context: Context,
    private val database: AppDatabase = AppDatabase.getInstance(context)
) {
    private val dao = database.trustSafetyDao()

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w("TrustSafetyRepo", "Firestore unavailable: ${e.message}")
            null
        }
    }

    fun observeSuspendedSellerIds(): Flow<Set<String>> =
        dao.observeSuspendedSellerIds().map { it.toSet() }

    fun observeAllSuspensions(): Flow<List<SellerSuspensionEntity>> =
        dao.observeAllSuspensions()

    fun observeAllFraudReports(): Flow<List<FraudReportEntity>> =
        dao.observeAllFraudReports()

    fun observeFraudReportsForUser(userId: String): Flow<List<FraudReportEntity>> =
        dao.observeFraudReportsForUser(userId)

    fun observeAllGrievances(): Flow<List<GrievanceTicketEntity>> =
        dao.observeAllGrievances()

    fun observeGrievancesForUser(userId: String): Flow<List<GrievanceTicketEntity>> =
        dao.observeGrievancesForUser(userId)

    fun observeAllAuditLogs(): Flow<List<TrustAuditLogEntity>> =
        dao.observeAllAuditLogs()

    fun observeActivityTimeline(entityId: String): Flow<List<InquiryActivityLogEntity>> =
        dao.observeTimelineForEntity(entityId)

    fun observeAllTimelines(): Flow<List<InquiryActivityLogEntity>> =
        dao.observeAllTimelines()

    suspend fun getTimelineForEntityOnce(entityId: String): List<InquiryActivityLogEntity> =
        dao.getTimelineForEntityOnce(entityId)

    suspend fun isSellerSuspended(sellerUserId: String): Boolean {
        if (sellerUserId.isBlank()) return false
        return dao.getSuspensionBySellerId(sellerUserId)?.isSuspended == true
    }

    suspend fun submitFraudReport(
        targetType: String,
        targetId: String,
        targetName: String,
        sellerUserId: String,
        reasonCode: String,
        description: String,
        reporterUserId: String,
        reporterName: String
    ): Result<FraudReportEntity> {
        return try {
            require(targetId.isNotBlank()) { "Target identifier is required" }
            require(description.trim().length >= 8) { "Please describe the issue (at least 8 characters)" }
            require(reporterUserId.isNotBlank()) { "Reporter sign-in is required" }

            val now = System.currentTimeMillis()
            val entity = FraudReportEntity(
                id = "fr_${now}_${(100..999).random()}",
                targetType = targetType.trim().take(40),
                targetId = targetId.trim().take(120),
                targetName = targetName.trim().take(160),
                sellerUserId = sellerUserId.trim().take(128),
                reasonCode = reasonCode.trim().take(60),
                description = description.trim().take(1200),
                reporterUserId = reporterUserId.trim().take(128),
                reporterName = reporterName.trim().ifBlank { "Farmer" }.take(120),
                status = FraudReportStatus.OPEN,
                adminNote = "",
                resolutionAction = "NONE",
                createdAtMillis = now,
                updatedAtMillis = now
            )
            dao.upsertFraudReport(entity)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("fraud_reports")?.document(entity.id)?.set(
                        mapOf(
                            "targetType" to entity.targetType,
                            "targetId" to entity.targetId,
                            "targetName" to entity.targetName,
                            "sellerUserId" to entity.sellerUserId,
                            "reasonCode" to entity.reasonCode,
                            "description" to entity.description,
                            "reporterUserId" to entity.reporterUserId,
                            "reporterName" to entity.reporterName,
                            "status" to entity.status,
                            "adminNote" to entity.adminNote,
                            "resolutionAction" to entity.resolutionAction,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (e: Exception) {
                Log.w("TrustSafetyRepo", "Remote fraud report deferred: ${e.message}")
            }
            Result.success(entity)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun resolveFraudReportByAdmin(
        reportId: String,
        newStatus: String,
        resolutionAction: String,
        adminNote: String,
        adminEmail: String,
        suspendSellerToo: Boolean = false
    ): Result<Unit> {
        return try {
            val current = dao.getFraudReportById(reportId)
                ?: return Result.failure(NoSuchElementException("Fraud report $reportId not found"))
            val now = System.currentTimeMillis()
            val updated = current.copy(
                status = newStatus,
                resolutionAction = resolutionAction.trim().take(60),
                adminNote = adminNote.trim().take(600),
                updatedAtMillis = now
            )
            dao.upsertFraudReport(updated)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("fraud_reports")?.document(reportId)?.update(
                        mapOf(
                            "status" to updated.status,
                            "resolutionAction" to updated.resolutionAction,
                            "adminNote" to updated.adminNote,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}

            recordAuditLog(
                adminEmail = adminEmail,
                action = "RESOLVE_FRAUD_REPORT",
                targetType = current.targetType,
                targetId = current.targetId,
                summary = "Report $reportId -> $newStatus ($resolutionAction): ${adminNote.take(120)}"
            )

            if (suspendSellerToo && current.sellerUserId.isNotBlank()) {
                suspendSeller(
                    sellerUserId = current.sellerUserId,
                    businessName = current.targetName,
                    reason = adminNote.ifBlank { "Suspended after fraud report $reportId (${current.reasonCode})" },
                    adminEmail = adminEmail
                )
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun suspendSeller(
        sellerUserId: String,
        businessName: String,
        reason: String,
        adminEmail: String
    ): Result<Unit> {
        return try {
            require(sellerUserId.isNotBlank()) { "Seller UID is required for suspension" }
            val now = System.currentTimeMillis()
            val suspension = SellerSuspensionEntity(
                sellerUserId = sellerUserId.trim().take(128),
                businessName = businessName.trim().take(160),
                isSuspended = true,
                reason = reason.trim().ifBlank { "Trust & safety policy violation" }.take(400),
                suspendedByAdmin = adminEmail.trim().take(150),
                updatedAtMillis = now
            )
            dao.upsertSuspension(suspension)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("seller_suspensions")?.document(suspension.sellerUserId)?.set(
                        mapOf(
                            "sellerUserId" to suspension.sellerUserId,
                            "businessName" to suspension.businessName,
                            "isSuspended" to true,
                            "reason" to suspension.reason,
                            "suspendedByAdmin" to suspension.suspendedByAdmin,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}

            recordAuditLog(
                adminEmail = adminEmail,
                action = "SUSPEND_SELLER",
                targetType = "SELLER",
                targetId = suspension.sellerUserId,
                summary = "Suspended ${suspension.businessName.ifBlank { suspension.sellerUserId }}: ${suspension.reason.take(120)}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun unsuspendSeller(
        sellerUserId: String,
        adminEmail: String,
        note: String = "Restored after verification review"
    ): Result<Unit> {
        return try {
            val current = dao.getSuspensionBySellerId(sellerUserId)
            val now = System.currentTimeMillis()
            val updated = SellerSuspensionEntity(
                sellerUserId = sellerUserId,
                businessName = current?.businessName ?: sellerUserId,
                isSuspended = false,
                reason = note.trim().take(400),
                suspendedByAdmin = adminEmail.trim().take(150),
                updatedAtMillis = now
            )
            dao.upsertSuspension(updated)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("seller_suspensions")?.document(sellerUserId)?.set(
                        mapOf(
                            "sellerUserId" to sellerUserId,
                            "businessName" to updated.businessName,
                            "isSuspended" to false,
                            "reason" to updated.reason,
                            "suspendedByAdmin" to updated.suspendedByAdmin,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}

            recordAuditLog(
                adminEmail = adminEmail,
                action = "UNSUSPEND_SELLER",
                targetType = "SELLER",
                targetId = sellerUserId,
                summary = "Restored seller $sellerUserId: ${note.take(120)}"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun submitGrievanceTicket(
        userId: String,
        userName: String,
        category: String,
        subject: String,
        description: String,
        relatedEntityId: String = ""
    ): Result<GrievanceTicketEntity> {
        return try {
            require(userId.isNotBlank()) { "User session required" }
            require(subject.trim().length >= 4) { "Subject is too short" }
            require(description.trim().length >= 8) { "Please describe your grievance in detail" }

            val now = System.currentTimeMillis()
            val ticket = GrievanceTicketEntity(
                id = "grv_${now}_${(100..999).random()}",
                userId = userId.trim().take(128),
                userName = userName.trim().ifBlank { "Farmer" }.take(120),
                category = category.trim().take(60),
                subject = subject.trim().take(160),
                description = description.trim().take(1500),
                relatedEntityId = relatedEntityId.trim().take(120),
                status = GrievanceStatus.SUBMITTED,
                adminResponse = "",
                createdAtMillis = now,
                updatedAtMillis = now
            )
            dao.upsertGrievance(ticket)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("grievance_tickets")?.document(ticket.id)?.set(
                        mapOf(
                            "userId" to ticket.userId,
                            "userName" to ticket.userName,
                            "category" to ticket.category,
                            "subject" to ticket.subject,
                            "description" to ticket.description,
                            "relatedEntityId" to ticket.relatedEntityId,
                            "status" to ticket.status,
                            "adminResponse" to "",
                            "createdAt" to FieldValue.serverTimestamp(),
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}
            Result.success(ticket)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun respondToGrievanceByAdmin(
        ticketId: String,
        newStatus: String,
        adminResponse: String,
        adminEmail: String
    ): Result<Unit> {
        return try {
            val current = dao.getGrievanceById(ticketId)
                ?: return Result.failure(NoSuchElementException("Grievance ticket $ticketId not found"))
            val updated = current.copy(
                status = newStatus,
                adminResponse = adminResponse.trim().take(1200),
                updatedAtMillis = System.currentTimeMillis()
            )
            dao.upsertGrievance(updated)
            try {
                if (firestore != null && Firebase.auth.currentUser != null) {
                    firestore?.collection("grievance_tickets")?.document(ticketId)?.update(
                        mapOf(
                            "status" to updated.status,
                            "adminResponse" to updated.adminResponse,
                            "updatedAt" to FieldValue.serverTimestamp()
                        )
                    )?.await()
                }
            } catch (_: Exception) {}

            recordAuditLog(
                adminEmail = adminEmail,
                action = "RESPOND_GRIEVANCE",
                targetType = "GRIEVANCE",
                targetId = ticketId,
                summary = "Grievance $ticketId -> $newStatus"
            )
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun logActivityEvent(
        entityType: String,
        entityId: String,
        actorRole: String,
        actorName: String,
        statusLabel: String,
        note: String = ""
    ) {
        if (entityId.isBlank()) return
        val now = System.currentTimeMillis()
        dao.insertActivityLog(
            InquiryActivityLogEntity(
                id = "act_${now}_${(1000..9999).random()}",
                entityType = entityType,
                entityId = entityId,
                actorRole = actorRole,
                actorName = actorName.ifBlank { actorRole },
                statusLabel = statusLabel,
                note = note.take(400),
                timestampMillis = now
            )
        )
    }

    suspend fun recordAuditLog(
        adminEmail: String,
        action: String,
        targetType: String,
        targetId: String,
        summary: String
    ) {
        val now = System.currentTimeMillis()
        val entry = TrustAuditLogEntity(
            id = "taudit_${now}_${(100..999).random()}",
            adminEmail = adminEmail.trim().take(150),
            action = action.trim().take(80),
            targetType = targetType.trim().take(50),
            targetId = targetId.trim().take(120),
            summary = summary.trim().take(500),
            createdAtMillis = now
        )
        dao.insertAuditLog(entry)
        try {
            if (firestore != null && Firebase.auth.currentUser != null) {
                firestore?.collection("trust_audit_logs")?.document(entry.id)?.set(
                    mapOf(
                        "adminEmail" to entry.adminEmail,
                        "action" to entry.action,
                        "targetType" to entry.targetType,
                        "targetId" to entry.targetId,
                        "summary" to entry.summary,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )?.await()
            }
        } catch (_: Exception) {}
    }

    suspend fun syncSuspensionsFromFirestore() {
        try {
            val db = firestore ?: return
            if (Firebase.auth.currentUser == null) return
            val snap = db.collection("seller_suspensions").get().await()
            snap.documents.forEach { d ->
                val sellerId = d.getString("sellerUserId") ?: d.id
                val suspended = d.getBoolean("isSuspended") ?: false
                dao.upsertSuspension(
                    SellerSuspensionEntity(
                        sellerUserId = sellerId,
                        businessName = d.getString("businessName") ?: "",
                        isSuspended = suspended,
                        reason = d.getString("reason") ?: "",
                        suspendedByAdmin = d.getString("suspendedByAdmin") ?: "",
                        updatedAtMillis = System.currentTimeMillis()
                    )
                )
            }
        } catch (e: Exception) {
            Log.w("TrustSafetyRepo", "Suspension sync deferred: ${e.message}")
        }
    }
}
