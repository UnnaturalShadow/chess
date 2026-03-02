package requests;

public class LoginRequest
{

    private String username;
    private String password;

    // Default constructor for Gson
    public LoginRequest()
    {}

    public LoginRequest(String username, String password)
    {
        this.username = username;
        this.password = password;
    }

    // --- Getters ---
    public String username()
    { return username; }
    public String password()
    { return password; }
}