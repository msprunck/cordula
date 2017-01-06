module Auth.Init exposing(..)

import Auth.Models
import Auth.Messages exposing (Msg(..))


init : Maybe Auth.Models.LoggedInUser -> Auth.Models.Model
init initialData =
    let
        state = case initialData of
            Just user ->
                Auth.Models.LoggedIn user

            Nothing ->
                Auth.Models.LoggedOut
    in
        ( Auth.Models.Model state Nothing )
