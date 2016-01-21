# bahmni-offline
Repo to hold bahmni offline code. This will support Chromium app and Android app as of now.

To run android app

1. install Node.js. if you dont have it
2. npm install -g cordova ionic, for any issues with ionic installation refer http://ionicframework.com/getting-started/
3. npm install -g try-thread-sleep
4. Follow cordova platform guide to install android sdk, tools. Instead of Android studio android support plugin for intellij can also be used.  http://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html
5. Add the following sdk packages (http://developer.android.com/sdk/installing/adding-packages.html)
    latest sdk tools
    latest platform tools
    latest build tools
    android 5.1.1(API 22) platform
    android 4.0.3(API 15) platform
6. Checkout out repo
7. cd bahmni-offline/android
8. ionic platform remove android
9. ionic platform add android
10. git checkout .
11. ionic build android
12. In Intellij File -> New -> Project existing sources (bahmni-offline/android/platforms/android)
13. to deploy the app shift + f10
