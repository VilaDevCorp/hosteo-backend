package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.BaseEntityDto;
import com.viladevcorp.hosteo.model.dto.FailedImportedEventDto;
import com.viladevcorp.hosteo.model.types.EventSource;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

import lombok.*;

@Entity
@Table(name = "failed_imported_events")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FailedImportedEvent extends BaseEntity {

  @ManyToOne
  @JoinColumn(name = "apartment_id")
  private Apartment apartment;

  @Column
  private String name;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Instant startDate;

  @Column
  @Temporal(TemporalType.TIMESTAMP)
  private Instant endDate;

  @NotNull
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private EventSource source;

  @NotNull
  @Column(nullable = false)
  private String error;

  @Override
  public BaseEntityDto toDto() {
    return new FailedImportedEventDto(this);
  }
}