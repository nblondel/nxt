package nxt.lo52;

import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.widget.Chronometer;

public class ChronoHandler extends Handler 
{	
	private Chronometer mChrono; //connaissance
	
	public ChronoHandler(Chronometer pChrono)
	{
		mChrono = pChrono;
	}
	
	@Override
	public void handleMessage(Message msg)
	{
		// process incoming messages here
		switch (msg.what)
		{
			case (Consts.START_CHRONO):
				mChrono.setBase(SystemClock.elapsedRealtime());
				mChrono.start();
				break;
			
			case (Consts.STOP_CHRONO):
				mChrono.stop();
				break;
				
		}
		super.handleMessage(msg);
	}

}
