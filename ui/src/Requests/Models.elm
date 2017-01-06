module Requests.Models exposing (..)

import Dict

type alias RequestId =
    String

type HttpMethod = Get | Post

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
    , formParams : Params
    , queryParams : Params
    , headers : (Dict.Dict String String)
    , body : String
    }

type alias Request =
    { id : RequestId
    , name : String
    , description : String
    , inb : InboundRequest
    , proxy : ProxifiedRequest
    , owner : String
    , updated_at : String
    , created_at : String
    }

