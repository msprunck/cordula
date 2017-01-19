module Update exposing (..)

import Auth.Update
import Messages exposing (Msg(..))
import Models exposing (Model)
import Requests.Update


update : Msg -> Model -> ( Model, Cmd Msg )
update msg model =
    case msg of
        RequestsMsg subMsg ->
            let
                ( updatedRequests, cmd ) =
                    Requests.Update.update subMsg model.requests
            in
                ( { model | requests = updatedRequests }, Cmd.map RequestsMsg cmd )
        AuthenticationMsg subMsg ->
            let
                ( updatedAuthModel, cmd) =
                    Auth.Update.update subMsg model.authModel
            in
                ( {model | authModel = updatedAuthModel}, Cmd.map AuthenticationMsg cmd )

