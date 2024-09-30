package us.kbase.test.userprofile;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.lang.reflect.Field;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

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
import us.kbase.testutils.controllers.mongo.MongoController;
import us.kbase.userprofile.FilterParams;
import us.kbase.userprofile.SetUserProfileParams;
import us.kbase.userprofile.User;
import us.kbase.userprofile.UserProfile;
import us.kbase.userprofile.UserProfileClient;
import us.kbase.userprofile.UserProfileServer;

public class FullServerTest {
	
	private static File tempDir;
	
	private static UserProfileServer SERVER;
	private static MongoController MONGO;
	private static UserProfileClient CLIENT;
	private static UserProfileClient USR1_CLIENT;
	private static UserProfileClient ADMIN_CLIENT;

	private static String USER1_NAME;
	
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
		System.out.println("calling filterUsers(\"\"), got users: length: "+users.size());
	}
	
	@Test
	public void testBasicPath() throws Exception {

		List<UserProfile> profile = CLIENT.getUserProfile(Arrays.asList(USER1_NAME));
		assertEquals(1, profile.size());
		assertNull(profile.get(0));

		// User1 creates a profile
		String jsonProfile1 = "{\"stuff\":\"yeah\"}";
		UserProfile p = new UserProfile().withUser(new User()
						.withUsername(USER1_NAME)
						.withRealname("User One")
						.withThumbnail("User One Thumbnail"))
				.withProfile(UObject.fromJsonString(jsonProfile1));
		USR1_CLIENT.setUserProfile(new SetUserProfileParams().withProfile(p));

		// Profile is visible to an anonymous user
		List<UserProfile> profiles = CLIENT.getUserProfile(Arrays.asList(USER1_NAME));
		assertEquals(1, profiles.size());
		UserProfile ret = profiles.get(0);
		assertEquals(USER1_NAME, ret.getUser().getUsername());
		assertEquals("User One", ret.getUser().getRealname());
		assertEquals("User One Thumbnail", ret.getUser().getThumbnail());
		assertEquals("yeah", ret.getProfile().asMap().get("stuff").asScalar());

		// Admin updates profile
		String jsonProfile2 = "{\"stuff\":\"yeah2\"}";
		UserProfile p2 = new UserProfile()
								.withUser(new User()
											.withUsername(USER1_NAME)
											.withRealname("User One"))
								.withProfile(UObject.fromJsonString(jsonProfile2));
		ADMIN_CLIENT.setUserProfile(new SetUserProfileParams().withProfile(p2));

		// Profile is updated as expected
		List<UserProfile> profiles2 = CLIENT.getUserProfile(Arrays.asList(USER1_NAME));
		assertEquals(1, profiles2.size());
		UserProfile ret2 = profiles2.get(0);
		assertEquals(USER1_NAME, ret2.getUser().getUsername());
		assertEquals("yeah2", ret2.getProfile().asMap().get("stuff").asScalar());


		// User1 adds a field to the profile
		String jsonProfileUpdate = "{\"new_stuff\":\"yeah\"}";
		UserProfile p3 = new UserProfile().withUser(new User()
						.withUsername(USER1_NAME)
						.withRealname("User One")
						.withThumbnail("User One Thumbnail updated"))
				.withProfile(UObject.fromJsonString(jsonProfileUpdate));
		USR1_CLIENT.updateUserProfile(new SetUserProfileParams().withProfile(p3));

		// Profile is updated as expected
		List<UserProfile> profiles3 = CLIENT.getUserProfile(Arrays.asList(USER1_NAME));
		assertEquals(1, profiles3.size());
		UserProfile ret3 = profiles3.get(0);
		assertEquals(USER1_NAME, ret3.getUser().getUsername());
		assertEquals("User One", ret3.getUser().getRealname());
		assertEquals("User One Thumbnail updated", ret3.getUser().getThumbnail());
		assertEquals("yeah2", ret3.getProfile().asMap().get("stuff").asScalar());
		assertEquals("yeah", ret3.getProfile().asMap().get("new_stuff").asScalar());


		// Make sure that when we filter users, we get at least this one hit.
		List<User> users = CLIENT.filterUsers( new FilterParams().withFilter(""));
		assertTrue(0<users.size());
		users = CLIENT.filterUsers( new FilterParams().withFilter(USER1_NAME));
		assertTrue(0<users.size());
	}

	@BeforeClass
	public static void setUpClass() throws Exception {
		
		// TODO TEST use the auth controller in the auth2 repo and start up a local service

		// Parse the test config variables
		final String testcfg = System.getProperty("test.cfg");
		final Ini cfgini = new Ini(new File(testcfg));
		final String secName = "UserProfile";
		final Section sec = cfgini.get(secName);
		if (sec == null) {
			throw new Exception(String.format(
					"Missing section %s in config file %s", secName, testcfg));
		}
		
		final String tempDirName = sec.get("test.temp-dir");
		String johnWickTheTempDir = sec.get("test.remove-temp-dir");
		final String mongoExePath = sec.get("test.mongo-exe-path");
		final String adminToken = sec.get("test.admin-token");
		final String user1Token = sec.get("test.usr1-token");
		final String authServiceUrl = sec.get("test.auth-service-url");
		final String authAllowInsecureString = sec.get("test.auth-service-url-allow-insecure");
		
		// start mongo
		MONGO = new MongoController(mongoExePath, Paths.get(tempDirName));
		
		System.out.println("test.temp-dir         = " + tempDirName);
		System.out.println("test.mongo-exe-path     = " + mongoExePath);
		System.out.println("test.auth-service-url = " + authServiceUrl);
		System.out.println("test.auth-service-url-allow-insecure = " + authAllowInsecureString);

		// Create tokens for the admin account and usr1 account
		ConfigurableAuthService authService = new ConfigurableAuthService(
				new AuthConfig()
					.withKBaseAuthServerURL(new URL(authServiceUrl))
					.withAllowInsecureURLs("true".equals(authAllowInsecureString))
			);

		final AuthToken user1AuthToken = authService.validateToken(user1Token);
		USER1_NAME = user1AuthToken.getUserName();

		final AuthToken adminAuthToken = authService.validateToken(adminToken);


		//create the temp directory for this test
		removeTempDir = false;
		if (johnWickTheTempDir != null) {
			removeTempDir = johnWickTheTempDir.trim().matches("1|yes|true");
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
		ws.add(UserProfileServer.CFG_MONGO_HOST, "localhost:" + MONGO.getServerPort());
		ws.add(UserProfileServer.CFG_MONGO_DB , "user_profile_test");
		ws.add(UserProfileServer.CFG_ADMIN, adminAuthToken.getUserName());
		ws.add("auth-service-url", authServiceUrl);
		ws.add("auth-service-url-allow-insecure", authAllowInsecureString);
		
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
		CLIENT.setIsInsecureHttpConnectionAllowed(true);
		USR1_CLIENT = new UserProfileClient(new URL("http://localhost:" + SERVER.getServerPort()),
				user1AuthToken);
		USR1_CLIENT.setIsInsecureHttpConnectionAllowed(true);
		ADMIN_CLIENT = new UserProfileClient(new URL("http://localhost:" + SERVER.getServerPort()),
				adminAuthToken);
		ADMIN_CLIENT.setIsInsecureHttpConnectionAllowed(true);
	}
	
	@AfterClass
	public static void tearDownClass() throws Exception {
		if (SERVER != null) {
			System.out.print("Killing user profile server... ");
			SERVER.stopServer();
			System.out.println("Done");
		}
		if (MONGO != null) {
			System.out.print("John Wick is approaching the Mongo instance... ");
			MONGO.destroy(removeTempDir);
			System.out.println("it's over");
		}
		if(removeTempDir) {
			FileUtils.deleteDirectory(tempDir);
		}
	}
	
}
