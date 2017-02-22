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

import us.kbase.auth.AuthToken;
import us.kbase.auth.ConfigurableAuthService;
import us.kbase.auth.AuthConfig;

import us.kbase.common.service.UObject;

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
	private static UserProfileClient USR1_CLIENT;
	private static UserProfileClient ADMIN_CLIENT;

	private static String USER1_NAME;
	private static String ADMIN_NAME;
	
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
	}
	
	@Test
	public void testBasicPath() throws Exception {

		// User1 creates a profile
		String jsonProfile1 = "{\"stuff\":\"yeah\"}";
		UserProfile p = new UserProfile()
								.withUser(new User()
											.withUsername(USER1_NAME)
											.withRealname("User One"))
								.withProfile(UObject.fromJsonString(jsonProfile1));
		USR1_CLIENT.setUserProfile(new SetUserProfileParams().withProfile(p));

		// Profile is visible
		List<UserProfile> profiles = CLIENT.getUserProfile(Arrays.asList(USER1_NAME));
		assertEquals(1, profiles.size());
		UserProfile ret = profiles.get(0);
		assertEquals(USER1_NAME, ret.getUser().getUsername());
		assertEquals("yeah", ret.getProfile().asMap().get("stuff").asScalar());

		// Admin updates profile
		String jsonProfile2 = "{\"stuff\":\"yeah2\"}";
		UserProfile p2 = new UserProfile()
								.withUser(new User()
											.withUsername(USER1_NAME)
											.withRealname("User One"))
								.withProfile(UObject.fromJsonString(jsonProfile2));
		ADMIN_CLIENT.setUserProfile(new SetUserProfileParams().withProfile(p2));

		// Profile is updated
		List<UserProfile> profiles2 = CLIENT.getUserProfile(Arrays.asList(USER1_NAME));
		assertEquals(1, profiles2.size());
		UserProfile ret2 = profiles2.get(0);
		assertEquals(USER1_NAME, ret2.getUser().getUsername());
		assertEquals("yeah2", ret2.getProfile().asMap().get("stuff").asScalar());


		FilterParams fp = new FilterParams().withFilter("");
		List<User> users = CLIENT.filterUsers(fp);
		System.out.println("got users: length: "+users.size());
	}
	
	@BeforeClass
	public static void setUpClass() throws Exception {

		// Parse the test config variables
		String tempDirName = System.getProperty("test.temp-dir");
		
		String mongoHost = System.getProperty("test.mongodb-host");
		String mongoDatabase = System.getProperty("test.mongodb-database");
		String admin = System.getProperty("test.admin");
		String adminPwd = System.getProperty("test.admin-pwd");
		String adminToken = System.getProperty("test.admin-token");
		String user1 = System.getProperty("test.usr1");
		String user1Pwd = System.getProperty("test.usr1-pwd");
		String user1Token = System.getProperty("test.usr1-token");
		String authServiceUrl = System.getProperty("test.auth-service-url");
		
		String s = System.getProperty("test.remove-temp-dir");
		
		System.out.println("test.temp-dir         = " + tempDirName);
		System.out.println("test.mongodb-host     = " + mongoHost);
		System.out.println("test.mongodb-database = " + mongoDatabase);
		System.out.println("test.admin            = " + admin);
		System.out.println("test.usr1             = " + user1);
		System.out.println("test.usr1             = " + user1Token);
		System.out.println("test.auth-service-url = " + authServiceUrl);

		// Create tokens for the admin account and usr1 account
		ConfigurableAuthService authService = new ConfigurableAuthService(
														new AuthConfig()
															.withKBaseAuthServerURL(
																new URL(authServiceUrl)
															)
													);

		AuthToken user1AuthToken;
		if(user1Token!=null && !user1Token.isEmpty()) {
			System.out.println("Validating test usr1 with provided token");
			user1AuthToken = authService.validateToken(user1Token);
		} else {
			System.out.println("Validating test usr1 with provided user name and password");
			user1AuthToken = authService.login(user1, user1Pwd).getToken();
		}
		USER1_NAME = user1AuthToken.getUserName();

		AuthToken adminAuthToken;
		if(adminToken!=null && !adminToken.isEmpty()) {
			System.out.println("Validating test admin with provided token");
			adminAuthToken = authService.validateToken(adminToken);
		} else {
			System.out.println("Validating test admin with provided user name and password");
			adminAuthToken = authService.login(admin, adminPwd).getToken();
		}
		ADMIN_NAME = user1AuthToken.getUserName();



		//create the temp directory for this test
		removeTempDir = false;
		if(s!=null) {
			if(s.trim().equals("1") || s.trim().equals("yes")) {
				removeTempDir = true;
			}
		}
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
		ws.add(UserProfileServer.CFG_PROP_AUTH_SERVICE_URL, authServiceUrl);
		
		ini.store(iniFile);
		iniFile.deleteOnExit();

		Map<String, String> env = getenv();
		env.put("KB_DEPLOYMENT_CONFIG", iniFile.getAbsolutePath());
		env.put("KB_SERVICE_NAME", "UserProfile");

		SERVER = new UserProfileServer();
		new ServerThread(SERVER).start();
		System.out.println("Main thread waiting for server to start up");
		while (SERVER.getServerPort() == null) {
			Thread.sleep(200);
		}
		System.out.println("Test server listening on "+SERVER.getServerPort() );


		CLIENT = new UserProfileClient(new URL("http://localhost:" + SERVER.getServerPort()));
		CLIENT.setAuthAllowedForHttp(true);
		USR1_CLIENT = new UserProfileClient(new URL("http://localhost:" + SERVER.getServerPort()), user1AuthToken);
		USR1_CLIENT.setAuthAllowedForHttp(true);
		ADMIN_CLIENT = new UserProfileClient(new URL("http://localhost:" + SERVER.getServerPort()), adminAuthToken);
		ADMIN_CLIENT.setAuthAllowedForHttp(true);
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
