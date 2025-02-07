package com.svc.elevator.scan.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Person {
  protected String name;
  // Effectively a person knows where they want to go when they enter
  //   So both events are populated
  protected OutsideElevatorRequest outsideElevatorRequest; // before a person enters an elevator
  protected InsideElevatorRequest insideElevatorRequest;   // after a person enters an elevator

  @Override
  public String toString() {
    String ret = "On floor " + outsideElevatorRequest.getInitialFloor() + " "
      + outsideElevatorRequest.getDesiredDirection().name();
    if (outsideElevatorRequest.getDesiredDirection() == PersonDirection.UP) {
      return ret + "   to floor " + insideElevatorRequest.getDestinationFloor() + " " + name + " is waiting.";
    } else {
      return ret + " to floor " + insideElevatorRequest.getDestinationFloor() + " " + name + " is waiting.";
    }
  }
}
