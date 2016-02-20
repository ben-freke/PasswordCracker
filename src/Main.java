import java.io.*;
import java.security.MessageDigest;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

public class Main {

    private static HashMap<String, String> users;
    private static HashMap<String, String> words;

    public static void main(String[] args)
    {

        try{
            users = getData("D:\\Software Development\\Java\\Password Cracker\\src\\password.txt");
            words = getWords("D:\\Software Development\\Java\\Password Cracker\\src\\dictionary.txt");

        } catch (Exception e){
            System.out.println("Oh dear. An error parsing the file occurred.");
        }

        for(Map.Entry<String, String> entry : users.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String result = testString(value);
            if (result != null){

                System.out.println(key + ": " + result);
            }
        }


    }

    private static String testString(String string)
    {
        if (words.containsKey(string)){
            return words.get(string);
        } else {
            return null;
        }
    }

    private static String concatenate(String s1, String s2){
        return s1 + s2;
    }

    private static String capitalise(String s1) {
        char[] cArray = s1.toCharArray();
        cArray[0] = Character.toUpperCase(cArray[0]);
        return new String(cArray);
    }

    private static String replaceCommonLetters(String s1)
    {
        char[] cArray = s1.toCharArray();
        for (int i = 0; i < cArray.length; i++)
        {
            switch (cArray[i]) {
                case 'e':
                    cArray[i] = '3';
                    break;
                case 'E':
                    cArray[i] = '3';
                    break;
                case 'o':
                    cArray[i] = '0';
                    break;
                case 'O':
                    cArray[i] = '0';
                    break;
            }
        }
        return new String(cArray);
    }

    private static HashMap<String, String> getData(String filename) throws Exception{
            File file = new File(filename);
            FileReader fileReader = new FileReader(file);
            HashMap<String, String> users = new HashMap<String, String>();
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
                    users.put(username, hashKey);
                    pointer = hashLength;
                    i = pointer + 1;
                }
            }
            return users;
    }

    private static HashMap<String, String> getWords(String filename) throws Exception
    {

        String line = null;
        FileReader fileReader =
                new FileReader(filename);
        BufferedReader bufferedReader =
                new BufferedReader(fileReader);
        HashMap<String, String> dictionary = new HashMap<String, String>();
        while((line = bufferedReader.readLine()) != null) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            line = line.toLowerCase();
            md.update(line.getBytes("UTF-8"));
            byte[] digest = md.digest();
            String hashedWord = String.format("%064x", new java.math.BigInteger(1, digest));
            dictionary.put(hashedWord, line);
        }
        bufferedReader.close();

        fileReader =
                new FileReader(filename);
        bufferedReader =
                new BufferedReader(fileReader);
        while((line = bufferedReader.readLine()) != null) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            line = line.toLowerCase();
            line = capitalise(line);
            md.update(line.getBytes("UTF-8"));
            byte[] digest = md.digest();
            String hashedWord = String.format("%064x", new java.math.BigInteger(1, digest));
            dictionary.put(hashedWord, line);
        }
        bufferedReader.close();

        fileReader =
                new FileReader(filename);
        bufferedReader =
                new BufferedReader(fileReader);
        while((line = bufferedReader.readLine()) != null) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            line = line.toLowerCase();
            line = replaceCommonLetters(line);
            md.update(line.getBytes("UTF-8"));
            byte[] digest = md.digest();
            String hashedWord = String.format("%064x", new java.math.BigInteger(1, digest));
            dictionary.put(hashedWord, line);
        }
        bufferedReader.close();


        fileReader =
                new FileReader(filename);
        bufferedReader =
                new BufferedReader(fileReader);
        while((line = bufferedReader.readLine()) != null) {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            line = line.toLowerCase();
            line = capitalise(line);

            BufferedReader bufferedReader2 =
                    new BufferedReader(fileReader);
            String line2 = null;
            while((line2 = bufferedReader2.readLine()) != null) {
                String line3 = concatenate(line, line2);
                md.update(line.getBytes("UTF-8"));
                byte[] digest = md.digest();
                String hashedWord = String.format("%064x", new java.math.BigInteger(1, digest));
                dictionary.put(hashedWord, line3);
            }
            bufferedReader2.close();
        }
        bufferedReader.close();

        return dictionary;
    }
}
