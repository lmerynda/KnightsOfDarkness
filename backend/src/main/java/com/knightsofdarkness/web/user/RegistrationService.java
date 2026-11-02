package com.knightsofdarkness.web.user;

import java.security.SecureRandom;
import java.util.HexFormat;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.knightsofdarkness.web.common.ActionResult;
import com.knightsofdarkness.web.kingdom.KingdomService;
import com.knightsofdarkness.web.kingdom.model.KingdomRepository;
import com.knightsofdarkness.web.security.RegisterRequestDto;
import com.knightsofdarkness.web.user.model.UserEntity;

@Service
public class RegistrationService {
    private static final Pattern kingdomNamePattern = Pattern.compile("[\\p{L}0-9][\\p{L}0-9 _'-]{1,22}[\\p{L}0-9]");
    private static final Pattern emailPattern = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[^@\\s]+");
    public static final int minPasswordLength = 8;
    /** a trial account's address; .invalid is reserved, so no real address can collide with it */
    public static final String trialDomain = "@trial.invalid";
    private static final SecureRandom random = new SecureRandom();

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

    public static boolean isTrial(String email)
    {
        return email.endsWith(trialDomain);
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

        var problem = accountProblem(email, password).or(() -> kingdomNameProblem(kingdomName));
        if (problem.isPresent())
        {
            return problem;
        }

        kingdomService.createKingdom(kingdomName);
        userService.saveUser(new UserEntity(email, kingdomName, passwordEncoder.encode(password)));
        return Optional.empty();
    }

    /**
     * a kingdom founded with only its name: the account gets a placeholder address and a random password, which the browser keeps
     * so the ruler can come back, until they keep the kingdom with an address and password of their own
     */
    @Transactional
    public ActionResult<TrialAccountDto> startTrial(String requestedName)
    {
        var kingdomName = requestedName == null ? "" : requestedName.strip();
        var problem = kingdomNameProblem(kingdomName);
        if (problem.isPresent())
        {
            return ActionResult.failure(problem.get());
        }

        var email = "trial-" + secret(8) + trialDomain;
        var password = secret(16);
        kingdomService.createKingdom(kingdomName);
        userService.saveUser(new UserEntity(email, kingdomName, passwordEncoder.encode(password)));
        return ActionResult.success("Kingdom founded", new TrialAccountDto(email, password, kingdomName));
    }

    /**
     * a trial ruler keeps the kingdom: the placeholder account gives way to their own address and password
     */
    @Transactional
    public ActionResult<String> keepTrial(String currentEmail, String requestedEmail, String password)
    {
        if (!isTrial(currentEmail))
        {
            return ActionResult.failure("This kingdom already has an account of its own");
        }
        var email = requestedEmail == null ? "" : requestedEmail.strip().toLowerCase();
        var problem = accountProblem(email, password == null ? "" : password);
        if (problem.isPresent())
        {
            return ActionResult.failure(problem.get());
        }
        var trial = userService.getUserByEmail(currentEmail);
        if (trial.isEmpty())
        {
            return ActionResult.failure("This trial account is gone");
        }

        userService.deleteUser(trial.get());
        userService.saveUser(new UserEntity(email, trial.get().kingdomName, passwordEncoder.encode(password)));
        return ActionResult.success("The kingdom is yours to keep; sign in with your address from now on", email);
    }

    private Optional<String> accountProblem(String email, String password)
    {
        if (!emailPattern.matcher(email).matches() || isTrial(email))
        {
            return Optional.of("Enter a valid email address");
        }
        if (password.length() < minPasswordLength)
        {
            return Optional.of("Password must be at least " + minPasswordLength + " characters long");
        }
        if (userService.hasUserWithEmail(email))
        {
            return Optional.of("An account with this email already exists");
        }
        return Optional.empty();
    }

    private Optional<String> kingdomNameProblem(String kingdomName)
    {
        if (!kingdomNamePattern.matcher(kingdomName).matches())
        {
            return Optional.of("Kingdom name must be 3 to 24 letters, digits, spaces, dashes or apostrophes");
        }
        if (kingdomRepository.getAllKingdoms().stream().anyMatch(kingdom -> kingdom.getName().equalsIgnoreCase(kingdomName)))
        {
            return Optional.of("A kingdom with this name already exists");
        }
        return Optional.empty();
    }

    private static String secret(int bytes)
    {
        var buffer = new byte[bytes];
        random.nextBytes(buffer);
        return HexFormat.of().formatHex(buffer);
    }
}
