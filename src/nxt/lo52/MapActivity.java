package nxt.lo52;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.ImageButton;
import android.widget.ImageView;

public class MapActivity extends Activity
{
	//Chrono
	private Chronometer mChrono;
	private ChronoListener mChronoListener;
	private ChronoHandler mChronoHandler;
	
	//Direct Move
	private Button mButtonAvancer;
	private Button mButtonReculer;
	private Button mButtonGauche;
	private Button mButtonDroite;
	private Button mButtonEmergencyStop;

	//Map
	private Button mButtonStart;
	private Button mButtonStop;
	private Button mButtonOptimize;
	
	private int[][] mData; //murs, points
	private int[][] mTypeOfData; //pour savoir si on affiche un mur vertical ou horizontal principalement
	private int[][] mParcoursDecouvert;
	private ImageButton[][] mWalls;
	int mTypeOfWallDrawable;
	int mTypeOfNonWallDrawable;
	
	private List <Direction> mDirectionsPrises;

	//Receive and send messages
	private InputStream nxtInputStream=null;
	private Thread msgReceiver = null;
	private boolean isWorking = true;
	private boolean mIsConnected = false;

	public void createThread()
	{
		msgReceiver = new Thread() {
			@Override
			public void run()
			{
				while(isWorking) {
					if(Consts.mBluetoothSocket != null) {
						receiveMessage();
					}
				}
				Log.e(Consts.TAG, "fin du thread");
			}
		};
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);

		Log.e(Consts.TAG, "onCreate");
		Log.e(Consts.TAG, "orientationRobot: "+Consts.orientationRobot);
		Log.e(Consts.TAG, "positionRobot: "+Consts.positionRobot.x+","+Consts.positionRobot.y);
		
		setContentView(R.layout.map);
		mDirectionsPrises = new ArrayList<Direction>();

		mChrono = (Chronometer)findViewById(R.id.chronometer);
		mChronoHandler = new ChronoHandler(mChrono);
		mChronoListener = new ChronoListener();
		mChrono.setOnChronometerTickListener(mChronoListener);

		createThread();
		setChrono();

		// int[7][11]
		int[][] typeOfDataTmp = {
				{Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point},
				{Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall },
				{Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point},
				{Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall },
				{Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point},
				{Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall, Consts.nothing, Consts.horizontalWall },
				{Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point, Consts.verticalWall, Consts.point}
		};

		mTypeOfData = new int[7][11];
		for(int i=0; i<7; i++) {
			System.arraycopy(typeOfDataTmp[i], 0, mTypeOfData[i], 0, 11);
		}

		int[][] DataTmp = {
				{R.id.point0_0, R.id.mur0_1, R.id.point0_2, R.id.mur0_3, R.id.point0_4, R.id.mur0_5, R.id.point0_6, R.id.mur0_7, R.id.point0_8, R.id.mur0_9, R.id.point0_10},
				{R.id.mur1_0, 0, R.id.mur1_2, 0, R.id.mur1_4, 0, R.id.mur1_6, 0, R.id.mur1_8, 0, R.id.mur1_10},
				{R.id.point2_0, R.id.mur2_1, R.id.point2_2, R.id.mur2_3, R.id.point2_4, R.id.mur2_5, R.id.point2_6, R.id.mur2_7, R.id.point2_8, R.id.mur2_9, R.id.point2_10},
				{R.id.mur3_0, 0, R.id.mur3_2, 0, R.id.mur3_4, 0, R.id.mur3_6, 0, R.id.mur3_8, 0, R.id.mur3_10},
				{R.id.point4_0, R.id.mur4_1, R.id.point4_2, R.id.mur4_3, R.id.point4_4, R.id.mur4_5, R.id.point4_6, R.id.mur4_7, R.id.point4_8, R.id.mur4_9, R.id.point4_10},
				{R.id.mur5_0, 0, R.id.mur5_2, 0, R.id.mur5_4, 0, R.id.mur5_6, 0, R.id.mur5_8, 0, R.id.mur5_10},
				{R.id.point6_0, R.id.mur6_1, R.id.point6_2, R.id.mur6_3, R.id.point6_4, R.id.mur6_5, R.id.point6_6, R.id.mur6_7, R.id.point6_8, R.id.mur6_9, R.id.point6_10}
		};

