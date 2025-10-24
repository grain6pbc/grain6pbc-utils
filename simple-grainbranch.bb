#!/usr/bin/env bb
(require '[babashka.process :refer [shell]]
         '[clojure.string :as str])

(defn get-current-graintime []
  (let [now (java.time.Instant/now)
        graintime (str "12025-10-24-" (.format (java.time.format.DateTimeFormatter/ofPattern "HHmm") 
                                                (java.time.ZonedDateTime/ofInstant now (java.time.ZoneId/of "America/Los_Angeles"))))]
    graintime))

(defn create-grainbranch [description]
  (let [graintime (get-current-graintime)
        clean-description (-> description
                              (str/replace #"[^a-zA-Z0-9\s-]" "")
                              (str/replace #"\s+" "-")
                              (str/lower-case))
        grainbranch-name (str graintime "-" clean-description)]
    
    (println "🌾 Creating grainbranch:" grainbranch-name)
    
    ;; Create grainbranch metadata
    (spit "grainbranch.md" 
          (str "# Grainbranch: " grainbranch-name "\n\n"
               "**Description**: " description "\n"
               "**Created**: " (java.time.Instant/now) "\n"
               "**Graintime**: " graintime "\n\n"
               "This grainbranch represents an immutable version of the grain6pbc-utils repository.\n\n"
               "## Utilities Included\n\n"
               "- **Clelte**: Clojure to Svelte compiler\n"
               "- **Clotoko**: Clojure to Motoko compiler for ICP\n"
               "- **Poshmark Scraper**: Real product data for GrainThrift\n"
               "- **GrainDaemon**: Repository synchronization daemon\n"
               "- **GrainMode**: AI voice mode management (Trish/Glow)\n"))
    
    ;; Create and push grainbranch
    (shell "git" "checkout" "-b" grainbranch-name)
    (shell "git" "add" "grainbranch.md")
    (shell "git" "commit" "-m" (str "🌾 Create grainbranch: " grainbranch-name))
    (shell "git" "push" "origin" grainbranch-name)
    
    ;; Set as default branch
    (shell "gh" "api" "repos/grain6pbc/grain6pbc-utils" "--method" "PATCH" 
           "--field" (str "default_branch=" grainbranch-name))
    
    (println "✅ Created grainbranch:" grainbranch-name)
    (println "📁 URL: https://github.com/grain6pbc/grain6pbc-utils/tree/" grainbranch-name)
    
    grainbranch-name))

(defn -main [& args]
  (let [description (or (first args) "Grain6PBC Utilities Collection")]
    (create-grainbranch description)))
