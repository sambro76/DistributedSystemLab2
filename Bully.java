//Name: SAMNANG CHAY
//Id: 1000732321
//Login Id: sxc2321
//Summer '17
//Lab 02

//LIBRARIES
import java.awt.event.ActionEvent; //Import action event class
import java.awt.event.ActionListener; //Import action listener class for certain event to be handled
import javax.swing.JButton; //Import JButton to create button
import javax.swing.JFrame; //Import Swing Frame
import javax.swing.JLabel; //Import JLabel to display label for a short text string or an image, or both
import javax.swing.JPanel; //Import JPanel to create container
import javax.swing.JScrollPane; //To provide scroll-able view of a lightweight component
import javax.swing.JTextArea; //To provide multi-line area that displays plain text
import javax.swing.Timer; //Import swing timer
import javax.swing.JTextField; //Use to show TextField
import java.util.StringTokenizer; //Use this class to split string into short strings separated by a space
//LIBRARIES FOR SOCKET CONNECTION
import java.net.ServerSocket; //Use this class to implement server socket
import java.net.Socket; //Use this class to implement client socket
import java.io.BufferedReader; //Use this class to read text from character-input stream by buffering characters
import java.io.IOException; //Class to handle exception
import java.io.InputStreamReader; //class to read bytes and decodes them into characters
import java.io.PrintWriter; //Prints formatted representations of objects to a text-output stream

//Create class called Bully
public class Bully extends JFrame {
	//Declare static final serialVersionUID field of type long
	private static final long serialVersionUID = 1L;
	
	//Declare variables
	public JPanel JPanel;  //Panel used to contain objects
	public JButton btnStartElection = new JButton("Start Election");  
	public JButton btnCrash = new JButton("Crash");  
	public JButton btnResume = new JButton("Resume"); 
	public static JTextArea txtArea = new JTextArea(); //Text area for logging messages
	private static int defPort = 6060;  //Base port number
	static String host ="localhost"; //Assign host variable for local host name
	static procThread thread = new procThread(); //Declare a client thread
	public static int portNo = 0, procID = 0; //Declare base port number, base process number
	static ServerSocket servSocket; //Declare server socket
	public static boolean portFlag = false; //Declare port flag to check the port connection status
		
