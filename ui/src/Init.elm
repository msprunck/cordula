module Init exposing (..)

import Auth.Init
import Auth.Models
import Messages exposing (Msg)
import Models exposing (Model, initialModel)


init : Maybe Auth.Models.LoggedInUser -> ( Model, Cmd Msg )
init initialUser =
    let
        currentAuthModel =
            (Auth.Init.init initialUser)
    in
        ( initialModel currentAuthModel, Cmd.none )
