window.onload = function(e){
    handleImplementationURL(localStorage.getItem('host'));
}

var handleImplementationURL = function(data) {
  if (data) {
    hideImplementationInputs();
    goToHome();
  } else {
    enterButton.addEventListener('click', function() {
      var source = document.getElementById('ipValue').value;
      if(source){
        hideImplementationInputs();
        localStorage.setItem('host',source);
        goToHome();
      }
    });
  }
};

var hideImplementationInputs = function() {
  document.getElementById('enterButton').style.display = 'none';
  document.getElementById('ipValue').style.display = 'none';
};

var goToHome = function(){
  window.location = "app/home/index.html#device/chrome-app";
};
