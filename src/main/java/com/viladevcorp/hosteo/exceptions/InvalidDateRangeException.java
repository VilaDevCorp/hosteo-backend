package com.viladevcorp.hosteo.exceptions;

import lombok.NoArgsConstructor;

@NoArgsConstructor
public class InvalidDateRangeException extends Exception {
  public InvalidDateRangeException(String message) {
    super(message);
  }
}
