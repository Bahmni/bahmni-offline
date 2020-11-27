# bahmni-offline
Repo to hold bahmni offline code. This will support Chromium app and Android app as of now.

**To run android app**

//** Use Below commands in terminal **//
1. install Node.js. if you dont have it
2. npm install -g cordova ionic, for any issues with ionic installation refer http://ionicframework.com/getting-started/
3. npm install -g try-thread-sleep
4. export ANDROID_HOME=/Users/user/Library/Android/sdk
   export JAVA_HOME=/Library/Java/JavaVirtualMachines/jdk1.8.0_231.jdk/Contents/Home(Set Path if its not set already)
5. Add the following sdk packages
   Android 5.1.1(API 22) platform
6. checkout cd bahmni-offline/android
7. cordova platform rm android
8. cordova platform add android
9. cordova build android
/******************************/ 
10. In Android Studio File -> New -> Import Project (bahmni-offline/android/platforms/android)
11. Run the Project (Shift + F10) Or cordova run --emulator
12. If the assets are not loaded, (This error appears on Emulator -> `Application Error, net::ERR_FILE_NOT_FOUND file:///android_asset/www/index.html was not found`) then hardcode the launchURL in 
    `ConfigXmlParser.java ->
         public String getLaunchUrl() {
        return "file:///android_asset/www/index.html";
    }`

**To run andoid test**

1. cd android/platforms/android
2. ./gradlew clean connectedAndroidTest
