import TSim.*;
import java.util.HashMap; 
import java.util.Map; 
import java.util.concurrent.*;


public class Lab1 {
	
	Thread first,second;
	public HashMap<Integer, Touple> sensorLocation = new HashMap<Integer, Touple>();
	public Semaphore[] semaphores = new Semaphore[9];
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 new Lab1(args);
	}
	
  public Lab1(String[] args) {
    TSimInterface tsi = TSimInterface.getInstance();
 
    

    sensorLocation.put(1, new Touple(8,5));
    sensorLocation.put(2, new Touple(6,7));
    sensorLocation.put(3, new Touple(10,7));
    sensorLocation.put(4, new Touple(9,8));
    sensorLocation.put(5, new Touple(15,7));
    sensorLocation.put(6, new Touple(15,8));
    sensorLocation.put(7, new Touple(19,7));
    sensorLocation.put(8, new Touple(17,9));
    sensorLocation.put(9, new Touple(13,9));
    sensorLocation.put(10, new Touple(13,10));
    sensorLocation.put(11, new Touple(6,9));
    sensorLocation.put(12, new Touple(6,10));
    sensorLocation.put(13, new Touple(2,9));
    sensorLocation.put(14, new Touple(1,11));
    sensorLocation.put(15, new Touple(5,11));
    sensorLocation.put(16, new Touple(3,13));
    
    for(int i = 0;i <9;i ++){
    	if(i == 0 || i == 7) semaphores[i] = new Semaphore(0);
    	else semaphores[i] = new Semaphore(1);
    }
    // A == 0
    // B == 1
    // C == 2
    // D == 3
    // E == 4
    // F == 5
    // G == 6
    // H == 7
    // I == 8
 
   
    
    
    try {
       first = new Thread(new Train(tsi,1,Integer.valueOf(args[0]).intValue(), true, 0, this));
       second = new Thread(new Train(tsi,2,Integer.valueOf(args[1]).intValue(), false, 7,  this));
      
    }
    catch (CommandException e) {
      e.printStackTrace();    // or only e.getMessage() for the error
      System.exit(1);
    }
    second.start();
    first.start();
  }
}

class Touple {
	private int x, y;
	public Touple(int x, int y){
		this.x = x; 
		this.y = y;
	}
	public int getX(){return this.x;}
	public int getY(){return this.y;}	 
}

class Train implements Runnable {
	private TSimInterface tsi;
	private boolean goesDown;
	private int currentSection;
	private int id, speed;
	private Lab1 main;
	private boolean inStation = true;
	public Train(TSimInterface tsi,int id, int speed,boolean goesDown, int currentSection, Lab1 main)throws CommandException {
		this.tsi = tsi;		
		this.speed = speed;
		this.id = id;
		this.main = main;
		this.goesDown = goesDown;
		this.currentSection = currentSection;
		 try {
			 this.tsi.setSpeed(id,speed);
			 }
		 catch (CommandException e) {
		     throw e;
		    }
		 }
	
