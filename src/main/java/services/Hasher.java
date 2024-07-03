package services;


import com.google.common.hash.Hashing;
import org.jetbrains.annotations.NotNull;

import java.nio.charset.StandardCharsets;

public class Hasher {
    public static @NotNull String hash(String username) {
        return Hashing.sha256().hashString(username, StandardCharsets.UTF_8).toString();
    }
}
