package com.viladevcorp.hosteo.model.types;

public enum Alert {
  DAYS_LEFT_2_UNASSIGNED,
  DAYS_LEFT_5_UNASSIGNED,
  DAYS_LEFT_2_NOT_COMPLETED;

  /** Red alerts are urgent (2-day) alerts; yellow alerts are warning (5-day) alerts. */
  public boolean isRed() {
    return this == DAYS_LEFT_2_UNASSIGNED || this == DAYS_LEFT_2_NOT_COMPLETED;
  }
}