package requests;

public class RegisterRequest
{

    private String username;
    private String password;
    private String email;

    // Default constructor for Gson
    public RegisterRequest()
    {}

    public RegisterRequest(String username, String password, String email)
    {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // --- Getters ---
    public String username()
    { return username; }
    public String password()
    { return password; }
    public String email()
    { return email; }
}