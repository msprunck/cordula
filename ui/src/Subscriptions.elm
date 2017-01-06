module Subscriptions exposing (..)

import Auth.Helpers
import Messages exposing (Msg(..))
import Ports exposing (auth0authResult)

subscriptions : a -> Sub Msg
subscriptions model =
    auth0authResult (Auth.Helpers.handleAuthResult >> AuthenticationMsg)
