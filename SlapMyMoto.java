
// This is freaking ugly.

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;


public class SlapMyMoto {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String bootmode = exec("getprop ro.boot.write_protect", false);
		String api = exec("getprop ro.build.version.sdk", false);
		String uevent = exec("ls -l /sys/kernel/uevent_helper", false);
		String model = exec("getprop ro.product.model", false);
		
		System.out.println("SlapMyMoto 1.0");
		System.out.println("by Justin Case");
		System.out.println("Optional Donations can be sent to:");
		System.out.println("Google Wallet: jcase@cunninglogic.com");
		System.out.println("Paypal: jcase@cunninglogic.com");
		System.out.println("Bitcoin: 1Newifz6yETTmbziCsZZstmHHPH6ejNr75\n");
		
		unpackZip("/sdcard/", "SlapMyMoto.jar");
		
		if (!model.startsWith("XT")) {
			System.out.println("Unsupported device");
			System.exit(0);
		}
		
		if (!api.trim().equals("17")) {
			System.out.println("You must be on Android 4.2.2 before using SlapMyMoto, try downgrading?");
			System.exit(0);
		}
		
		if (bootmode.trim().equals("1")) {
			System.out.println("SlapMyMoto 1.0 does not support write protected devices.");
			System.out.println("Please root using RockMyMoto, then remove write protection with MotoWpNoMo,");
			System.out.println("and restore to stock before continuing with SlapMyMoto.");
			System.exit(0);
		}
		
		if (new File("/system/bin/su").exists()||new File("/system/xbin/su").exists()) {
			System.out.println("SlapMyMoto does not support rooted devices");
			System.exit(0);
		}
		
		if (uevent.contains("system")) {
			step2();
		} else {
			step1();
		}
	}
	
	public static void step2() {
		System.out.println("After reboot, please apply the OTA then continue following the guide");
		System.out.println("Rebooting shortly...");
		String command = "#!/system/bin/sh\n" +
				"/system/bin/mount -o remount,rw /system\n" +
				"/system/bin/dd if=/sdcard/jbrecovery2.img of=/dev/block/platform/msm_sdcc.1/by-name/recovery\n" +
				"/system/bin/rm /system/bin/log\n" +
				"echo \"#!/system/bin/sh\" > /system/bin/log\n" +
				"echo \"/system/bin/chown 2000.2000 /sys/kernel/uevent_helper\" >> /system/bin/log\n" +
				"/system/bin/chmod 755 /system/bin/log\n" +
				"/system/bin/rm -r /data/system/sensors\n" +
				"/system/bin/mv /data/system/sensors-backup /data/system/sensors\n" +
				"/system/bin/rm -r /data/system/sensors-*\n" +
				"/system/bin/rm /data/command.sh\n" +
				"/system/bin/sync\n" +
				"/system/bin/sleep 2\n" +
				"/system/bin/reboot\n";
		
		try {
			BufferedWriter out = new BufferedWriter(new FileWriter("/data/command.sh"));
			out.write(command);
			out.close();

		}
		catch (IOException e) 
		{   	    }

		exec("chmod 755 /data/command.sh; echo /data/command.sh > /sys/kernel/uevent_helper", false);
	}
	
	public static void step1() {
		String dirEXT = "backup";
		
    	while (new File("/data/system/sensors-" + dirEXT).exists()) {
    		dirEXT = dirEXT + "a";
    	}
    	
    	exec("mv /data/system/sensors /data/system/sensors-" + dirEXT + "; mkdir /data/system/sensors; ln -s /sys/kernel/uevent_helper /data/system/sensors/settings", false);
    	System.out.println("Please reboot your device, then using impactor run SlapMyMoto again");
	}
	
	public static String exec(String cmd, boolean root){
		String shell = "sh";
		if (root) {
			shell = "su";
		}
		
		
	        
        final StringBuilder output = new StringBuilder();
        Process process;
        BufferedReader read = null;
        try {
      	  process = Runtime.getRuntime().exec(shell);
      	  DataOutputStream dos = new DataOutputStream(process.getOutputStream());
      	  dos.writeBytes(cmd + "\n");
      	  read = new BufferedReader(new InputStreamReader(process.getInputStream()));
      	  dos.writeBytes("exit\n");
      	  dos.flush();
      	  String line;
      	  String separator = System.getProperty("line.separator");
   
      	  while ((line = read.readLine()) != null) {
      		  output.append(line);
      		  output.append(separator);
      	  }

      	  try {
      		  process.waitFor();
      		  if (process.exitValue() != 255) {

      		  }
      		  else {

      		  }
      	  } catch (InterruptedException e) {
      		  e.printStackTrace();
      	  }
        } catch (IOException e) {
      	  e.printStackTrace();
        }
        return output.toString();
	}
	
	private static boolean unpackZip(String path, String zipname)
	{       //source: http://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android cause im lazy
	     InputStream is;
	     ZipInputStream zis;
	     try 
	     {
	         String filename;
	         is = new FileInputStream(path + zipname);
	         zis = new ZipInputStream(new BufferedInputStream(is));          
	         ZipEntry ze;
	         byte[] buffer = new byte[1024];
	         int count;

	         while ((ze = zis.getNextEntry()) != null) 
	         {
	             // zapis do souboru
	             filename = ze.getName();

	             // Need to create directories if not exists, or
	             // it will generate an Exception...
	             if (ze.isDirectory()) {
	                File fmd = new File(path + filename);
	                fmd.mkdirs();
	                continue;
	             }

	             FileOutputStream fout = new FileOutputStream(path + filename);

	             // cteni zipu a zapis
	             while ((count = zis.read(buffer)) != -1) 
	             {
	                 fout.write(buffer, 0, count);             
	             }

	             fout.close();               
	             zis.closeEntry();
	         }

	         zis.close();
	     } 
	     catch(IOException e)
	     {
	         e.printStackTrace();
	         return false;
	     }

	    return true;
	}
}
