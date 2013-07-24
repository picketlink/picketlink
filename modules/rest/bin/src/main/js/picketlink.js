pl = {};

pl.createHttpRequest = function(mimeType) {
  if (window.XMLHttpRequest) {
    var req = new XMLHttpRequest();
    if (mimeType !== null && req.overrideMimeType) {
      req.overrideMimeType(mimeType);
    }
    return req;
  }
  else {
    return new ActiveXObject("Microsoft.XMLHTTP");
  }
};

pl.addUser = function(u) {

};

pl.addGroup = function() {

};

pl.addRole = function() {

};

pl.updateUser = function(u) {

};

pl.updateGroup = function(u) {

};

pl.updateRole = function(u) {

};

pl.deleteUser = function(id) {

};

pl.deleteGroup = function(id) {

};

pl.deleteRole = function(id) {

};

pl.createQuery = function() {

};
