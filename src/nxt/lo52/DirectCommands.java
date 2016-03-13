package nxt.lo52;

import java.io.DataOutputStream;
import java.io.IOException;

import android.util.Log;

public class DirectCommands 
{
	public static void setMotor(int pMotor, int pPower)
	{
		byte[] setCmd = new byte[13];
		setCmd[0] = (byte) Consts.DIRECT_COMMAND_REPLY;
		setCmd[1] = (byte) Consts.SET_OUTPUT_STATE;
		setCmd[2] = (byte) pMotor;
		setCmd[3] = (byte) pPower;
		setCmd[4] = (byte) 0x01;
		setCmd[5] = (byte) 0x01;
		setCmd[6] = (byte) 0x00;
		setCmd[7] = (byte) 0x20;
		setCmd[8] = (byte) 0x00;
		setCmd[9] = (byte) 0x00;
		setCmd[10] = (byte) 0x00;
		setCmd[11] = (byte) 0x00;
		setCmd[12] = (byte) 0x00;
		
		Log.e(Consts.TAG, "setRobot("+pMotor+", "+pPower+")");
		try {
			if(Consts.mBluetoothSocket != null) {
				DataOutputStream out = new DataOutputStream(Consts.mBluetoothSocket.getOutputStream());
				out.write(setCmd.length & 0xff);
				out.write((setCmd.length >> 8) & 0xff);
				out.write(setCmd);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void launchProgram(String pProgramName)
	{
		byte[] setCmd = new byte[pProgramName.length()+3];
		setCmd[0] = (byte) Consts.DIRECT_COMMAND_REPLY;
		setCmd[1] = (byte) Consts.START_PROGRAM;
		
		for (int pos=0; pos<pProgramName.length(); pos++) {
			setCmd[2+pos] = (byte) pProgramName.charAt(pos);
		}
		setCmd[pProgramName.length()+2] = 0;

		try {
			if(Consts.mBluetoothSocket != null) {
				DataOutputStream out = new DataOutputStream(Consts.mBluetoothSocket.getOutputStream());
	
				out.write(setCmd.length & 0xff);
				out.write((setCmd.length >> 8) & 0xff);
				out.write(setCmd, 0, setCmd.length);
				out.flush();
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void stopProgram()
	{
		byte[] setCmd = new byte[2];
		setCmd[0] = (byte) Consts.DIRECT_COMMAND_REPLY;
		setCmd[1] = (byte) Consts.STOP_PROGRAM;

		try {
			if(Consts.mBluetoothSocket != null) {
				DataOutputStream out = new DataOutputStream(Consts.mBluetoothSocket.getOutputStream());
				out.write(setCmd.length & 0xff);
				out.write((setCmd.length >> 8) & 0xff);
				out.write(setCmd, 0, setCmd.length);
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void sendMessage(String pMsg)
	{
		BluetoothMessage msg = new BluetoothMessage(5, pMsg);

		Log.e(Consts.TAG, "sendMessage("+pMsg+")");
		try {
			if(Consts.mBluetoothSocket != null) {
				DataOutputStream out = new DataOutputStream(Consts.mBluetoothSocket.getOutputStream());
				out.write(msg.toBytes());
				out.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
