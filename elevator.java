import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

//Final Project 381 Simulation and Modeling
//Elevator Simulation
//Jonathan Dinh
//Seungwon Chun

/* More on final project requirements:
1a. There is a future events list that contains an event/time in the future during which a new person
will arrive in the building. This is implemented on line 229. The exponential distrubution is implemented on lines 209 and 212
- When a person leaves the futureevents list, a new person is added on lines 326 and 347
- Note: exactly one person is on the futureevents list at a given time.

1b. Each elevator on the elevators array contains an attribute, finishtime, which represents some time
in the future during which an elevator will start or finish a task. This is implemented on line 120, when the
attribute is created. Instead of gathering both, elevator events and people events onto one futureevents list, 
they are separated in this manner: people events on the arraylist called futureevents, and elevator events 
in the finishtime attribute for each elevator object on the elevator array. The finishtime value is updated on line 582

2. The current clock time is shown in each iteration.
In the case of a person arriving in the building, the delayed queue, as well as the futureevents list is shown.
In the case of an elevator starting or finishing a task, the elevator finishtimes are shown, and the elevators are shown
in index order. 
The delayed list in this program is represented by an arraylist called waitingqueue, which represents
all customers in the building that are currently waiting for an elevator. Once an elevator picks them up,
they are transferred from the waitingqueue to the onboard arraylist. */

public class elevator {
	// Global variables
	static int personCount = 0;
	static Double clock = 0.0;
	static Double prevclock = 0.0;

	/* A Person is a customer that will arrive on a floor between 0 and 5, and request an elevator 
	with a desired direction. Each Person will have an: 
	- index
	- arrival time: the time they move from the future events list to the waiting queue.
	- current floor: this value will update as they move from floor to floor with an elevator.
	- desired floor: the floor that the customer would like to be moved to.
	- desired direction: 0 for down, 1 for up
	- status: waiting or onboard
	- the elevator number that they are on (0-3)*/
	public static class Person implements Comparable<Person> {
		int index;
		double arrivaltime;
		int currentfloor;
		int desiredfloor;

		// up = 1 -- down = 0
		int desireddirection;

		// Possible statuses: waiting, onboard
		String status;
		int elevator;

		public Person(int index, double arrivaltime, int currentfloor, int desiredfloor, 
		String status, int elevator, int desireddirection) {
			this.index = index;
			this.arrivaltime = arrivaltime;
			this.currentfloor = currentfloor;
			this.desiredfloor = desiredfloor;
			this.status = status;
			this.elevator = elevator;
			this.desireddirection = desireddirection;
		}

		public int getDesiredfloor() {
			return desiredfloor;
		}

		public void setDesiredfloor(int desiredfloor) {
			this.desiredfloor = desiredfloor;
		}

		public double getArrivaltime() {
			return arrivaltime;
		}
		public int getIndex() {
			return index;
		}
		public void setArrivaltime(double arrivaltime) {
			this.arrivaltime = arrivaltime;
		}
		@Override
		public String toString() {
			return  "Person " + index + " waiting on floor " + currentfloor + 
			" [arrivaltime = " + arrivaltime + "] wants go to floor " + desiredfloor ;
		}

		@Override
		public int compareTo(Person P) {
			int comp =((Person)P).getDesiredfloor();
			
			return this.desiredfloor - comp;
		}
	}

	/* An elevator will move from floor to floor, satisfying requests made by customers in the most 
	optimal way. An elevator will have an:
	- index: 0 - 3
	- direction: 0 for down, 1 for up
	- currentfloor: 0 - 5. This value will be updated as the elevator moves from floor to floor.
	- capacity: the number of people that the elevator is currently carrying.
	- status: moving or standby. If the elevator is not fulfilling any requests, it is on standby.
	- finishtime: the time when the elevator will finish fulfilling its current requests. 
				  it will not be given another request until the clock >= this time. */

	public static class Elevator {
		int index;
		int direction;
		int currentfloor;
		int capacity;
		String status;
		Double finishtime;
		public Elevator(int index, int direction, int currentfloor, int capacity, String status, 
		Double finishtime) {
			this.index = index;
			this.direction = direction;
			this.currentfloor = currentfloor;
			this.capacity = capacity;
			this.status = status;
			this.finishtime = finishtime;

		}

