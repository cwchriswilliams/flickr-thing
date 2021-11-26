(ns flickr-fetcher.lib.clj-helpers
  "Contains helpers for common clj functions")

(defn maybe-continue
  "If the predicate passes, calls f on input, otherwise returns input
  Arguments:
    - predicate to validate against input
    - input to validate and perform function on
    - function to perform if prdicate passes
  Returns:
    - input if predicate fails, otherwise (f input)
  
  This is designed for usage in piplines to early abort pipelines if required:
    (let [may-f (partial maybe-continue string?)]
      (-> input
        (may-f f1)
        (may-f f2)
        (may-f f3)))"
  [predicate input f]
  (if (predicate input)
    (f input)
    input))
