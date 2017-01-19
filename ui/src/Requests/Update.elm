module Requests.Update exposing (..)

import Requests.Messages exposing (Msg(..))
import Requests.Models exposing (Request)


update : Msg -> List Request -> ( List Request, Cmd Msg )
update message requests =
    case message of
        OnFetchAll (Ok newRequests) ->
            ( newRequests, Cmd.none )

        OnFetchAll (Err error) ->
            ( requests, Cmd.none )
