module View exposing (..)

import Auth.Helpers
import Auth.Messages
import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Messages exposing (Msg(..))
import Models exposing (Model)
import Requests.List


view : Model -> Html Msg
view model =
    div []
        [ page model ]


page : Model -> Html Msg
page model =
    div [] [Html.map RequestsMsg (Requests.List.view model.requests),
                div [ class "container" ]
                [ div [ class "jumbotron text-center" ]
                      [ div []
                            (case Auth.Helpers.tryGetUserProfile model.authModel of
                                 Nothing ->
                                 [ p [] [ text "Please log in" ] ]

                                 Just user ->
                                 [ p [] [ img [ src user.picture ] [] ]
                                 , p [] [ text ("Hello, " ++ user.name ++ "!") ]
                                 ]
                            )
                      , p []
                          [ button
                                [ class "btn btn-primary"
                                , onClick
                                      (AuthenticationMsg
                                           (if Auth.Helpers.isLoggedIn model.authModel then
                                                Auth.Messages.LogOut
                                            else
                                                Auth.Messages.ShowLogIn
                                           )
                                      )
                                ]
                                [ text
                                      (if Auth.Helpers.isLoggedIn model.authModel then
                                           "Logout"
                                       else
                                           "Login"
                                      )
                                ]
                          ]
                      ]
                ]]
