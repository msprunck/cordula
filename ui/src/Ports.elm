port module Ports exposing (..)

import Auth.Models

-- Auth0

port auth0showLock : Auth.Models.Options -> Cmd msg
port auth0authResult : (Auth.Models.RawAuthenticationResult -> msg) -> Sub msg
port auth0logout : () -> Cmd msg

