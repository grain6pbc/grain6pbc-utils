#!/usr/bin/env bb
(require '[babashka.process :refer [shell]]
         '[clojure.string :as str])

(defn get-current-graintime []
  (let [now (java.time.Instant/now)
        graintime (str "12025-10-24-" (.format (java.time.format.DateTimeFormatter/ofPattern "HHmm") 
                                                (java.time.ZonedDateTime/ofInstant now (java.time.ZoneId/of "America/Los_Angeles"))))]
    graintime))

(defn create-grainbranch-for-repo [repo-name description]
  (println "🌾 Creating grainbranch for" repo-name "...")
  
  (let [graintime (get-current-graintime)
        clean-description (-> description
                              (str/replace #"[^a-zA-Z0-9\s-]" "")
                              (str/replace #"\s+" "-")
                              (str/lower-case))
        grainbranch-name (str graintime "-" clean-description)]
    
    (try
      ;; Clone repository temporarily
      (shell {:dir "/tmp"} "git" "clone" (str "https://github.com/grain6pbc/" repo-name) (str "temp-" repo-name))
      
      (let [temp-dir (str "/tmp/temp-" repo-name)]
        ;; Create grainbranch
        (shell {:dir temp-dir} "git" "checkout" "-b" grainbranch-name)
        
        ;; Create grainbranch metadata
        (spit (str temp-dir "/grainbranch.md") 
              (str "# Grainbranch: " grainbranch-name "\n\n"
                   "**Repository**: " repo-name "\n"
                   "**Description**: " description "\n"
                   "**Created**: " (java.time.Instant/now) "\n"
                   "**Graintime**: " graintime "\n\n"
                   "This grainbranch represents an immutable version of the " repo-name " repository.\n"
                   "It serves as a stable reference point for documentation, courses, and deployment.\n\n"
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
        
        ;; Commit and push grainbranch
        (shell {:dir temp-dir} "git" "add" "grainbranch.md")
        (shell {:dir temp-dir} "git" "commit" "-m" (str "🌾 Create grainbranch: " grainbranch-name))
        (shell {:dir temp-dir} "git" "push" "origin" grainbranch-name)
        
        ;; Set as default branch
        (shell "gh" "api" (str "repos/grain6pbc/" repo-name) "--method" "PATCH" 
               "--field" (str "default_branch=" grainbranch-name))
        
        ;; Update repository description
        (let [grainbranch-url (str "https://github.com/grain6pbc/" repo-name "/tree/" grainbranch-name)]
          (shell "gh" "api" (str "repos/grain6pbc/" repo-name) "--method" "PATCH"
                 "--field" (str "description=" description " | Grainbranch: " grainbranch-url)))
        
        ;; Clean up
        (shell {:dir "/tmp"} "rm" "-rf" (str "temp-" repo-name))
        
        (println "✅ Created grainbranch:" grainbranch-name)
        (println "📁 URL: https://github.com/grain6pbc/" repo-name "/tree/" grainbranch-name)
        
        {:repo repo-name :grainbranch grainbranch-name :success true})
      
      (catch Exception e
        (println "❌ Error creating grainbranch for" repo-name ":" (.getMessage e))
        {:repo repo-name :error (.getMessage e) :success false}))))

(defn create-all-grainbranches []
  (println "🌾 Creating grainbranches for all grain6pbc repositories...")
  
  (let [repos [
        {:name "grain6" :description "Complete Grain Network Monorepo Template"}
        {:name "grainbarrel" :description "Grain Network Build System"}
        {:name "grainpbc" :description "Public Benefit Corporation Framework"}
        {:name "grainzsh" :description "Grain Network Zsh Configuration"}
        {:name "grainenvvars" :description "Environment Variables Management"}
        {:name "humble-social-client" :description "Humble UI Social Client"}
        {:name "clojure-dfinity" :description "Clojure DFINITY Integration"}
        {:name "grainsource-sway" :description "Sway Configuration Archive"}
        {:name "grainsource-gnome" :description "GNOME Configuration"}
        {:name "grainaltproteinproject" :description "Alternative Protein Project"}
        {:name "clojure-google-drive-mcp" :description "Google Drive MCP Bindings"}
        {:name "graindrive" :description "Clojure Google Drive Integration"}
        {:name "grainlexicon" :description "Documentation Vocabulary"}
        {:name "grainneovedic" :description "Neovedic Timestamp System"}
        {:name "grainwifi" :description "Dual-Wifi Connection Manager"}
        {:name "grain-nightlight" :description "GNOME Night Light Daemon"}
        {:name "grainicons" :description "Icon Management Library"}
        {:name "graincasks" :description "AppImage Package Manager"}
        {:name "graindisplay" :description "Universal Display Management"}
        {:name "graindroid" :description "Open-Hardware Android Phone"}
        {:name "graindaemon" :description "S6/SixOS Daemon Framework"}
        {:name "urbit-source" :description "Urbit Source Code"}
        {:name "grainnixos-qemu-ubuntu-framework16" :description "NixOS QEMU Template"}
        {:name "grainstore" :description "Verified Dependencies Management"}
        {:name "grainnetwork" :description "Main Grain Network Documentation"}
        {:name "grainsource" :description "Git-Compatible Decentralized VCS"}
        {:name "grainpack" :description "External GPU Jetpack"}
        {:name "graincamera" :description "AVIF-Compatible Mirrorless Camera"}
        {:name "grainwriter" :description "RAM-Only E-Ink Writing Device"}
        {:name "grainclay" :description "Networked Package Manager"}
        {:name "grainconv" :description "Universal Type Conversion System"}
        {:name "grainmusic" :description "Decentralized Music Streaming"}
        {:name "grainspace" :description "Unified Decentralized Platform"}
        {:name "grainweb" :description "Browser Git Explorer AI Atlas"}
        {:name "grain-metatypes" :description "Foundational Type Definitions"}
        {:name "clotoko" :description "Clojure-to-Motoko Transpiler"}
        {:name "clojure-icp" :description "ICP DFINITY Integration"}
        {:name "clojure-sixos" :description "SixOS Integration"}
        {:name "clojure-s6" :description "S6 Supervision Wrapper"}
        ]]
    
    (let [results (map (fn [repo] 
                         (create-grainbranch-for-repo (:name repo) (:description repo))) 
                       repos)]
      
      (println "\n🎉 Grainbranch creation complete!")
      (println "📊 Summary:")
      
      (let [successful (filter :success results)
            failed (filter #(not (:success %)) results)]
        
        (println "✅ Successful:" (count successful))
        (doseq [result successful]
          (println "  -" (:repo result) "→" (:grainbranch result)))
        
        (when (seq failed)
          (println "❌ Failed:" (count failed))
          (doseq [result failed]
            (println "  -" (:repo result) ":" (:error result))))
        
        (println "\n🌐 All grainbranches are now default branches with updated descriptions!")))))

(defn -main [& args]
  (create-all-grainbranches))