	//Core variables
	public static int maxIDs=5, //limit number of client/port to 5
			upperBoundID=0, //used to receive the flag when there is no higher priority 
			countOKs=0, //used to count number of OKs received
			crashedPort = 0, //used to store port number that is crashed 
			coordID=0; //used to store process ID of the coordinator 
	public static boolean rejoinFlag = false, crashedFlag = false;  //used as initial state and will define the state of crashed and resumed 
	// Constructor that displays the UI
	public Bully() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 50, 529, 348);
		JPanel = new JPanel();
		setContentPane(JPanel);
		JPanel.setLayout(null);
		txtArea.setEditable(false); //Set TextArea to be non-editable
			
		//Create a scroll-able panel for TextArea
		JScrollPane JScrollPanel = new JScrollPane(); 
		JScrollPanel.setViewportView(txtArea);
		JScrollPanel.setBounds(139, 48, 370, 255);
		JPanel.add(JScrollPanel);
		
		//Create a Start Election button
		btnStartElection.setBounds(10, 13, 110, 23);
		JPanel.add(btnStartElection);
		
		btnCrash.setBounds(201, 13, 70, 23);
		JPanel.add(btnCrash);
		btnResume.setEnabled(false);
		
		btnResume.setBounds(276, 13, 87, 23);
		JPanel.add(btnResume);
		
		//Create a Clear log button
		JButton btnClear = new JButton("Clear");
		btnClear.setBounds(422, 13, 70, 23);
		JPanel.add(btnClear);

		JLabel lblLog = new JLabel("Logs:");
		lblLog.setBounds(139, 31, 37, 23);
		JPanel.add(lblLog);
		
		txtTime = new JTextField();
		txtTime.setEditable(false);
		txtTime.setBounds(36, 199, 61, 20);
		JPanel.add(txtTime);
		txtTime.setColumns(10);
		
		txtCoord = new JTextField();
		txtCoord.setEditable(false);
		txtCoord.setBounds(35, 102, 31, 20);
		JPanel.add(txtCoord);
		txtCoord.setColumns(10);
		
		JLabel lblTimer = new JLabel("Time Delay (in ms):");
		lblTimer.setBounds(10, 175, 121, 23);
		JPanel.add(lblTimer);
		
		JLabel lblCurrentCoordinator = new JLabel("Current Coordinator:");
		lblCurrentCoordinator.setBounds(10, 74, 131, 23);
		JPanel.add(lblCurrentCoordinator);
		
		JLabel lblMinsMax = new JLabel("(Min: 5s; Max: 15s)");
		lblMinsMax.setBounds(10, 230, 108, 23);
		JPanel.add(lblMinsMax);
		
		//START_ELECTION button
		btnStartElection.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(coordID==0){  //condition to prevent new election to be started when known coordinator is present and live.
					startElection("ELECTION "+procID,procID+1);
					btnStartElection.setEnabled(false); //Set the Election button to be disabled	
				}
				else {
					txtArea.append("\nCoordinator is already chosen and alive.");
				}
			}
		});
		
		//CLEAR button
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				txtArea.setText("");	
			}
		});
			
		//CRASH button
		btnCrash.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				btnCrash.setEnabled(false); //Set the Crash button to be disabled
				btnResume.setEnabled(true); //Set the Resume button to be enabled
				btnStartElection.setEnabled(false); //Set Start Election button to be disabled
				try {
					thread.suspend();
					servSocket.close();  //disconnect the server socket for the crash client
					crashedPort = defPort + procID; //Assign the crashed port
					txtArea.append("\nCrashed..!!!\n");
					txtTime.setText("");
					txtCoord.setText("");
					countOKs=0;
				} catch (IOException ex) {  //Catch actions if not crashed
					txtArea.append("\nError, can't crash this process now.");
				}
			}
		});

		//Resume button to restore process from crash
		btnResume.addActionListener(new ActionListener() {
			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e) {
				btnCrash.setEnabled(true); //Set Crash button to be enabled
				btnResume.setEnabled(false); //Set Reset button to be disabled
				btnStartElection.setEnabled(true);  //Set Start_Election button to be enabled
				try {
					servSocket = new ServerSocket(crashedPort);  //open server socket back after crashed process resumed
					thread.resumeThread(crashedPort, procID, servSocket);  // update the thread variables
					thread.resume();
					thread.resumeProc(1,procID);  //call resumeProc method in thread class to resume Election processes
					txtArea.append("\n    Thread restarted...");
					crashedPort = 0;
				} catch (IOException ex) {  // Exception to shut down the process when crashed port is unavailable
					System.out.println("\nNo available port.");
					System.exit(1);
				}
			}
		});
	}
		
	//Send ELECTION message to the next process and inform about the election
	public void startElection(String token, int procID) {
		//Create client socket starting with process ID
	   	for(int i=procID ; i<=maxIDs+1 ; i++) {
			try {
				Socket clientSocket = new Socket(host, defPort + i);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream()); //Create the PrintWriter for the socket
				//Send the token into the PrintWriter for that particular socket
				out.println(token);
				out.flush();  //flush the PrintWriter output
				out.close();  //close the PrintWriter
				clientSocket.close();  //close the socket
				txtArea.append("\nElection message sent to: " + i); //Print the first Message in TextArea
			}
			catch(Exception ex) {
				upperBoundID=i;  //store the upper bound process ID for the count of OKs message 
				//once a process can't find higher ID, it will declare itself to be Coordinator 
				if(i==procID){   //This is the last process ID and becomes Coordinator
					try {int electorID=i-1;
			    		String token2="NEXT_ELECTOR "+electorID;
			            Socket sckt = new Socket(host, defPort+electorID);
			            PrintWriter out = new PrintWriter(sckt.getOutputStream());
			            out.println(token2);
			            out.flush();  
			            out.close();  
			            sckt.close();
			    	}
			        catch(Exception e) {
			        	System.out.println("An error occurs");
			        }
				}
				break;
			}
		}
	}
	    
	JTextField txtTime;
	JTextField txtCoord;
	    
	//Main method
	@SuppressWarnings("static-access")
	public static void main(String[] args) {
		Bully gui = new Bully();
		gui.setVisible(true);		//Setting frame visible
		//Open server sockets starting with port 8081. Limit number of ports to 5
		//Check the available port number before making the connection
		for(int i=1;i<=gui.maxIDs;i++) {
			ServerSocket svrSocket;
			portNo = defPort + i;
			try{
				svrSocket= new ServerSocket(portNo); //Declare new socket
				procID = portNo - defPort; //Create process ID based on port number 
				gui.setTitle("Bully Algorithm" + ", Process ID: " + i);
				svrSocket.close();
				txtArea.append("Connected to: <" + host + ":" + portNo + ">");
				portFlag = true;
				break;  //Break 'for' loop whenever a port is available
			}catch (IOException e){}
		}
			
		//Condition to limit number of ports to maxIDs value assigned above
		if(portFlag == false) {
			System.out.println("No more ports available");
			System.exit(1);
		}
		//When port is available
		else {
			try {
				servSocket = new ServerSocket(portNo);
				thread.start(); //Initialize Thread for each process and call thread run() method
				thread.init(portNo, procID, gui, servSocket); //Send variables to Thread init()method 
			} catch (IOException e) {
				System.out.println("No process found on port " + portNo);
				System.exit(1);
			}
		}
	}
}

