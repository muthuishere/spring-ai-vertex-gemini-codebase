package apps.springai.embeddings;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @SneakyThrows
    public String getAccessToken() {
        GoogleCredentials credentials = GoogleCredentials.getApplicationDefault();
        credentials.refreshIfExpired();
        AccessToken token = credentials.getAccessToken();
        return token.getTokenValue(); // Returns the token value as a String
    }
}