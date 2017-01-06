'use strict';

require('ace-css/css/ace.css');
require('font-awesome/css/font-awesome.css');

// Require index.html so it gets copied to dist
require('./index.html');

var Elm = require('./Main.elm');
var mountNode = document.getElementById('main');

// Auth0 authentication
var Auth0Lock = require('auth0-lock');

var lock = new Auth0Lock('0ZE6WlsV37O07xHsBD6dUikKBtw4wvVB', 'cordula.auth0.com');
var storedProfile = localStorage.getItem('profile');
var storedToken = localStorage.getItem('token');
var authData = storedProfile && storedToken ? { profile: JSON.parse(storedProfile), token: storedToken } : null;
var elmApp = Elm.Main.embed(mountNode, authData);
elmApp.ports.auth0showLock.subscribe(function(opts) {
  opts.connections = ['Username-Password-Authentication'];
  lock.show(opts, function(err, profile, token) {
    var result = { err: null, ok: null };
    if (!err) {
      result.ok = { profile: profile, token: token };
      localStorage.setItem('profile', JSON.stringify(profile));
      localStorage.setItem('token', token);
    } else {
      result.err = err.details;
      // Ensure that optional fields are on the object
      result.err.name = result.err.name ? result.err.name : null;
      result.err.code = result.err.code ? result.err.code : null;
      result.err.statusCode = result.err.statusCode ? result.err.statusCode : null;
    }
    elmApp.ports.auth0authResult.send(result);
  });
});
elmApp.ports.auth0logout.subscribe(function(opts) {
  localStorage.removeItem('profile');
  localStorage.removeItem('token');
});
