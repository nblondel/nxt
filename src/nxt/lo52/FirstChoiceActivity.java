package nxt.lo52;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class FirstChoiceActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.firstpointchoice);
		
		Spinner spinnerX = (Spinner)findViewById(R.id.spinnerX);
	    ArrayAdapter<CharSequence> adapterX = ArrayAdapter.createFromResource(
	            this, R.array.x_array, android.R.layout.simple_spinner_item);
	    adapterX.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerX.setAdapter(adapterX);
	    spinnerX.setOnItemSelectedListener(new OnItemSelectedListener() 
	    {
	    	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    		String str = parent.getItemAtPosition(pos).toString();
	    		try {
	    			int value = Integer.parseInt(str);
	    			Log.e(Consts.TAG, "value X: "+value);
	    			Consts.positionRobot.x = value * 2; 
	    			Consts.positionDepart.x = Consts.positionRobot.x;
	    			// les points sont toutes les coordonnées 2*x car il y a les murs entre chaque
	    		} catch(NumberFormatException nfe) {
	    			System.out.println("Could not parse x: " + nfe);
	    		}
	    	}

	    	@Override public void onNothingSelected(AdapterView<?> arg0) { }
	    });
	    
	    Spinner spinnerY = (Spinner)findViewById(R.id.spinnerY);
	    ArrayAdapter<CharSequence> adapterY = ArrayAdapter.createFromResource(
	            this, R.array.y_array, android.R.layout.simple_spinner_item);
	    adapterY.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerY.setAdapter(adapterY);
	    spinnerY.setOnItemSelectedListener(new OnItemSelectedListener() 
	    {
	    	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
	    		String str = parent.getItemAtPosition(pos).toString();
	    		try {
	    			int value = Integer.parseInt(str);
	    			Log.e(Consts.TAG, "value Y: "+value);
	    			Consts.positionRobot.y = value * 2;
	    			Consts.positionDepart.y = Consts.positionRobot.y;
	    			// les points sont toutes les coordonnées 2*x car il y a les murs entre chaque
	    		} catch(NumberFormatException nfe) {
	    			System.out.println("Could not parse y: " + nfe);
	    		}
	    	}

	    	@Override public void onNothingSelected(AdapterView<?> arg0) { }
	    });

	    Spinner spinnerO = (Spinner)findViewById(R.id.spinnerOrientation);
	    ArrayAdapter<CharSequence> adapterO = ArrayAdapter.createFromResource(
	            this, R.array.orientation_array, android.R.layout.simple_spinner_item);
	    adapterO.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinnerO.setAdapter(adapterO);
	    spinnerO.setOnItemSelectedListener(new OnItemSelectedListener() 
	    {
	    	    public void onItemSelected(AdapterView<?> parent,
	    	        View view, int pos, long id) {
	    	          String str = parent.getItemAtPosition(pos).toString();
	    	          
	    	          Consts.orientationRobot = Consts.NORTH;
	    	          if(str.equals("WEST")) {
	    	        	  Consts.orientationRobot = Consts.WEST;
	    	          } else if(str.equals("EAST")) {
	    	        	  Consts.orientationRobot = Consts.EAST;
	    	          } else if(str.equals("SOUTH")) {
	    	        	  Consts.orientationRobot = Consts.SOUTH;
	    	          }
	    	          
	    	          Log.e(Consts.TAG, "value O: "+str+", int = "+Consts.orientationRobot);
	    	    }
	    	    
				@Override public void onNothingSelected(AdapterView<?> arg0) { }
	    });
	    
	    Button buttonChoiceOk = (Button)findViewById(R.id.buttonChoiceOk);
	    buttonChoiceOk.setOnTouchListener(new OnTouchListener() 
		{
			public boolean onTouch(View v, MotionEvent event) 
			{
				int action = event.getAction();
				if (action == MotionEvent.ACTION_DOWN) {
					Intent myIntentPref = new Intent(FirstChoiceActivity.this, MapActivity.class);
					FirstChoiceActivity.this.startActivity(myIntentPref);
				}
				return true;
			}
		});
	}
}
