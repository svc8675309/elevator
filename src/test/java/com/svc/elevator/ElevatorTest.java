package com.svc.elevator;

import com.github.javafaker.Faker;
import com.svc.elevator.scan.Elevator;
import com.svc.elevator.scan.controller.ElevatorController;
import com.svc.elevator.scan.model.*;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
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

  /**
   * Big test to test general behavior
   */
  @Test
  public void bigTest() throws Exception {
    ElevatorController elevatorController = new ElevatorController(
      new Elevator(1, 9, new AtomicInteger(1), ElevatorDirection.IDLE));
    // Used for unittest automatic completion
    CountDownLatch idleLatch = new CountDownLatch(1);
    elevatorController.startElevator(idleLatch);
    try {
      // Everyone is waiting
      elevatorController.submitPeople(generatePeople(elevatorController.getElevator(), 100));
      Thread.sleep(500);
      elevatorController.submitPeople(generatePeople(elevatorController.getElevator(), 20));
      // Wait ( in real life there would be no countdown latch )
      idleLatch.await(1000 * 60, TimeUnit.SECONDS);
    } finally {
      elevatorController.stopElevator();
    }
  }

  /**
   * Test to ensure elevator will not waste time moving in a direction with no passengers either waiting or riding
   */
  @Test
  public void testEfficientDirection() throws Exception {

    // Start the elevator on floor 5
    Elevator elevator = new Elevator(1, 9, new AtomicInteger(5), ElevatorDirection.IDLE);
    ElevatorController elevatorController = new ElevatorController(elevator);

    try {
      // Not interested in automatic test determination when elevator is empty
      elevatorController.startElevator(null);

      // Elevator should not be moving
      Assertions.assertEquals(elevator.getDirection(), ElevatorDirection.IDLE, "Error elevator has direction!");

      // Need a thread to wait for the person to finish since the elevator is on another thread
      CountDownLatch personGotOff = new CountDownLatch(1);

      // A person on floor 6 wants to go to floor 7.
      final Person person = new Person(faker.name().firstName(),
        new OutsideElevatorRequest(6, PersonDirection.UP),
        new InsideElevatorRequest(7));
      elevatorController.submitPeople(Collections.singletonList(person));

      // In production code this is managed by an executor service, but for a unit test direct Thread usage is fine.
      Thread personChecker = new Thread(() -> {
        try {
          while (!person.getExitedElevator().get()) {
            Thread.sleep(100);
          }
          personGotOff.countDown();
        } catch (InterruptedException ie) {
          // expected
        }
      });
      personChecker.start();

      // Max amount of time to wait for this unit test
      personGotOff.await(10000, TimeUnit.SECONDS);

      Assertions.assertTrue(person.getBoardedElevator().get(), "Person has not boarded the elevator!");
      Assertions.assertTrue(person.getExitedElevator().get(), "Person has not left the elevator!");
      Assertions.assertEquals(7, elevator.getCurrentFloor().get(), "Elevator is not on floor 7!");
      Assertions.assertEquals(ElevatorDirection.IDLE, elevator.getDirection(), "Error elevator has direction!");
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
