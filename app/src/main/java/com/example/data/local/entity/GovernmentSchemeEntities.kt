package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.GovernmentScheme
import com.example.data.model.SchemeFarmerType

@Entity(tableName = "cached_government_schemes")
data class GovernmentSchemeEntity(
    @PrimaryKey
    val id: String,
    val govLevel: String,
    val category: String,
    val extraCategoriesCsv: String = "",
    val farmerTypesCsv: String = SchemeFarmerType.ALL_FARMERS.name,
    val activityFocus: String,
    val northBengalPriority: Int,
    val discontinued: Boolean,
    val applicationStatus: String,
    val officialUrl: String,
    val officialSourceUrl: String,
    val lastVerifiedDate: String,
    val nameEn: String,
    val nameBn: String,
    val nameHi: String,
    val departmentEn: String,
    val departmentBn: String,
    val departmentHi: String,
    val shortDescEn: String,
    val shortDescBn: String,
    val shortDescHi: String,
    val objectivesEn: String,
    val objectivesBn: String,
    val objectivesHi: String,
    val whoCanApplyEn: String,
    val whoCanApplyBn: String,
    val whoCanApplyHi: String,
    val eligibilityEn: String,
    val eligibilityBn: String,
    val eligibilityHi: String,
    val documentsEn: String,
    val documentsBn: String,
    val documentsHi: String,
    val benefitsEn: String,
    val benefitsBn: String,
    val benefitsHi: String,
    val procedureEn: String,
    val procedureBn: String,
    val procedureHi: String,
    val statusNoteEn: String,
    val statusNoteBn: String,
    val statusNoteHi: String,
    val cropTagsEn: String,
    val cropTagsBn: String,
    val cropTagsHi: String,
    val updatedByAdmin: String,
    val cachedAtMillis: Long = System.currentTimeMillis(),
    val isLocalAdminOverride: Boolean = false
) {
    fun toModel(): GovernmentScheme = GovernmentScheme(
        id = id,
        govLevel = govLevel,
        category = category,
        extraCategories = extraCategoriesCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() },
        farmerTypes = farmerTypesCsv
            .split(",")
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .ifEmpty { listOf(SchemeFarmerType.ALL_FARMERS.name) },
        activityFocus = activityFocus,
        northBengalPriority = northBengalPriority,
        discontinued = discontinued,
        applicationStatus = applicationStatus,
        officialUrl = officialUrl,
        officialSourceUrl = officialSourceUrl,
        lastVerifiedDate = lastVerifiedDate,
        nameEn = nameEn,
        nameBn = nameBn,
        nameHi = nameHi,
        departmentEn = departmentEn,
        departmentBn = departmentBn,
        departmentHi = departmentHi,
        shortDescEn = shortDescEn,
        shortDescBn = shortDescBn,
        shortDescHi = shortDescHi,
        objectivesEn = objectivesEn,
        objectivesBn = objectivesBn,
        objectivesHi = objectivesHi,
        whoCanApplyEn = whoCanApplyEn,
        whoCanApplyBn = whoCanApplyBn,
        whoCanApplyHi = whoCanApplyHi,
        eligibilityEn = eligibilityEn,
        eligibilityBn = eligibilityBn,
        eligibilityHi = eligibilityHi,
        documentsEn = documentsEn,
        documentsBn = documentsBn,
        documentsHi = documentsHi,
        benefitsEn = benefitsEn,
        benefitsBn = benefitsBn,
        benefitsHi = benefitsHi,
        procedureEn = procedureEn,
        procedureBn = procedureBn,
        procedureHi = procedureHi,
        statusNoteEn = statusNoteEn,
        statusNoteBn = statusNoteBn,
        statusNoteHi = statusNoteHi,
        cropTagsEn = cropTagsEn,
        cropTagsBn = cropTagsBn,
        cropTagsHi = cropTagsHi,
        updatedByAdmin = updatedByAdmin
    )
}

fun GovernmentScheme.toEntity(
    cachedAtMillis: Long = System.currentTimeMillis(),
    isLocalAdminOverride: Boolean = false
): GovernmentSchemeEntity = GovernmentSchemeEntity(
    id = id,
    govLevel = govLevel,
    category = category,
    extraCategoriesCsv = extraCategories
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .joinToString(","),
    farmerTypesCsv = farmerTypes
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .distinct()
        .ifEmpty { listOf(SchemeFarmerType.ALL_FARMERS.name) }
        .joinToString(","),
    activityFocus = activityFocus,
    northBengalPriority = northBengalPriority,
    discontinued = discontinued,
    applicationStatus = applicationStatus,
    officialUrl = officialUrl,
    officialSourceUrl = officialSourceUrl,
    lastVerifiedDate = lastVerifiedDate,
    nameEn = nameEn,
    nameBn = nameBn,
    nameHi = nameHi,
    departmentEn = departmentEn,
    departmentBn = departmentBn,
    departmentHi = departmentHi,
    shortDescEn = shortDescEn,
    shortDescBn = shortDescBn,
    shortDescHi = shortDescHi,
    objectivesEn = objectivesEn,
    objectivesBn = objectivesBn,
    objectivesHi = objectivesHi,
    whoCanApplyEn = whoCanApplyEn,
    whoCanApplyBn = whoCanApplyBn,
    whoCanApplyHi = whoCanApplyHi,
    eligibilityEn = eligibilityEn,
    eligibilityBn = eligibilityBn,
    eligibilityHi = eligibilityHi,
    documentsEn = documentsEn,
    documentsBn = documentsBn,
    documentsHi = documentsHi,
    benefitsEn = benefitsEn,
    benefitsBn = benefitsBn,
    benefitsHi = benefitsHi,
    procedureEn = procedureEn,
    procedureBn = procedureBn,
    procedureHi = procedureHi,
    statusNoteEn = statusNoteEn,
    statusNoteBn = statusNoteBn,
    statusNoteHi = statusNoteHi,
    cropTagsEn = cropTagsEn,
    cropTagsBn = cropTagsBn,
    cropTagsHi = cropTagsHi,
    updatedByAdmin = updatedByAdmin,
    cachedAtMillis = cachedAtMillis,
    isLocalAdminOverride = isLocalAdminOverride
)

@Entity(
    tableName = "government_scheme_bookmarks",
    primaryKeys = ["userId", "schemeId"]
)
data class GovernmentSchemeBookmarkEntity(
    val userId: String,
    val schemeId: String,
    val bookmarkedAtMillis: Long = System.currentTimeMillis()
)
