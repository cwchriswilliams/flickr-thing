(ns flickr-fetcher.lib.fetcher-test
  (:require [clojure.test :refer [deftest testing is]]
            [flickr-fetcher.lib.fetcher :as sut]
            [clojure.string :as string]))

(deftest get-file-name-from-url-test
  (testing "Returns the last element of the url as the filename including extension"
    (is (= "the-file.jpg" (sut/get-file-name-from-url "http://example.com/the-file.jpg")))
    (is (= "the-file2.jpg" (sut/get-file-name-from-url "http://example.com/the-file2.jpg")))
    (is (= "the-file.jpg" (sut/get-file-name-from-url "/home/user/the-file.jpg")))
    (is (= "the-file.jpg" (sut/get-file-name-from-url "www.example.com/the-file.jpg")))
))

(deftest build-download-path-test
  (testing "Returns the folder path with the file path appended"
    (is (= "/home/user/the-file.jpg" (sut/build-download-path "the-file.jpg" "/home/user/")))
    (is (= "/home/user/the-file2.jpg" (sut/build-download-path "the-file2.jpg" "/home/user/")))
    (is (= "/home/user2/the-file.jpg" (sut/build-download-path "the-file.jpg" "/home/user2/")))
))

(def valid-entry-html "<entry><link type=\"image/jpeg\" /></entry>")
(def valid-entry-hickory {:attrs {:type "image/jpeg"}, :content nil :tag :link :type :element})

(deftest public-photo-entries-selector-test
  (testing "Returns empty collection when no entries found"
    (is (empty? (sut/public-photo-entries-selector (sut/string->hickory ""))))
    (is (empty? (sut/public-photo-entries-selector (sut/string->hickory "<html></html>"))))
    (is (empty? (sut/public-photo-entries-selector (sut/string->hickory "<entry></entry>"))))
    (is (empty? (sut/public-photo-entries-selector (sut/string->hickory "<entry><a /></entry>"))))
    (is (empty? (sut/public-photo-entries-selector (sut/string->hickory "<entry><a type=\"image/png\" /></entry>"))))
  )

  (testing "Finds single entry when single entry available"
    (is (= [valid-entry-hickory] (sut/public-photo-entries-selector (sut/string->hickory "<entry><link type=\"image/jpeg\" /></entry>"))))
  )

  (testing "Finds multiple entries when multiple entries available"
    (is (= (vec (repeat 3 valid-entry-hickory)) 
      (sut/public-photo-entries-selector 
        (sut/string->hickory (string/join (repeat 3 valid-entry-html))))))
    (is (= (vec (repeat 9 valid-entry-hickory))
           (sut/public-photo-entries-selector
            (sut/string->hickory (string/join (repeat 9 valid-entry-html))))))
    )
)

(deftest get-href-attribute-from-hickory-entry-test
  (testing "Returns nil when contains no href attribute"
    (is (nil? (sut/get-href-attribute-from-hickory-entry {})))
    (is (nil? (sut/get-href-attribute-from-hickory-entry {:attrs {}})))
    (is (nil? (sut/get-href-attribute-from-hickory-entry {:href {}})))
    (is (nil? (sut/get-href-attribute-from-hickory-entry {:attrs {:not-href ""}}))))
  (testing "Returns href attribute when available"
    (is (= "href-string" (sut/get-href-attribute-from-hickory-entry {:attrs {:href "href-string"}})))
    (is (= "href-string2" (sut/get-href-attribute-from-hickory-entry {:attrs {:href "href-string2"}})))
))

(deftest get-href-attribute-from-hickory-entries-test
  (testing "Returns empty collection for empty input"
    (is (empty? (sut/get-href-attribte-from-hickory-entries []))))
  (testing "Returns href attribute of each of input collection"
    (is (= ["href-string"] (sut/get-href-attribte-from-hickory-entries [{:attrs {:href "href-string"}}])))
    (is (= ["href-string" "href-string2"] (sut/get-href-attribte-from-hickory-entries [{:attrs {:href "href-string"}} {:attrs {:href "href-string2"}}])))
))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Integration Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest ^:integration public-photo-entries-selector-integration-test
  (testing "loads the correct entries from the example-data"
    (is (= 
    ["https://live.staticflickr.com/65535/51699200367_b0884059f9_b.jpg"
     "https://live.staticflickr.com/65535/51699200502_f099f98063_b.jpg"
     "https://live.staticflickr.com/65535/51699200627_da33f62621_b.jpg"
     "https://live.staticflickr.com/65535/51699200812_4bc2101c49_b.jpg"
     "https://live.staticflickr.com/65535/51699200877_b71322b4ca_b.jpg"
     "https://live.staticflickr.com/65535/51699201042_dc940e903b_b.jpg"
     "https://live.staticflickr.com/65535/51699996831_e87fa85605_b.jpg"
     "https://live.staticflickr.com/65535/51699997231_0484cd4ced_b.jpg"
     "https://live.staticflickr.com/65535/51699997296_3fa79b2cb3_b.jpg"
     "https://live.staticflickr.com/65535/51700274713_2227097c45_b.jpg"
     "https://live.staticflickr.com/65535/51700275493_01322db60b_b.jpg"
     "https://live.staticflickr.com/65535/51700670624_0e5c4b400a_b.jpg"
     "https://live.staticflickr.com/65535/51700670934_32ff9a925d_z.jpg"
     "https://live.staticflickr.com/65535/51700671049_d1dd768087_b.jpg"
     "https://live.staticflickr.com/65535/51700883570_f6e760a070_b.jpg"
     "https://live.staticflickr.com/65535/51700883580_115097c593_b.jpg"
     "https://live.staticflickr.com/65535/51700883865_ae3ec113e2_b.jpg"
     "https://live.staticflickr.com/65535/51700883870_4e893a5e54_b.jpg"
     "https://live.staticflickr.com/65535/51700883960_de5591fffb_b.jpg"
     "https://live.staticflickr.com/65535/51700884190_37c83812f7_b.jpg"] 
     (sut/get-image-urls-from-public-photo-atom-data (slurp "test/flickr_fetcher/data/example.xml"))))))