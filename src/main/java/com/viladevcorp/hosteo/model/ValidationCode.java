package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.utils.ValidationCodeType;

import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.util.*;

@Entity
@Table(name = "validation_codes")
@Getter
@Setter
@NoArgsConstructor
public class ValidationCode extends BaseEntity {

  public static final int EXPIRATION_MINUTES = 15;

  private String createNewCode() {
    Random random = new Random();
    String newCode = Integer.toString(random.nextInt(999999));
    if (newCode.length() < 6) {
      return "0".repeat(6 - newCode.length()) + newCode;
    } else {
      return newCode;
    }
  }

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id")
  private User user;

  private String code = createNewCode();
  private String type;
  private boolean used = false;

  public ValidationCode(ValidationCodeType type) {
    this.type = type.getType();
  }
}
