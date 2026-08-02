package com.viladevcorp.hosteo.model;

import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Table(name = "user_sessions")
@NoArgsConstructor
@Getter
@Setter
public class UserSession extends BaseEntity {

  private Instant deletedAt;

  public UserSession(User user) {
    this.user = user;
  }

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;
}
