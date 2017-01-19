module Auth.Commands exposing (..)

import Auth.Models exposing(Options)
import Ports exposing(..)


defaultOpts : Options
defaultOpts =
    {}

showLock: Options -> Cmd msg
showLock options =
    auth0showLock options

logOut: () -> Cmd msg
logOut =
    auth0logout
