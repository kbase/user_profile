package us.kbase.userprofile;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;





import org.bson.BasicBSONObject;
/*
import org.apache.commons.lang3.StringUtils;*/
import org.bson.types.ObjectId;
import org.jongo.FindAndModify;
import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.jongo.marshall.MarshallingException;

import us.kbase.common.mongo.GetMongoDB;
import us.kbase.common.mongo.exceptions.InvalidHostException;
import us.kbase.common.mongo.exceptions.MongoAuthException;
import us.kbase.common.service.UObject;
import us.kbase.common.utils.CountingOutputStream;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.mongodb.util.JSON;

public class MongoController {

	private static final String COL_PROFILES = "profiles";

	private final DB db;
	private final DBCollection profiles;
	private final Jongo jongo;
	private final MongoCollection jProfiles;
	
	public MongoController(final String host, final String database,final int mongoRetryCount)
			throws UnknownHostException, IOException, InvalidHostException,InterruptedException {
		
		db = GetMongoDB.getDB(host, database, mongoRetryCount, 10);
		profiles = db.getCollection(COL_PROFILES);
		jongo = new Jongo(db);
		jProfiles = jongo.getCollection(COL_PROFILES);
		ensureIndex();
		/*System.out.println(getProfile("test3"));
		
		User u = new User().withUsername("mike3").withRealname("oh yeah");
		Map<String,String> m = new HashMap<String,String>();
		//m.put("email", "test@test.com");
		m.put("stuff", "things");
		UserProfile up = new UserProfile().withUser(u)
							.withProfile(new UObject(m));
		setProfile(up);
		System.out.println(getProfile("mike2"));
		
		System.out.println("filtering...");
		filterUsers("ik");*/
	}
	
	public List<User> filterUsers(String filter) {
		
		if(filter.trim().isEmpty()) {
			//return all
			DBCursor cursor = profiles.find(new BasicDBObject(),
							new BasicDBObject().append("user",1));
			List<User> users = new ArrayList<User>(cursor.count());
			while(cursor.hasNext()) {
				User u = new User();
				DBObject d = (DBObject)cursor.next().get("user");
				if(!d.containsField("username")) continue;
				u.setUsername(d.get("username").toString());
				if(d.containsField("realname")) {
					u.setRealname(d.get("realname").toString());
				}
				if(d.containsField("thumbnail")) {
					u.setThumbnail(d.get("thumbnail").toString());
				}
				//System.out.println(u);
				users.add(u);
			}
			return users;
		}
		
		// for now we do text search here instead of on the DB side
		// todo: see if we can install a text search index
		DBCursor cursor = profiles.find(new BasicDBObject(),
				new BasicDBObject().append("user",1));
		List<User> users = new ArrayList<User>(cursor.count());
		String [] terms = filter.split("\\s+");
		while(cursor.hasNext()) {
			User u = new User();
			DBObject d = (DBObject)cursor.next().get("user");
			if(!d.containsField("username")) continue;
			String uname = d.get("username").toString();
			u.setUsername(uname);
			boolean add = true;
			for(int i=0; i<terms.length; i++) {
				if(!uname.toLowerCase().contains(terms[i].toLowerCase())){
					add = false; break;
				}
			}
			
			if(d.containsField("realname")) {
				String rname = d.get("realname").toString();
				u.setRealname(rname);
				if(!add) {
					add = true;
					for(int i=0; i<terms.length; i++) {
						if(!rname.toLowerCase().contains(terms[i].toLowerCase())
							&& !uname.toLowerCase().contains(terms[i].toLowerCase())){
							add = false; break;
						}
					}
				}
			}
			if(d.containsField("thumbnail")) {
				u.setThumbnail(d.get("thumbnail").toString());
			}
			
			// only add if it was found
			if(add) {
				//System.out.println(u);
				users.add(u);
			}
		}
		return users;
	}
	
	private void ensureIndex() {
		DBObject userUnique = new BasicDBObject("user.username",1);
		//DBObject userText = new BasicDBObject("user.username","text");
		DBObject unique = new BasicDBObject("unique",1);
		//DBObject nameText = new BasicDBObject("user.realname","text");
		profiles.resetIndexCache();
		profiles.ensureIndex(userUnique, unique);
		//profiles.ensureIndex(userText);
		//profiles.ensureIndex(nameText);
	}
	
	
	public boolean exists(String username) {
		DBObject query = new BasicDBObject("user.username",username);
		if(profiles.findOne(query, new BasicDBObject())==null) {
			return false;
		}
		return true;
	}
	
	
	public UserProfile getProfile(String username) {
		DBObject query = new BasicDBObject("user.username",username);
		DBObject result = profiles.findOne(query);
		if(result==null) return null;
		DBObject dbUser = ((DBObject)result.get("user"));
		User user = new User().withUsername(dbUser.get("username").toString());
		if(dbUser.get("realname")!=null) user.setRealname(dbUser.get("realname").toString());
		if(dbUser.get("thumbnail")!=null) user.setRealname(dbUser.get("thumbnail").toString());
		
		UserProfile up = new UserProfile().withUser(user);
		if(result.get("profile")!=null) {
			up.setProfile(new UObject(result.get("profile")));
		}
		return up;
	}
	
	// assume UserProfile is validated
	public void setProfile(UserProfile up) {
		if(exists(up.getUser().getUsername())) {
			DBObject user = new BasicDBObject("username",up.getUser().getUsername());
			if(up.getUser().getRealname()!=null)
				user.put("realname", up.getUser().getRealname());
			if(up.getUser().getThumbnail()!=null)
				user.put("thumbnail", up.getUser().getThumbnail());
			
			DBObject update = new BasicDBObject("user",user);
			if(up.getProfile()!=null) {
				JsonNode profileNode = up.getProfile().asJsonNode();
				System.out.println(profileNode);
				if( profileNode.isObject() ) {
					System.out.println("here");
					Iterator<Entry<String, JsonNode>> fields = profileNode.fields();
					while(fields.hasNext()) {
						Entry<String,JsonNode> e= fields.next();
						update.put("profile."+e.getKey(), JSON.parse(e.getValue().toString()));
					}
				} else {
					throw new RuntimeException("Profile must be an object if defined.");
				}
			}
			System.out.println(update);
			profiles.update(
					new BasicDBObject("user.username",up.getUser().getUsername()),
					new BasicDBObject("$set",update));
		} else {
			DBObject user = new BasicDBObject("username",up.getUser().getUsername())
								.append("realname", up.getUser().getRealname())
								.append("thumbnail", up.getUser().getThumbnail());
			
			DBObject profile = new BasicDBObject("user",user);
			if(up.getProfile()!=null) {
				if(up.getProfile().asJsonNode().isObject())
					profile.put("profile", JSON.parse(up.getProfile().asJsonNode().toString()));
				else {
					throw new RuntimeException("Profile must be an object if defined.");
				}
			} else {
				profile.put("profile", null);
			}
			profiles.insert(profile);
		}
	}
	
	
}
