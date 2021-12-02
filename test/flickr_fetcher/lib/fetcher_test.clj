(ns flickr-fetcher.lib.fetcher-test
  (:require [clojure.test :refer [deftest testing is]]
            [flickr-fetcher.lib.fetcher :as sut]))

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

(deftest extract-json-data-from-flickr-feed-test
  (testing "Returns the contents of jsonFlickrFeed(...) if found"
    (is (= "{\"field1\": 4}" (sut/extract-json-data-from-flickr-feed "jsonFlickrFeed({\"field1\": 4})")))
    (is (= "{\"field1\": 4, \"field2\": 5}" (sut/extract-json-data-from-flickr-feed "jsonFlickrFeed({\"field1\": 4, \"field2\": 5})")))
    (is (= "{\n\"field1\": 4\n}" (sut/extract-json-data-from-flickr-feed "jsonFlickrFeed({\n\"field1\": 4\n})"))))
  
  (testing "Returns emtpy string if jsonFlickrFeed(...) not found"
    (is (= "" (sut/extract-json-data-from-flickr-feed "")))
    (is (= "" (sut/extract-json-data-from-flickr-feed "jsonNotFlickrFeed({\"field1\": 4})")))
    (is (= "" (sut/extract-json-data-from-flickr-feed "{\"field1\": 4})")))
    (is (= "" (sut/extract-json-data-from-flickr-feed "jsonFlickrFeed({\"field1\": 4}")))))

(deftest get-image-urls-from-public-photo-json-data-test
  (testing "Returns empty collection for empty string"
    (is (empty? (sut/get-image-urls-from-public-photo-json-data ""))))
  (testing "Returns empty collection for invalid input"
    (is (empty? (sut/get-image-urls-from-public-photo-json-data "{}")))
    (is (empty? (sut/get-image-urls-from-public-photo-json-data "{\"items\": [{\"media\": {\"m\": \"url\"}}]}")))
    (is (empty? (sut/get-image-urls-from-public-photo-json-data "({\"items\": [{\"media\": {\"m\": \"url\"}}]})")))
    (is (empty? (sut/get-image-urls-from-public-photo-json-data "jsonFlickrFeed2({\"items\": [{\"media\": {\"m\": \"url\"}}]})"))))
  (testing "Returns the items->media->m elements for valid input"
    (is (= ["url"] (sut/get-image-urls-from-public-photo-json-data "jsonFlickrFeed({\"items\": [{\"media\": {\"m\": \"url\"}}]})")))
    (is (= ["url", "url2"] (sut/get-image-urls-from-public-photo-json-data "jsonFlickrFeed({\"items\": [{\"media\": {\"m\": \"url\"}}, {\"media\": {\"m\": \"url2\"}}]})")))))


(deftest get-resize-fn-test
  (testing "Returns :force-resize if :height and :width are provided and :maintain-ratio? is false"
    (is (= :force-resize (sut/get-resize-fn {:height 50 :width 32 :maintain-ratio? false}))))
  (testing "Returns :resize-xy if :height and :width are provided and :maintain-ratio? is not false"
    (is (= :resize-xy (sut/get-resize-fn {:height 50 :width 32 :maintain-ratio? true})))
    (is (= :resize-xy (sut/get-resize-fn {:height 50 :width 32}))))
  (testing "Returns :resize-x if :width is provided and :height is not"
    (is (= :resize-x (sut/get-resize-fn {:width 32 :maintain-ratio? true})))
    (is (= :resize-x (sut/get-resize-fn {:width 32}))))
  (testing "Returns :resize-y if :height is provided and :width is not"
    (is (= :resize-y (sut/get-resize-fn {:height 32 :maintain-ratio? true})))
    (is (= :resize-y (sut/get-resize-fn {:height 32}))))
  (testing "Throws exception if neither :height nor :width provided"
    (is (thrown? Exception (sut/get-resize-fn {::maintain-ratio? true})))
    (is (thrown? Exception (sut/get-resize-fn {::maintain-ratio? false})))
    (is (thrown? Exception (sut/get-resize-fn {})))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
; Integration Tests
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;

(deftest ^:integration public-photo-entries-selector-integration-test
  (testing "loads the correct entries from the example-data"
    (is (= 
    ["https://live.staticflickr.com/65535/51709435827_42474e6acb_m.jpg"
     "https://live.staticflickr.com/65535/51709436337_84ef6fd85d_m.jpg"
     "https://live.staticflickr.com/65535/51709436347_9a7591b6ef_m.jpg"
     "https://live.staticflickr.com/65535/51709436442_b50d948867_m.jpg"
     "https://live.staticflickr.com/65535/51709436497_17db77be89_m.jpg"
     "https://live.staticflickr.com/65535/51709436507_546d9a311f_m.jpg"
     "https://live.staticflickr.com/65535/51710221256_4a24a5b003_m.jpg"
     "https://live.staticflickr.com/65535/51710221521_45ef50e554_m.jpg"
     "https://live.staticflickr.com/65535/51710221636_cbb2f91910_m.jpg"
     "https://live.staticflickr.com/65535/51710221646_654448df21_m.jpg"
     "https://live.staticflickr.com/65535/51710499193_538876cdf4_m.jpg"
     "https://live.staticflickr.com/65535/51710499273_2d6a8b2ce1_m.jpg"
     "https://live.staticflickr.com/65535/51710499583_337e42d308_m.jpg"
     "https://live.staticflickr.com/65535/51710900239_08a45f6670_m.jpg"
     "https://live.staticflickr.com/65535/51710900519_c9fd117ee0_m.jpg"
     "https://live.staticflickr.com/65535/51710900524_efa1bc19b4_m.jpg"
     "https://live.staticflickr.com/65535/51711104555_c049e6b158_m.jpg"
     "https://live.staticflickr.com/65535/51711104675_be0147044c_m.jpg"
     "https://live.staticflickr.com/65535/51711104710_c1f6c0c8eb_m.jpg"
     "https://live.staticflickr.com/65535/51711104890_b38004cd1b_m.jpg"] 
     (sut/get-image-urls-from-public-photo-json-data (slurp "test/flickr_fetcher/data/example.json"))))))