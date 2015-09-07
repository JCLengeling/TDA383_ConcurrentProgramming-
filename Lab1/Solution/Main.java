import TSim.*;


public class Main {
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 new Main(args);
	}
	
  public Main(String[] args) {
    TSimInterface tsi = TSimInterface.getInstance();

    try {
      tsi.setSpeed(1,10);
      tsi.setSpeed(2,20);    
     }
    catch (CommandException e) {
      e.printStackTrace();    // or only e.getMessage() for the error
      System.exit(1);
    }
  }




}
