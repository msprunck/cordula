module Auth.Update exposing (..)

import Auth.Messages exposing (Msg(..))
import Auth.Models exposing (Model, AuthenticationResult)

update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        AuthenticationResult result ->
            let
                ( newState, error ) =
                    case result of
                        Ok user ->
                            ( Auth0.LoggedIn user, Nothing )

                        Err err ->
                            ( model.state, Just err )
            in
                ( { model | state = newState, lastError = error }, Cmd.none )

        ShowLogIn ->
            ( model, model.showLock Auth0.defaultOpts )

        LogOut ->
            ( { model | state = Auth0.LoggedOut }, model.logOut () )
