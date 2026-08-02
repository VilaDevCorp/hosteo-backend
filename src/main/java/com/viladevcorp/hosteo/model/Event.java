package com.viladevcorp.hosteo.model;

import com.viladevcorp.hosteo.model.dto.EventDto;
import com.viladevcorp.hosteo.model.types.EventSource;
import com.viladevcorp.hosteo.model.types.EventState;
import com.viladevcorp.hosteo.model.types.EventType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "events")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
@AllArgsConstructor
public class Event extends BaseEntity {

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventType type;

  @NotNull
  @Column(nullable = false)
  private String name;

  @Enumerated(EnumType.STRING)
  private EventSource source;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private EventState state;

  @ManyToOne
  @JoinColumn(name = "apartment_id")
  private Apartment apartment;

  @OneToMany(mappedBy = "event")
  @Builder.Default
  private Set<Assignment> assignments = new HashSet<>();

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant startDate;

  @NotNull
  @Column(nullable = false)
  @Temporal(TemporalType.TIMESTAMP)
  private Instant endDate;

  @Override
  public EventDto toDto() {
    return new EventDto(this);
  }
}
