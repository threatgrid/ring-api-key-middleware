(ns ring-api-key-middleware.core-test
  (:require [clojure.test :refer :all]
            [ring-api-key-middleware.core :refer :all]))

(deftest get-api-key-test
  (testing "If recover correctly authorization header from a request"
    (is (= (get-api-key {:headers {"authorization" "apiKey foo"}})
           "foo")
        "the right API should be parsed correctly")
    (is (nil? (get-api-key {:headers {"authorization" "Bearer foo"}}))
        "Wrong header style shouldn't return any api-key")
    (is (nil? (get-api-key {:headers {"authorization" " apiKey foo"}}))
        "space at the begining of the header is forbidden")))

(deftest wrap-api-key-fn-test
  (testing "test the middleware"
    (let [request-with-known-auth {:headers {"authorization" "apiKey foo"}}
          request-with-unknown-auth {:headers {"authorization" "apiKey bar"}}
          request-with-other-auth {:headers {"authorization" "Bearer foo"}}
          request-with-no-auth {}

          check-is-foo (fn [x] (when (= x "foo") {:user "UserFoo"}))]
      (is (= ((wrap-api-key-fn identity check-is-foo)
              request-with-known-auth)
             (assoc request-with-known-auth
                    :api-key-infos {:user "UserFoo"}))
          "apiKey foo should provide the user UserFoo")

      (is (= (:status
              ((wrap-api-key-fn identity check-is-foo)
               request-with-unknown-auth))
             401)
          "bad API Key are refused")

      (is (= ((wrap-api-key-fn identity check-is-foo)
              request-with-other-auth)
             request-with-other-auth)
          "When not using apiKey Authorization kind, the middleware should let the request pass as-is to the handler")

      (is (= ((wrap-api-key-fn identity check-is-foo)
              request-with-no-auth)
             request-with-no-auth)
          "When no Authorization header is used the middleware should let the request pass as-is to the handler"))))
