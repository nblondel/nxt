package nxt.lo52;

import android.util.Log;
import android.widget.Chronometer;

public class ChronoListener implements Chronometer.OnChronometerTickListener
{
	private int tick = 0;
	
	@Override
	public void onChronometerTick(Chronometer chronometer) {
		Log.e("NXT_LO52", "ticks: "+tick);
		
		/*if(tick == 5) {
			Message msg = Message.obtain();
			msg.what = ConstsHandlers.STOP_CHRONO;
			mHandler.sendMessage(msg);
		}*/
		
		tick++;
	}

}
