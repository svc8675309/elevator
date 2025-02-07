package com.svc.elevator;

import com.github.javafaker.Faker;
import com.svc.elevator.scan.Elevator;
import com.svc.elevator.scan.controller.ElevatorController;
import com.svc.elevator.scan.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class ElevatorTest {
  private static Faker faker;

  @BeforeAll
  public static void beforeAll() {
    faker = new Faker();
  }

  @Test
  public void randomElevatorRun() throws Exception{
    ElevatorController elevatorController = new ElevatorController(
      new Elevator(1, 9, new AtomicInteger(1), ElevatorDirection.IDLE));
    CountDownLatch idleLatch = new CountDownLatch(1);
    elevatorController.startElevator(idleLatch);
    try {
      List<Person> people = generatePeople(elevatorController.getElevator(), 100);
      elevatorController.submitPeople(people);
      idleLatch.await(10 * 1000, TimeUnit.SECONDS);
    } finally {
      elevatorController.stopElevator();
    }
  }

  private static List<Person> generatePeople(@NonNull Elevator elevator, int count) {
    int min = elevator.getMinFloor();
    int max = elevator.getMaxFloor();

    List<Person> ret = new ArrayList<>(count); // although it may be less on collision
    for (int i = 0; i < count; i++) {
      int initialFloor = (int) (Math.random() * (max - min + 1)) + min;
      int destinationFloor = (int) (Math.random() * (max - min + 1)) + min;
      if (initialFloor == destinationFloor) {
        continue;
      }
      PersonDirection pd = initialFloor < destinationFloor ? PersonDirection.UP : PersonDirection.DOWN;
      Person person = new Person(faker.name().firstName(),
        new OutsideElevatorRequest(initialFloor, pd),
        new InsideElevatorRequest(destinationFloor));
      ret.add(person);
    }
    return ret;
  }
}
