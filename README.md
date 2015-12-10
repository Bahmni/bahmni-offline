# bahmni-offline
Repo to hold bahmni offline code. This will support Chromium app and Android app as of now.

To run android app

1. install Node.js. if you dont have it
2. npm install -g cordova ionic, for any issues with ionic installation refer http://ionicframework.com/getting-started/
3. npm install -g try-thread-sleep
4. brew install gradle. When importing project into IDE it will ask for gradle home location. Point it to /usr/local/Cellar/gradle/X.X/libexec
5. Follow cordova platform guide to install android sdk, tools. Instead of Android studio android support plugin for intellij can also be used.  http://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html
6. Add the following sdk packages (http://developer.android.com/sdk/installing/adding-packages.html)
    latest sdk tools
    latest platform tools
    android 5.1.1(API 22) platform
    android 4.0.3(API 15) platform
7. Checkout out repo
8. cd bahmni-offline/android
9. ionic platform remove android
10. ionic platform add android
11. git checkout .
12. ionic build android
13. In Intellij File -> New -> Project existing sources (bahmni-offline/android/platforms/android)
14. to deploy the app shift + f10
