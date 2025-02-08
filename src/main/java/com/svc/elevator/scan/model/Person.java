package com.svc.elevator.scan.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.atomic.AtomicBoolean;

@Getter
public class Person {
  protected String name;
  protected AtomicBoolean boardedElevator = new AtomicBoolean(false);
  protected AtomicBoolean exitedElevator = new AtomicBoolean(false);
  // Effectively a person knows where they want to go when they enter
  //   So both events are populated
  protected OutsideElevatorRequest outsideElevatorRequest; // before a person enters an elevator
  protected InsideElevatorRequest insideElevatorRequest;   // after a person enters an elevator

  public Person(String name, OutsideElevatorRequest outsideElevatorRequest, InsideElevatorRequest insideElevatorRequest) {
    this.name = name;
    this.outsideElevatorRequest = outsideElevatorRequest;
    this.insideElevatorRequest = insideElevatorRequest;
  }

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
