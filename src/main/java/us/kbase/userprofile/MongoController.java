package us.kbase.userprofile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;



/*
import org.apache.commons.lang3.StringUtils;*/

import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.IndexOptions;
import us.kbase.common.service.UObject;

import com.fasterxml.jackson.databind.JsonNode;
import com.mongodb.MongoClientSettings;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import org.bson.Document;

public class MongoController {

	private static final String COL_PROFILES = "profiles";

	private final MongoCollection<Document> profiles;
	//private final MongoCollection jProfiles;
	
	public MongoController(final String host, final String database) {
		
		final MongoDatabase db = getDB(host, database, null, null);
		profiles = db.getCollection(COL_PROFILES);
		//jProfiles = jongo.getCollection(COL_PROFILES);
		ensureIndex();
		/*System.out.println(getProfile("mike3"));
		
		User u = new User().withUsername("mike3").withRealname("oh yeah");
		Map<String,String> m = new HashMap<String,String>();
		m.put("email", "test@test.com");
		//m.put("stuff", "things");
		UserProfile up = new UserProfile().withUser(u)
							.withProfile(new UObject(m));
		setProfile(up);
		System.out.println(getProfile("mike2"));
		
		System.out.println("filtering...");
		filterUsers("ik");*/
	}
	
	public MongoController(final String host, final String database,
						   final String mongoUser, final String mongoPswd) {
		final MongoDatabase db = getDB(host, database, mongoUser, mongoPswd);
		profiles = db.getCollection(COL_PROFILES);
		//jProfiles = jongo.getCollection(COL_PROFILES);
		ensureIndex();
	}
	
	private MongoDatabase getDB(final String host, final String db, final String user, final String pwd) {
		// TODO update to non-deprecated APIs
		final MongoClientSettings.Builder mongoBuilder = MongoClientSettings.builder().applyToClusterSettings(
				builder -> builder.hosts(Arrays.asList(new ServerAddress(host))));
		final MongoClient cli;
		if (user != null) {
			final MongoCredential creds = MongoCredential.createCredential(
					user, db, pwd.toCharArray());
			// unclear if and when it's safe to clear the password
			cli = MongoClients.create(mongoBuilder.credential(creds).build());
		} else {
			cli = MongoClients.create(mongoBuilder.build());
		}
		return cli.getDatabase(db);
	}


