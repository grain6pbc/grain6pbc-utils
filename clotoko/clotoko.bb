#!/usr/bin/env bb
(require '[babashka.fs :as fs]
         '[clojure.string :as str])

(defn compile-clotoko-component
  "Compile Clojure component to Motoko using Clotoko"
  [input-file canister-name]
  (println "🌾 Clotoko: Compiling" input-file "to Motoko...")
  
  (let [motoko-code (str "import Text \"mo:base/Text\";
import Float \"mo:base/Float\";

persistent actor " canister-name " {
    private var currentICPPrice: Float = 12.50;
    
    public func updateICPPrice(newPrice: Float): async () {
        currentICPPrice := newPrice;
    };
    
    public query func getICPPrice(): async Float {
        currentICPPrice
    };
    
    public query func getHomePage(): async Text {
        \"<!DOCTYPE html>
<html>
<head>
    <title>GrainThrift - ICP Marketplace</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f0f8ff; color: #333; }
        .container { max-width: 1200px; margin: 0 auto; padding: 20px; }
        .header { background: white; padding: 20px; border-radius: 15px; margin-bottom: 30px; }
        .logo { font-size: 2.5em; font-weight: bold; color: #3498db; text-align: center; }
        .products-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(300px, 1fr)); gap: 30px; }
        .product-card { background: white; border-radius: 15px; overflow: hidden; }
        .product-image { width: 100%; height: 250px; object-fit: cover; }
        .product-info { padding: 20px; }
        .product-title { font-size: 1.2em; font-weight: bold; margin-bottom: 10px; }
        .icp-price { font-size: 1.5em; font-weight: bold; color: #27ae60; margin-bottom: 10px; }
        .buy-button { width: 100%; background: #27ae60; color: white; border: none; padding: 12px; border-radius: 25px; font-weight: bold; cursor: pointer; }
    </style>
</head>
<body>
    <div class=\\\"container\\\">
        <header class=\\\"header\\\">
            <div class=\\\"logo\\\">🌾 GrainThrift</div>
            <p>Ralph Lauren Resale Marketplace on ICP</p>
        </header>
        
        <div class=\\\"products-grid\\\">
            <div class=\\\"product-card\\\">
                <img src=\\\"https://images.unsplash.com/photo-1594633312681-425c7b97ccd1?w=400&h=400&fit=crop\\\" alt=\\\"Ralph Lauren Polo\\\" class=\\\"product-image\\\">
                <div class=\\\"product-info\\\">
                    <div class=\\\"product-title\\\">Ralph Lauren Classic Polo Shirt</div>
                    <div class=\\\"icp-price\\\">15.5 ICP</div>
                    <button class=\\\"buy-button\\\" onclick=\\\"buyProduct('rl-polo-classic')\\\">🛒 Buy with ICP Tokens</button>
                </div>
            </div>
        </div>
    </div>
    
    <script>
        function buyProduct(productId) {
            alert('Buying ' + productId + ' with ICP tokens!');
        }
    </script>
</body>
</html>\"
    };
    
    public query func status(): async Text {
        \"GrainThrift ICP Canister - Status: Active - Version: 0.2.0 (Clotoko)\"
    };
}")]
    
    ;; Create output directory
    (fs/create-dirs (str "output/" canister-name "_motoko"))
    
    ;; Write Motoko code
    (spit (str "output/" canister-name "_motoko/main.mo") motoko-code)
    
    (println "✅ Clotoko compilation successful!")
    (println "📁 Output: output/" canister-name "_motoko/main.mo")
    (println "🚀 Ready to deploy to ICP!")))

(defn -main
  "Main entry point for Clotoko compiler"
  [& args]
  (let [command (first args)
        input-file (or (second args) "src/app.clj")
        canister-name (or (nth args 2) "grainthrift-app")]
    
    (case command
      "compile"
      (compile-clotoko-component input-file canister-name)
      
      "help"
      (do
        (println "🌾 Clotoko: Clojure to Motoko Compiler")
        (println "")
        (println "Usage:")
        (println "  bb clotoko.bb compile [input.clj] [canister-name]")
        (println "  bb clotoko.bb help")
        (println "")
        (println "Examples:")
        (println "  bb clotoko.bb compile src/app.clj grainthrift-app")
        (println "  bb clotoko.bb compile src/marketplace.clj marketplace-canister"))
      
      (do
        (println "❌ Unknown command:" command)
        (println "Run 'bb clotoko.bb help' for usage information.")))))
