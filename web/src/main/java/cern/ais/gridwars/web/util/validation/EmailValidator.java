package cern.ais.gridwars.web.util.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;


/**
 * Custom email validation implementation, since the default Hibernate Validator email validation is a bit too relaxed.
 *
 * See: http://www.baeldung.com/registration-with-spring-mvc-and-spring-security
 */
public class EmailValidator implements ConstraintValidator<ValidEmail, String> {

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static final Pattern pattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    public void initialize(ValidEmail constraintAnnotation) {
    }

    @Override
    public boolean isValid(String email, ConstraintValidatorContext context){
        return email == null || email.isEmpty() || pattern.matcher(email).matches();
    }
}
