package com.svc.elevator.scan;

import com.svc.elevator.scan.model.ElevatorDirection;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NonNull;

import java.util.concurrent.atomic.AtomicInteger;

@Getter
@AllArgsConstructor
public class Elevator {
  private int minFloor;
  private int maxFloor;
  private AtomicInteger currentFloor;
  private ElevatorDirection direction;

  public void setDirection(@NonNull ElevatorDirection elevatorDirection){
    this.direction = elevatorDirection;
  }

  @Override
  public String toString() {
    return ">>> Elevator is at floor " + currentFloor.get() + " and is going " + direction.name();
  }
}
