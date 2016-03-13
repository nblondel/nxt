package nxt.lo52;

public class BluetoothMessage 
{
	   final static public int INCOMING = 0;
	   final static public int REQUEST  = 1;

	   private int mMailbox;
	   private String mMessage;

	   public BluetoothMessage(int pMailbox, String pMessage) 
	   {
	      mMailbox = pMailbox;
	      mMessage = pMessage;
	   }

	   public byte[] toBytes() 
	   {
		   String msg = mMessage + "\0";

		   byte[] cmd = new byte[80];

		   cmd[2] = (byte)0x80;
		   cmd[3] = (byte)0x09;
		   cmd[4] = (byte)mMailbox;
		   cmd[5] = (byte)( msg.length() & 0xff );

		   for(int i=0; i<msg.length(); i++) {
			   cmd[6+i] = (byte)msg.charAt(i);
		   }

		   cmd[0] = (byte)( (cmd.length - 2) & 0xFF );
		   cmd[1] = (byte)( (cmd.length - 2) >> 8 );

		   return cmd;
	   }
}

