# Mobile Computing Assignment
This project is for the purpose of Mobile Computing Assignment, an application on smartphone.

## Functionality
1. Allow people to get(pull) a particular WiFi password from a server, without having to asking people over and over again.
2. Can update(post) a particular WiFi password to a server, thus sharing the password with other user.
3. Help people automatically connect to any available and only strong WiFi signal, depending on he or she's location.
4. To seamlessly switch between Mobile / Cellular Data(2G, 3G, 4G, LTE) and WiFi, forming a single network switch.

Video demonstration can be found in https://youtu.be/MDc32ac0FVg

### Test Cases
Situation | Expected output
------------ | -------------
WiFi is enabled / User turn on WiFi | Check internet access and connection timeout?
Internet access is available | Fetch data
No Internet access | Connect to strong public WiFi. Repeat checking
Still no | Enable Mobile / Cellular Data. Repeat checking
Failed - Internet access is not possible | Alerting user and get WiFi scan result

## Built With
* [Android Studio](https://developer.android.com/studio/index.html) - The IDE used

## Authors
* **Lim Kha Shing** - [kslim888](https://www.linkedin.com/in/lim-kha-shing-836a24120/)

## Acknowledgments
Special thanks to :
* [Lau Khai Yuen](https://lkyyuen.com/) for helping in databases
* [Chooi Yi Ying](https://www.facebook.com/Yying.1008) for giving inspiration and ideas
