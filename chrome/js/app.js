document.addEventListener('DOMContentLoaded', function () {
    getFromDB('implementationURL', handleImplementationURL);
}, false);

var handleImplementationURL = function(data) {

    if (data && data.implementationURL) {
        hideImplementationInputs();
        document.querySelector('webview').setAttribute(
            'src', data.implementationURL);
    }
    else {
        enterButton.addEventListener('click', function () {
            hideImplementationInputs();
            var source = "https://" +  document.getElementById('ipValue').value + "/bahmni/home/#/device/chrome-app";
            document.querySelector('webview').setAttribute('src', source);
            setInDB({'implementationURL': source});
        });
    }
};

var setInDB= function(object){
    chrome.storage.local.set(object);
};

var getFromDB= function(key, promise){
    chrome.storage.local.get(key, function(data){
        promise(data);
    });
};

var hideImplementationInputs = function() {
    document.getElementById('enterButton').style.display = 'none';
    document.getElementById('ipValue').style.display = 'none';
};

var maximizeWebviews = function () {
    var webview = document.querySelector("webview");
    webview.style.height = document.documentElement.clientHeight + "px";
    webview.style.width = document.documentElement.clientWidth + "px";
};

onload = maximizeWebviews;
window.onresize = maximizeWebviews;