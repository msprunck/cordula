module Requests.Commands exposing (..)

import Http
import Json.Decode as Decode exposing (andThen)
import Json.Decode.Pipeline exposing (required, hardcoded, optional, decode)
import Requests.Models exposing (Request, HttpMethod(..), InboundRequest, Params, ProxifiedRequest, Response)
import Requests.Messages exposing (..)


fetchAll : Cmd Msg
fetchAll =
    Http.get fetchAllUrl collectionDecoder
        |> Http.send OnFetchAll


fetchAllUrl : String
fetchAllUrl =
    "http://ks.sprunck.com:3000/request/"


collectionDecoder : Decode.Decoder (List Request)
collectionDecoder =
    Decode.list requestDecoder


requestDecoder : Decode.Decoder Request
requestDecoder =
    decode Request
        |> required "id" Decode.string
        |> required "name" Decode.string
        |> optional "description" (Decode.maybe Decode.string) Nothing
        |> required "in" inbDecoder
        |> required "proxy" proxyDecoder
        |> optional "response" (Decode.maybe responseDecoder) Nothing
        |> required "owner" Decode.string
        |> required "updated_at" Decode.string
        |> required "created_at" Decode.string

inbDecoder : Decode.Decoder InboundRequest
inbDecoder =
    decode InboundRequest
        |> required "path" Decode.string
        |> required "method" httpMethodDecoder


httpMethod : String -> Decode.Decoder HttpMethod
httpMethod method =
    case method of
        "get" -> Decode.succeed Get
        "post" -> Decode.succeed Post
        _ -> Decode.succeed Unknown

httpMethodDecoder : Decode.Decoder HttpMethod
httpMethodDecoder =
    andThen httpMethod Decode.string

proxyDecoder : Decode.Decoder ProxifiedRequest
proxyDecoder =
    decode ProxifiedRequest
        |> required "uri" Decode.string
        |> required "method" httpMethodDecoder
        |> optional "form-params" (Decode.maybe paramsDecoder) Nothing
        |> optional "query-params" (Decode.maybe paramsDecoder) Nothing
        |> optional "headers" (Decode.maybe (Decode.dict Decode.string)) Nothing
        |> optional "body" (Decode.maybe Decode.string) Nothing

paramsDecoder : Decode.Decoder Params
paramsDecoder =
    decode Params
        |> required "mergeValues" Decode.bool
        |> required "values" (Decode.dict Decode.string)

responseDecoder : Decode.Decoder Response
responseDecoder =
    decode Response
        |> required "body" Decode.string
        |> required "headers" (Decode.dict Decode.string)


