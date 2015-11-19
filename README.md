# bahmni-offline
Repo to hold bahmni offline code. This will support Chromium app and Android app as of now.

To run android app

1. install Node.js. if you dont have it
2. npm install -g cordova ionic, for any issues with ionic installation refer http://ionicframework.com/getting-started/
3. npm install -g try-thread-sleep
4. Follow cordova platform guide to install android sdk, tools and Android Studio if not already installed http://cordova.apache.org/docs/en/latest/guide/platforms/android/index.html
4. Checkout out repo
5. cd bahmni-offline/android
6. ionic platform remove android
7. ionic platform add android
8. git checkout .
9. In Android studio File -> New -> Import project (bahmni-offline/android/platforms/android)
10. to deploy the app shift + f10
