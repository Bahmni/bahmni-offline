// Ionic Starter App

// angular.module is a global place for creating, registering and retrieving Angular modules
// 'starter' is the name of this angular module example (also set in a <body> attribute in index.html)
// the 2nd parameter is an array of 'requires'
var app=angular.module('starter', ['ionic']);

app.run(function($ionicPlatform) {
  $ionicPlatform.ready(function() {
    // Hide the accessory bar by default (remove this to show the accessory bar above the keyboard
    // for form inputs)
    if(window.cordova && window.cordova.plugins.Keyboard) {
      cordova.plugins.Keyboard.hideKeyboardAccessoryBar(true);
    }
    if(window.StatusBar) {
      StatusBar.styleDefault();
    }
    if(window.localStorage['host']){
      window.open('https://'+ window.localStorage['host']+'/bahmni/#/device/android','_self')
    }
  });
});

app.controller("hostController",function($scope){
  $scope.ipValue ='';
  $scope.submit = function(){
    console.log("coming",this.ipValue);
    window.localStorage['host'] = this.ipValue;
    window.open('https://'+this.ipValue+'/bahmni/#/device/android','_self')
  };
});
