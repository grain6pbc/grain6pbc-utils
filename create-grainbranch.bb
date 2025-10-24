#!/usr/bin/env bb
(require '[babashka.process :refer [shell]]')
(require '[babashka.fs :as fs]
         '[clojure.string :as str])

(defn get-current-graintime
  "Get current graintime for grainbranch naming"
  []
  (let [now (java.time.Instant/now)
        graintime (str "12025-10-24-" (.format (java.time.format.DateTimeFormatter/ofPattern "HHmm") 
                                                (java.time.ZonedDateTime/ofInstant now (java.time.ZoneId/of "America/Los_Angeles"))))]
    graintime))

(defn create-grainbranch
  "Create grainbranch for current repository"
  [description]
  (let [graintime (get-current-graintime)
        clean-description (-> description
                              (str/replace #"[^a-zA-Z0-9\s-]" "")
                              (str/replace #"\s+" "-")
                              (str/lower-case)
                              (str/replace #"-+" "-")
                              (str/replace #"^-|-$" ""))
        grainbranch-name (str graintime "-" clean-description)]
    
    (println "🌾 Creating grainbranch:" grainbranch-name)
    
    ;; Create grainbranch metadata file
    (spit "grainbranch.md" 
          (str "# Grainbranch: " grainbranch-name "\n\n"
               "**Description**: " description "\n"
               "**Created**: " (java.time.Instant/now) "\n"
               "**Graintime**: " graintime "\n\n"
               "This grainbranch represents an immutable version of the grain6pbc-utils repository.\n"
               "It serves as a stable reference point for documentation, courses, and deployment.\n\n"
               "## Utilities Included\n\n"
               "- **Clelte**: Clojure to Svelte compiler\n"
               "- **Clotoko**: Clojure to Motoko compiler for ICP\n"
               "- **Poshmark Scraper**: Real product data for GrainThrift\n"
               "- **GrainDaemon**: Repository synchronization daemon\n"
               "- **GrainMode**: AI voice mode management (Trish/Glow)\n\n"
               "## Usage\n\n"
               "This grainbranch can be used for:\n"
               "- Documentation generation\n"
               "- Course material creation\n"
               "- Stable deployment references\n"
               "- Version pinning for dependencies\n\n"
               "## Integration\n\n"
               "This grainbranch integrates with:\n"
               "- Grain6PBC organization structure\n"
               "- GrainNetwork documentation system\n"
               "- GrainCourse educational platform\n"
               "- GrainDaemon deployment automation\n"))
    
    ;; Create and switch to grainbranch
    (shell "git" "checkout" "-b" grainbranch-name)
    (shell "git" "add" "grainbranch.md")
    (shell "git" "commit" "-m" (str "🌾 Create grainbranch: " grainbranch-name))
    (shell "git" "push" "origin" grainbranch-name)
    
    ;; Set as default branch
    (shell "gh" "api" "repos/grain6pbc/grain6pbc-utils" "--method" "PATCH" 
           "--field" (str "default_branch=" grainbranch-name))
    
    ;; Update repository description
    (let [grainbranch-url (str "https://github.com/grain6pbc/grain6pbc-utils/tree/" grainbranch-name)]
      (shell "gh" "api" "repos/grain6pbc/grain6pbc-utils" "--method" "PATCH"
             "--field" (str "description=Grain6PBC Utilities: Clelte (Clojure→Svelte), Clotoko (Clojure→Motoko), Poshmark Scraper, GrainDaemon, GrainMode | Grainbranch: " grainbranch-url)))
    
    (println "✅ Created grainbranch:" grainbranch-name)
    (println "📁 URL: https://github.com/grain6pbc/grain6pbc-utils/tree/" grainbranch-name)
    (println "🌐 Set as default branch with updated description!")
    
    grainbranch-name))

(defn -main
  "Main entry point for grainbranch creation"
  [& args]
  (let [description (or (first args) "Grain6PBC Utilities Collection")]
    (create-grainbranch description)))
