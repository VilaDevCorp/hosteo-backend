package com.viladevcorp.hosteo.model;

import java.time.Instant;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.viladevcorp.hosteo.model.dto.BaseEntityDto;
import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor
@Getter
@Setter
@SuperBuilder
@AllArgsConstructor
public abstract class BaseEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @CreatedDate
  @Column(updatable = false)
  private Instant createdAt;

  @ManyToOne
  @JoinColumn(name = "createdBy")
  @CreatedBy
  @JsonIgnore
  private User createdBy;

  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null || this.getClass() != obj.getClass()) {
      return false;
    }
    BaseEntity other = (BaseEntity) obj;
    return this.id.equals(other.id);
  }

  public int hashCode() {
    // if ID is null, return System.identityHashCode for uniqueness in memory
    return id != null ? id.hashCode() : System.identityHashCode(this);
  }

  public BaseEntityDto toDto() {
    throw new UnsupportedOperationException(
        "toDto() is not implemented for " + this.getClass().getName());
  }
}
