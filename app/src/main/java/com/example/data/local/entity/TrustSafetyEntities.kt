package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

object FraudTargetType {
    const val SELLER = "SELLER"
    const val MACHINERY = "MACHINERY"
    const val NURSERY_PRODUCT = "NURSERY_PRODUCT"
    const val EXPERT = "EXPERT"
    const val ORDER = "ORDER"
}

object FraudReasonCode {
    const val ADVANCE_PAYMENT_SCAM = "ADVANCE_PAYMENT_SCAM"
    const val FAKE_LISTING = "FAKE_LISTING"
    const val WRONG_PRICE = "WRONG_PRICE"
    const val IMPERSONATION = "IMPERSONATION"
    const val HARASSMENT_OR_ABUSE = "HARASSMENT_OR_ABUSE"
    const val OTHER = "OTHER"
}

object FraudReportStatus {
    const val OPEN = "OPEN"
    const val UNDER_REVIEW = "UNDER_REVIEW"
    const val ACTION_TAKEN = "ACTION_TAKEN"
    const val DISMISSED = "DISMISSED"
}

object GrievanceStatus {
    const val SUBMITTED = "SUBMITTED"
    const val IN_REVIEW = "IN_REVIEW"
    const val RESOLVED = "RESOLVED"
}

object ActivityEntityType {
    const val MACHINERY_INQUIRY = "MACHINERY_INQUIRY"
    const val MACHINERY_RENTAL = "MACHINERY_RENTAL"
    const val MACHINERY_SERVICE = "MACHINERY_SERVICE"
    const val PRODUCE_OFFER = "PRODUCE_OFFER"
    const val NURSERY_ORDER = "NURSERY_ORDER"
}

@Entity(
    tableName = "fraud_reports",
    indices = [
        Index(value = ["status", "createdAtMillis"]),
        Index(value = ["reporterUserId"]),
        Index(value = ["sellerUserId"])
    ]
)
data class FraudReportEntity(
    @PrimaryKey
    val id: String,
    val targetType: String,
    val targetId: String,
    val targetName: String,
    val sellerUserId: String,
    val reasonCode: String,
    val description: String,
    val reporterUserId: String,
    val reporterName: String,
    val status: String = FraudReportStatus.OPEN,
    val adminNote: String = "",
    val resolutionAction: String = "NONE",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "grievance_tickets",
    indices = [
        Index(value = ["userId", "createdAtMillis"]),
        Index(value = ["status"])
    ]
)
data class GrievanceTicketEntity(
    @PrimaryKey
    val id: String,
    val userId: String,
    val userName: String,
    val category: String,
    val subject: String,
    val description: String,
    val relatedEntityId: String = "",
    val status: String = GrievanceStatus.SUBMITTED,
    val adminResponse: String = "",
    val createdAtMillis: Long = System.currentTimeMillis(),
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "seller_suspensions",
    indices = [
        Index(value = ["isSuspended"])
    ]
)
data class SellerSuspensionEntity(
    @PrimaryKey
    val sellerUserId: String,
    val businessName: String,
    val isSuspended: Boolean,
    val reason: String,
    val suspendedByAdmin: String,
    val updatedAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "trust_audit_logs",
    indices = [
        Index(value = ["createdAtMillis"])
    ]
)
data class TrustAuditLogEntity(
    @PrimaryKey
    val id: String,
    val adminEmail: String,
    val action: String,
    val targetType: String,
    val targetId: String,
    val summary: String,
    val createdAtMillis: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "inquiry_activity_logs",
    indices = [
        Index(value = ["entityId", "timestampMillis"]),
        Index(value = ["entityType"])
    ]
)
data class InquiryActivityLogEntity(
    @PrimaryKey
    val id: String,
    val entityType: String,
    val entityId: String,
    val actorRole: String,
    val actorName: String,
    val statusLabel: String,
    val note: String,
    val timestampMillis: Long = System.currentTimeMillis()
)