		mData = new int[7][11];
		for(int i=0; i<7; i++) {
			System.arraycopy(DataTmp[i], 0, mData[i], 0, 11);
		}
		
		setWallsFunction();

		int[][] labyPourOptimisation = {
				{Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown},
				{Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown},
				{Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown},
				{Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown},
				{Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown},
				{Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown, Consts.wall, Consts.unknown},
				{Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown, Consts.unknown},
		};

		mParcoursDecouvert = new int[7][11];
		for(int i=0; i<7; i++) {
			System.arraycopy(labyPourOptimisation[i], 0, mParcoursDecouvert[i], 0, 11);
		}

		ImageView imageRobot = (ImageView)findViewById(mData[Consts.positionRobot.x][Consts.positionRobot.y]);
		if(imageRobot != null) {
			mParcoursDecouvert[Consts.positionRobot.x][Consts.positionRobot.y] = Consts.point;
			switch(Consts.orientationRobot) {
			case Consts.NORTH: imageRobot.setImageResource(R.drawable.pointavecrobotdirectionnorth); break;
			case Consts.WEST: imageRobot.setImageResource(R.drawable.pointavecrobotdirectionwest); break;
			case Consts.EAST: imageRobot.setImageResource(R.drawable.pointavecrobotdirectioneast); break;
			case Consts.SOUTH: imageRobot.setImageResource(R.drawable.pointavecrobotdirectionsouth); break;
			}
		}

		// --------------------------- BOUTONS DIRECT MOVE -------------------------------
		mButtonAvancer = (Button)findViewById(R.id.buttonAvancer);
		mButtonReculer = (Button)findViewById(R.id.buttonReculer);
		mButtonGauche = (Button)findViewById(R.id.buttonGauche);
		mButtonDroite = (Button)findViewById(R.id.buttonDroite);
		mButtonEmergencyStop = (Button)findViewById(R.id.buttonDirectCommandsStop);
		
