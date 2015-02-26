package utility;

public class UserSingleton {
	private static UserSingleton user;
	private String username;
	private String password;
	private boolean isLoggedIn;

	public static UserSingleton getUserSingleton() {
		if(user == null) {
			user = new UserSingleton();
		}
		return user;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public boolean isLoggedIn() {
		return isLoggedIn;
	}

	public void setLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

}
