var pl = {
  loggedIn: false,
  account: null,
  basePath: "",
  createRequestObject: function(callback) {
    var r;
    if (window.XMLHttpRequest) {
      r = new XMLHttpRequest();
    } else {
      r = new ActiveXObject("Microsoft.XMLHTTP");
    }
    r.onreadystatechange = function() {
      if (r.readyState == 4) {
        if (r.status >= 200 && r.status <= 299) {
          // Done to avoid a memory leak
          window.setTimeout(function() {
            r.onreadystatechange = function() {};
          }, 0);
          if (callback) {
            callback(r.responseText);
          }
        }
      }
    }
    return r;
  },
  login: function(username, password, callback) {
    var cb = function(response) {
      if (typeof response == "string" && response.length > 0) {
        var acct = JSON.parse(response);
        if (acct != null) {
          pl.loggedIn = true;
          pl.account = acct;
          if (callback) {
            callback.call();
          }
        }
      }
    }
    var r = pl.createRequestObject(cb);
    r.open("POST", pl.basePath + "/auth/login", true);
    r.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    r.send(JSON.stringify({username: username, password: password}));
  }
};
