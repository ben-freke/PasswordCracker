import java.io.BufferedReader;
import java.io.FileReader;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;

/**
 * Created by benfreke on 20/02/2016.
 */
public class PasswordThread {


    public void run(SortedMap<Integer, String> users) {
        try{

            for(Map.Entry<Integer, String> entry : users.entrySet()) {
                int key = entry.getKey();
                String value = entry.getValue();
                boolean found = false;
                if (Main.pwds.containsKey(value)) {
                    Main.decrptedUsers.put(key, Main.pwds.get(value));
                    continue;
                }
                if (Main.dict.containsKey(value)) {
                    Main.decrptedUsers.put(key, Main.dict.get(value));
                    continue;
                }
                for(Map.Entry<String, String> dictEntry : Main.dict.entrySet()) {
                    String line = dictEntry.getValue();
                    if (checkFound(line.toUpperCase(), value))
                    {
                        Main.decrptedUsers.put(key, line.toUpperCase());
                        break;
                    }

                    if (checkFound(capitalise(line), value))
                    {
                        Main.decrptedUsers.put(key, capitalise(line));
                        break;
                    }

                    if (checkFound(replace('o', '0', line), value))
                    {
                        Main.decrptedUsers.put(key, replace('o', '0', line));
                        break;
                    }

                    if (checkFound(replace('e', '3', line), value))
                    {
                        Main.decrptedUsers.put(key, replace('e', '3', line));
                        break;
                    }

                    if (checkFound(replace('a', '@', line), value))
                    {
                        Main.decrptedUsers.put(key, replace('a', '@', line));
                        break;
                    }


                    /**String pwd = concat(line, value);
                    if (pwd != null) {
                        found = true;
                        System.out.println(key + ":" + pwd);
                        break;
                    }**/
                }
            }
        } catch (Exception e){
            System.out.println("Oh dear. An error parsing the file occurred.");
            e.printStackTrace();
        }
    }

    public boolean checkFound(String value, String test)
    {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            value = value.toLowerCase();
            md.update(value.getBytes("UTF-8"));
            byte[] digest = md.digest();
            String hashedWord = String.format("%064x", new java.math.BigInteger(1, digest));
            if (test.equals(hashedWord)) return true;
            return false;
        } catch (Exception e) {return false;}
    }

    public String concat(String test, String value)
    {
        for(Map.Entry<String, String> dictEntry : Main.dict.entrySet()) {
            String line = dictEntry.getValue();
            String concat = test + line;
            if (checkFound(value, concat)){
                return concat;
            }
        }
        return null;
    }

    public String concatYears(String test, String value)
    {
        for (int i = 1940; i < 2040; i++)
        {
            if (checkFound(test + String.valueOf(i), value)) return test + String.valueOf(i);
            if (checkFound(test.toUpperCase() + String.valueOf(i), value)) return test.toUpperCase() + String.valueOf(i);

        }
        return null;
    }

    public String capitalise(String s1) {
       return s1.substring(0, 1).toUpperCase() + s1.substring(1);
    }

    public String replace(char r1, char r2, String s1)
    {
        char[] cArray = s1.toCharArray();
        for (int i = 0; i<cArray.length; i++)
        {
            if (cArray[i] == r1) cArray[i] = r2;
        }
        return String.valueOf(cArray);
    }

}
