(ns clotoko.compiler
  "Clotoko: Clojure to Motoko Compiler
  
   A bridge between Clojure's elegance and ICP's power.
   Compiles Clojure functions to Motoko canister code."
  (:require [clojure.string :as str]
            [clojure.walk :as walk]
            [clojure.data.json :as json]
            [clojure.java.io :as io]
            [clojure.tools.reader :as reader]
            [clojure.tools.reader.reader-types :as reader-types]))

;; ═══════════════════════════════════════════════════════════
;; CLOTOKO COMPILER CORE
;; ═══════════════════════════════════════════════════════════

(defn clojure-to-motoko-type
  "Convert Clojure types to Motoko types"
  [clojure-type]
  (case clojure-type
    :string "Text"
    :number "Nat"
    :boolean "Bool"
    :vector "Array"
    :map "Record"
    :keyword "Text"
    :symbol "Text"
    "Text")) ; Default to Text

(defn clojure-to-motoko-value
  "Convert Clojure values to Motoko syntax"
  [value]
  (cond
    (string? value) (str "\"" value "\"")
    (number? value) (str value)
    (boolean? value) (if value "true" "false")
    (keyword? value) (str "\"" (name value) "\"")
    (vector? value) (str "[" (str/join ", " (map clojure-to-motoko-value value)) "]")
    (map? value) (str "{" (str/join ", " (map (fn [[k v]] (str (clojure-to-motoko-value k) " = " (clojure-to-motoko-value v))) value)) "}")
    :else (str "\"" (str value) "\"")))

(defn compile-clojure-fn
  "Compile a Clojure function to Motoko syntax"
  [fn-symbol fn-body]
  (let [fn-name (name fn-symbol)
        motoko-name (str/replace fn-name "-" "_")]
    (str "public query func " motoko-name "() : Text {\n"
         "    " (clojure-to-motoko-value fn-body) "\n"
         "}")))

(defn compile-clojure-defn
  "Compile a Clojure defn to Motoko function"
  [form]
  (let [[_ fn-name & body] form
        fn-body (last body)]
    (compile-clojure-fn fn-name fn-body)))

(defn compile-clojure-form
  "Compile any Clojure form to Motoko"
  [form]
  (cond
    (and (list? form) (= (first form) 'defn))
    (compile-clojure-defn form)
    
    (and (list? form) (= (first form) 'def))
    (let [[_ var-name value] form]
      (str "let " (name var-name) " = " (clojure-to-motoko-value value) ";"))
    
    (string? form)
    (clojure-to-motoko-value form)
    
    :else
    (str "// " (str form))))

;; ═══════════════════════════════════════════════════════════
;; CANISTER GENERATION
;; ═══════════════════════════════════════════════════════════

(defn generate-motoko-canister
  "Generate complete Motoko canister from Clojure code"
  [clojure-code canister-name]
  (let [forms (reader/read-string (str "(" clojure-code ")"))
        compiled-functions (map compile-clojure-form forms)]
    (str "import Text \"mo:base/Text\";
import Time \"mo:base/Time\";
import Principal \"mo:base/Principal\";

actor " canister-name " {
    " (str/join "\n\n    " compiled-functions) "
}")))

(defn generate-dfx-json
  "Generate dfx.json configuration for the canister"
  [canister-name]
  (json/write-str
    {:version 1
     :canisters {canister-name {:main (str "src/" canister-name "_motoko/main.mo")
                                :type "motoko"}}
     :defaults {:build {:args ""
                        :packtool ""}}
     :output_env_file ".env"
     :networks {:local {:bind "127.0.0.1:4943"
                        :type "ephemeral"}}}
    :escape-slash false))

;; ═══════════════════════════════════════════════════════════
;; FILE OPERATIONS
;; ═══════════════════════════════════════════════════════════

(defn compile-clojure-file
  "Compile a Clojure file to Motoko canister"
  [input-file output-dir canister-name]
  (let [clojure-code (slurp input-file)
        motoko-code (generate-motoko-canister clojure-code canister-name)
        output-file (io/file output-dir (str canister-name "_motoko/main.mo"))]
    
    ;; Create output directory
    (.mkdirs (.getParentFile output-file))
    
    ;; Write Motoko code
    (spit output-file motoko-code)
    
    ;; Generate dfx.json
    (spit (io/file output-dir "dfx.json") (generate-dfx-json canister-name))
    
    {:status :success
     :input-file input-file
     :output-file output-file
     :canister-name canister-name}))

(defn compile-project
  "Compile entire Clojure project to ICP canisters"
  [project-dir]
  (let [src-dir (io/file project-dir "src")
        output-dir (io/file project-dir "compiled")]
    
    (when (.exists src-dir)
      (doseq [file (file-seq src-dir)]
        (when (and (.isFile file) (.endsWith (.getName file) ".clj"))
          (let [canister-name (str/replace (.getName file) ".clj" "")]
            (compile-clojure-file file output-dir canister-name)))))))

;; ═══════════════════════════════════════════════════════════
;; COMMAND LINE INTERFACE
;; ═══════════════════════════════════════════════════════════

(defn -main
  "Main entry point for Clotoko compiler"
  [& args]
  (let [command (first args)
        input-file (second args)
        canister-name (nth args 2 "grain6-canister")]
    
    (case command
      "compile"
      (do
        (println "🌾 Clotoko: Compiling Clojure to Motoko...")
        (let [result (compile-clojure-file input-file "." canister-name)]
          (println "✅ Compilation successful!")
          (println "📁 Output:" (:output-file result))
          (println "🚀 Canister:" (:canister-name result))))
      
      "project"
      (do
        (println "🌾 Clotoko: Compiling project...")
        (compile-project (or input-file "."))
        (println "✅ Project compilation complete!"))
      
      "help"
      (do
        (println "🌾 Clotoko: Clojure to Motoko Compiler")
        (println "")
        (println "Usage:")
        (println "  bb clotoko compile <input.clj> [canister-name]")
        (println "  bb clotoko project [project-dir]")
        (println "  bb clotoko help")
        (println "")
        (println "Examples:")
        (println "  bb clotoko compile src/my-canister.clj my-canister")
        (println "  bb clotoko project ."))
      
      (do
        (println "❌ Unknown command:" command)
        (println "Run 'bb clotoko help' for usage information.")))))
