module Models exposing (..)

import Auth.Models
import Requests.Models exposing (Request)


type alias Model =
    { requests : List Request
    , authModel : Auth.Models.Model
    }


initialModel : Auth.Models.Model -> Model
initialModel authModel=
    { requests = []
    , authModel = authModel
    }
