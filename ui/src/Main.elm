module Main exposing (..)

import Auth.Models
import Html exposing (Html, div, text, programWithFlags)
import Init exposing (init)
import Messages exposing (Msg)
import Models exposing (Model)
import Subscriptions exposing (subscriptions)
import Update exposing (update)
import View exposing (view)


-- MAIN

main : Program (Maybe Auth.Models.LoggedInUser) Model Msg
main =
    programWithFlags
        { init = init
        , view = view
        , update = update
        , subscriptions = subscriptions
        }
