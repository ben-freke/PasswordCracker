import java.io.*;
import java.security.MessageDigest;
import java.util.*;

public class Main{

    public static HashMap<String, String> users;
    public static HashMap<String, String> users_pw_index;
    private static HashMap<String, String> crackedUsers;
    public static HashMap<String, String> dict;
    public static HashMap<String, String> pwds;
    public static ArrayList<String> concatList;

    /**
     * Variables that must be set by the operator.
     */
    private static final String DICTIONARY_LOCATION = "D:\\Software Development\\Java\\Password Cracker\\src\\dictionary.txt";
    private static final String CONCACT_DICT_LOCATION = "D:\\Software Development\\Java\\Password Cracker\\src\\concatDict.txt";
    public static final String PASSWORDS_LOCATION = "D:\\Software Development\\Java\\Password Cracker\\src\\passwords.txt";
    private static final String USERS_LOCATION = "D:\\Software Development\\Java\\Password Cracker\\src\\samplepassword.txt";
    private static final int NUM_MINUTES_RUN = 10;

    /**
     * The main method runs the program. This method creates different threads for actions in the password cracking
     * sequence and runs them. Finally, this thread stops these password cracking attempts when a specified time
     * limit has been exceeded.
     * @param args
     */

    public static void main(String[] args)
    {
        long startTime = System.currentTimeMillis();
        dict = getDict(DICTIONARY_LOCATION);
        users = getData();
        concatList = getConcatList();
        crackedUsers = new HashMap<>();

        /**
         * Start the concatenation thread.
         */
        Thread concatThread = (new Thread() {
            public void run() {
                (new PasswordThread()).concat();
            }
        });
        concatThread.start();

        /**
         * Start the search thread.
         */
        Thread searchThread = (new Thread() {
            public void run() {
                (new PasswordThread()).search();
            }
        });
        searchThread.start();

        /**
         * Start the numbers thread.
         */
        Thread numbersThread = (new Thread() {
            public void run() {
                (new PasswordThread()).numbers();
            }
        });
        numbersThread.start();

        /**
         * Start the capitalisation thread.
         */
        Thread capitalsThread = (new Thread() {
            public void run() {
                (new PasswordThread()).capitals();
            }
        });
        capitalsThread.start();

        /**
         * Start the symbol concatenation thread.
         */
        Thread symbolConcatThread = (new Thread() {
            public void run() {
                (new PasswordThread()).symbolConcat();
            }
        });
        symbolConcatThread.start();

        /**
         * Start the letter replacement thread.
         */
        Thread letterReplacementThread = (new Thread() {
            public void run() {
                (new PasswordThread()).letterReplacement();
            }
        });
        letterReplacementThread.start();

        /**
         * Start the search thread for the big password list.
         */
        Thread passwordListCheckerThread = (new Thread() {
            public void run() {
                (new PasswordThread()).passwordList();
            }
        });
        passwordListCheckerThread.start();

        Boolean running = true;
        while (true) {
            if ((System.currentTimeMillis() - startTime) > (NUM_MINUTES_RUN*60000)-10000){
                searchThread.stop();
                numbersThread.stop();
                capitalsThread.stop();
                symbolConcatThread.stop();
                letterReplacementThread.stop();
                passwordListCheckerThread.stop();
                concatThread.stop();
                running = false;
            }
            if (!running)
            {
                try {
                    PrintWriter writer = new PrintWriter("output", "UTF-8");
                    for(Map.Entry<String, String> entry : crackedUsers.entrySet()) writer.write("user" + entry.getKey() + ":" + entry.getValue() + "\n");
                    writer.close();
                    System.out.println("Completed Successfully");
                } catch (Exception e){System.out.println("Saving Data Failed");}
                break;

            }
        }
    }

    /**
     * Takes in UserID and the Plan Text Password when a thread has found a user.
     * Adds the user to the crackedUsers HashMap and removes the user from the users HashMap so
     * other processes do not attempt to waste resources on that password.
     * @param userID
     * @param planTxtPassword
     */
    public static void foundUser(int userID, String planTxtPassword)
    {
        crackedUsers.put(String.valueOf(userID), planTxtPassword);
        users.remove(userID);
    }

    /**
     * Takes in a (hashed) password and returns the integer of the relevant user's ID.
     * If no password is found, function returns -1.
     * @param password
     * @return
     */
    public static int findUserByPassowrd(String password)
    {
        for (int i = 0; i < users.size(); i++) if (users.get(String.valueOf(i)) == password) return i;
        return -1;

    }

    /**
     * Takes in a filename and returns a HashMap of the list of users, with the UserID Int (in string form) in the
     * key and the hashed password in the value fields respectively
     */
    private static HashMap<String, String> getData()
    {
        try {
            File file = new File(USERS_LOCATION);
            FileReader fileReader = new FileReader(file);
            HashMap<String, String> users1 = new HashMap<>();
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
                    users1.put(parts[1], hashKey);
                    pointer = hashLength;
                    i = pointer + 1;
                }
            }
            return users1;
        } catch (Exception e) {
            return null;
        }

    }

    /**
     * Returns the ArrayList for the dictionary used in the concatenation function. This is a smaller dictionary than
     * the main dictionary in order to reduce the efficiency of the program.
     * @return
     */
    private static ArrayList<String> getConcatList()
    {
        try {
            BufferedReader bufferedReader = new BufferedReader(new FileReader(CONCACT_DICT_LOCATION));
            int i = 0;
            concatList = new ArrayList<>();
            String line;
            while((line = bufferedReader.readLine()) != null) {
                concatList.add(i, line);
                i++;
            }
            return concatList;
        } catch (Exception e)
        {
            return null;
        }
    }

    /**
     * Takes in a filename and returns HashMap, with the hashedWord as the key in order to speed up
     * processing. Used for dictionaries
     * @param filename
     * @return
     */
    public static HashMap<String, String> getDict(String filename)
    {
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
