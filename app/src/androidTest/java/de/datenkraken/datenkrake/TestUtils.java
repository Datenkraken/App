package de.datenkraken.datenkrake;

import java.util.Random;

/**
 * Class containing useful functionality for testing purpose.
 * @author Daniel Thoma - daniel.thoma@stud.tu-darmstadt.de
 */
class TestUtils {


    /**
     * Generates a random string for testing purpose.
     * @param n max length, n constraints: 1 <= n <= 100
     * @return random String
     */
    static String generateRandomString(int n) {
        if (n < 1) {
            n = 1;
        } else if (n > 100) {
            n = 100;
        }
        Random r = new Random();
        n = r.nextInt(n - 1) + 1; // at least 1

        StringBuilder s = new StringBuilder();
        for(;0 < n; n--) {
            s.append((char) (r.nextInt(90) + 33)); // convert a number from 33 - 126 to it's ascii symbol and appends it
        }
        return s.toString();
    }

    /**
     * Generates a random link for testing purpose.
     * The link will look like this: "https://www.&lt;lowercase characters&gt;.com/&lt;lowercase characters&gt;.
     * @param n max length for the url, n constraints: 19 <= n <= 100
     * @return random link
     */
    static String generateRandomLink(int n) {
        String pre = "https://www.";
        String topDomain = ".com";
        int minLength = (pre.length() + topDomain.length() + 3);
        if(n < minLength) {
            n = minLength;
        } else if(n > 100) {
            n = 100;
        }

        n -= (pre.length() + topDomain.length() + 1); // +1 cause we will add a /

        Random r = new Random();
        int domainLength = r.nextInt(n - 2) + 1; // at least 1, save 1 for path
        int pathLength = r.nextInt(n - domainLength) + 1;

        StringBuilder s = new StringBuilder();
        s.append(pre);
        for(;0 < domainLength; domainLength--) {
            s.append((char) (r.nextInt(25) + 97)); // adds only lowercase character
        }
        s.append(topDomain).append("/");
        for(;0 < pathLength; pathLength--) {
            s.append((char) (r.nextInt(25) + 97)); // adds only lowercase character
        }
        return s.toString();
    }
}
