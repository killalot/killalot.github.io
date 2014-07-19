@Table
@Entity
public class Database{

	@Id	
	protected Long id;

	private String name;

	/* Optional fields */
	private String version;

	private String username;

	private String password;

	private String server; // if databases are held in multiple servers

	// getters and setters omitted
}