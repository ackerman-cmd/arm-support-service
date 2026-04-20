package com.base.armsupportservice.dto.appeal

import com.base.armsupportservice.domain.appeal.AppealMessageAttachment
import java.util.UUID

data class AppealMessageAttachmentResponse(
    val id: UUID,
    val attachmentType: String,
    val fileName: String,
    val mimeType: String,
    val s3Url: String,
    val fileSize: Long?,
) {
    companion object {
        fun from(a: AppealMessageAttachment) =
            AppealMessageAttachmentResponse(
                id = a.id,
                attachmentType = a.attachmentType,
                fileName = a.fileName,
                mimeType = a.mimeType,
                s3Url = a.s3Url,
                fileSize = a.fileSize,
            )
    }
}