		mButtonEmergencyStop.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					DirectCommands.sendMessage("S");
				}
				return true;
			}
		});

		mButtonAvancer.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {	
					/* Old method */
					//DirectCommands.setMotor(0, 100);
					//DirectCommands.setMotor(1, 100);
					DirectCommands.sendMessage("F");
				}
				else if (action == MotionEvent.ACTION_UP) {		
					/* Old method */
					//DirectCommands.setMotor(0, 0);
					//DirectCommands.setMotor(1, 0);
					DirectCommands.sendMessage("0");
				}
				return true;
			}
		});

		mButtonReculer.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {					
					/* Old method */
					//DirectCommands.setMotor(0, -100);
					//DirectCommands.setMotor(1, -100);
					DirectCommands.sendMessage("B");
				}
				else if (action == MotionEvent.ACTION_UP) {		
					/* Old method */
					//DirectCommands.setMotor(0, 0);
					//DirectCommands.setMotor(1, 0);
					DirectCommands.sendMessage("0");
				}
				return true;
			}
		});

		mButtonGauche.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {					
					/* Old method */
					//DirectCommands.setMotor(0, 100);
					DirectCommands.sendMessage("L");
				}
				else if (action == MotionEvent.ACTION_UP) {		
					/* Old method */
					//DirectCommands.setMotor(0, 0);
					DirectCommands.sendMessage("0");
				}
				return true;
			}
		});

		mButtonDroite.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					/* Old method */
					//DirectCommands.setMotor(1, 100);
					DirectCommands.sendMessage("R");
				}
				else if (action == MotionEvent.ACTION_UP) {
					/* Old method */
					//DirectCommands.setMotor(1, 0);
					DirectCommands.sendMessage("0");
				}
				return true;
			}
		});

		mButtonOptimize = (Button)findViewById(R.id.buttonOptim);
		mButtonOptimize.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					if(Consts.mBluetoothSocket != null) {
						optimize();
					}
				}
				return true;
			}
		});
		
	}

	/* TODO To be tested */
	/*@Override
	public void onSaveInstanceState(Bundle savedInstanceState) 
	{
		super.onSaveInstanceState(savedInstanceState);
		
		savedInstanceState.putBoolean("mIsConnected", mIsConnected);
	}
	
	@Override
	public void onRestoreInstanceState(Bundle savedInstanceState) 
	{
	  super.onRestoreInstanceState(savedInstanceState);

	  mIsConnected = savedInstanceState.getBoolean("mIsConnected");
	}*/

	protected void setWallsFunction()
	{
		mWalls = new ImageButton[7][11];
		for(int i=0; i<7; i++) {
			for(int j=0; j<11; j++) {
				Log.e(Consts.TAG, "mTypeOfData[i][j] = "+mTypeOfData[i][j]+" (vertical="+Consts.verticalWall+", horizontal="+Consts.horizontalWall);
				
				if(mTypeOfData[i][j] == Consts.verticalWall || mTypeOfData[i][j] == Consts.horizontalWall) {
					mWalls[i][j] = (ImageButton)findViewById(mData[i][j]);
					Log.e(Consts.TAG, "mWalls["+i+"]["+j+"] = "+mData[i][j]);

					if(mTypeOfData[i][j] == Consts.verticalWall) {
						Log.e(Consts.TAG, "verticalWall");
						mTypeOfWallDrawable = R.drawable.murvertical;
						mTypeOfNonWallDrawable = R.drawable.passagevertical;
					} else if(mTypeOfData[i][j] == Consts.horizontalWall) {
						mTypeOfWallDrawable = R.drawable.murhorizontal;
						mTypeOfNonWallDrawable = R.drawable.passagehorizontal;
						Log.e(Consts.TAG, "horizontalWall");
					}

					final ImageButton wall = mWalls[i][j];
					mWalls[i][j].setOnTouchListener(new OnTouchListener() 
					{
						private boolean active = false;
						private int typeOfWallDrawable = mTypeOfWallDrawable;
						private int typeOfNonWallDrawable = mTypeOfNonWallDrawable;
						public boolean onTouch(View v, MotionEvent event) 
						{
							int action = event.getAction();
							if (action == MotionEvent.ACTION_DOWN) {					
								Log.e(Consts.TAG, "Mur");

								if(active) {
									/* Enlever manuellement un mur */
									wall.setImageResource(typeOfNonWallDrawable);
									active = false;
								} else {
									/* Ajoute manuellement un mur */
									Log.e(Consts.TAG, "setImageResource("+typeOfWallDrawable+")");
									wall.setImageResource(typeOfWallDrawable);
									active = true;
								}
							}
							return true;
						}
					});
				}
			}
		}
	}
	
	protected void optimize()
	{		
		Log.e(Consts.TAG, "Labyrinthe decouvert:");
		for(int i=6; i>=0; i--) {
			String line = new String();
			for(int j=0; j<11; j++) {
				if(mParcoursDecouvert[i][j] == Consts.point) {
					line = line.concat(" . ");
				} else if(mParcoursDecouvert[i][j] == Consts.wall) {
					line = line.concat(" # ");
				} else if(mParcoursDecouvert[i][j] == Consts.line) {
					line = line.concat("   ");
				} else {
					line = line.concat(" x ");
				}
			}
			Log.e(Consts.TAG, ""+line);
		}

		boolean turnBackStillExisting = true;
		while(turnBackStillExisting == true)
		{
			synchronized (mDirectionsPrises) {  
				Log.e(Consts.TAG, "Directions prises:");
				Iterator<Direction> it = mDirectionsPrises.iterator();
				while(it.hasNext()) {
					Direction current = it.next();
					Log.e(Consts.TAG, "d:"+current.directionPrise+", i:"+current.intersection);
				}
				Log.e(Consts.TAG, "will find Turn Back");
		
				int indexOfLastIntersection = 0;
				int index = 0;
				boolean turnBackFound = false;
				it = mDirectionsPrises.iterator();
				while(it.hasNext() && turnBackFound == false) {
					Direction current = it.next();
					if(current.intersection == true) {
						indexOfLastIntersection = index;
					}
					
					if(current.directionPrise == Consts.TURN_BACK) {
						turnBackFound = true;
						
						int ecartEntreLastInterEtTurnBack = (2*index) - (2*indexOfLastIntersection); 
						Log.e(Consts.TAG, "index="+index+", indeOf="+indexOfLastIntersection);
						Log.e(Consts.TAG, "ecart= "+ecartEntreLastInterEtTurnBack);
						//directions à enlever avant et après le turn back + changer le sens après l'écart (Left -> Right, Right -> Left)
						
						//enlever les mouvements qui ont permis d'aller dans le cul de sac
						for(int i=indexOfLastIntersection; i<indexOfLastIntersection+ecartEntreLastInterEtTurnBack; i++) {
							Direction d = mDirectionsPrises.remove(indexOfLastIntersection); 
							// Il faut enlever toujours le même car les index sont décrémentés à chaque fois
							Log.e(Consts.TAG, "removed object: ("+d.directionPrise+","+d.intersection+")");
						}
	
						//changer la direction après le cul de sac (en sortant on va à Droite = sans y entrer on va à Gauche)
						Direction change = mDirectionsPrises.get(indexOfLastIntersection);
						if(change.directionPrise == Consts.LEFT) change.directionPrise = Consts.RIGHT;
						else if(change.directionPrise == Consts.RIGHT) change.directionPrise = Consts.LEFT;
						mDirectionsPrises.set(indexOfLastIntersection, change);
						
					}
					index++;
				}
				
				if(turnBackFound == true) {
					boolean found = false;
					it = mDirectionsPrises.iterator();
					while(it.hasNext()) {
						Direction current = it.next();
						if(current.directionPrise == Consts.TURN_BACK) found = true;
					}
					if(found == true) turnBackStillExisting = true;
					else turnBackStillExisting = false;
				} else {
					turnBackStillExisting = false;
				}
				
				it = mDirectionsPrises.iterator();
				Log.e(Consts.TAG, "Parcours final:");
				while(it.hasNext()) {
					Direction current = it.next();
					Log.e(Consts.TAG, "d:"+current.directionPrise+", i:"+current.intersection);
				}
				if(turnBackStillExisting == true) Log.e(Consts.TAG, "TurnBack toujours presente, va refaire une boucle");
			}
		}
		
		Log.e(Consts.TAG, "Plus de turnBack, optimisation terminee. Construction de la chaine.");
		
		String messageToNxt = "1";
		Iterator<Direction> it = mDirectionsPrises.iterator();
		while(it.hasNext()) {
			Direction current = it.next();
			messageToNxt = messageToNxt.concat(current.directionToStr());
		}
		
		Log.e(Consts.TAG, "message final: "+messageToNxt);
		DirectCommands.sendMessage(messageToNxt);
	}

	protected void setChrono()
	{
		// --------------------------- BOUTONS CHRONO -------------------------------
		mButtonStart = (Button)findViewById(R.id.buttonStartChrono);
		mButtonStart.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) 
			{
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					if(Consts.mBluetoothSocket != null) {
						Message msg = Message.obtain();
						msg.what = Consts.START_CHRONO;
						mChronoHandler.sendMessage(msg);
	
						mButtonStart.setEnabled(false);
						mButtonStop.setEnabled(true);
	
						try {
							if(mIsConnected == false) {
								mIsConnected = true;
								msgReceiver.start();
							}
						} catch(Exception e) {
							e.printStackTrace();
						}
						DirectCommands.launchProgram("projet.rxe");
					}

					return true;

				default: return false;
				}
			}
		});
		mButtonStart.setEnabled(true);

		mButtonStop = (Button)findViewById(R.id.buttonStopChrono);
		mButtonStop.setOnTouchListener(new OnTouchListener() {
			@Override
			public boolean onTouch(View arg0, MotionEvent event) 
			{
				switch (event.getAction() & MotionEvent.ACTION_MASK) 
				{
				case MotionEvent.ACTION_DOWN:
					if(Consts.mBluetoothSocket != null) {
						Message msg = Message.obtain();
						msg.what = Consts.STOP_CHRONO;
						mChronoHandler.sendMessage(msg);
	
						mButtonStart.setEnabled(true);
						mButtonStop.setEnabled(false);
	
						//msgReceiver.interrupt();
						DirectCommands.stopProgram();
					}

					return true;

				default: return false;
				}                               
			}
		});
		mButtonStop.setEnabled(false);
	}

	protected void receiveMessage()
	{
		try {
			if(Consts.mBluetoothSocket != null) {
				nxtInputStream = Consts.mBluetoothSocket.getInputStream();
				int length = nxtInputStream.read();
				Log.e(Consts.TAG, "length byte: "+length);
				length = (nxtInputStream.read() << 8) + length;
				Log.e(Consts.TAG, "length byte apres decalage: "+length);
				if(length > 0) {
					byte[] message = new byte[length];
					nxtInputStream.read(message);
					if(message != null && message.length >= 2 && message.length < 8) {
						String str = new String(message);
						str = str.substring(0, str.length()-1);
						Log.e(Consts.TAG, "message recu string: "+str);
	
						Direction newDirection = new Direction();
						int nombreMurs = 0;
						
						if(str.substring(0, 1).equals("M"))
						{
							Log.e(Consts.TAG, "premier bool: "+str.substring(1, 2));
							boolean left = str.substring(1, 2).equals("1");
							if(left == true) {
								wallReceived(Consts.FORWARD); Log.e(Consts.TAG, "mur FORWARD");
								nombreMurs++;
							}
							Log.e(Consts.TAG, "deuxieme bool: "+str.substring(2, 3));
							left = str.substring(2, 3).equals("1");
							if(left == true) {
								wallReceived(Consts.LEFT); Log.e(Consts.TAG, "mur LEFT");
								nombreMurs++;
							}
							Log.e(Consts.TAG, "troisieme bool: "+str.substring(3, 4));
							left = str.substring(3, 4).equals("1");
							if(left == true) {
								wallReceived(Consts.RIGHT); Log.e(Consts.TAG, "mur RIGHT");
								nombreMurs++;
							}
							
							if(nombreMurs < 2) {
								newDirection.intersection = true;
							}
							
							if(str.substring(4, 5).equals("D"))
							{
								String direction = str.substring(5, 6);
								if(direction.equals("F")) {
									directionReceived(Consts.FORWARD, true); Log.e(Consts.TAG, "direction FORWARD");
									newDirection.directionPrise = Consts.FORWARD;
								}
								else if(direction.equals("L")) {
									directionReceived(Consts.LEFT, true); Log.e(Consts.TAG, "direction LEFT");
									newDirection.directionPrise = Consts.LEFT;
								}
								else if(direction.equals("R")) {
									directionReceived(Consts.RIGHT, true); Log.e(Consts.TAG, "direction RIGHT");
									newDirection.directionPrise = Consts.RIGHT;
								}
								else if(direction.equals("T")) {
									directionReceived(Consts.TURN_BACK, true); Log.e(Consts.TAG, "direction TURN BACK");
									newDirection.directionPrise = Consts.TURN_BACK;
								}
								
								synchronized (mDirectionsPrises) {
									mDirectionsPrises.add(newDirection);
									Log.e(Consts.TAG, "add newDirection: "+newDirection.directionPrise);
									Log.e(Consts.TAG, "new List:");
									Iterator<Direction> it = mDirectionsPrises.iterator();
									while(it.hasNext()) {
										Direction current = it.next();
										Log.e(Consts.TAG, "d:"+current.directionPrise+", i:"+current.intersection);
									}
								}
							}
						} else if(str.substring(0, 1).equals("D")) {
							String direction = str.substring(1, 2);
							if(direction.equals("F")) {
								directionReceived(Consts.FORWARD, false); Log.e(Consts.TAG, "direction FORWARD");
								newDirection.directionPrise = Consts.FORWARD;
							}
							else if(direction.equals("L")) {
								directionReceived(Consts.LEFT, false); Log.e(Consts.TAG, "direction LEFT");
								newDirection.directionPrise = Consts.LEFT;
							}
							else if(direction.equals("R")) {
								directionReceived(Consts.RIGHT, false); Log.e(Consts.TAG, "direction RIGHT");
								newDirection.directionPrise = Consts.RIGHT;
							}
							else if(direction.equals("T")) {
								directionReceived(Consts.TURN_BACK, false); Log.e(Consts.TAG, "direction TURN BACK");
								newDirection.directionPrise = Consts.TURN_BACK;
							}

							mDirectionsPrises.add(newDirection);
							Log.e(Consts.TAG, "add newDirection: "+newDirection.directionPrise);
							Log.e(Consts.TAG, "new List:");
							Iterator<Direction> it = mDirectionsPrises.iterator();
							while(it.hasNext()) {
								Direction current = it.next();
								Log.e(Consts.TAG, "d:"+current.directionPrise+", i:"+current.intersection);
							}
						}
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Selon l'orientation du robot et les murs à côté de lui, on donne les coordonnées à ajouter à la position
	// du robot pour trouver le mur en question et le montrer dans le circuit
	private Point FindWallMoveAccordingTo(int pWallPosition /* F/L/R */)
	{
		Point move = new Point(0,0);

		switch(Consts.orientationRobot) {
		case Consts.NORTH:
			switch(pWallPosition) {
			case Consts.FORWARD: move.x = 1; break;
			case Consts.LEFT: move.y = -1; break;
			case Consts.RIGHT: move.y = 1; break;
			}
			break;

		case Consts.WEST:
			switch(pWallPosition) {
			case Consts.FORWARD: move.y = -1; break;
			case Consts.LEFT: move.x = -1; break;
			case Consts.RIGHT: move.x = 1; break;
			}
			break;

		case Consts.EAST:
			switch(pWallPosition) {
			case Consts.FORWARD: move.y = 1; break;
			case Consts.LEFT: move.x = 1; break;
			case Consts.RIGHT: move.x = -1; break;
			}
			break;

		case Consts.SOUTH:
			switch(pWallPosition) {
			case Consts.FORWARD: move.x = -1; break;
			case Consts.LEFT: move.y = 1; break;
			case Consts.RIGHT: move.y = -1; break;
			}
			break;
		}

		return move;
	}

	private void displayWall(final Point pPosition)
	{
		if(pPosition.x >= 0 && pPosition.y >= 0 && pPosition.x < 7 && pPosition.y < 11)
		{
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ImageButton wallToShow = (ImageButton)findViewById(mData[pPosition.x][pPosition.y]);

					Log.e(Consts.TAG, "wall: ("+pPosition.x+","+pPosition.y+")");

					if(wallToShow != null) {
						if(mTypeOfData[pPosition.x][pPosition.y] == Consts.verticalWall) {
							wallToShow.setImageResource(R.drawable.murvertical);
							mParcoursDecouvert[pPosition.x][pPosition.y] = Consts.wall;
						} else if (mTypeOfData[pPosition.x][pPosition.y] == Consts.horizontalWall) {
							wallToShow.setImageResource(R.drawable.murhorizontal);
							mParcoursDecouvert[pPosition.x][pPosition.y] = Consts.wall;
						}
					}
				}
			});
		}
	}

	protected void wallReceived(int pDirection)
	{
		Point move = FindWallMoveAccordingTo(pDirection);
		Log.e(Consts.TAG, "move: ("+move.x+","+move.y+"), Consts.positionRobot: ("+Consts.positionRobot.x+","+Consts.positionRobot.y+")");
		Point newWallPosition = new Point(Consts.positionRobot.x+move.x, Consts.positionRobot.y+move.y);
		displayWall(newWallPosition);
	}

	//Contrairement au murs, on bouge de 2 car entre chaque point il y a un mur
	private Point FindDirectionMoveAccordingTo(int pDirection /* F/L/R */)
	{
		Point move = new Point(0,0);

		switch(Consts.orientationRobot) {
		case Consts.NORTH:
			switch(pDirection) {
			case Consts.FORWARD: move.x = 2; break;
			case Consts.LEFT: move.y = -2; break;
			case Consts.RIGHT: move.y = 2; break;
			case Consts.TURN_BACK: move.x = -2; break;
			}
			break;

		case Consts.WEST:
			switch(pDirection) {
			case Consts.FORWARD: move.y = -2; break;
			case Consts.LEFT: move.x = -2; break;
			case Consts.RIGHT: move.x = 2; break;
			case Consts.TURN_BACK: move.y = 2; break;
			}
			break;

		case Consts.EAST:
			switch(pDirection) {
			case Consts.FORWARD: move.y = 2; break;
			case Consts.LEFT: move.x = 2; break;
			case Consts.RIGHT: move.x = -2; break;
			case Consts.TURN_BACK: move.y = -2; break;
			}
			break;

		case Consts.SOUTH:
			switch(pDirection) {
			case Consts.FORWARD: move.x = -2; break;
			case Consts.LEFT: move.y = 2; break;
			case Consts.RIGHT: move.y = -2; break;
			case Consts.TURN_BACK: move.x = 2; break;
			}
			break;
		}

		return move;
	}

	private void refreshOrientation(int pDirection /* F/L/R */)
	{
		int newOrientation = Consts.orientationRobot; // enlève le cas du FORWARD

		switch(newOrientation) {
		case Consts.NORTH:
			switch(pDirection) {
			case Consts.LEFT: newOrientation = Consts.WEST; break;
			case Consts.RIGHT: newOrientation = Consts.EAST; break;
			case Consts.TURN_BACK: newOrientation = Consts.SOUTH; break;
			}
			break;

		case Consts.SOUTH:
			switch(pDirection) {
			case Consts.LEFT: newOrientation = Consts.EAST; break;
			case Consts.RIGHT: newOrientation = Consts.WEST; break;
			case Consts.TURN_BACK: newOrientation = Consts.NORTH; break;
			}
			break;

		case Consts.WEST:
			switch(pDirection) {
			case Consts.LEFT: newOrientation = Consts.SOUTH; break;
			case Consts.RIGHT: newOrientation = Consts.NORTH; break;
			case Consts.TURN_BACK: newOrientation = Consts.EAST; break;
			}
			break;

		case Consts.EAST:
			switch(pDirection) {
			case Consts.LEFT: newOrientation = Consts.NORTH; break;
			case Consts.RIGHT: newOrientation = Consts.SOUTH; break;
			case Consts.TURN_BACK: newOrientation = Consts.WEST; break;
			}
			break;
		}

		if(Consts.orientationRobot != newOrientation) Consts.orientationRobot = newOrientation;
	}

	private void moveRobot(final Point pMove, final int pDirection, final boolean pOptimize)
	{
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Consts.positionRobot.offset(pMove.x, pMove.y);
				if(Consts.positionRobot.x >= 0 && Consts.positionRobot.y >= 0 && Consts.positionRobot.x < 7 && Consts.positionRobot.y < 11) {
					
					// annuler le mouvement, mettre un point vide et remettre le mouvement pour afficher le robot
					Consts.positionRobot.offset(-pMove.x, -pMove.y);
					ImageView imageRobot = (ImageView)findViewById(mData[Consts.positionRobot.x][Consts.positionRobot.y]);
					if(imageRobot != null) {
						/* 	Enlever le robot et mettre un point normal.
							Si c'est la position de départ, on mets une autre ressource (point rouge) */
						if(Consts.positionRobot.equals(Consts.positionDepart.x, Consts.positionDepart.y)) {
							imageRobot.setImageResource(R.drawable.pointlabdepart);
						} else {
							imageRobot.setImageResource(R.drawable.pointlab);
						}
					}
					Consts.positionRobot.offset(pMove.x, pMove.y);

					// deplacer le robot sur la map
					imageRobot = (ImageView)findViewById(mData[Consts.positionRobot.x][Consts.positionRobot.y]);
					if(imageRobot != null) {
						if(pOptimize == true) {
							mParcoursDecouvert[Consts.positionRobot.x][Consts.positionRobot.y] = Consts.point;
							if(pMove.x == -2) {
								mParcoursDecouvert[Consts.positionRobot.x + 1][Consts.positionRobot.y] = Consts.line;
							} else if(pMove.x == 2) {
								mParcoursDecouvert[Consts.positionRobot.x - 1][Consts.positionRobot.y] = Consts.line;
							}
	
							if(pMove.y == -2) {
								mParcoursDecouvert[Consts.positionRobot.x][Consts.positionRobot.y + 1] = Consts.line;
							} else if(pMove.y == 2) {
								mParcoursDecouvert[Consts.positionRobot.x][Consts.positionRobot.y - 1] = Consts.line;
							}
						}

						switch(Consts.orientationRobot) {
						case Consts.NORTH: imageRobot.setImageResource(R.drawable.pointavecrobotdirectionnorth); break;
						case Consts.WEST: imageRobot.setImageResource(R.drawable.pointavecrobotdirectionwest); break;
						case Consts.EAST: imageRobot.setImageResource(R.drawable.pointavecrobotdirectioneast); break;
						case Consts.SOUTH: imageRobot.setImageResource(R.drawable.pointavecrobotdirectionsouth); break;
						}
					}
				} else {
					Consts.positionRobot.offset(-pMove.x, -pMove.y);
				}
			}
		});
	}

	// move the NXT
	protected void directionReceived(int pDirection, boolean pOptimize)
	{
		Point move = FindDirectionMoveAccordingTo(pDirection);
		Log.e(Consts.TAG, "move: ("+move.x+","+move.y+")");
		refreshOrientation(pDirection); // refresh Consts.orientationRobot;
		Log.e(Consts.TAG, "nouvelle orientation: "+Consts.orientationRobot);
		moveRobot(move, pDirection, pOptimize); // refresh Consts.positionRobot and move it on the map
	}
}
