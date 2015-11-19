# bahmni-offline
Repo to hold bahmni offline code. This will support Chromium app and Android app as of now.

To run android app

1. install Node.js. if you dont have it
2. npm install -g cordova ionic, for any issues with ionic installation refer http://ionicframework.com/getting-started/
3. Checkout out repo
4. cd bahmni-offline/android
5. ionic platform remove android
6. ionic platform add android
7. git checkout .
8. In Android studio File -> New -> Import project (bahmni-offline/android/platforms/android)
9. to deploy the app shift + f10
