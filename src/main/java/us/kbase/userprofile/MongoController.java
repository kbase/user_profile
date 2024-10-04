package us.kbase.userprofile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



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
	
	public MongoController(final String host, final String database) {
		final MongoDatabase db = getDB(host, database, null, null);
		profiles = db.getCollection(COL_PROFILES);
		ensureIndex();
	}
	
	public MongoController(final String host, final String database,
						   final String mongoUser, final String mongoPswd) {
		final MongoDatabase db = getDB(host, database, mongoUser, mongoPswd);
		profiles = db.getCollection(COL_PROFILES);
		ensureIndex();
	}
	
	private MongoDatabase getDB(final String host, final String db, final String user, final String pwd) {
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
				if (!d.containsKey("username")) {
					continue;
				}
				User u = new User();
				u.setUsername(d.get("username").toString());

				if (d.get("realname") != null) {
					u.setRealname(d.get("realname").toString());
				}
				if (d.get("thumbnail") != null) {
					u.setThumbnail(d.get("thumbnail").toString());
				}

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
			if(!d.containsKey("username")) {
				continue;
			}
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
				users.add(u);
			}
		}
		return users;
	}
	
	private void ensureIndex() {
		Document userUnique = new Document("user.username",1);
		IndexOptions uniqueOptions = new IndexOptions().unique(true);
		profiles.createIndex(userUnique, uniqueOptions);
	}

	public boolean exists(String username) {
		return findProfileByUsername(username) != null;
    }

	private Document findProfileByUsername(String username) {
		Document query = new Document("user.username", username);
		return profiles.find(query).first();
	}


	public UserProfile getProfile(String username) {
		Document query = new Document("user.username", username);
		Document result = profiles.find(query).first();
		if(result == null) {
			return null;
		}
		Document dbUser = result.get("user", Document.class);
		User user = new User().withUsername(dbUser.get("username").toString());
		if(dbUser.get("realname") != null) {
			user.setRealname(dbUser.get("realname").toString());
		}
		if(dbUser.get("thumbnail") != null) {
			user.setThumbnail(dbUser.get("thumbnail").toString());
		}
		
		UserProfile up = new UserProfile().withUser(user);
		Document profile = result.get("profile", Document.class);
		if(profile != null) {
			// gotta be a better way to do this
			up.setProfile(UObject.fromJsonString(profile.toJson()));
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
						replacement.put("profile", Document.parse(up.getProfile().asJsonNode().toString()));
					} else {
						throw new RuntimeException("Profile must be an object if defined.");
					}
				} else {
					replacement.put("profile", null);
				}
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
						profile.put("profile", Document.parse(up.getProfile().asJsonNode().toString()));
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

				if(profileNode.isObject()) {
					Iterator<Map.Entry<String, JsonNode>> fields = profileNode.fields();
					while(fields.hasNext()) {
						Map.Entry<String, JsonNode> e = fields.next();
						update.put("profile." + e.getKey(), e.getValue().asText());
					}
				} else {
					throw new RuntimeException("Profile must be an object if defined.");
				}
			}

			profiles.updateOne(
					new Document("user.username", up.getUser().getUsername()),
					new Document("$set", update));

		} else {
			Document user = new Document("username", up.getUser().getUsername())
								.append("realname", up.getUser().getRealname())
								.append("thumbnail", up.getUser().getThumbnail());

			Document profile = new Document("user", user);
			if(up.getProfile() != null) {
				if(up.getProfile().asJsonNode().isObject()) {
					profile.put("profile", Document.parse(up.getProfile().asJsonNode().toString()));
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
