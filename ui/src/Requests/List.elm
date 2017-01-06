module Requests.List exposing (..)

import Html exposing (..)
import Html.Attributes exposing (class)
import Requests.Messages exposing (..)
import Requests.Models exposing (Request)


view : List Request -> Html Msg
view requests =
    div []
        [ nav requests
        , list requests
        ]


nav : List Request -> Html Msg
nav requests =
    div [ class "clearfix mb2 white bg-black" ]
        [ div [ class "left p2" ] [ text "Requests" ] ]


list : List Request -> Html Msg
list requests =
    div [ class "p2" ]
        [ table []
            [ thead []
                [ tr []
                    [ th [] [ text "Id" ]
                    , th [] [ text "Name" ]
                    , th [] [ text "Level" ]
                    , th [] [ text "Actions" ]
                    ]
                ]
            , tbody [] (List.map requestRow requests)
            ]
        ]


requestRow : Request -> Html Msg
requestRow request =
    tr []
        [ td [] [ text request.id ]
        , td [] [ text request.name ]
        , td [] [ text (toString request.description) ]
        , td []
            []
        ]
