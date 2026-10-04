package com.viladevcorp.hosteo.model.dto;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AlertInfo {

  /** Alerts ordered by severity (red first, then yellow) and start date ascending. */
  private List<AlertItem> alerts = new ArrayList<>();

  /** Number of red (urgent 2-day) alerts. */
  @JsonProperty("nRedAlerts")
  private int nRedAlerts;

  /** Number of yellow (warning 5-day) alerts. */
  @JsonProperty("nYellowAlerts")
  private int nYellowAlerts;
}
