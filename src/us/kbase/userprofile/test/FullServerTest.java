package us.kbase.userprofile.test;

import static org.junit.Assert.*;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.commons.io.FileUtils;
import org.ini4j.Ini;
import org.ini4j.Profile.Section;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import us.kbase.userprofile.FilterParams;
import us.kbase.userprofile.SetUserProfileParams;
import us.kbase.userprofile.User;
import us.kbase.userprofile.UserProfile;
import us.kbase.userprofile.UserProfileClient;
import us.kbase.userprofile.UserProfileServer;

/*
 * 
 * 
 */
public class FullServerTest {
	
	private static File tempDir;
	
	private static UserProfileServer SERVER;
	private static UserProfileClient CLIENT;
	
	private static boolean removeTempDir;
	
	private static class ServerThread extends Thread {
		private UserProfileServer server;
		private ServerThread(UserProfileServer server) {
			this.server = server;
		}
		public void run() {
			try {
				server.startupServer();
			} catch (Exception e) {
				System.err.println("Can't start server:");
				e.printStackTrace();
			}
		}
	}
	
	//http://quirkygba.blogspot.com/2009/11/setting-environment-variables-in-java.html
	@SuppressWarnings("unchecked")
	private static Map<String, String> getenv() throws NoSuchFieldException,
			SecurityException, IllegalArgumentException, IllegalAccessException {
		Map<String, String> unmodifiable = System.getenv();
		Class<?> cu = unmodifiable.getClass();
		Field m = cu.getDeclaredField("m");
		m.setAccessible(true);
		return (Map<String, String>) m.get(unmodifiable);
	}
	
	
	@Test
	public void testFilterUsers() throws Exception {
		FilterParams p = new FilterParams().withFilter("");
		List<User> users = CLIENT.filterUsers(p);
		System.out.println("got users: length: "+users.size());
		
		//assertTrue("Testing that test_method_1 returns from listMethodIdsAndNames()",
		//		methods.get("test_method_1").equals("Test Method 1"));
	}
	
	@Test
	public void testGetAndSetUserProfile() throws Exception {
		//SetUserProfileParams p = new SetUserProfileParams().withProfile(
		//						new UserProfile());
		//CLIENT.setUserProfile(p);
		//System.out.println("got users: length: "+users.size());
		
		//assertTrue("Testing that test_method_1 returns from listMethodIdsAndNames()",
		//		methods.get("test_method_1").equals("Test Method 1"));
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception {

		// Parse the test config variables
		String tempDirName = System.getProperty("test.temp-dir");
		
		String mongoHost = System.getProperty("test.mongodb-host");
		String mongoDatabase = System.getProperty("test.mongodb-database");
		String admin = System.getProperty("test.admin");
		String adminPwd = System.getProperty("test.admin-pwd");
		String user1 = System.getProperty("test.usr1");
		String user1Pwd = System.getProperty("test.usr1-pwd");
		
		String s = System.getProperty("test.remove-temp-dir");
		removeTempDir = false;
		if(s!=null) {
			if(s.trim().equals("1") || s.trim().equals("yes")) {
				removeTempDir = true;
			}
		}
		
		System.out.println("test.temp-dir    = " + tempDirName);
		
		System.out.println("test.mongodb-host              = " + mongoHost);
		System.out.println("test.mongodb-database       = " + mongoDatabase);
		System.out.println("test.admin = " + admin);
		System.out.println("test.usr1            = " + user1);
		
		//create the temp directory for this test
		tempDir = new File(tempDirName);
		if (!tempDir.exists())
			tempDir.mkdirs();
		
		//create the server config file
		File iniFile = File.createTempFile("test", ".cfg", tempDir);
		if (iniFile.exists()) {
			iniFile.delete();
		}
		System.out.println("Created temporary config file: " + iniFile.getAbsolutePath());
		
		Ini ini = new Ini();
		Section ws = ini.add("UserProfile");
		
		ws.add("mongodb-host", mongoHost);
		ws.add("mongodb-database", mongoDatabase);
		ws.add("mongodb-retry", "0");
		ws.add("admin", admin);
		
		ini.store(iniFile);
		iniFile.deleteOnExit();

		Map<String, String> env = getenv();
		env.put("KB_DEPLOYMENT_CONFIG", iniFile.getAbsolutePath());
		env.put("KB_SERVICE_NAME", "NarrativeMethodStore");

		SERVER = new UserProfileServer();
		new ServerThread(SERVER).start();
		System.out.println("Main thread waiting for server to start up");
		while (SERVER.getServerPort() == null) {
			Thread.sleep(100);
		}
		System.out.println("Test server listening on "+SERVER.getServerPort() );
		CLIENT = new UserProfileClient(new URL("http://localhost:" + SERVER.getServerPort()));
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if (SERVER != null) {
			System.out.print("Killing user profile server... ");
			SERVER.stopServer();
			System.out.println("Done");
		}
		if(removeTempDir) {
			FileUtils.deleteDirectory(tempDir);
		}
	}
	
}
