package com.sangeetsetu.app.model

enum class ApprovalStatus {
    PENDING, APPROVED, REJECTED
}

data class ArtistProfile(
    val artistId: String = "",
    val name: String = "",
    val phone: String = "",
    val category: String = "",
    val experienceYears: Int = 0,
    val state: String = "",
    val district: String = "",
    val city: String = "",
    val isAvailable: Boolean = true,
    val rating: Float = 0.0f,
    val bio: String = "",
    val idProofUrl: String = "", 
    val profilePicUrl: String = "",
    val approvalStatus: ApprovalStatus = ApprovalStatus.APPROVED,
    val isVerifiedBadge: Boolean = false, 
    val rejectionReason: String = "",
    val appliedAt: Long = System.currentTimeMillis()
)
