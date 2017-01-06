module Messages exposing (..)

import Auth.Messages
import Requests.Messages


type Msg
    = RequestsMsg Requests.Messages.Msg
    | AuthenticationMsg Auth.Messages.Msg
