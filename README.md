# bahmni-offline
Repo to hold bahmni offline code. This will support Chromium app and Android app as of now.

To run android app

1. install Node.js. if you dont have it
2. npm install -g cordova ionic, for any issues with ionic installation refer http://ionicframework.com/getting-started/
3. npm install -g try-thread-sleep
4. brew install gradle. When importing project into IDE it will ask for gradle home location. Point it to /usr/local/Cellar/gradle/X.X/libexec
5. Follow cordova platform guide to install android sdk, tools. Instead of Android studio android support plugin for intellij can also be used.  http://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html
6. Checkout out repo
7. cd bahmni-offline/android
8. ionic platform remove android
9. ionic platform add android
10. git checkout .
11. In Android studio File -> New -> Import project (bahmni-offline/android/platforms/android)
12. to deploy the app shift + f10
