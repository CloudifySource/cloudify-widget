import models.User;
import org.junit.Test;


import org.junit.*;

import play.Play;
import play.mvc.*;
import play.test.*;
import play.libs.F.*;

import static play.test.Helpers.*;
import static org.fest.assertions.Assertions.*;

/**
 * User: guym
 * Date: 12/23/12
 * Time: 7:27 AM
 *
 *
 *
 * http://blog.matthieuguillermin.fr/2012/03/unit-testing-tricks-for-play-2-0-and-ebean/
 * https://groups.google.com/forum/#!topic/play-framework/jjZuwIHNMIk
 *
 * Play-test does not have log prints
 *
 * https://groups.google.com/forum/?fromgroups=#!topic/play-framework/ydU081RerA8
 *
 */
public class TestLogin {
    @Test
    public void testLogin(){
        int a = 1 + 1;
        assertThat(a).isEqualTo(2);
    }


    //////// guy - play claims to support fakeApplication for running unit tests.
    /// I tried settings this up, but kept failing.
    /// Spring had problems
    /// DB had problems.
    /// Intellij had problems..

    /// ==> too many problems. Only simple JUnit stuff work.
    /// have to resort to pure remote selenium tests.
//    @Test
//    public void findById() {
//        Helpers.inMemoryDatabase(  )
//        running(fakeApplication(), new Runnable() {
//           public void run() {
//               User macintosh = User.find.byId(21l);
//               assertThat(macintosh.getFullName()).isEqualTo("Macintosh");
//           }
//        });
//    }
}
