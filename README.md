# Elevator Coding Challenge
*  Scott Van Camp

## Build and run
```
mvn clean install
```
Note unit test output for [ElevatorTest](https://github.com/svc8675309/elevator/blob/main/src/test/java/com/svc/elevator/ElevatorTest.java)

## SCAN ( Disk Scheduling Algorithms ) - But for Elevators

## Key Concepts
### Store requests:
1. The elevator system keeps track of all floor requests, noting whether passengers want to go up or down.
### Direction selection:
   * Initial direction: When idle, the elevator starts moving in the direction with the most requests.
   * Change direction: Once the elevator reaches the last request in its current direction, it reverses and starts servicing requests in the opposite direction.
## Implementation steps:
1. Receive request:
   When a passenger presses a button, the elevator system records the floor number and direction of travel.
2. Analyze requests:
   The system calculates the number of people wanting to go up and down from the current floor.
3. Choose direction:
   Based on the analysis, the elevator starts moving in the direction with the most requests.
4. Service requests:
   While moving in a chosen direction, stop at each floor with a request in that direction.
5. Reverse direction:
   When the elevator reaches the last request in the current direction, change direction and start servicing requests in the opposite direction.
## Advantages
1. No starvation.
2. Efficiently moves from idle to handle the most people waiting. 
## Disadvantages
1. This algorithm is not fair because it causes a long waiting time for passengers that just missed the elevator.
## Considerations for optimization:
### Prioritization:
   Closest first: While traveling in a direction, prioritize requests closer to the elevator's current position to minimize travel time.
   ```
   Example: If 10 people were going up to floor 3 and 1 person was going up to
   floor 2, go to floor 3 first then back down to floor 2.
   ```
### Load capacity:
   The system should also consider the elevator's weight capacity to avoid exceeding it.
### Group control:
   In buildings with multiple elevators, a group control system can coordinate elevator movements to efficiently distribute passenger traffic.
### Destination dispatch:
   Advanced systems can allow passengers to pre-select their destination floor, enabling the elevator to prioritize requests based on the final destination. 
### Fire and Alarm:
   Force elevator to stop on a fire event to let people off. 
