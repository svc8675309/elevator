package com.svc.elevator.scan.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * When inside of an elevator this request is possible
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InsideElevatorRequest {
  // all could be final, but we may want to jsonify these, so effectively final.
  protected int destinationFloor; // Desired floor of a person inside
}
