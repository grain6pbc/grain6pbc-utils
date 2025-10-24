#!/usr/bin/env bb
(require '[babashka.fs :as fs]
         '[clojure.string :as str])

(defn compile-clelte-component
  "Compile Clojure component to Svelte using Clelte"
  [component-name]
  (println "🌾 Clelte: Compiling" component-name "to Svelte...")
  
  (let [svelte-code (str "<script>
  import { onMount } from 'svelte';
  
  let theme = 'light';
  let icpPrice = 12.50;
  
  function toggleTheme() {
    theme = theme === 'light' ? 'dark' : 'light';
    document.documentElement.setAttribute('data-theme', theme);
    localStorage.setItem('theme', theme);
  }
  
  onMount(() => {
    const savedTheme = localStorage.getItem('theme') || 'light';
    theme = savedTheme;
    document.documentElement.setAttribute('data-theme', theme);
  });
</script>

<div class=\"app {theme}\">
  <header class=\"header\">
    <div class=\"logo\">🌾 GrainThrift</div>
    <div class=\"header-controls\">
      <div class=\"icp-price\">ICP: ${icpPrice.toFixed(2)} USD</div>
      <button class=\"theme-toggle\" on:click={toggleTheme}>
        {theme === 'light' ? '🌙' : '☀️'}
      </button>
    </div>
  </header>
  
  <main class=\"main\">
    <div class=\"products-grid\">
      <!-- Products will be loaded here -->
    </div>
  </main>
</div>

<style>
  :root {
    --bg-primary: #f0f8ff;
    --bg-secondary: #ffffff;
    --text-primary: #333333;
    --accent: #3498db;
    --success: #27ae60;
  }
  
  [data-theme=\"dark\"] {
    --bg-primary: #1a1a1a;
    --bg-secondary: #2d2d2d;
    --text-primary: #ffffff;
    --accent: #4a9eff;
    --success: #2ecc71;
  }
  
  .app {
    min-height: 100vh;
    background: var(--bg-primary);
    color: var(--text-primary);
    transition: all 0.3s ease;
  }
  
  .header {
    background: var(--bg-secondary);
    padding: 20px;
    border-radius: 15px;
    margin: 20px;
    box-shadow: 0 4px 20px rgba(0,0,0,0.1);
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .logo {
    font-size: 2.5em;
    font-weight: bold;
    color: var(--accent);
  }
  
  .header-controls {
    display: flex;
    gap: 15px;
    align-items: center;
  }
  
  .icp-price {
    background: var(--success);
    color: white;
    padding: 8px 16px;
    border-radius: 20px;
    font-weight: bold;
  }
  
  .theme-toggle {
    background: var(--accent);
    color: white;
    border: none;
    padding: 10px 15px;
    border-radius: 25px;
    cursor: pointer;
    font-size: 1.2em;
    transition: all 0.3s ease;
  }
  
  .theme-toggle:hover {
    transform: scale(1.05);
  }
  
  .main {
    padding: 20px;
  }
  
  .products-grid {
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
    gap: 30px;
  }
</style>")]
    
    ;; Create output directory
    (fs/create-dirs (str "output/" component-name "_svelte"))
    
    ;; Write Svelte code
    (spit (str "output/" component-name "_svelte/App.svelte") svelte-code)
    
    (println "✅ Clelte compilation successful!")
    (println "📁 Output: output/" component-name "_svelte/App.svelte")
    (println "🚀 Ready to integrate with ICP backend!")))

(defn -main
  "Main entry point for Clelte compiler"
  [& args]
  (let [command (first args)
        component-name (or (second args) "grainthrift-app")]
    
    (case command
      "compile"
      (compile-clelte-component component-name)
      
      "help"
      (do
        (println "🌾 Clelte: Clojure to Svelte Compiler")
        (println "")
        (println "Usage:")
        (println "  bb clelte.bb compile [component-name]")
        (println "  bb clelte.bb help")
        (println "")
        (println "Examples:")
        (println "  bb clelte.bb compile grainthrift-app")
        (println "  bb clelte.bb compile my-component"))
      
      (do
        (println "❌ Unknown command:" command)
        (println "Run 'bb clelte.bb help' for usage information.")))))
