module Requests.Models exposing (..)

import Dict

type alias RequestId =
    String

type HttpMethod = Get | Post | Unknown

type alias InboundRequest =
    { path : String
    , method : HttpMethod
    }

type alias Params =
    { mergeValues : Bool
    , values : (Dict.Dict String String)
    }

type alias ProxifiedRequest =
    { uri : String
    , method : HttpMethod
    , formParams : Maybe Params
    , queryParams : Maybe Params
    , headers : Maybe (Dict.Dict String String)
    , body : Maybe String
    }

type alias Response =
    { body : String
    , headers : (Dict.Dict String String)
    }

type alias Request =
    { id : RequestId
    , name : String
    , description : Maybe String
    , inb : InboundRequest
    , proxy : ProxifiedRequest
    , response : Maybe Response
    , owner : String
    , updated_at : String
    , created_at : String
    }

