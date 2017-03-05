package com.android.trongvu.atcommander;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import android.util.Log;

public class ExecuteAsRootBase
{
   public static String TAG = ExecuteAsRootBase.class.getSimpleName();
   public static boolean canRunRootCommands()
   {
      boolean retval = false;
      Process suProcess;

      try
      {
         suProcess = Runtime.getRuntime().exec("su");

         DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());
         DataInputStream osRes = new DataInputStream(suProcess.getInputStream());

         if (null != os && null != osRes)
         {
            // Getting the id of the current user to check if this is root
            os.writeBytes("id\n");
            os.flush();

            String currUid = osRes.readLine();
            Log.d(TAG, "currUid = " + currUid);
            boolean exitSu = false;
            if (null == currUid)
            {
               retval = false;
               exitSu = false;
               Log.d(TAG, "Can't get root access or denied by user");
            }
            else if (true == currUid.contains("uid=0"))
            {
               retval = true;
               exitSu = true;
               Log.d(TAG, "Root access granted");
            }
            else
            {
               retval = false;
               exitSu = true;
               Log.d(TAG, "Root access rejected: " + currUid);
            }

            if (exitSu)
            {
               os.writeBytes("exit\n");
               os.flush();
            }
         }
         suProcess.waitFor();
      }
      catch (Exception e)
      {
         // Can't get root !
         // Probably broken pipe exception on trying to write to output stream (os) after su failed, meaning that the device is not rooted

         retval = false;
         Log.d(TAG, "Root access rejected [" + e.getClass().getName() + "] : " + e.getMessage());
      }

      return retval;
   }

   public static final boolean execute()
   {
      boolean retval = false;

      try
      {
         ArrayList<String> commands = getCommandsToExecute();
         if (null != commands && commands.size() > 0)
         {
            Process suProcess = Runtime.getRuntime().exec("su");

            DataOutputStream os = new DataOutputStream(suProcess.getOutputStream());

            // Execute commands that require root access
            for (String currCommand : commands)
            {
               os.writeBytes(currCommand + "\n");
               os.flush();
            }

            os.writeBytes("exit\n");
            os.flush();

            try
            {
               int suProcessRetval = suProcess.waitFor();
               if (255 != suProcessRetval)
               {
                  // Root access granted
                  retval = true;
               }
               else
               {
                  // Root access denied
                  retval = false;
               }
            }
            catch (Exception ex)
            {
               Log.e(TAG, "Error executing root action", ex);
            }
         }
      }
      catch (IOException ex)
      {
         Log.w(TAG, "Can't get root access", ex);
      }
      catch (SecurityException ex)
      {
         Log.w(TAG, "Can't get root access", ex);
      }
      catch (Exception ex)
      {
         Log.w(TAG, "Error executing internal operation", ex);
      }

      return retval;
   }
   protected static ArrayList<String> getCommandsToExecute() {
		// TODO Auto-generated method stub
       // by default, if there is no input value for configuration_switch,
       // Apple device will use configuration #4 and Samsung device will use configuration #2
		String [] commands = new String[] {"configuration_switch"};
		return new ArrayList<String>(Arrays.asList(commands));
	}
}