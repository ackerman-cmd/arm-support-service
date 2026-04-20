package com.base.armsupportservice.domain.appeal

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime
import java.util.UUID

@Entity
@Table(name = "appeal_message_attachments", schema = "arm_support")
class AppealMessageAttachment(
    @Id
    val id: UUID = UUID.randomUUID(),
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: AppealMessage,
    /** photo, doc, audio_message */
    @Column(name = "attachment_type", nullable = false, length = 50)
    val attachmentType: String,
    @Column(name = "file_name", nullable = false)
    val fileName: String,
    @Column(name = "mime_type", nullable = false, length = 100)
    val mimeType: String,
    /** S3/MinIO object key in vk_service storage */
    @Column(name = "s3_key", nullable = false)
    val s3Key: String,
    /** Full URL accessible for download */
    @Column(name = "s3_url", nullable = false)
    val s3Url: String,
    @Column(name = "file_size")
    val fileSize: Long? = null,
    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)
