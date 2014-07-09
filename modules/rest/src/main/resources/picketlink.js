var pl = {
  loggedIn: undefined,
  token: null,
  account: null,
  basePath: "",
  getToken: function() {
    var token = window.localStorage.getItem("token");
    if (token == null) {
      token = pl.token;
    }
    return token;
  },
  setToken: function(token) {
    window.localStorage.setItem("token", token);
    pl.token = token;
  },
  clearToken: function() {
    window.localStorage.removeItem("token");
  },
  createRequestObject: function(callback, failCallback) {
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
        } else if (r.status == 401) { // unauthorized
          // Done to avoid a memory leak
          window.setTimeout(function() {
            r.onreadystatechange = function() {};
          }, 0);
          if (failCallback) {
            failCallback(r.responseText);
          }        
        }
      }
    }
    return r;
  },
  status: function(callback) {
    var cb = function(response) {
      if (typeof response == "string" && response.length > 0) {
        var acct = JSON.parse(response);
        if (acct != null) {
          pl.loggedIn = true;
          pl.account = acct;
        }
      }
      if (callback) {
        callback.call();
      }      
    };
    var cbFail = function() {
      pl.loggedIn = false;
      if (callback) {
        callback.call();
      }
    }
    var r = pl.createRequestObject(cb, cbFail);
    r.open("GET", pl.basePath + "/auth/status", true);
    if (pl.getToken() != null) {
      r.setRequestHeader("Authorization", "Token " + pl.getToken());
    }
    r.send();
  },
  login: function(username, password, callback) {
    var cb = function(response) {
      if (typeof response == "string" && response.length > 0) {
        var acct = JSON.parse(response);
        if (acct != null) {
          pl.loggedIn = true;
//          var wt = pl.jwt.WebTokenParser.parse(acct.authctoken);
//          var payload = JSON.parse(pl.jwt.base64urldecode(wt.payloadSegment));
//          pl.token = payload.jti;
          pl.setToken(acct.authctoken);
          pl.account = acct;
          if (callback) {
            callback.call();
          }
        }
      }
    };
    var r = pl.createRequestObject(cb);
    r.open("POST", pl.basePath + "/auth/login", true);
//    r.setRequestHeader("Content-Type", "application/json;charset=UTF-8");
    r.setRequestHeader("Authorization", "Basic " + btoa(username + ":" + password));
//    r.send(JSON.stringify({username: username, password: password}));
    r.send(JSON.stringify({}));
  },
  logout: function(callback) {
    var cb = function(response) {
      if (typeof response == "string" && response.length > 0) {
        var result = JSON.parse(response);
        if (result == true) {
          pl.loggedIn = false;
          pl.account = null;
          pl.clearToken();
          if (callback) {
            callback.call();
          }
        }
      }
    }
    var r = pl.createRequestObject(cb);
    r.open("GET", pl.basePath + "/auth/logout", true);
    if (pl.getToken() != null) {
      r.setRequestHeader("Authorization", "Token " + pl.getToken());
    }    
    r.send();
  }
};

