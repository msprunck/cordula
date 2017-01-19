module Requests.Messages exposing (..)


import Http
import Requests.Models exposing (Request)


type Msg
    = OnFetchAll (Result Http.Error (List Request))
