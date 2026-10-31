package com.knightsofdarkness.web.user;

import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.security.RegisterRequestDto;
import com.knightsofdarkness.web.user.model.UserEntity;

@Service
public class RegistrationService {
    private static final Pattern kingdomNamePattern = Pattern.compile("[\\p{L}0-9][\\p{L}0-9 _'-]{1,22}[\\p{L}0-9]");
    private static final Pattern emailPattern = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    public static final int minPasswordLength = 8;

    private final IUserService userService;
    private final KingdomService kingdomService;
    private final KingdomRepository kingdomRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(IUserService userService, KingdomService kingdomService, KingdomRepository kingdomRepository, PasswordEncoder passwordEncoder)
    {
        this.userService = userService;
        this.kingdomService = kingdomService;
        this.kingdomRepository = kingdomRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * @return the reason registration was refused, empty when the kingdom was founded
     */
    @Transactional
    public Optional<String> register(RegisterRequestDto request)
    {
        var email = request.email == null ? "" : request.email.strip().toLowerCase();
        var kingdomName = request.kingdomName == null ? "" : request.kingdomName.strip();
        var password = request.password == null ? "" : request.password;

        if (!emailPattern.matcher(email).matches())
        {
            return Optional.of("Enter a valid email address");
        }
        if (!kingdomNamePattern.matcher(kingdomName).matches())
        {
            return Optional.of("Kingdom name must be 3 to 24 letters, digits, spaces, dashes or apostrophes");
        }
        if (password.length() < minPasswordLength)
        {
            return Optional.of("Password must be at least " + minPasswordLength + " characters long");
        }
        if (userService.hasUserWithEmail(email))
        {
            return Optional.of("An account with this email already exists");
        }
        if (kingdomRepository.getAllKingdoms().stream().anyMatch(kingdom -> kingdom.getName().equalsIgnoreCase(kingdomName)))
        {
            return Optional.of("A kingdom with this name already exists");
        }

        kingdomService.createKingdom(kingdomName);
        userService.saveUser(new UserEntity(email, kingdomName, passwordEncoder.encode(password)));
        return Optional.empty();
    }
}