//Class for handling of threads for each process
class procThread extends Thread {
	//Declare variables for each thread 
	public int processID;
	public int portNumber;
	public BufferedReader readBuff;
	public Bully gui;
	public ServerSocket servSoc;
	public Socket client;	
	public int defPort = 6060;  //Assign client ports to match the server port
	static String host ="localhost"; //Declare the local host name
	
	//Initializing variables for the Thread to run
	@SuppressWarnings("static-access")
	public void init(int portNumber, int processID ,Bully gui, ServerSocket servSoc){
		this.portNumber = portNumber; //assign the port number
		this.processID = processID; //assign the process number 
		this.gui = gui; //assign the frame
		this.servSoc = servSoc;	 //assign the server socket
		gui.txtArea.append("\nThread initialized...");
	}	
	
	//Run() method of the Thread
	@SuppressWarnings("static-access")
	public void run(){
		int getElectorID=0;  //assign elector ID variable
		int tmpTime=0;  //assign temporary delay time for each timer
		while(true) {
			try {
	 	 		//Generate random time between 5s to 15s for each process
				double random=Math.random()*15*1000; //set upper bound timing to 15s
				if(random<5000){tmpTime=(int) (random)+5000;} //set lower bound timer to 5s  
				else {tmpTime=(int) (random); }
				//Assign time variable for each timer to have random delay
	            final int time=tmpTime;
	            
	            //timer1 for initial live broadcasting
    			Timer timer1=new Timer(time, new ActionListener() {
    				public void actionPerformed(ActionEvent arg1) {
      				  if(gui.coordID>0){
						try {
								Socket clientSocket = new Socket(host, defPort + gui.coordID);
								clientSocket.close();
						} catch (Exception e) {informCrashed(processID);}
      				  }
    				}
    			});
	            //timer2 for live broadcasting after crash
       			Timer timer2=new Timer(time, new ActionListener() {
    				public void actionPerformed(ActionEvent arg2) {
      				  if(gui.coordID>0){
						try {
								Socket clientSocket = new Socket(host, defPort + gui.coordID);
								clientSocket.close();
						} catch (Exception e) {informCrashed(processID);}
      				  }
    				}
    			});
	            //timer3 for live broadcasting after crashed process resuming       			
       			Timer timer3=new Timer(time, new ActionListener() {
    				public void actionPerformed(ActionEvent arg3) {
      				  if(gui.coordID>0){
						try {
								Socket clientSocket = new Socket(host, defPort + gui.coordID);
								clientSocket.close();
						} catch (Exception e) {informCrashed(processID);}
      				  }
    				}
    			});
       			
    			client = servSoc.accept(); //Accept incoming socket
				readBuff = new BufferedReader(new InputStreamReader(client.getInputStream()));	//Open input reader
	            String token = readBuff.readLine(); //Read received token
	            Thread.sleep(1000); //Delay Thread running by 1s
	            
	            //Received tokens
	            StringTokenizer strToken = new StringTokenizer(token); //Break the token into strings 
	            switch(strToken.nextToken()) {	//Iterate through string's first word
	        		case "ELECTION":  //Action to be performed when it is an Election message
	        			gui.txtArea.append("\nElection received from ID: "+ strToken.nextToken()); //output the Election message
	        			Thread.sleep(1500);  
	        			int electorID = Integer.parseInt(token.substring(token.length()-1));
	        			sendOK(electorID);	//Call sendOK method to send "OK" message back to Elector
	        			break;
	        			
	        		case "OK":  //Show OK message in the Elector process window
	        			String token2=strToken.nextToken();  //Retrieve token from the higher process IDs
	        			gui.txtArea.append("\n OK from: "+token2);  //Output OK-ed process ID on Elector
	        			gui.countOKs++;   //count number of OK messages to get to the latest for condition 'if' below
	        			getElectorID=Integer.parseInt(strToken.nextToken()); //Retrieve Elector ID
	        			
	        			if(getElectorID+gui.countOKs+1==gui.upperBoundID){   //condition to go to next Elector if all OKs are counted
	        				gui.txtArea.append("\n"+"This is no longer the Elector...");
	        				gui.btnStartElection.setEnabled(true);
        					gotoNextElector(processID+1,processID);  //Then go to next elector
	        			}
	        			break;
	        			
	        		case "NEXT_ELECTOR":  //output new Elector
	        			gui.txtArea.append("\nThis is new Elector. ");  
	        			gui.countOKs=0;  //reset number of OK messages to 0.
	        			Thread.sleep(2000); 
	        			
	        			doNextElection("ELECTION "+processID,processID+1); //Call method to doNextElection()
	        			
	        			if(processID+1==gui.upperBoundID){   //if last processID+1 is equal to next unavailable client 
	        				gui.txtArea.append("\n\nThis is COORDINATOR!!! "); //Then declare itself as Coordinator
	        				Thread.sleep(2000);
	        				gui.txtTime.setText("");  //empty value on delay time field 
	        				gui.countOKs=0;  //reset number of OK messages to 0.
		        			timer1.stop();
		        			timer2.stop();
		        			timer3.stop();
        					declareCoord(1,processID);  //declare Coordinator promotion to other processes starting with process Id 1
	        			}
	        			break;
	        			
	        		case "COORDINATOR":  //Action to be performed when it is a Coordinator message
	        			int coordID=Integer.parseInt(strToken.nextToken());  //retrieve Coordinator ID
	        			gui.txtCoord.setText(""+coordID);  //set value in coordinator field
	        			gui.coordID=coordID;  //store coordID in each process
        				
	        			if(processID==coordID){  //When the message reaches back to Coordinator itself, it starts to broadcast live message
	        				Thread.sleep(1000);  
	        				gui.txtTime.setText("");  //empty value on delay time field 
		        			gui.txtArea.append("\nBroadcasting:");  //output the message
       						goLive(1, coordID);  //Live Coordinator message to other processes starting with process Id 1
	        			}
	        			else { //if the process is not the coordinator, then stop all timers
	        				timer1.stop();
	        				timer2.stop();
	        				timer3.stop();
	        				gui.txtArea.append("\nCoordinator declared at process: "+coordID); //output the coordinator declaration
        				}
	        			break;
	        			
	                case "LIVE":  //Action to be performed when it is an Live message
	        			if(processID==gui.coordID){  //When the message reaches back to Coordinator itself, it starts to broadcast live message
		        				Thread.sleep(5000);   //Sleep thread 5s before broadcasting
			        			gui.txtArea.append("\nRebroadcasting...");
			        			gui.txtTime.setText("");  //empty value on delay time field  
		        				goLive(1,processID);  //Re-broadcasting live message to other processes, then stop its timer if any running
		        				timer1.stop();
		        				timer2.stop();
		        				timer3.stop();
	        			}
	        			else {	//If not Coordinator, then perform action based on condition whether it is crashed flag or rejoin flag
	        				if(gui.crashedFlag==false&&gui.rejoinFlag==false){  //This is the initial flag state that fire only timer1
		        				timer1.setRepeats(false);  //tell timer1 to fire once.
		        				timer1.start();   //Set timer to start for non-coordinator processes
		        				timer2.stop();
		        				timer3.stop();
		        				if(gui.coordID>0){  //To avoid output alive message after process resumed
		        					gui.txtArea.append("\nCoordinator is alive");
		        					gui.txtTime.setText(""+time);
		        				}
	        				}
	        				if(gui.crashedFlag==true&&gui.rejoinFlag==false){  //This is the crashed flag state that fire only timer2
	        					timer1.stop();
		        				timer2.setRepeats(false);  //tell timer2 to fire once.
		        				timer2.start();   //Set timer to start for non-coordinator processes
		        				timer3.stop();
		        				if(gui.coordID>0){  //To avoid output alive message after process resumed
		        					gui.txtArea.append("\nCoordinator is alive");
		        					gui.txtTime.setText(""+time);
		        				}
	        				}
	        				if(gui.crashedFlag==false&&gui.rejoinFlag==true){  //This is the rejoin flag state from crash that fire only timer3
	        					timer1.stop();
	        					timer2.stop();
	        					timer3.setRepeats(false);  //tell timer3 to fire once.
	        					timer3.start();  //Set timer to start for non-coordinator processes
		        				if(gui.coordID>0){  //To avoid output alive message after process resumed
		        					gui.txtArea.append("\nCoordinator is alive");	
		        					gui.txtTime.setText(""+time);
		        				}
	        				}
	        			}
	        			break;
	                	
	                case "CRASHED":  //Action to perform when the Crashed message is called from the timer
	                	gui.coordID=0;  //set coordID variables to 0 in each process
	                	gui.crashedFlag=true;  //flag that there is process crashed
	                	gui.rejoinFlag=false;  //state rejoinFlage to false
	                	gui.txtCoord.setText("");  //empty Coordinator text field
	                	gui.txtTime.setText("");  //empty time text field
               			gui.txtArea.append("\nCoordinator is no longer responding!"); 
               			gui.startElection("ELECTION "+processID,processID+1); //call startElection method to initiate Election
               			
               			break;
	        			
	                case "RESUME":  //Action to perform when crashed process is resumed 
	                	gui.coordID=0;  //set coordID variables to 0 in each process
	                	gui.crashedFlag=false;  //flag that there is no process crashed
	                	gui.rejoinFlag=true;  //state rejoinFlage to true
	                	gui.txtCoord.setText("");  //empty Coordinator text field
	                	gui.txtTime.setText("");  //empty time text field
	                		                	
	                	int procID=Integer.parseInt(strToken.nextToken());
	                	if(processID==procID){  //if this is Resume message coming back to Coordinator, then tell it to initiate Election
	                		Thread.sleep(2000);
							gui.txtArea.append("\n    This process is back online... ");
	                		Thread.sleep(5000);
	                		gui.txtArea.append("\nInitiating new election...");
	                		gui.startElection("ELECTION "+processID,processID+1);  //call startElection method to initiate Election
	                	}
	                	else{
	                		gui.txtArea.append("\n    A crashed process resumed, waiting for new election");
	                	}
	                	break;
		        } //End of switch()
			} //End of Try
			catch (IOException e) {
				System.out.println("Exception 1: " + e);
			} 
			catch (NullPointerException e) {
				//System.out.println("Exception 2: " + e);
	        } 
			catch (InterruptedException e) {
	        	System.out.println("Exception 3: " + e);
			} 
			finally {
	            try {
	            	readBuff.close();
	            } catch (IOException ex) {
	            	System.out.println("Exception 4: " + ex);
	            }
	        }
		} //End of While
	} //End of run()

