import aceCss from 'ace-css/css/ace.css';
import faCss from 'font-awesome/css/font-awesome.css';

// Require index.html so it gets copied to dist
import index from './index.html';

//var Elm = require('./Main.elm');
import Elm from './Main.elm';

// Auth0 authentication
import Auth0Lock from 'auth0-lock';

const cid = '0ZE6WlsV37O07xHsBD6dUikKBtw4wvVB';
const domain = 'cordula.auth0.com';
const options = {
  allowedConnections: ['google-oauth2', 'facebook']
};

const lock = new Auth0Lock(cid, domain, options);
const storedProfile = localStorage.getItem('profile');
const storedToken = localStorage.getItem('token');
const authData = storedProfile && storedToken ? { profile: JSON.parse(storedProfile), token: storedToken } : null;
const mountNode = document.getElementById('main');
const elmApp = Elm.Main.embed(mountNode, authData);
elmApp.ports.auth0showLock.subscribe(function(opts) {
  lock.show(opts);
});
lock.on("authenticated", function(authResult) {
  lock.getUserInfo(authResult.accessToken, function(err, profile) {
    const result = { err: null, ok: null };
    if (!err) {
      result.ok = { profile: profile, token: authResult.idToken };
      localStorage.setItem('profile', JSON.stringify(profile));
      localStorage.setItem('token', authResult.idToken);
    } else {
      result.err = err.details;
      // Ensure that optional fields are on the object
      result.err.name = result.err.name ? result.err.name : null;
      result.err.code = result.err.code ? result.err.code : null;
      result.err.statusCode = result.err.statusCode ? result.err.statusCode : null;
    }
    console.log(result);
    elmApp.ports.auth0authResult.send(result);
  });
});
elmApp.ports.auth0logout.subscribe(function(opts) {
  localStorage.removeItem('profile');
  localStorage.removeItem('token');
});
