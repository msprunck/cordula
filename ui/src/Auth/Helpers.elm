module Auth.Helpers exposing (..)

import Auth.Messages exposing (Msg(..))
import Auth.Models exposing (AuthenticationResult, RawAuthenticationResult)

handleAuthResult : RawAuthenticationResult -> Msg
handleAuthResult =
    mapResult >> AuthenticationResult

mapResult : RawAuthenticationResult -> AuthenticationResult
mapResult result =
    case ( result.err, result.ok ) of
        ( Just msg, _ ) ->
            Err msg

        ( Nothing, Nothing ) ->
            Err { name = Nothing, code = Nothing, statusCode = Nothing, description = "No information was received from the authentication provider" }

        ( Nothing, Just user ) ->
            Ok user