	public List<User> filterUsers(String filter) {
		List<User> users = new ArrayList<>();
		
		if(filter.trim().isEmpty()) {
			//return all
			FindIterable<Document> docs = profiles.find(new Document()).projection(new Document("user", 1));
			for (Document document : docs) {
				Document d = document.get("user", Document.class);
				if (d == null || !d.containsKey("username")) continue;
				User u = new User();
				u.setUsername(d.get("username").toString());
				if (d.containsKey("realname")) {
					if (d.get("realname") != null) {
						u.setRealname(d.get("realname").toString());
                    }
                }
				if (d.containsKey("thumbnail")) {
					if (d.get("thumbnail") != null) {
                        u.setThumbnail(d.get("thumbnail").toString());
                    }
                }
                //System.out.println(u);
                users.add(u);
            }
			return users;
		}
		
		// for now we do text search here instead of on the DB side
		// todo: see if we can install a text search index
		FindIterable<Document> docs = profiles.find(new Document()).projection(new Document("user", 1));
		String [] terms = filter.split("\\s+");
		for(Document document : docs) {
			Document d = document.get("user", Document.class);
			if(d == null || !d.containsKey("username")) continue;
			User u = new User();
			String uname = d.get("username").toString();
			u.setUsername(uname);
			boolean add = true;
			for(int i = 0; i < terms.length; i++) {
				if(!uname.toLowerCase().contains(terms[i].toLowerCase())){
					add = false; break;
				}
			}
			
			if(d.containsKey("realname")) {
				if(d.get("realname")!=null) {
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
			}
			if(d.containsKey("thumbnail")) {
				if(d.get("thumbnail")!=null) {
					u.setThumbnail(d.get("thumbnail").toString());
				}
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
		Document userUnique = new Document("user.username",1);
		//DBObject userText = new BasicDBObject("user.username","text");
		IndexOptions uniqueOptions = new IndexOptions().unique(true);
		//DBObject nameText = new BasicDBObject("user.realname","text");
		profiles.createIndex(userUnique, uniqueOptions);
		//profiles.ensureIndex(userText);
		//profiles.ensureIndex(nameText);
	}
	
	
	public boolean exists(String username) {
		Document query = new Document("user.username", username);
		return profiles.find(query).first() != null;
    }
	
	
	public UserProfile getProfile(String username) {
		Document query = new Document("user.username", username);
		Document result = profiles.find(query).first();
		if(result == null) return null;
		Document dbUser = result.get("user", Document.class);
		User user = new User().withUsername(dbUser.get("username").toString());
		if(dbUser.get("realname") != null) user.setRealname(dbUser.get("realname").toString());
		if(dbUser.get("thumbnail") != null) user.setRealname(dbUser.get("thumbnail").toString());
		
		UserProfile up = new UserProfile().withUser(user);
		if(result.get("profile") != null) {
			//System.out.println(result.get("profile").toString());
			//gotta be a better way to do this
			up.setProfile(UObject.fromJsonString(result.get("profile").toString()));
		}
		return up;
	}
	
	
	// assume UserProfile is validated
		public void setProfile(UserProfile up) {
			if(exists(up.getUser().getUsername())) {
				Document user = new Document("username", up.getUser().getUsername());

				if(up.getUser().getRealname() != null) {
					user.put("realname", up.getUser().getRealname());
				}
				if(up.getUser().getThumbnail() != null) {
					user.put("thumbnail", up.getUser().getThumbnail());
				}
				
				Document replacement = new Document("user", user);
				if(up.getProfile() != null) {
					if(up.getProfile().asJsonNode().isObject()) {
						replacement.put("profile", Document.parse(up.getProfile().asJsonNode().toString()).toJson());
					} else {
						throw new RuntimeException("Profile must be an object if defined.");
					}
				} else {
					replacement.put("profile", null);
				}
				System.out.println(replacement);
				profiles.updateOne(
						new Document("user.username", up.getUser().getUsername()),
						new Document("$set", replacement));
			} else {
				Document user = new Document("username", up.getUser().getUsername())
									.append("realname", up.getUser().getRealname())
									.append("thumbnail", up.getUser().getThumbnail());

				Document profile = new Document("user", user);
				if (up.getProfile() != null) {
					if (up.getProfile().asJsonNode().isObject()) {
						profile.put("profile", Document.parse(up.getProfile().asJsonNode().toString()).toJson());
					} else {
						throw new RuntimeException("Profile must be an object if defined.");
					}
				} else {
					profile.put("profile", null);
				}
				profiles.insertOne(profile);
			}
		}
	
	
	// assume UserProfile is validated
	public void updateProfile(UserProfile up) {
		if(exists(up.getUser().getUsername())) {
			Document user = new Document("username", up.getUser().getUsername());

			if(up.getUser().getRealname() != null) {
				user.put("realname", up.getUser().getRealname());
			}
			if(up.getUser().getThumbnail() != null) {
				user.put("thumbnail", up.getUser().getThumbnail());
			}
			
			Document update = new Document("user", user);
			if(up.getProfile() != null) {
				JsonNode profileNode = up.getProfile().asJsonNode();
				System.out.println(profileNode);
				if(profileNode.isObject()) {
					Iterator<Entry<String, JsonNode>> fields = profileNode.fields();
					while(fields.hasNext()) {
						Entry<String,JsonNode> e = fields.next();
						update.put("profile." + e.getKey(), Document.parse(e.getValue().toString()).toJson());
					}
				} else {
					throw new RuntimeException("Profile must be an object if defined.");
				}
			}
			System.out.println(update);
			profiles.updateOne(
					new Document("user.username",up.getUser().getUsername()),
					new Document("$set",update));
		} else {
			Document user = new Document("username", up.getUser().getUsername())
								.append("realname", up.getUser().getRealname())
								.append("thumbnail", up.getUser().getThumbnail());

			Document profile = new Document("user", user);
			if(up.getProfile() != null) {
				if(up.getProfile().asJsonNode().isObject()) {
					profile.put("profile", Document.parse(up.getProfile().asJsonNode().toString()).toJson());
				} else {
					throw new RuntimeException("Profile must be an object if defined.");
				}
			} else {
				profile.put("profile", null);
			}
			profiles.insertOne(profile);
		}
	}
	
	
}