		public void setIndex(int newIndex) {
			this.index = newIndex;
		}
		public void setDirection(int newDirection) {
			this.direction = newDirection;
		}
		public void setCurrentFloor(int newCurrentFloor) {
			this.currentfloor = newCurrentFloor;
		}
		public void setCapacity(int newCapacity) {
			this.capacity = newCapacity;
		}
		public void setStatus (String newStatus) {
			this.status = newStatus;
		}
		public void setfinishtime (Double newfinishtime) {
			this.finishtime = newfinishtime;
		}
 		public int getIndex() {
			return index;
		}
		public int getDirection() {
			return direction;
		}
		public int getCurrentFloor() {
			return currentfloor;
		}
		public int getCapacity() {
			return capacity;
		}
		public String getStatus() {
			return status;
		}
		public Double getfinishtime() {
			return finishtime;
		}
		
		@Override
		public String toString() {
			return ("index: " +this.getIndex()+
					" current direction: "+this.getDirection()+
					" finishing floor: "+this.getCurrentFloor()+
					" finishing capacity: "+this.getCapacity()+
					" finishing status: "+this.getStatus()+
					" finishtime: "+this.getfinishtime());
		}




	
	}
	
	/* Method that is used to create a new person.  */
	static Person createPerson() {
		Random random = new Random();
		double u;
		double exp;

		u = random.nextDouble();
		int current_f = random.nextInt(10);  // random variable for current floor
		int desired_f = random.nextInt(5);    // random variable for desired floor
		int desireddirection;
		if( current_f == 0 || current_f > 5) {  // 50% of current floor is floor zero
			current_f = 0;
		}
		if( current_f == desired_f ) {      // avoid to have current floor = desired floor
			desired_f = desired_f -1;
		}
		if( desired_f == -1) {				
			desired_f = desired_f + 2;
		}
		if (desired_f > current_f) {
			desireddirection = 1;
		}
		else desireddirection = 0;
		if(current_f == 0) { 
			exp = clock + (-Math.log( 1 - u )) * (6); 
		}
		else { 
			exp = clock + (-Math.log( 1 - u )) * (3);
		}
		
		Person newPerson = new Person(personCount,exp,current_f,desired_f,"waiting",0, desireddirection);
		personCount++;
		return newPerson;
	}
	public static void main(String arg[]) throws FileNotFoundException{
		PrintStream out = new PrintStream(new FileOutputStream("output.txt")); 
    	System.setOut(out);


		/* Delayed List: Waitingqueue is an ArrayList of Person objects containing the people that  
		are waiting for an elevator on a given floor that they arrive on. */
		ArrayList<Person> waitingqueue = new ArrayList<Person>();
		/* Future Events list of People arriving: Futureevents is an ArrayList of Person objects  
		containing people that are scheduled to arrive at a later time in the future. */
		ArrayList<Person> futureevents = new ArrayList<Person>();
		// Onboard is an ArrayList of Person objects containing people that are currently onboard an elevator.
		ArrayList<Person> onboard = new ArrayList<Person>();
		// Elevators[] is an array containing 4 Elevator objects
		/* Future Events list pt. 2: each Elevator contains a finishtime, which represents some event
		in the future, during which an elevator will start or finish a task. */
		Elevator[] elevators = new Elevator[4];
		elevators[0] = new Elevator(0, 1, 0, 0, "standby", 0.0);
		elevators[1] = new Elevator(1, 1, 0, 0, "standby", 0.0);
		elevators[2] = new Elevator(2, 1, 0, 0, "standby", 0.0);
		elevators[3] = new Elevator(3, 1, 0, 0, "standby", 0.0);

		for (int i = 1; i <= 20; i++) {
			System.out.println("[iteration: " + i + "]");
			// System.out.println(waitingqueue);
			/* Test cases to check if the next event to occur is either generating a person or moving an elevator.
			The waitingqueue can be empty, but the futureevents list can't. There must always be an 
			event scheduled to happen in the future. In these test cases, we compare the futureevents
			list of people arriving with the elevator finishtimes to find out which event will occur next. */
			Boolean generatePerson = false;
			Boolean specific = false;
			Boolean multiplePerson = false;

			// If this is the first iteration, we must generate a person.
			if (i == 1) {
				System.out.println("First iteration");
				specific = true;
				generatePerson = true;
			}
			/* If elevators are all on standby and no one in the waitingqueue, elevators have 
			nothing to do. We can generate a person. */
			else if (waitingqueue.size() == 0) {
				// Check if all elevators are on standby
				int check = 0;
				if (onboard.size() == 0) generatePerson = true;
				for (int elevator = 0; elevator < elevators.length; elevator++) {
					if (elevators[elevator].status != "standby") check = 1;
				}
				if (check == 0) {
					System.out.println("No one in the building, generating a person.");
					specific = true;
					generatePerson = true;
				}
			}
			/* If a person arrives before the previous person is able to leave in an elevator, 
			we consider those people arriving "at the same time." */
			else if (waitingqueue.size() > 0) {
				if (futureevents.get(0).arrivaltime - waitingqueue.get(waitingqueue.size()-1).arrivaltime < 2) {
					System.out.println("People arrived at the same time");
					specific = true;
					multiplePerson = true;
					generatePerson = true;
				} 
			}
			// if waitingqueue is not empty and there are elevators on standby, move those elevators
			if (multiplePerson == false) {
				if (waitingqueue.size() > 0) {
					outer: for (int elevator = 0; elevator < elevators.length;elevator++) {
						if (elevators[elevator].status == "standby" && futureevents.get(0).arrivaltime > elevators[elevator].finishtime) {
							//System.out.println("Standby elevators are picking up people in waitingqueue");
							specific = true;
							generatePerson = false;
							break outer;
						}
					}
				}
			}
			if (specific == false) {
					outer: for (int elevator = 0; elevator < elevators.length; elevator++) {
					// If the next person is scheduled to arrive before the moving elevators are available
					if (elevators[elevator].finishtime > 0.0 && elevators[elevator].finishtime >= clock && elevators[elevator].status == "moving") {
						if (futureevents.get(0).arrivaltime < elevators[elevator].finishtime) {
							// System.out.println("Peoples turn");
							generatePerson = true;                             
						}
						/* If there is a moving elevator that is scheduled to finish before the next
						person is scheduled to arrive, we must handle that elevator. */
						else {
							// System.out.println ("Elevators turn");
							generatePerson = false;
							break outer;
						}
					}
				}
			}

			// --------------------------- AFTER TEST CASES: --------------------------------
			// If we found that the next scheduled event is the arrival of a person:
			if (generatePerson == true) {
				if (i == 1) { 
					// Create the first person to arrive in the building.
					Person firstPerson = createPerson();
					clock = firstPerson.arrivaltime;
					waitingqueue.add(firstPerson);
					/* Arrival of a person generates a new person. This will ensure that the 
					futureevents ArrayList will never be empty. */
					Person newPerson = createPerson();
					futureevents.add(newPerson); 
					System.out.println("[Clock: "+clock+"]");
				}
				// If this is not the first iteration of the simulation:
				else { 
					/* Set the clock to the arrivaltime of the next person scheduled to arrive.
					Transfer that person from the futureevents ArrayList to the waitingqueue 
					(the arrival of the person is no longer a futureevent with the updated clock).
					This is "the arrival of the person to the building."*/
					if (clock < futureevents.get(0).arrivaltime) {
						prevclock = clock; // test
						clock = futureevents.get(0).arrivaltime;
					}
					if (clock >= prevclock) {
						System.out.println("Success");
					}
					else System.out.println ("Failure");
					waitingqueue.add(futureevents.get(0));
					futureevents.remove(futureevents.get(0));
					// Arrival of a person generates a new person
					Person newPerson = createPerson();
					futureevents.add(newPerson);
					System.out.println("[Clock: "+clock+"]");
				}
				// Printing out the waitingqueue.
				for(Person p: waitingqueue) {
					System.out.print(p + "\n");
				}
				// Printing out the next futureevent.
				System.out.println("future event: "+futureevents.get(0));
				System.out.print("\n");
			}

			// If the next scheduled event is the finishing of an elevator task:
			else {
				System.out.println("waiting queue size: "+ waitingqueue.size());
				// ----------------- Find the elevator that is next to move -----------------------
				
				/* Compare elevators' finish time to find next available elevator to move. The 
				next elevator to move is the one with the shortest finish time. */
				/* If the waitingqueue is empty, we only check the elevators that are not on standby, 
				The elevators that are on standby have no tasks to complete. */
				Elevator currentElevator = elevators[0];
				Arrays.sort(elevators,(ele1,ele2)->Double.compare(ele1.getfinishtime(), ele2.getfinishtime()));
				
				if (waitingqueue.size() == 0) { 
					
					outer:for (int elevator = 0; elevator < elevators.length; elevator++) {
						if (elevators[elevator].status == "moving"  ) {
							currentElevator = elevators[elevator];
							break outer;
						} 
					}
				} 
				/* If the waiting queue is not empty, we include the elevators without people
				onboard as well, as they will be able to pick up people in the waitingqueue. */
				
				else {
						
						for (int elevator = 0; elevator < elevators.length; elevator++) {
						// Elevators on standby will have priority over elevators that are moving.
						// This will ensure that we utilize all the elevators possible.
						/*if (elevators[elevator].status == "standby" && elevators[elevator].finishtime < currentElevator.finishtime) {
							beststandby = elevators[elevator];
						}*/
						/* If there are no elevators on standby, the next elevator to move is the
						elevator that will finish their task before the others.*/
						if (elevators[elevator].finishtime < currentElevator.finishtime) {
							currentElevator = elevators[elevator];
						}
					}
				}
				
				// Update the clock.
				// We don't want the clock to update to 0.0.
				if (currentElevator.finishtime == 0.0) {
					prevclock = clock;
					clock += currentElevator.finishtime;
				}
				else {
					/*if (currentElevator.status != "standby") {
						clock = currentElevator.finishtime;
					}*/
					if (clock < currentElevator.finishtime) {
						prevclock = clock;
						clock = currentElevator.finishtime;
					}
				}
				Arrays.sort(elevators,(ele1,ele2)->Double.compare(ele1.getIndex(), ele2.getIndex()));
				// test code
				if (clock >= prevclock) {
					System.out.println("Success");
				}
				else System.out.println ("Failure");
				System.out.println("currentElevator: " + currentElevator.index);
				
				System.out.println("[Clock: "+clock+"]");
				// --------------------- Implementing elevator movement ---------------------------
				// ------------------------ Find the designated floor -----------------------------
				// designatedfloor is the floor that the elevator will move to next.
				int designatedfloor = 0;		
				/* pickordrop lets the user know if the elevator is picking someone up or dropping someone off.
				0: picking someone up, 1: dropping someone off, -1: reset to floor 0 and go on standby.
				-2: finished resetting */
				int pickordrop = 0;
				// If the currentElevator is empty, it can take the next person in the waitingqueue.
				if (currentElevator.capacity == 0) {
					if (waitingqueue.size() != 0) {
						designatedfloor = waitingqueue.get(0).currentfloor;
					}
					else currentElevator.status = "standby";
				}
				/* If the currentelevator has people onboard, then we have to compare the elevator 
				passengers' desired floor with the next floor in the waitingqueue. We must also compare
				each passengers' desired floor with each other to find the passenger whose floor is 
				closest to the elevator's current floor. If the elevator's passengers' desired floor 
				comes before the next floor in the waitingqueue, we must
				go to that floor first.*/
				else {
					if (waitingqueue.size() != 0) {
						designatedfloor = waitingqueue.get(0).currentfloor;
						// Check all people onboard an elevator
						for (int person = 0; person < onboard.size(); person++) {
							// If they're on the elevator
							if (onboard.get(person).elevator == currentElevator.index) {
								if (currentElevator.direction == 1) {
									if (onboard.get(person).desiredfloor <= designatedfloor) {
										designatedfloor = onboard.get(person).desiredfloor;
										pickordrop = 1;
									} 
									else {
										designatedfloor = waitingqueue.get(0).currentfloor;
									}
								}
								else {
									if (onboard.get(person).desiredfloor >= designatedfloor) {
										designatedfloor = onboard.get(person).desiredfloor;
										pickordrop = 1;
									} 
									else {
										designatedfloor = waitingqueue.get(0).currentfloor;
									}
								}
							}
						}
					}
					/* If waitingqueue size is 0 but there are people on the elevator, compare the 
					passengers' desired floors with each other to find the passenger whos' 
					desired floor is closest to the elevator's current floor.*/
					else {
						if (currentElevator.direction == 1) {
							designatedfloor = 6;
							for (int person = 0; person < onboard.size(); person++) {
								// if they're on the elevator
								if (onboard.get(person).elevator == currentElevator.index) {
									if (onboard.get(person).desiredfloor <= designatedfloor) {
										designatedfloor = onboard.get(person).desiredfloor;
										pickordrop = 1;
									} 
								}
							}
						}
						
						else {
							designatedfloor = -1;
							for (int person = 0; person < onboard.size(); person++) {
								// if they're on the elevator
								if (onboard.get(person).elevator == currentElevator.index) {
									if (onboard.get(person).desiredfloor >= designatedfloor) {
										designatedfloor = onboard.get(person).desiredfloor;
										pickordrop = 1;
									} 
								}
							}			 
						}
					}
				}
				// ------------------ Move the elevator to the designated floor --------------------
				/* totalservicetime is the total amount of time it will take the elevator to 
				complete its task. */
				double totalservicetime = 0;
				if (currentElevator.direction == 1) {
					if (designatedfloor < currentElevator.currentfloor) {
						currentElevator.direction = 0;
						totalservicetime += 6 * (currentElevator.currentfloor - designatedfloor);
					}
					else totalservicetime += 6 * (designatedfloor - currentElevator.currentfloor);
				}
				else {
					if (designatedfloor > currentElevator.currentfloor) {
						currentElevator.direction = 1;
						totalservicetime += 6 * (designatedfloor - currentElevator.currentfloor);
					}
					else totalservicetime += 6 * (currentElevator.currentfloor - designatedfloor);
				}

				
					// Move the elevator
				currentElevator.currentfloor = designatedfloor;
				
				// If the elevator is now on the topmost floor, it can only go down and vice versa.
				if (currentElevator.currentfloor == 5) {
					currentElevator.direction = 0;
				}
				if (currentElevator.currentfloor == 0) {
					currentElevator.direction = 1;
				}
				// Update the currentfloor of the elevators' passengers to the elevator's new currentfloor.
				for (int passenger = 0; passenger < onboard.size(); passenger++) {
					if (onboard.get(passenger).elevator == currentElevator.index) {
						onboard.get(passenger).currentfloor = currentElevator.currentfloor;
					}
				}
				// Drop off passengers if the elevator is now at their desired floor.
				for (int passenger = 0; passenger < onboard.size(); passenger++) {
					if (onboard.get(passenger).elevator == currentElevator.index &&
					onboard.get(passenger).desiredfloor == currentElevator.currentfloor) {
						totalservicetime += 2;
						currentElevator.capacity--;
						onboard.remove(onboard.get(passenger));
					}
				} // drop off passengers end
				if (currentElevator.capacity == 0) {
					currentElevator.status = "standby";
				}
				// Pick up people
				for (int waiting = 0; waiting < waitingqueue.size(); waiting++) {
					if (waitingqueue.get(waiting).currentfloor == currentElevator.currentfloor) {
						/* If the elevator is at a person's currentfloor and that person is the 
						elevator's first passenger, then they can change the elevator's 
						direction to their desired direction. */
						if (currentElevator.capacity == 0) {
							totalservicetime += 2;
							currentElevator.capacity++;
							onboard.add(waitingqueue.get(waiting));
							onboard.get(onboard.size()-1).elevator = currentElevator.index;
							waitingqueue.remove(waiting);
							for (int p = 0; p < onboard.size(); p++) {
								if (onboard.get(p).elevator == currentElevator.index) {
									currentElevator.direction = onboard.get(p).desireddirection;
								}
							}
						}
						/* If the elevator has other passengers onboard already, then people can 
						only get on the elevator if their desireddirection is the same as the
						elevator's currentdirection. */
						else if (waitingqueue.get(waiting).desireddirection == currentElevator.direction) {
							totalservicetime += 2;
							currentElevator.capacity++;
							onboard.add(waitingqueue.get(waiting));
							onboard.get(onboard.size()-1).elevator = currentElevator.index;
							waitingqueue.remove(waiting);
						}
					}
				} // pick up people end
				// The elevator's finishtime is the time that the elevator will finish with their task.
				currentElevator.finishtime = clock + totalservicetime;
				// futureevents.add(currentElevator.finishtime);
				
				if (currentElevator.capacity > 0) currentElevator.status = "moving";
				/*if (currentElevator.capacity == 0 && clock >= currentElevator.finishtime)  {
					currentElevator.status = "standby";
					pickordrop = -1;
				}*/

				// Output Information
				if (pickordrop == 0) System.out.println("\n[elevator " + currentElevator.index + " picking up Person " + "at " + designatedfloor + "]");
				else if (pickordrop == 1) System.out.println("[elevator " + currentElevator.index + " dropping off Person " + "at " + designatedfloor + "]");
				
				// The information shown is only true once the clock >= the elevator finish time.
				/* i.e.: if an elevator's capacity is 0 and the elevator's finish time > clock,
				we can assume that the elevator is still moving with a capacity of 1. Once
				the clock >= elevator finish time, then the elevator has finished its task of dropping
				its passenger off and its capacity will be 0. */
				for(Elevator elevator: elevators) {
					System.out.println(elevator);
				}
				System.out.println("future event: "+futureevents);
				System.out.print("\n");
			}
		} // main for loop
	} // public static void main
} //class end