// 
// The following code adopted from https://github.com/michaelrhanson/jwt-js
//
//jwt-js - JSON Web Tokens implemented in pure JavaScript
//
//authors:
//  Michael Hanson <mhanson@mozilla.com>
//
//Unless otherwise indicated, all code in this project is covered by the
//MPL1.1/GPL2.0/LGPL2.1 trilicense included at the end of this file.
//
pl.jwt = {
  // convert a base64url string to hex
  b64urlmap: "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_",
  b64urltohex: function(s) {
    var ret = ""
    var i;
    var k = 0; // b64 state, 0-3
    var slop;
    for(i = 0; i < s.length; ++i) {
      var v = b64urlmap.indexOf(s.charAt(i));
      if(v < 0) continue;
      if(k == 0) {
        ret += int2char(v >> 2);
        slop = v & 3;
        k = 1;
      }
      else if(k == 1) {
        ret += int2char((slop << 2) | (v >> 4));
        slop = v & 0xf;
        k = 2;
      }
      else if(k == 2) {
        ret += int2char(slop);
        ret += int2char(v >> 2);
        slop = v & 3;
        k = 3;
      }
      else {
        ret += int2char((slop << 2) | (v >> 4));
        ret += int2char(v & 0xf);
        k = 0;
      }
    }
    if(k == 1)
      ret += int2char(slop << 2);
    return ret;
  },
  base64urlencode: function(arg) {
    var s = window.btoa(arg); // Standard base64 encoder
    s = s.split('=')[0]; // Remove any trailing '='s
    s = s.replace(/\+/g, '-'); // 62nd char of encoding
    s = s.replace(/\//g, '_'); // 63rd char of encoding
    // TODO optimize this; we can do much better
    return s;
  },
  base64urldecode: function(arg) {
    var s = arg;
    s = s.replace(/-/g, '+'); // 62nd char of encoding
    s = s.replace(/_/g, '/'); // 63rd char of encoding
    switch (s.length % 4) // Pad with trailing '='s
    {
      case 0: break; // No pad chars in this case
      case 2: s += "=="; break; // Two pad chars
      case 3: s += "="; break; // One pad char
      default: throw new InputException("Illegal base64url string!");
    }
    return window.atob(s); // Standard base64 decoder
  },
  NoSuchAlgorithmException: function(message) {
    this.message = message;
    this.toString = function() { return "No such algorithm: "+this.message; };
  },
  NotImplementedException: function(message) {
    this.message = message;
    this.toString = function() { return "Not implemented: "+this.message; };
  },
  InputException: function(message) {
    this.message = message;
    this.toString = function() { return "Malformed input: "+this.message; };
  },
  jsonObj: function(strOrObject) {
    if (typeof strOrObject == "string") {
      return JSON.parse(strOrObject);
    }
    return strOrObject;
  },
  constructAlgorithm: function(jwtAlgStr, key) {
    if ("ES256" === jwtAlgStr) {
      throw new NotImplementedException("ECDSA-SHA256 not yet implemented");
    } else if ("ES384" === jwtAlgStr) {
      throw new NotImplementedException("ECDSA-SHA384 not yet implemented");
    } else if ("ES512" === jwtAlgStr) {
      throw new NotImplementedException("ECDSA-SHA512 not yet implemented");
    } else if ("HS256" === jwtAlgStr) {
      return new HMACAlgorithm("sha256", key);
    } else if ("HS384" === jwtAlgStr) {
      throw new NotImplementedException("HMAC-SHA384 not yet implemented");
    } else if ("HS512" === jwtAlgStr) {
      throw new NotImplementedException("HMAC-SHA512 not yet implemented");
    } else if ("RS256" === jwtAlgStr) {
      return new RSASHAAlgorithm("sha256", key);
    } else if ("RS384" === jwtAlgStr) {
      throw new NotImplementedException("RSA-SHA384 not yet implemented");
    } else if ("RS512" === jwtAlgStr) {
      throw new NotImplementedException("RSA-SHA512 not yet implemented");
    } else {
      throw new NoSuchAlgorithmException("Unknown algorithm: " + jwtAlgStr);
    }
  },
};

pl.jwt.WebToken = function(objectStr, algorithm) {
  this.objectStr = objectStr;
  this.pkAlgorithm = algorithm;
};

pl.jwt.WebToken.prototype = {
  serialize: function(key) {
    var header = pl.jwt.jsonObj(this.pkAlgorithm);
    var jwtAlgStr = header.alg;
    var algorithm = constructAlgorithm(jwtAlgStr, key);
    var algBytes = pl.jwt.base64urlencode(this.pkAlgorithm);
    var jsonBytes = pl.jwt.base64urlencode(this.objectStr);

    var stringToSign = algBytes + "." + jsonBytes;
    algorithm.update(stringToSign);
    var digestValue = algorithm.finalize();

    var signatureValue = algorithm.sign();
    return algBytes + "." + jsonBytes + "." + signatureValue;
  },
  verify: function(key) {
    var header = pl.jwt.jsonObj(this.pkAlgorithm);
    var jwtAlgStr = header.alg;
    var algorithm = constructAlgorithm(jwtAlgStr, key);
    algorithm.update(this.headerSegment + "." + this.payloadSegment);
    algorithm.finalize();
    return algorithm.verify(this.cryptoSegment);
  }
};

pl.jwt.WebTokenParser = {
  parse: function(input) {
    var parts = input.split(".");
    if (parts.length != 3) {
      throw new MalformedWebToken("Must have three parts");
    }
    var token = new pl.jwt.WebToken();
    token.headerSegment = parts[0];
    token.payloadSegment = parts[1];
    token.cryptoSegment = parts[2];

    token.pkAlgorithm = pl.jwt.base64urldecode(parts[0]);
    return token;
  }
};

pl.jwt.HMACAlgorithm = function(hash, key) {
  if (hash == "sha256") {
    this.hash = sjcl.hash.sha256;
  } else {
    throw new NoSuchAlgorithmException("HMAC does not support hash " + hash);
  }
  this.key = sjcl.codec.utf8String.toBits(key);
};

pl.jwt.HMACAlgorithm.prototype = {
  update: function(data) {
    this.data = data;
  },
  finalize: function() {
  },
  sign: function() {
    var hmac = new sjcl.misc.hmac(this.key, this.hash);
    var result = hmac.encrypt(this.data);
    return pl.jwt.base64urlencode(window.atob(sjcl.codec.base64.fromBits(result)));
  },
  verify: function(sig) {
    var hmac = new sjcl.misc.hmac(this.key, this.hash);
    var result = hmac.encrypt(this.data);
    return pl.jwt.base64urlencode(window.atob(sjcl.codec.base64.fromBits(result))) == sig; 
  }
};

pl.jwt.RSASHAAlgorithm = function(hash, keyPEM) {
  if (hash == "sha1") {
    this.hash = "sha1";
  } else if (hash == "sha256") {
    this.hash = "sha256";
  } else {
    throw new NoSuchAlgorithmException("JWT algorithm: " + hash);  
  }
  this.keyPEM = keyPEM;
};

pl.jwt.RSASHAAlgorithm.prototype = {
  update: function(data) {
    this.data = data;
  },
  finalize: function() {
  },
  sign: function() {
    var rsa = new RSAKey();
    rsa.readPrivateKeyFromPEMString(this.keyPEM);
    var hSig = rsa.signString(this.data, this.hash);
    return pl.jwt.base64urlencode(pl.jwt.base64urldecode(hex2b64(hSig))); // TODO replace this with hex2b64urlencode!
  },
  verify: function(sig) {
    var result = this.keyPEM.verifyString(this.data, pl.jwt.b64urltohex(sig));
    return result;
  }
};