	public void run() {
		while(true) {
		 try {
			 SensorEvent event = tsi.getSensor(this.id);
			 //if(this.id == 2) System.err.println(event.toString()); 
			 
			 //System.err.println(main.semaphores[0].toString());
			 
			 
			 int tmpY = 0, tmpX= 0, status = -1 ;
			 tmpY = event.getYpos();
			 tmpX = event.getXpos();
			 status = event.getStatus();
			 
			 int currentSensor = -1;
			 
			 for(Map.Entry<Integer, Touple> entry : main.sensorLocation.entrySet()) {
				    
				    Touple value = entry.getValue();
				    
				    if (value.getX() == tmpX && value.getY() == tmpY){
				    	currentSensor = entry.getKey();
				    }

				}
			
	
			 
			 if (this.goesDown && status == 1){
				 switch (currentSensor) {
				 	case 1: 
				 	case 2:
				 		//Special Case acquire C
				 		tsi.setSpeed(this.id,0);
				 		main.semaphores[2].acquire();
				 		System.err.println("Train " + this.id + " Acquired crossing");
				 		tsi.setSpeed(this.id,this.speed);
	                    break;
				 	case 3:
				 	case 4:
				 		//Special Case release C;
				 		main.semaphores[2].release();
				 		System.err.println("Train " + this.id + " Released crossing");
	                    break;
				 	case 5:  
				 		//acquire D and if free set Switch
				 		tsi.setSpeed(this.id,0);
				 		main.semaphores[3].acquire();
				 		tsi.setSpeed(this.id,this.speed);
				 		tsi.setSwitch(17,7,2);
				 		
				 		System.err.println("Train " + this.id + " Acquired D Switch UP");
				 		
				 		break;
				 	case 6:
				 		//acquire D and if free set Switch
				 		tsi.setSpeed(this.id,0);
				 		main.semaphores[3].acquire();
				 		tsi.setSpeed(this.id,this.speed);
				 		tsi.setSwitch(17,7,1);
				 		
				 		System.err.println("Train " + this.id + " Acquired D Switch DOWN");
	                    break;
				 	case 7:  
				 		//release currentSection, name currentSection and release
				 		main.semaphores[currentSection].release();
				 		System.err.println("Train " + this.id + " Released " + currentSection);
				 		currentSection = 3;
				 		break;
				 	case 8:
				 		//acquire E, if fails acquire F and set switch accordingly;
				 		
				 		
				 		
				 		if (main.semaphores[4].tryAcquire()){// might be problem with switches
				 			tsi.setSwitch(15,9,2);
				 			System.err.println("Train " + this.id + " Acquired E Switch UP");
				 		}else{
				 			main.semaphores[5].acquire();
				 			tsi.setSwitch(15,9,1);
				 			System.err.println("Train " + this.id + " Acquired F Switch DOWN");
				 		}				 			
				 		break;
				 	case 9:  
				 		//release currentSection, name currentSection and release
				 		main.semaphores[currentSection].release();
				 		System.err.println("Train " + this.id + " Released " + currentSection);
				 		currentSection= 4;
				 		break;
				 	case 10:
				 		//release currentSection, name currentSection and release
				 		main.semaphores[currentSection].release();
				 		System.err.println("Train " + this.id + " Released " + currentSection);
				 		currentSection= 5;
	                    break;
				 	case 11:  
				 		//acquire G and if free set Switch
				 		tsi.setSpeed(this.id,0);
				 		main.semaphores[6].acquire();
				 		tsi.setSpeed(this.id,this.speed);
				 		tsi.setSwitch(4,9,1);
				 		System.err.println("Train " + this.id + " Acquired G Switch UP");
				 		break;
				 	case 12:
				 		//acquire G and if free set Switch
				 		tsi.setSpeed(this.id,0);
				 		main.semaphores[6].acquire();
				 		tsi.setSpeed(this.id,this.speed);
				 		tsi.setSwitch(4,9,2);
				 		System.err.println("Train " + this.id + " Acquired G Switch DOWN");
	                    break;
				 	case 13:  
				 		//release currentSection, name currentSection and release
				 		main.semaphores[currentSection].release();
				 		System.err.println("Train " + this.id + " Released " + currentSection);
				 		currentSection= 6;
				 		break;
				 	case 14:
				 		//acquire H, if fails acquire I and set switch accordingly;
				 		if (main.semaphores[7].tryAcquire()){// might be problem with switches
				 			tsi.setSwitch(3,11,1);
				 			System.err.println("Train " + this.id + " Acquired H Switch UP");
				 		}else{
				 			main.semaphores[8].acquire();
				 			tsi.setSwitch(3,11,2);
				 			System.err.println("Train " + this.id + " Acquired I Switch DOWN");
				 		}		
	                    break;
				 	case 15:  
				 		//release currentSection, name currentSection and release
				 		main.semaphores[currentSection].release();
				 		System.err.println("Train " + this.id + " Released " + currentSection);
				 		currentSection= 7;
				 		break;
				 	case 16:
				 		//release currentSection, name currentSection and release
				 		main.semaphores[currentSection].release();
				 		System.err.println("Train " + this.id + " Released " + currentSection);
				 		currentSection= 8;
	                    break;
				 	default:
				 		 this.goesDown = !this.goesDown;
						 this.tsi.setSpeed(this.id, 0);
						 Thread.sleep(2*20*Math.abs(this.speed) +1500);
						 this.speed = this.speed *-1;
						 this.tsi.setSpeed(this.id,this.speed);
						 break;
				 }					
			}else if (!this.goesDown && status == 1){
				switch (currentSensor) {
					case 1: 
					case 2:
						//release crossing;
						main.semaphores[2].release();
						System.err.println("Train " + this.id + " Released Crossing");
						break;
					case 3:  
					case 4:
						// stop, acquire crossing C, cross it
						tsi.setSpeed(this.id, 0);
						main.semaphores[2].acquire();
						tsi.setSpeed(this.id,this.speed);
						System.err.println("Train " + this.id + " Acquired Crossing");
						break;
					case 5:  
						//release current section and set new current section A
						main.semaphores[currentSection].release();
						System.err.println("Train " + this.id + " Released " + currentSection);
						currentSection = 0;
						break;
					case 6:
						//release current section and set new current section B
						main.semaphores[currentSection].release();
						System.err.println("Train " + this.id + " Released " + currentSection );
						currentSection = 1;
						break;
					case 7:  
						//try acquire A if fails, acquire B, set switch
						if(main.semaphores[0].tryAcquire()) {
							tsi.setSwitch(17,7,2);
							System.err.println("Train " + this.id + " Acquired A Switch UP");
						} else {
							main.semaphores[1].acquire();
							tsi.setSwitch(17,7,1);
							System.err.println("Train " + this.id + " Acquired B Switch DOWN");
						}
						break;
					case 8:
						//release current section and set new current section D
						main.semaphores[currentSection].release();
						System.err.println("Train " + this.id + " Released " + currentSection);
						currentSection = 3;
						break;
					case 9:  
						//acquire D and set switch accordingly
						tsi.setSpeed(this.id,0);
						main.semaphores[3].acquire();
						tsi.setSpeed(this.id,this.speed);
						tsi.setSwitch(15,9,2);
						System.err.println("Train " + this.id + " Acquired D Switch UP");
						break;
					case 10:
						//acquire D and set switch accordingly
						tsi.setSpeed(this.id,0);
						main.semaphores[3].acquire();
						tsi.setSpeed(this.id,this.speed);
						tsi.setSwitch(15,9,1);
						System.err.println("Train " + this.id + " Acquired D Switch DOWN");
						break;
					case 11:  
						//release G and set new currentSection to E
						main.semaphores[currentSection].release();
						System.err.println("Train " + this.id + " Released " + currentSection);
						currentSection = 4;
						break;
					case 12:
						//release G and set new currentSection to F
						main.semaphores[currentSection].release();
						System.err.println("Train " + this.id + " Released " + currentSection);
						currentSection = 5;
						break;
					case 13:
						// try to acquire E, if it fails, acquire F and set switch accordingly
						if(main.semaphores[4].tryAcquire()) {
							tsi.setSwitch(4,9,1);
							System.err.println("Train " + this.id + " Acquired E Switch UP");
						} else {
							main.semaphores[5].acquire();
							tsi.setSwitch(4,9,2);
							System.err.println("Train " + this.id + " Acquired F Switch DOWN");
						}
						break;
					case 14:
						// release current section, set a new one to G
						main.semaphores[currentSection].release();
						System.err.println("Train " + this.id + " Released " + currentSection);
						currentSection = 6;
						break;
					case 15:  
						//acquire G set switch up
						tsi.setSpeed(this.id, 0);
						main.semaphores[6].acquire();
						tsi.setSpeed(this.id, this.speed);
						tsi.setSwitch(3,11,1);
						System.err.println("Train " + this.id + " Acquired G Switch UP");
						break;
					case 16:
						//acquire G set switch down
						tsi.setSpeed(this.id, 0);
						main.semaphores[6].acquire();
						tsi.setSpeed(this.id, this.speed);
						tsi.setSwitch(3,11,2);
						System.err.println("Train " + this.id + " Acquired G Switch DOWN");
						break;
					default:
						 this.goesDown = !this.goesDown;
						 this.tsi.setSpeed(this.id, 0);
						 Thread.sleep(2*20*Math.abs(this.speed) +1500);
						 this.speed = this.speed *-1;
						 this.tsi.setSpeed(this.id,this.speed);
						 break;
					}
				}
			 }
		 catch (Exception e) {
			 	System.err.println(e);
		    }
		}
					
	}
}
