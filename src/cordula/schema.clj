(ns cordula.schema
  (:require [schema.core :as s]))

(s/defschema MapStr
  {(s/cond-pre s/Keyword s/Str) s/Str})

(s/defschema Params
  {:merge-values s/Bool
   :values MapStr})

(s/defschema InboundRequest
  {:path s/Str
   :method (s/enum "get" "post")})

(s/defschema ProxyfiedRequest
  {:uri s/Str
   :method (s/enum "get" "post")
   (s/optional-key :form-params) Params
   (s/optional-key :query-params) Params
   (s/optional-key :headers) MapStr
   (s/optional-key :body) s/Str})

(s/defschema Response
  {:body s/Str
   :headers MapStr})

(s/defschema Request
  {:id s/Str
   :name s/Str
   (s/optional-key :description) s/Str
   :in InboundRequest
   :proxy ProxyfiedRequest
   (s/optional-key :response) Response})

(s/defschema NewRequest (dissoc Request :id))
(s/defschema UpdatedRequest NewRequest)
