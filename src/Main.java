import java.io.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.regex.Pattern;

public class Main{

    private static HashMap<String, String> users;
    public static HashMap<String, String> dict;
    private static final String DICTIONARY_LOCATION = "/Users/benfreke/Software Development/Java/PasswordCracker/src/dictionary.txt";
    private static final String USERS_LOCATION = "/Users/benfreke/Software Development/Java/PasswordCracker/src/password.txt";
    private static final int NUM_THREADS = 4;

    public static void main(String[] args)
    {
        List<SortedMap<Integer, String>> listOfMaps = new ArrayList<>();
        try {
            dict = getDict(DICTIONARY_LOCATION);
            System.out.println("Dictionary Size: " + dict.size());
            TreeMap<Integer, String> users = getData(USERS_LOCATION);
            int sections = (int) Math.ceil(users.size() / NUM_THREADS);
            int cumulativeSum = 0;
            for (int i = 0; i < NUM_THREADS; i++){
                if (i == NUM_THREADS-1) listOfMaps.add(i, users.subMap(cumulativeSum,  users.size()));
                else listOfMaps.add(i, users.subMap(cumulativeSum, cumulativeSum = cumulativeSum + sections));
            }
            for (int i = 0; i<listOfMaps.size(); i++){
                final int value = i;
                (new Thread() {
                    public void run() {
                        long startTime = System.currentTimeMillis();
                        PasswordThread passwordThread = new PasswordThread();
                        passwordThread.run(listOfMaps.get(value));
                        long endTime   = System.currentTimeMillis();
                        long totalTime = endTime - startTime;
                    }
                }).start();
            }
        } catch (Exception e) {
        }
    }
    private static TreeMap<Integer, String> getData(String filename) throws Exception
    {
            File file = new File(filename);
            FileReader fileReader = new FileReader(file);
            TreeMap<Integer, String> users = new TreeMap<>();
            char[] charArray = new char[(int) file.length() ];
            fileReader.read(charArray);
            int pointer = 0;
            for(int i=0;i<charArray.length;i++)
            {
                if (String.valueOf(charArray[i]).equals(":"))
                {
                    int usrLength = i - pointer;
                    StringBuilder builder = new StringBuilder();
                    for (int j = pointer; j < usrLength + pointer; j++)
                    {
                        if (!String.valueOf(charArray[j]).equals("\n"))
                            builder.append(String.valueOf(charArray[j]));
                    }
                    String username = builder.toString();
                    builder = new StringBuilder();
                    pointer = i+1;
                    int hashLength = pointer+64;
                    for (int k = pointer; k < hashLength; k++){
                        if (!String.valueOf(charArray[k]).equals("\n"))
                            builder.append(String.valueOf(charArray[k]));
                    }
                    String hashKey = builder.toString();
                    String[] parts = username.split("user");
                    users.put(Integer.parseInt(parts[1]), hashKey);
                    pointer = hashLength;
                    i = pointer + 1;
                }
            }
            return users;
    }

    private static HashMap<String, String> getDict(String filename) {
        try {
            HashMap<String, String> dict = new HashMap<>();
            String line;
            BufferedReader bufferedReader = new BufferedReader(new FileReader(filename));
            int i = 0;
            while((line = bufferedReader.readLine()) != null) {

                MessageDigest md = MessageDigest.getInstance("SHA-256");
                line = line.toLowerCase();
                md.update(line.getBytes("UTF-8"));
                byte[] digest = md.digest();
                String hashedWord = String.format("%064x", new java.math.BigInteger(1, digest));
                dict.put(hashedWord, line);
                i++;
            }
            return dict;
        } catch (Exception e) {
            return null;
        }
    }




}
