package com.svc.elevator.scan.controller;

import com.svc.elevator.scan.Elevator;
import com.svc.elevator.scan.model.ElevatorDirection;
import com.svc.elevator.scan.model.Person;
import com.svc.elevator.scan.model.PersonDirection;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Controls the elevator for this simulation
 */
@Slf4j
@Getter
public class ElevatorController {
  private final Elevator elevator;
  private final AtomicBoolean stopElevator = new AtomicBoolean(false);
  private ExecutorService executorService;
  // List people by initial floor waiting to get on the elevator going up
  private final Map<Integer, List<Person>> waitingUp = Collections.synchronizedMap(new HashMap<>());
  // List people by initial floor waiting to get on the elevator going down
  private final Map<Integer, List<Person>> waitingDown = Collections.synchronizedMap(new HashMap<>());
  // List people by destination floor waiting to get off
  private final Map<Integer, List<Person>> waitingToGetOff = Collections.synchronizedMap(new HashMap<>());
  // Used for testing, may be null
  private CountDownLatch idleLatch;

  public ElevatorController(@NonNull Elevator elevator) {
    this.elevator = elevator;
  }

  /**
   * Get the Mock Elevator running
   *
   * @param idleLatch - if not null it is decremented on idle
   */
  public void startElevator(CountDownLatch idleLatch) {
    if (stopElevator.get()) {
      log.warn("Elevator is running must stop it first!");
      return;
    }
    executorService = Executors.newFixedThreadPool(1);
    this.idleLatch = idleLatch;

    // enqueue the mock running elevator
    // All the logic for scan
    executorService.execute(() -> {
      try {
        while (!stopElevator.get()) {
          if (elevator.getDirection().equals(ElevatorDirection.IDLE)) {
            // Should the elevator move?
            processIdle();
          } else {
            // Get people out and in
            processFloor();
            // move the elevator to the next floor
            processNextFloor();
          }
          Thread.sleep(100); // In real life this would be an event, but this is mocked operation
        }
      } catch (InterruptedException e) {
        // Normal it means that the test is over
      } catch (Exception e) {
        log.error("Exception while running elevator! {}", e.getMessage(), e);
      } finally {
        // regardless of why it stopped
        stopElevator.set(true);
      }
    });
  }

  /**
   * stop the elevator
   */
  public void stopElevator() {
    stopElevator.set(true);
    if (executorService != null) {
      executorService.shutdownNow();
    }
  }

  public void submitPeople(@NonNull List<Person> people) {
    for (Person person : people) {
      submitPersonRequest(person);
    }
  }

  /**
   * Submit a elevator boarding request
   *
   * @param person waiting to get on
   */
  public void submitPersonRequest(@NonNull Person person) {
    if (person.getOutsideElevatorRequest().getDesiredDirection().equals(PersonDirection.UP)) {
      waitingUp.computeIfAbsent(person.getOutsideElevatorRequest().getInitialFloor(),
        k -> Collections.synchronizedList(new ArrayList<>())).add(person);
      log.info(person.toString());
    } else if (person.getOutsideElevatorRequest().getDesiredDirection().equals(PersonDirection.DOWN)) {
      waitingDown.computeIfAbsent(person.getOutsideElevatorRequest().getInitialFloor(),
        k -> Collections.synchronizedList(new ArrayList<>())).add(person);
      log.info(person.toString());
    } else {
      log.warn("Unknown person direction {} !", person.getOutsideElevatorRequest().getDesiredDirection());
    }
  }

  private void processNextFloor() {
    // Check for idle, why go anywhere if there is no reason
    // Need to figure out what direction the elevator should go in first.
    long waitingToGoUp = waitingUp.values().stream()
      .mapToLong(List::size).sum();
    long waitingToGoDown = waitingDown.values().stream()
      .mapToLong(List::size).sum();
    long waitingToLeave = waitingToGetOff.values().stream()
      .mapToLong(List::size).sum();
    if (waitingToGoUp == 0 && waitingToGoDown == 0 && waitingToLeave == 0) {
      elevator.setDirection(ElevatorDirection.IDLE);
      if (idleLatch != null) {
        log.info("All Done :-)");
        idleLatch.countDown();
      }
    } else {
      int elevatorCurrentFloor = elevator.getCurrentFloor().get();
      if (elevator.getDirection().equals(ElevatorDirection.UP)) {
        if (elevatorCurrentFloor == elevator.getMaxFloor()) {
          // Switch directions
          elevator.setDirection(ElevatorDirection.DOWN);
          // In this case process floor
          processFloor();
          elevator.getCurrentFloor().decrementAndGet();
          log.info(elevator.toString());
        } else {
          elevator.getCurrentFloor().incrementAndGet();
          log.info(elevator.toString());
        }
      } else if (elevator.getDirection().equals(ElevatorDirection.DOWN)) {
        if (elevatorCurrentFloor == elevator.getMinFloor()) {
          // Switch directions
          elevator.setDirection(ElevatorDirection.UP);
          // In this case process floor
          processFloor();
          elevator.getCurrentFloor().incrementAndGet();
          log.info(elevator.toString());
        } else {
          elevator.getCurrentFloor().decrementAndGet();
          log.info(elevator.toString());
        }
      }
    }
  }

  /**
   * Get people out and in
   */
  private void processFloor() {
    int elevatorCurrentFloor = elevator.getCurrentFloor().get();
    // Process the people getting off the elevator first
    List<Person> leaving = waitingToGetOff.getOrDefault(elevatorCurrentFloor, Collections.emptyList());
    leaving.forEach(p -> log.info("{} is getting off on floor {}", p.getName(), elevatorCurrentFloor));
    // Bye Bye
    leaving.clear();

    // To board one must know if they want to go up or down...
    List<Person> entering = null;
    if (elevator.getDirection().equals(ElevatorDirection.UP)) {
      entering = waitingUp.getOrDefault(elevatorCurrentFloor, Collections.emptyList());
    } else {
      entering = waitingDown.getOrDefault(elevatorCurrentFloor, Collections.emptyList());
    }

    entering.forEach(p -> log.info("On the floor {} to floor {} {} is getting on going {}.",
      elevatorCurrentFloor, p.getInsideElevatorRequest().getDestinationFloor(), p.getName(), p.getOutsideElevatorRequest().getDesiredDirection()));

    // Add them to the waiting to get off group
    entering.forEach(p -> waitingToGetOff.computeIfAbsent(p.getInsideElevatorRequest().getDestinationFloor(),
      k -> Collections.synchronizedList(new ArrayList<>())).add(p));
    // they have all entered
    entering.clear();
  }

  /**
   * Should the elevator move?
   */
  private void processIdle() {
    // Need to figure out what direction the elevator should go in first.
    long waitingToGoUp = waitingUp.values().stream()
      .mapToLong(List::size).sum();
    long waitingToGoDown = waitingDown.values().stream()
      .mapToLong(List::size).sum();
    if (waitingToGoUp == 0 && waitingToGoDown == 0) {
      log.info("No people need the elevator, remaining idle.");
      elevator.setDirection(ElevatorDirection.IDLE);
    } else if (waitingToGoUp < waitingToGoDown && elevator.getCurrentFloor().get() != elevator.getMinFloor()) {
      elevator.setDirection(ElevatorDirection.DOWN);
      log.info(elevator.toString());
    } else {
      elevator.setDirection(ElevatorDirection.UP);
      log.info(elevator.toString());
    }
  }

}
