package id.project.lab.model;

import org.junit.Test;
import static org.junit.Assert.*;

public class UserTest {
    @Test
    public void testUserValidation() {
        User user = new User("", "", "", "", "");
        assertFalse(user.isValid());
    }
}
