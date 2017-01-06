module Auth.Messages exposing (..)

import Auth.Models exposing (AuthenticationResult)


type Msg
    = AuthenticationResult AuthenticationResult
    | ShowLogIn
    | LogOut
