(ns clelte.compiler
  "Clelte: Clojure to Svelte Compiler
  
   Compiles Clojure code to Svelte components, enabling
   functional programming with reactive UI components."
  (:require [clojure.string :as str]
            [clojure.walk :as walk]))

;; ═══════════════════════════════════════════════════════════
;; CLELTE COMPILER CORE
;; ═══════════════════════════════════════════════════════════

(defn clojure-to-svelte-component
  "Convert Clojure component definition to Svelte component"
  [clojure-component]
  (let [{:keys [name props state methods template]} clojure-component]
    (str "<script>
  " (when state
      (str "let " (str/join ", " (map #(str (name %) " = " (pr-str %)) state)) ";"))
  "
  " (when methods
      (str/join "\n  " (map (fn [[method-name method-body]]
                              (str "function " (name method-name) "() {\n    " method-body "\n  }"))
                            methods)))
  "
</script>

" template "

<style>
  /* Component styles */
</style>")))

(defn compile-clelte-component
  "Compile a Clojure component to Svelte"
  [component-name clojure-code]
  (let [parsed-component (read-string clojure-code)
        svelte-code (clojure-to-svelte-component parsed-component)]
    {:name component-name
     :svelte-code svelte-code
     :compiled-at (java.time.Instant/now)}))

;; ═══════════════════════════════════════════════════════════
;; GRAINTHRIFT CLELTE COMPONENTS
;; ═══════════════════════════════════════════════════════════

(def grainthrift-app-component
  "Main GrainThrift application component in Clojure"
  {:name "GrainThriftApp"
   :props {:theme "light" :icp-price 0}
   :state {:products [] :cart [] :theme "light" :icp-price 0}
   :methods {:toggle-theme "theme = theme === 'light' ? 'dark' : 'light'"
             :add-to-cart "cart = [...cart, product]"
             :update-icp-price "icpPrice = await fetchICPPrice()"}
   :template "<div class=\"app {theme}\">
  <header class=\"header\">
    <div class=\"logo\">🌾 GrainThrift</div>
    <div class=\"theme-toggle\">
      <button on:click={toggleTheme}>
        {theme === 'light' ? '🌙' : '☀️'}
      </button>
    </div>
    <div class=\"icp-price\">
      ICP: ${icpPrice.toFixed(2)} USD
    </div>
  </header>
  
  <main class=\"main\">
    <div class=\"products-grid\">
      {#each products as product}
        <ProductCard {product} on:add-to-cart={addToCart} />
      {/each}
    </div>
  </main>
</div>"})

(def product-card-component
  "Product card component in Clojure"
  {:name "ProductCard"
   :props {:product {}}
   :state {}
   :methods {:buy-product "dispatch('buy-product', {product})"}
   :template "<div class=\"product-card\">
  <img src={product.image} alt={product.name} />
  <div class=\"product-info\">
    <h3>{product.name}</h3>
    <div class=\"price\">
      <span class=\"icp-price\">{product.price} ICP</span>
      <span class=\"usd-price\">${(product.price * icpPrice).toFixed(2)} USD</span>
    </div>
    <button on:click={buyProduct}>Buy Now</button>
  </div>
</div>"})

;; ═══════════════════════════════════════════════════════════
;; ICP PRICE INDEXING
;; ═══════════════════════════════════════════════════════════

(defn fetch-icp-price
  "Fetch current ICP price from CoinGecko API"
  []
  (try
    (let [response (slurp "https://api.coingecko.com/api/v3/simple/price?ids=internet-computer&vs_currencies=usd")
          data (clojure.data.json/read-str response)]
      (get-in data ["internet-computer" "usd"]))
    (catch Exception e
      (println "Error fetching ICP price:" (.getMessage e))
      0.0)))

(defn create-price-indexer
  "Create a price indexer that updates ICP price every 30 seconds"
  []
  (let [price-atom (atom 0.0)]
    (future
      (while true
        (try
          (let [new-price (fetch-icp-price)]
            (reset! price-atom new-price)
            (println "ICP price updated:" new-price "USD"))
          (catch Exception e
            (println "Price update error:" (.getMessage e))))
        (Thread/sleep 30000))) ; 30 seconds
    price-atom))

;; ═══════════════════════════════════════════════════════════
;; CLELTE COMPILER MAIN
;; ═══════════════════════════════════════════════════════════

(defn compile-grainthrift-app
  "Compile GrainThrift app to Svelte"
  []
  (let [app-component (compile-clelte-component "GrainThriftApp" grainthrift-app-component)
        product-component (compile-clelte-component "ProductCard" product-card-component)]
    {:app app-component
     :product-card product-component
     :compiled-at (java.time.Instant/now)}))

(defn -main
  "Main entry point for Clelte compiler"
  [& args]
  (println "🌾 Clelte: Clojure to Svelte Compiler")
  (let [compiled-app (compile-grainthrift-app)]
    (println "✅ Compiled GrainThrift app to Svelte")
    (println "📁 App component:" (:name (:app compiled-app)))
    (println "📁 Product component:" (:name (:product-card compiled-app)))
    (println "🚀 Ready to integrate with ICP backend!")))