	//sendOk method to response OK message back from higher process IDs to the current Elector 
    private void sendOK(int electorID) {
    	try {
    		String token="OK "+processID+" "+electorID;
            Socket sckt = new Socket(host, defPort+electorID);
            PrintWriter out = new PrintWriter(sckt.getOutputStream());
            out.println(token);
            out.flush();  
            out.close();  
            sckt.close();
    	}
        catch(Exception ex) {
          	System.out.println("Connection error...");
        }
    }
    
    //Go to next Elector based on the algorithm
    private void gotoNextElector(int electorID, int procID) {
    	try {
    		String token="NEXT_ELECTOR "+electorID;
            Socket sckt = new Socket(host, defPort+electorID);
            PrintWriter out = new PrintWriter(sckt.getOutputStream());
            out.println(token);
            out.flush();  
            out.close();  
            sckt.close();
    	}
        catch(Exception ex) {
        }
    }
    
    //do next Election
    @SuppressWarnings("static-access")
	private void doNextElection(String token, int procID) { //Send Next Election message on next process ID
		//Create client socket starting with current process ID
    	 for(int i=procID ; i<=gui.maxIDs+1 ; i++) {
    	 	try {
				Socket clientSocket = new Socket(host, defPort + i);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream()); //Create the PrintWriter for the socket
				out.println(token);
				out.flush();  //flush the PrintWriter output
				out.close();  //close the PrintWriter
				clientSocket.close();  //close the socket
				gui.txtArea.append("\nELECTION message sent to: " + i); //Print the first Message in TextArea
				}
			catch(Exception ex) {
				gui.upperBoundID=i;
				break;
			}
		}
    	 
   	}

	@SuppressWarnings("static-access")
	private void declareCoord(int j, int coordID) { //Promote Coordinator by sending message to all other processes
		//Create client socket starting with current process ID
    	 for(int i=j ; i<=coordID ; i++) {
    	 	try {
				Socket clientSocket = new Socket(host, defPort + i);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream()); //Create the PrintWriter for the socket
				out.println("COORDINATOR "+coordID);
				out.flush();  //flush the PrintWriter output
				out.close();  //close the PrintWriter
				clientSocket.close();  //close the socket
	     	 	if(i!=coordID){gui.txtArea.append("\nCoordinator message sent to: "+i); } //output message in TextArea
			}
			catch(Exception ex) {
				declareCoord(i+1,coordID);   //goto next process if one process is down
			}
		}
   	}
   
	
    //Send Coordinator alive message to all lower process IDs
    @SuppressWarnings("static-access")
	private void goLive(int j, int coordID) {
	//Create client socket starting with current process ID  	 
    	for(int i=j ; i<=coordID ; i++) {
    		try {
 	 			Socket clientSocket = new Socket(host, defPort + i);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream()); //Create the PrintWriter for the socket
				out.println("LIVE "+coordID+" "+i);
				out.flush();  //flush the PrintWriter output
				out.close();  //close the PrintWriter
				clientSocket.close();  //close the socket
    		}
    		catch(Exception ex) {
				goLive(i+1,coordID);//goto next process if any one process is down
				break;
    		}
    		if(i!=coordID){gui.txtArea.append("\n  Live message to: "+i); } //output message in TextArea	
    	}
    }

    //Inform other processes after the coordinator crashed
	private void informCrashed(int electIDafterCrash){
	 	try {
	 		Socket clientSocket = new Socket(host, defPort + electIDafterCrash);
			PrintWriter out = new PrintWriter(clientSocket.getOutputStream()); //Create the PrintWriter for the socket
			out.println("CRASHED ");
			out.flush();  //flush the PrintWriter output
			out.close();  //close the PrintWriter
			clientSocket.close();  //close the socket
	 		}
	 	catch(Exception ex) {
	 		System.out.println("Intermittently unresponsive.");
	 	}	
	}  
    
	//Resume the thread 
	public void resumeThread(int portNo, int procID ,ServerSocket servSoc) {
		this.portNumber = portNo; //reassign the port number
		this.processID = procID; //reassign the processID
		this.servSoc = servSoc;	 //reassign the socket
	}
	
	//Resume the election process
	@SuppressWarnings("static-access")
	public void resumeProc(int j, int procID){
        for(int i=j ; i<gui.maxIDs+1 ; i++) {
 	 		try {
 	 			Socket clientSocket = new Socket(host, defPort + i);
				PrintWriter out = new PrintWriter(clientSocket.getOutputStream()); //Create the PrintWriter for the socket
				out.println("RESUME "+procID);
				out.flush();  //flush the PrintWriter output
				out.close();  //close the PrintWriter
				clientSocket.close();  //close the socket
 	 		}
 	 		catch(Exception ex) {
 	 			resumeProc(i+1,procID);  //goto next process if any one process is down
				break;
 	 		}	
        }
	}
}