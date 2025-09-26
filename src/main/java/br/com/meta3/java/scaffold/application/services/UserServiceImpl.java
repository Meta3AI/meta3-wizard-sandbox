package br.com.meta3.java.scaffold.application.services;

import br.com.meta3.java.scaffold.api.dtos.UserCreateRequest;
import br.com.meta3.java.scaffold.domain.entities.User;
import br.com.meta3.java.scaffold.domain.repositories.UserRepository;
import br.com.meta3.java.scaffold.domain.services.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Application service that implements UserService.
 *
 * Responsibilities:
 * - Validate incoming DTO
 * - Map DTO to domain User entity
 * - Parse operadoras list/CSV preserving legacy behavior:
 *   The legacy code built a CSV of operator numeric IDs by taking a substring
 *   from UI list items (Copy(item,8,5) in Delphi, positions 8..12), converting
 *   to integer and concatenating with commas (no trailing comma).
 * - Call domain repository to save the entity and return success/failure.
 */
@Service
public class UserServiceImpl implements UserService {

    private static final Logger LOG = LoggerFactory.getLogger(UserServiceImpl.class);

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = Objects.requireNonNull(userRepository);
    }

    /**
     * Create a new user based on the incoming DTO.
     *
     * This method returns true when the user was persisted successfully; false otherwise.
     *
     * Note: The exact signature is chosen to reflect the legacy behavior (boolean success indicator).
     * If the real UserService interface declares a different signature, adapt accordingly.
     */
    @Override
    @Transactional
    public boolean createUser(UserCreateRequest request) {
        if (request == null) {
            LOG.warn("UserCreateRequest is null");
            return false;
        }

        // Basic validation (mirrors legacy ValidaTela behavior but focused on backend)
        if (!isValidText(request.getLogin()) ||
            !isValidText(request.getName()) ||
            !isValidText(request.getCpf())) {
            LOG.warn("Validation failed for user create request: missing required fields");
            return false;
        }

        // Determine status: preserve legacy semantics where status is 'A' (active) or 'I' (inactive).
        // TODO: (REVIEW) Legacy UI used a radio group index to decide status: index 0 => 'A' else 'I'.
        // Here we infer from provided DTO: prefer explicit status field, else boolean 'active', else default to 'A'.
        char status = 'A';
        if (StringUtils.hasText(request.getStatus())) {
            status = Character.toUpperCase(request.getStatus().trim().charAt(0));
            if (status != 'A' && status != 'I') {
                // normalize unexpected values to 'I' if not 'A'
                status = 'I';
            }
        } else if (request.getActive() != null) {
            status = request.getActive() ? 'A' : 'I';
        } else {
            status = 'A';
        }

        // Parse operadoras preserving legacy extraction logic
        // TODO: (REVIEW) Legacy code iterated UI list items and used Copy(item,8,5) to extract a 5-char numeric id,
        // then converted to integer and appended with commas, removing the trailing comma.
        // We reproduce that by supporting two DTO shapes:
        //  - a list of strings (e.g. UI list item texts) -> extract substring at positions 8..12 (1-based)
        //  - or an input CSV/list of already numeric IDs -> normalize and join
        String operadorasCsv = parseOperadoras(request.getOperadorasList(), request.getOperadorasCsv());

        // Map DTO to entity
        User user = new User();
        user.setLogin(request.getLogin().trim());
        user.setName(request.getName().trim());
        user.setCpf(request.getCpf().trim());
        user.setStatus(String.valueOf(status));
        user.setDescription(StringUtils.hasText(request.getDescription()) ? request.getDescription().trim() : null);
        user.setOperadoras(operadorasCsv);

        try {
            userRepository.save(user);
            return true;
        } catch (Exception ex) {
            // TODO: (REVIEW) In the legacy code an error message was shown via UI with V_MSG_ERRO details.
            // Here we log the exception for diagnostics and return false to indicate failure.
            LOG.error("Error saving user", ex);
            return false;
        }
    }

    private boolean isValidText(String s) {
        return StringUtils.hasText(s);
    }

    /**
     * Parse operadoras from either a list of UI-like item strings or from an already provided CSV.
     *
     * Legacy behavior details replicated:
     * - When items look like UI list entries, take substring starting at character 8, length 5 (1-based),
     *   trim it, parse to integer (to remove any leading zeros), then use the integer's string value.
     * - Concatenate values with commas, no trailing comma. If nothing parsed, return null.
     *
     * TODO: (REVIEW) We assume UI list items are at least 12 characters long to extract positions 8..12;
     * if not, we try to extract digits from the token as a fallback.
     */
    private String parseOperadoras(List<String> operadorasList, String operadorasCsv) {
        List<String> collected = new ArrayList<>();

        if (operadorasList != null && !operadorasList.isEmpty()) {
            for (String item : operadorasList) {
                if (!StringUtils.hasText(item)) continue;
                String extracted = extractLegacySubstringId(item);
                if (extracted == null) {
                    // fallback: try to extract digits from the entire string
                    String digits = item.replaceAll("\\D+", "");
                    if (StringUtils.hasText(digits)) {
                        try {
                            int v = Integer.parseInt(digits);
                            collected.add(String.valueOf(v));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                } else {
                    collected.add(extracted);
                }
            }
        } else if (StringUtils.hasText(operadorasCsv)) {
            // normalize CSV: split, trim, keep only numeric parts; preserve numeric conversion like legacy IntToStr(StrToInt(...))
            String[] parts = operadorasCsv.split(",");
            for (String p : parts) {
                if (!StringUtils.hasText(p)) continue;
                String token = p.trim();
                // If token contains non-digits, try to find digit sequence
                String digits = token.replaceAll("\\D+", "");
                if (!StringUtils.hasText(digits)) continue;
                try {
                    int v = Integer.parseInt(digits);
                    collected.add(String.valueOf(v));
                } catch (NumberFormatException ignored) {
                }
            }
        }

        if (collected.isEmpty()) return null;
        // join with commas, no trailing comma
        return collected.stream().collect(Collectors.joining(","));
    }

    /**
     * Try to mimic Delphi Copy(item,8,5) behaviour:
     * - Delphi strings are 1-based; Copy(s, 8, 5) extracts 5 chars starting at position 8 (1-based).
     * - In Java (0-based) that's substring(7, 12) if length allows.
     *
     * Returns the numeric string without leading zeros (converted via integer parse), or null if extraction fails.
     *
     * TODO: (REVIEW) If substring cannot be extracted (too short), return null and let caller attempt fallback extraction.
     */
    private String extractLegacySubstringId(String item) {
        if (item == null) return null;
        String s = item;
        if (s.length() >= 12) { // positions 1..12 => index 0..11, need indices 7..11 inclusive
            try {
                String sub = s.substring(7, 12); // end index exclusive
                if (!StringUtils.hasText(sub)) return null;
                // remove non-digit chars, then parse to int to mimic StrToInt/IntToStr behavior
                String digits = sub.replaceAll("\\D+", "");
                if (!StringUtils.hasText(digits)) return null;
                int v = Integer.parseInt(digits);
                return String.valueOf(v);
            } catch (Exception ex) {
                // any parsing/extraction problem -> return null for fallback
                return null;
            }
        }
        return null;
    }
}