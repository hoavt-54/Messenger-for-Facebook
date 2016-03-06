package hoahong.facebook.messenger.data;

import java.util.Collection;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName="user_groups")
public class UserGroup {
	
	@DatabaseField (id = true)
	private String id;
	
	@DatabaseField
	private String name;
	
	@ForeignCollectionField
	private Collection<User> users;
	
	
	public UserGroup() {
	}
	
	
	

	public UserGroup(String id, String name) {
		super();
		this.id = id;
		this.name = name;
	}




	public String getId() {
		return id;
	}


	public void setId(String id) {
		this.id = id;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}


	public Collection<User> getUsers() {
		return users;
	}


	public void setUsers(Collection<User> users) {
		this.users = users;
	}
	
	
}
