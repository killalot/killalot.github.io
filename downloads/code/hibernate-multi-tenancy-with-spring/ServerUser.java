public class ServerUser{
	@Id	
	protected Long id;

	private String username;

	private String password;

	private Boolean administrator;

	@JoinColumn
	@ManyToOne
	private Database database;
}