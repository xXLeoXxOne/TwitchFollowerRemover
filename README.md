# TwitchFollowerRemover
This program written in Java can remove your unwanted Followers on Twitch!

# Tutorial
1. Download [Botremover.jar](https://github.com/xXLeoXxOne/TwitchFollowerRemover/raw/main/BotRemover.jar)
2. Run the program with ```java -jar BotRemover.jar```
3. Enter the starting point you want to start at in the bots.txt list
4. Copy authentication URL to Browser, hit enter and copy the URL you got redirected to
5. Paste this URL, the program will request the authtoken from the API and save it inside currentauthcode.txt
6. If you havent already filled and created a bots.txt the program will stop and you will have to start it again after filling the file
7. Program will ask you if you want to continue, enter Y and press enter.
8. Wait a while and all of the followers you put into bots.txt are gone!

# Disclaimer
As this is my first Twitch Tool it is not very professional. It does what it has to do but it might not be the fastest and has a few bugs. I wrote it in Java because I needed a little challenge to learn from, it is my main language because of school and I wanted to help some streamers to remove their botted followers because our beloved [Commanderroot Follower Remover](https://twitch-tools.rootonline.de/follower_remover.php). There are also some security flaws but as long as you delete the currentauthcode.txt after you stopped using it nothing should happen to you. This Tool is fully local so you dont need to worry about someone getting your tokens. You have to get the list of followers you want to remove on your own so its very safe because it can only remove followers you defined.
I hope you like my tool!
