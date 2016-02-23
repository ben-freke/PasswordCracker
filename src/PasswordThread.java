import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Random;

/**
 * Created by benfreke on 20/02/2016.
 */

/**
 * Password Thread is designed for multiple functions to run as seperate threads, an interface with one static
 * class.
 *<p>
 *     The aim of PasswordThread is to hold and run the many different operations, and interface with one static class.
 *     Functions that find a password will remove it from the uncracked users HashMap and add it to the cracked users
 *     HashMap, thus reducing the amount of processing required by other processes.
 *
 *     Methods will use the same dictionaries for consistency.
 *</p>
 */
public class PasswordThread {

    /**
     * Searches a password list of common passwords.
     * This function reads in a password file (the location of which is set in main) in a thread. The file reading
     * is done in the thread to speed up the processing of large files.
     * This is useful as many people use similar passwords such as
     * 'qwertyiop' or 'zxcvbnm' which are not words. A list of common passwords will assist in finding passwords
     * from a password dump file. The more passwords that can be removed from the off mean the less passwords that
     * more computationally demanding applications have to deal with, such as concatenation.
     */
    public void passwordList()
    {
        Main.pwds = Main.getDict(Main.PASSWORDS_LOCATION);
        for(int i = 0; i < Main.users.size(); i++) {
            if (Main.pwds.containsKey(Main.users.get(String.valueOf(i)))){
                Main.foundUser(i, Main.pwds.get(Main.users.get(String.valueOf(i))));
            }
        }
    }

    /**
     * Loops through each word in the dictionary and concatenates each symbol in the ASCII table to the beginning and end
     * in separate attempts.
     */
    public void symbolConcat()
    {
        for (int i = 0; i < Main.dict.size(); i++){
            for (int j = 33; j < 48; j++)
            {
                String w1 = Main.dict.get(i) + Character.toString((char)j);
                String w2 = Character.toString((char)j) + Main.dict.get(i);
                int user = Main.findUserByPassowrd(hash(w1));
                if (user > -1) Main.foundUser(user, w1);
                user = Main.findUserByPassowrd(hash(w2));
                if (user > -1) Main.foundUser(user, w2);
            }
        }

    }

    /**
     * Replaces commonly replaced letters with their relevant symbols. Letters include:
     * <ul>
     *     <li>a -> @</li>
     *     <li>e -> 3</li>
     *     <li>i -> 1</li>
     *     <li>o -> 0</li>
     *     <li>s -> 5</li>
     * </ul>
     * Attempts to try both the capital and lower case version of these letters. As doing running each possible
     * combination of alternatives is computationally intense (2^n where n is the number of letters that could be
     * replaced), the method does this 5 times per word with random swaps.
     */
    public void letterReplacement()
    {
        HashMap<String, String> letters = new HashMap<>();
        letters.put("a", "@");
        letters.put("e", "3");
        letters.put("i", "1");
        letters.put("o", "0");
        letters.put("s", "5");
        try {
            Random random = new Random();
            for (int i = 0; i < Main.dict.size(); i++){
                for (int j = 0; j < 5; j++)
                {
                    char[] cArray = Main.dict.get(i).toCharArray();
                    for (int k = 0; k < cArray.length; k++)
                    {
                        if (letters.containsKey(String.valueOf(cArray[k]))){
                            if (random.nextBoolean()) cArray[k] = letters.get(String.valueOf(cArray[k])).charAt(0);
                        }
                    }
                    int user = Main.findUserByPassowrd(hash(cArray.toString()));
                    if (user > -1) {
                        Main.foundUser(user, cArray.toString());
                        break;
                    }
                }
            }
        } catch (Exception e){

        }

    }

    /**
     * Replaces the first letter of each word with a capital letter, and also capitalises the entire word
     */
    public void capitals()
    {

        for (int i = 0; i < Main.dict.size(); i++){
            try {
                int user = Main.findUserByPassowrd(hash(capitalise(Main.dict.get(i))));
                if (user > -1) Main.foundUser(user, String.valueOf(i));
                user = Main.findUserByPassowrd(hash(Main.dict.get(i)).toUpperCase());
                if (user > -1) Main.foundUser(user, String.valueOf(i));
            } catch (Exception e) {
                /**
                 * Likely null pointer exception. Catches all errors to continue running
                 */
            }

        }
    }

    /**
     * Searches dictionary for words
     */
    public void search()
    {
        for(int i = 0; i < Main.users.size(); i++) {
            if (Main.dict.containsKey(Main.users.get(String.valueOf(i))))
                Main.foundUser(i, Main.dict.get(Main.users.get(String.valueOf(i))));
        }
    }

    /**
     * Searches for number passwords by infinitely counting up from 0 to infinity.
     */
    public void numbers()
    {
        for (long i = 0; i > -1; i++){
            int user = Main.findUserByPassowrd(hash(String.valueOf(i)));
            if (user > -1) Main.foundUser(user, String.valueOf(i));
        }
    }


    /**
     * Loops through the dictionary and concatenates two words. Very computationally intense, n^n where n is the number
     * of words in the dictionary
     */
    public void concat()
    {
       for (int i = 0; i < Main.concatList.size(); i++){
           for (int j = 0; j < Main.concatList.size(); j++){

               /**
                * Concat words lower case
                */
               String c1 = Main.concatList.get(i) + Main.concatList.get(j);
               int user = Main.findUserByPassowrd(hash(c1));
               if (user > -1) Main.foundUser(user, c1);

               /**
                * Capitalise both words
                */
               String c2 = capitalise(Main.concatList.get(i)) + capitalise(Main.concatList.get(j));
               user = Main.findUserByPassowrd(hash(c2));
               if (user > -1) Main.foundUser(user, c2);
           }
       }
    }

    /**
     * Receives a string and returns the string after it has been hashed.
     * @param s1
     * @return
     */
    private String hash(String s1)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(s1.getBytes("UTF-8"));
            byte[] digest = md.digest();
            return String.format("%064x", new java.math.BigInteger(1, digest));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Receives a string, capitalises the first letter and returns it.
     * @param s1
     * @return
     */
    private String capitalise(String s1)
    {
        if (s1 != null) return s1.substring(0, 1).toUpperCase() + s1.substring(1);
        return null;
    }

}
