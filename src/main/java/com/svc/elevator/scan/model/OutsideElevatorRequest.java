package com.svc.elevator.scan.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * When outside of an elevator this request is possible
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class OutsideElevatorRequest {
  // all could be final, but we may want to jsonify these, so effectively final.
  protected int initialFloor;            // The floor that the person is waiting for the elevator on.
  protected PersonDirection desiredDirection;  // The direction that the person wishes to go.
}
