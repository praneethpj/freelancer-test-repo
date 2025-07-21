package com.respiroc.domain.chat.common.model


import jakarta.persistence.*
import org.hibernate.annotations.CreationTimestamp
import org.hibernate.annotations.UpdateTimestamp
import java.time.Instant

@MappedSuperclass
abstract class BaseAuditEntity {

    @CreationTimestamp
    @Column(updatable = false)
    var created_at: Instant? = null

    @UpdateTimestamp
    var updated_at: Instant? = null
}
