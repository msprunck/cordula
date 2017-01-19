module Auth.Update exposing (..)

import Auth.Commands exposing (..)
import Auth.Messages exposing (Msg(..))
import Auth.Models exposing (Model, AuthenticationResult, AuthenticationState(..))


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        AuthenticationResult result ->
            let
                ( newState, error ) =
                    case result of
                        Ok user ->
                            ( LoggedIn user, Nothing )

                        Err err ->
                            ( model.state, Just err )
            in
                ( { model | state = newState, lastError = error }, Cmd.none )

        ShowLogIn ->
            ( model, showLock defaultOpts )

        LogOut ->
            ( { model | state = LoggedOut }, logOut () )
