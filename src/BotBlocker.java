import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BotBlocker {
    int count;
    List<String> done = new ArrayList<>();

    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        System.out.println("Please enter where you want to continue, enter 0 if you want to start from the beginning of the bots.txt file");
        new BotBlocker(sc.nextInt());
    }

    public void block(int it, final String authcode) throws IOException {
        try {
            if (!testValid(authcode)) {
                System.out.println("Wrong authcode, doesnt have the permissions");
                File f = new File("./currentauthcode.txt");
                f.delete();
                return;
            } else {
                System.out.println("Authcode is valid!");
            }
        } catch (Exception e) {
            System.out.println("Invalid, please create a new Auth token: https://id.twitch.tv/oauth2/authorize?response_type=code&client_id=z2ckhpdb422p47m4q3yrubk8xky6m6&redirect_uri=https://localhost/&scope=user_blocks_read+user_blocks_edit");
            Scanner sc = new Scanner(System.in);
            System.out.println("Please paste the URL you got after authorising or enter \"exit\" :");
            String code = sc.nextLine();
            if (code.equals("exit")) {
                return;
            }
            code = code.replace("https://localhost/?code=", "");
            code = code.split("&")[0];
            try {
                block(it, getToken(code));
            } catch (Exception ee) {
                System.out.println("Please do that again, this URL is not valid anymore");
                block(it, "");
            }
            return;
        }


        final int streamerid = getUserIdFromToken(authcode);
        File f = new File("./bots.txt");
        Path filePath = f.toPath();
        Charset charset = Charset.defaultCharset();

        List<String> sList;
        try {
            sList = Files.readAllLines(filePath, charset);
        } catch (IOException e) {
            System.out.println("bots.txt does not exist, creating new...");
            f.createNewFile();
            System.out.println("Please fill the file and re-run the program.");
            return;
        }
        System.out.println("Starting to block all " + sList.size() + " Accounts, starting from account " + it + ".");
        Scanner sc = new Scanner(System.in);
        System.out.println("Do you really want to continue? Y/N");
        if (!sc.nextLine().equals("Y")) {
            return;
        }
        String userclientid = getUserClientIdFromToken(authcode);
        sList = sList.subList(it, sList.size());
        final List<String> finalSList = sList;
        class Threader extends Thread {
            final List<String> l;

            public Threader(List<String> pL) {
                l = pL;
            }
            @Override
            public void run() {
                int i = 0;
                while (true) {
                    if (i >= l.size()) {
                        //System.out.println("-------------------------------------------------ENDED THREAD " + Thread.currentThread().getName() + "-------------------------------------------------");
                        //System.out.println("Blocking of that many accounts failed, might already be deleted: " + testRemaining(finalSList));
                        return;
                    }
                    try {
                        Thread.sleep(700);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for(int failed = 0;failed<10;failed++){
                        try {
                            int id = getUserId(l.get(i), authcode, userclientid);
                            if (id != 0) {
                                blockbanUser(id, authcode, streamerid, finalSList.size());
                            }

                        } catch (Exception e) {
                            System.out.println("Thread " + Thread.currentThread().getName() + " is waiting 10 seconds...");
                            try {
                                Thread.sleep(10000);
                            } catch (InterruptedException interruptedException) {
                                interruptedException.printStackTrace();
                            }
                            System.out.println("Thread " + Thread.currentThread().getName() + " is continuing and retrying previous operation.");
                        }
                    }
                    i++;
                }
            }
        }
        int threads = 8;
        Threader t = new Threader(sList.subList(0, (sList.size()) / threads));
        t.setName("0");
        t.start();
        for (int j = 1; j < threads - 1; j++) {
            Threader t1 = new Threader(sList.subList(((sList.size()) / threads) * j, ((sList.size()) / threads) * (j + 1)));
            t1.setName(j + "");
            t1.start();
        }
        Threader t3 = new Threader(sList.subList(((sList.size()) / threads) * (threads - 1), sList.size()));
        t.setName("" + threads);
        t3.start();
    }

    public int getUserIdFromToken(String authcode) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL("https://id.twitch.tv/oauth2/validate").openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + authcode);
        con.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String s = in.readLine();
        s = s.split(",")[4];
        s = s.split("\"")[3];
        return Integer.parseInt(s);
    }

    public String getUserClientIdFromToken(String token) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL("https://id.twitch.tv/oauth2/validate").openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String s = in.readLine();
        s = s.split(",")[0];
        s = s.split("\"")[3];
        return s;
    }

    public boolean testValid(String token) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL("https://id.twitch.tv/oauth2/validate").openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "OAuth " + token);
        con.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String s = in.readLine();
        return s.contains("user_blocks_edit");
    }

    public BotBlocker(int it) throws IOException {
        File f = new File("./currentauthcode.txt");
        Path filePath = f.toPath();
        Charset charset = Charset.defaultCharset();

        List<String> sList = null;
        try {
            f.createNewFile();
            sList = Files.readAllLines(filePath, charset);
        } catch (IOException e) {
            e.printStackTrace();
        }
        assert sList != null;
        if (sList.isEmpty()) {
            block(it, "");
        } else {
            block(it, sList.get(0));
        }
    }

    public String getToken(String code) throws IOException {
        HttpURLConnection con = (HttpURLConnection) new URL("https://id.twitch.tv/oauth2/token?client_id=z2ckhpdb422p47m4q3yrubk8xky6m6&code=" + code + "&grant_type=authorization_code&redirect_uri=https://localhost/&client_secret=38ux1x5kfxdk35y1hqs71kkcy1nyi5").openConnection();
        con.setRequestMethod("POST");
        con.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String eg = in.readLine();
        String[] ergs = eg.split(",");
        String erg = ergs[0].substring(17);
        System.out.println(erg.replace("\"", ""));
        try {
            FileWriter myWriter = new FileWriter("currentauthcode.txt");
            myWriter.write(erg.replace("\"", ""));
            myWriter.close();
            System.out.println("Successfully wrote to the file.");
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return erg.replace("\"", "");
    }

    public void blockbanUser(int id, String token, int id2, int last) throws IOException {
        count++;
        loadingBar(count, last);
        if (id == 0) {
            return;
        }
        HttpURLConnection con = (HttpURLConnection) new URL("https://api.twitch.tv/kraken/users/" + id2 + "/blocks/" + id).openConnection();
        con.setRequestMethod("PUT");
        con.setRequestProperty("Authorization", "OAuth " + token);
        con.setRequestProperty("Client-id", "z2ckhpdb422p47m4q3yrubk8xky6m6");
        con.setRequestProperty("Accept", "application/vnd.twitchtv.v5+json");
        con.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        //System.out.println(in.readLine());
    }

    public void loadingBar(int count, int last) {
        System.out.print(count+"/"+last+"\r");
    }

    public int getUserId(String name, String token, String clientid) throws IOException {
        done.add(name);
        HttpURLConnection con = (HttpURLConnection) new URL("https://api.twitch.tv/helix/users?login=" + name).openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", "Bearer " + token);
        con.setRequestProperty("client-id", clientid);
        con.setDoOutput(true);
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String s = in.readLine();
        return toNum(s);
    }

    public List<String> testRemaining(List<String> sList) {
        List<String> res = new ArrayList<>();
        for (String s : sList) {
            if (!done.contains(s)) {
                res.add(s);
            }
        }
        return res;
    }

    public int toNum(String eg) {
        if (eg.contains("Bad Request")) {
            return 0;
        }
        try {
            String[] ergs = eg.split(",");
            String erg = ergs[0].substring(16);
            return Integer.parseInt(erg.replace("\"", ""));
        } catch (Exception e) {
            return 0;
        }
    }
}
