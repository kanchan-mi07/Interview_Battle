package com.interviewarena.config;

import com.interviewarena.entity.Category;
import com.interviewarena.entity.Difficulty;
import com.interviewarena.entity.OptionChoice;
import com.interviewarena.entity.Question;
import com.interviewarena.repository.QuestionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

import static com.interviewarena.entity.Category.*;
import static com.interviewarena.entity.Difficulty.*;
import static com.interviewarena.entity.OptionChoice.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final QuestionRepository questionRepository;

    @Override
    public void run(String... args) {
        Set<String> existing = new HashSet<>(questionRepository.findAllQuestionTexts());

        List<Question> toAdd = Stream.concat(buildQuestions().stream(), extraQuestions().stream())
                .filter(q -> !existing.contains(q.getQuestionText()))
                .toList();

        if (!toAdd.isEmpty()) {
            questionRepository.saveAll(toAdd);
        }
        log.info("Added {} new questions. Total questions: {}", toAdd.size(), questionRepository.count());
    }

    private Question q(Category cat, Difficulty diff, String text,
                       String a, String b, String c, String d,
                       OptionChoice correct, String explanation) {
        return Question.builder()
                .category(cat).difficulty(diff).questionText(text)
                .optionA(a).optionB(b).optionC(c).optionD(d)
                .correctOption(correct).explanation(explanation)
                .build();
    }

    // ======================================================================
    // Original 30 questions
    // ======================================================================
    private List<Question> buildQuestions() {
        return List.of(
                // ---------- JAVA ----------
                q(JAVA, EASY, "What does JVM stand for?",
                        "Java Variable Machine", "Java Virtual Machine", "Java Visual Machine", "Java Verified Machine",
                        B, "The JVM executes compiled bytecode, which makes Java platform independent."),
                q(JAVA, EASY, "Which keyword prevents a class from being inherited?",
                        "static", "abstract", "private", "final",
                        D, "A final class cannot be extended. String is an example."),
                q(JAVA, MEDIUM, "For Strings, what is the difference between == and equals()?",
                        "== compares content, equals() compares references",
                        "Both compare references",
                        "== compares references, equals() compares content",
                        "Both compare content",
                        C, "== checks whether two variables point to the same object. String.equals() compares characters."),
                q(JAVA, MEDIUM, "Which collection does NOT allow duplicate elements?",
                        "ArrayList", "HashSet", "LinkedList", "Vector",
                        B, "A Set stores unique elements. HashSet uses hashCode() and equals() to detect duplicates."),
                q(JAVA, HARD, "What happens if a HashMap key's hashCode() changes after insertion?",
                        "The map rehashes it automatically", "An exception is thrown",
                        "It works normally", "The entry may not be found on lookup",
                        D, "The entry sits in the bucket for the old hash, so lookups search the wrong bucket. Use immutable keys."),

                // ---------- SPRING BOOT ----------
                q(SPRING_BOOT, EASY, "Which annotation marks the main class and enables auto-configuration?",
                        "@SpringBootApplication", "@EnableWeb", "@Configuration", "@MainApplication",
                        A, "@SpringBootApplication combines @Configuration, @EnableAutoConfiguration and @ComponentScan."),
                q(SPRING_BOOT, EASY, "Which annotation combines @Controller and @ResponseBody?",
                        "@Service", "@RestController", "@Component", "@RequestMapping",
                        B, "@RestController writes return values straight to the HTTP response body, usually as JSON."),
                q(SPRING_BOOT, MEDIUM, "Which embedded server does spring-boot-starter-web use by default?",
                        "Jetty", "Undertow", "Tomcat", "Netty",
                        C, "Tomcat is the default. You can swap it by excluding it and adding another starter."),
                q(SPRING_BOOT, MEDIUM, "Which annotation injects a value from application.properties?",
                        "@Autowired", "@Inject", "@Bean", "@Value",
                        D, "@Value(\"${property.name}\") reads a property, optionally with a default."),
                q(SPRING_BOOT, HARD, "By default, what does @Transactional do when a RuntimeException is thrown?",
                        "Commits the transaction", "Rolls back the transaction",
                        "Ignores the exception", "Suspends the transaction",
                        B, "Spring rolls back on unchecked exceptions and errors by default, but not on checked exceptions."),

                // ---------- SQL ----------
                q(SQL, EASY, "Which clause filters rows before grouping?",
                        "WHERE", "HAVING", "GROUP BY", "ORDER BY",
                        A, "WHERE filters individual rows. HAVING filters groups after aggregation."),
                q(SQL, EASY, "Which statement is used to retrieve data from a table?",
                        "GET", "SELECT", "FETCH", "OPEN",
                        B, "SELECT is the SQL query statement for reading data."),
                q(SQL, MEDIUM, "What does HAVING do that WHERE cannot?",
                        "Sorts the result", "Joins tables",
                        "Filters groups using aggregate functions", "Removes duplicates",
                        C, "HAVING runs after GROUP BY, so it can use COUNT(), SUM() and similar functions."),
                q(SQL, MEDIUM, "Which join returns all rows from the left table and the matching rows from the right?",
                        "INNER JOIN", "RIGHT JOIN", "CROSS JOIN", "LEFT JOIN",
                        D, "LEFT JOIN keeps every left row and fills right-side columns with NULL when nothing matches."),
                q(SQL, HARD, "Which statement about PRIMARY KEY and UNIQUE is correct?",
                        "A table can have one PRIMARY KEY, and it cannot contain NULL",
                        "A table can have many PRIMARY KEYs",
                        "UNIQUE columns can never be NULL",
                        "PRIMARY KEY allows duplicates",
                        A, "One primary key per table, never NULL. A table can have many UNIQUE constraints."),

                // ---------- DSA ----------
                q(DSA, EASY, "What is the time complexity of binary search?",
                        "O(n)", "O(log n)", "O(n²)", "O(1)",
                        B, "Binary search halves the search space each step, so it takes O(log n) on a sorted array."),
                q(DSA, EASY, "Which data structure follows LIFO order?",
                        "Queue", "Stack", "Heap", "Graph",
                        B, "A stack removes the most recently added element first (Last In, First Out)."),
                q(DSA, MEDIUM, "What is the average time complexity of quicksort?",
                        "O(n)", "O(n²)", "O(n log n)", "O(log n)",
                        C, "Average case is O(n log n). Worst case is O(n²) with bad pivots."),
                q(DSA, MEDIUM, "Which data structure is typically used for breadth-first search?",
                        "Stack", "Queue", "Heap", "Tree",
                        B, "BFS visits nodes level by level, which needs FIFO order, so it uses a queue."),
                q(DSA, HARD, "In Java 8+, what is the worst-case lookup time in a HashMap bucket with many collisions?",
                        "O(1)", "O(n log n)", "O(n²)", "O(log n)",
                        D, "Long buckets are converted from linked lists to balanced trees, giving O(log n) instead of O(n)."),

                // ---------- OOP ----------
                q(OOP, EASY, "Which principle hides internal details and exposes only what is necessary?",
                        "Polymorphism", "Inheritance", "Encapsulation", "Abstraction",
                        C, "Encapsulation bundles data with methods and restricts direct access, typically with private fields and getters/setters."),
                q(OOP, EASY, "Which concept lets one method name behave differently for different objects?",
                        "Polymorphism", "Encapsulation", "Compilation", "Serialization",
                        A, "Polymorphism lets the same call run different implementations depending on the actual object."),
                q(OOP, MEDIUM, "When is method overloading resolved?",
                        "At runtime", "At compile time", "During garbage collection", "At class loading only",
                        B, "Overloading is compile-time (static) polymorphism. Overriding is resolved at runtime."),
                q(OOP, MEDIUM, "What can a Java class do with interfaces that it cannot do with classes?",
                        "Implement multiple interfaces", "Have constructors", "Hold instance state", "Be instantiated directly",
                        A, "Java allows only single class inheritance but any number of interfaces."),
                q(OOP, HARD, "What does the Liskov Substitution Principle state?",
                        "Classes should have only one reason to change",
                        "Subtypes must be usable wherever their base type is expected without breaking behaviour",
                        "High-level modules should not depend on low-level modules",
                        "Interfaces should be small and specific",
                        B, "If S is a subtype of T, objects of T can be replaced with objects of S without altering correctness."),

                // ---------- DBMS ----------
                q(DBMS, EASY, "In ACID, what does the letter A stand for?",
                        "Availability", "Atomicity", "Authorization", "Aggregation",
                        B, "Atomicity means a transaction happens completely or not at all."),
                q(DBMS, EASY, "Which key uniquely identifies each row in a table?",
                        "Foreign key", "Composite index", "Primary key", "Alternate view",
                        C, "The primary key uniquely identifies each record and cannot be NULL."),
                q(DBMS, MEDIUM, "What does First Normal Form (1NF) require?",
                        "No transitive dependencies", "No partial dependencies",
                        "Atomic values and no repeating groups", "Every table has a foreign key",
                        C, "1NF requires each column to hold a single, indivisible value."),
                q(DBMS, MEDIUM, "Which isolation level prevents dirty reads but still allows non-repeatable reads?",
                        "READ UNCOMMITTED", "SERIALIZABLE", "REPEATABLE READ", "READ COMMITTED",
                        D, "READ COMMITTED only lets a transaction see committed data, but a re-read can return changed values."),
                q(DBMS, HARD, "Which normal form removes transitive dependencies?",
                        "1NF", "2NF", "3NF", "BCNF only",
                        C, "3NF requires that non-key attributes depend only on the key, not on other non-key attributes.")
        );
    }

    // ======================================================================
    // 36 additional questions (6 per category)
    // ======================================================================
    private List<Question> extraQuestions() {
        return List.of(
                // ---------- JAVA ----------
                q(JAVA, EASY, "Which of these is NOT a primitive type in Java?",
                        "int", "boolean", "String", "char",
                        C, "String is a class (a reference type). int, boolean and char are primitives."),
                q(JAVA, EASY, "Which keyword is used to create a new object in Java?",
                        "new", "create", "alloc", "make",
                        A, "The new keyword allocates memory and calls the constructor."),
                q(JAVA, MEDIUM, "What is the default value of an uninitialized int instance variable?",
                        "null", "0", "undefined", "-1",
                        B, "Instance variables get default values. int defaults to 0, boolean to false and objects to null."),
                q(JAVA, MEDIUM, "Which statement about Java Strings is correct?",
                        "Strings are mutable", "Strings are immutable", "String is a primitive type", "Strings cannot be compared",
                        B, "Once created, a String cannot be changed. Operations like concat() return a new String."),
                q(JAVA, HARD, "What is the difference between checked and unchecked exceptions?",
                        "Checked exceptions must be handled or declared at compile time; unchecked ones need not be",
                        "Unchecked exceptions must always be caught",
                        "Checked exceptions only occur at runtime",
                        "There is no difference",
                        A, "IOException is checked. NullPointerException (a RuntimeException) is unchecked."),
                q(JAVA, HARD, "When does a finally block run?",
                        "Only when an exception occurs", "Only when there is no exception",
                        "Whether or not an exception occurs, except when the JVM exits or crashes", "Only after a catch block runs",
                        C, "finally runs even if try returns. System.exit() or a JVM crash can prevent it."),

                // ---------- SPRING BOOT ----------
                q(SPRING_BOOT, EASY, "Which file holds Spring Boot's default configuration?",
                        "pom.xml", "application.properties", "web.xml", "settings.json",
                        B, "application.properties (or application.yml) is read automatically at startup."),
                q(SPRING_BOOT, EASY, "Which annotation marks a class as a service layer bean?",
                        "@Service", "@Entity", "@Table", "@Id",
                        A, "@Service is a stereotype annotation that registers the class as a Spring bean."),
                q(SPRING_BOOT, MEDIUM, "What does @Autowired do?",
                        "Creates database tables", "Injects a dependency automatically", "Starts the embedded server", "Maps a URL to a method",
                        B, "Spring finds a matching bean and injects it. Constructor injection is preferred."),
                q(SPRING_BOOT, MEDIUM, "Which annotation maps HTTP POST requests to a controller method?",
                        "@PostMapping", "@GetMapping", "@PutMapping", "@RequestBody",
                        A, "@PostMapping is shorthand for @RequestMapping(method = POST)."),
                q(SPRING_BOOT, HARD, "What is the default scope of a Spring bean?",
                        "prototype", "request", "session", "singleton",
                        D, "By default Spring creates one shared instance of each bean per application context."),
                q(SPRING_BOOT, HARD, "What exception occurs when a lazy association is accessed after the session is closed?",
                        "NullPointerException", "LazyInitializationException", "ClassCastException", "StackOverflowError",
                        B, "Hibernate cannot load the lazy data without an open session. Fetch it inside a transaction."),

                // ---------- SQL ----------
                q(SQL, EASY, "Which keyword removes duplicate rows from a query result?",
                        "UNIQUE", "DISTINCT", "DIFFERENT", "REMOVE",
                        B, "SELECT DISTINCT returns only unique rows."),
                q(SQL, EASY, "Which function counts the number of rows?",
                        "SUM()", "TOTAL()", "COUNT()", "ROWS()",
                        C, "COUNT(*) counts rows. COUNT(column) ignores NULL values."),
                q(SQL, MEDIUM, "Which command removes all rows from a table but keeps the table structure?",
                        "DELETE FROM t (no WHERE) is the only way", "DROP TABLE t", "TRUNCATE TABLE t", "REMOVE t",
                        C, "TRUNCATE empties the table and keeps its definition. It is usually faster than DELETE."),
                q(SQL, MEDIUM, "What does COALESCE(a, b) return?",
                        "The larger value", "The first non-NULL argument", "A concatenation of a and b", "Always b",
                        B, "COALESCE returns the first argument that is not NULL."),
                q(SQL, HARD, "What is the main trade-off of adding an index?",
                        "Faster reads, but slower writes and more storage", "Faster writes, but slower reads",
                        "No effect on reads", "It always reduces storage",
                        A, "Indexes speed up lookups, but every insert, update and delete must also maintain them."),
                q(SQL, HARD, "Which join returns the Cartesian product of two tables?",
                        "INNER JOIN", "LEFT JOIN", "SELF JOIN", "CROSS JOIN",
                        D, "CROSS JOIN pairs every row of one table with every row of the other."),

                // ---------- DSA ----------
                q(DSA, EASY, "What is the time complexity of accessing an array element by index?",
                        "O(n)", "O(1)", "O(log n)", "O(n²)",
                        B, "Arrays are stored contiguously, so the address is computed directly."),
                q(DSA, EASY, "Which data structure follows FIFO order?",
                        "Queue", "Stack", "Tree", "Graph",
                        A, "A queue removes the oldest element first (First In, First Out)."),
                q(DSA, MEDIUM, "What is the worst-case search time in a balanced binary search tree?",
                        "O(1)", "O(n)", "O(log n)", "O(n log n)",
                        C, "A balanced tree has height log n, so a search follows at most log n nodes."),
                q(DSA, MEDIUM, "Which sorting algorithm is stable and has O(n log n) worst-case time?",
                        "Quick sort", "Heap sort", "Merge sort", "Selection sort",
                        C, "Merge sort keeps equal elements in order and is O(n log n) in every case."),
                q(DSA, HARD, "Which algorithm detects a cycle in a linked list using two pointers?",
                        "Dijkstra's algorithm", "Kruskal's algorithm", "Binary search", "Floyd's tortoise and hare",
                        D, "A slow and a fast pointer will meet inside the cycle if one exists. It uses O(1) extra space."),
                q(DSA, HARD, "Dijkstra's shortest path algorithm does not work correctly when the graph has:",
                        "Cycles", "Negative edge weights", "Undirected edges", "More than 100 nodes",
                        B, "Negative weights break the assumption that a finalized node has its shortest distance. Use Bellman-Ford."),

                // ---------- OOP ----------
                q(OOP, EASY, "Which keyword is used for class inheritance in Java?",
                        "implements", "extends", "inherits", "super",
                        B, "A class uses extends to inherit from another class. implements is for interfaces."),
                q(OOP, EASY, "What is an object in OOP?",
                        "A blueprint for a class", "A package", "An instance of a class", "A compile-time error",
                        C, "A class is the blueprint. An object is a concrete instance created from it."),
                q(OOP, MEDIUM, "What is method overriding?",
                        "Defining two methods with the same name and different parameters", "Hiding a parent's field",
                        "Calling a parent constructor", "A subclass providing its own implementation of an inherited method",
                        D, "Overriding replaces the parent's behaviour and is resolved at runtime."),
                q(OOP, MEDIUM, "What is true about an abstract class in Java?",
                        "It cannot be instantiated and may contain abstract methods", "It must have only abstract methods",
                        "It cannot have constructors", "It cannot be extended",
                        A, "Abstract classes are meant to be extended. They can mix abstract and concrete methods."),
                q(OOP, HARD, "What does the guideline 'favor composition over inheritance' mean?",
                        "Always use deep class hierarchies", "Avoid interfaces",
                        "Build behaviour by combining objects rather than relying on deep inheritance", "Make all fields public",
                        C, "Composition is more flexible and avoids the tight coupling that inheritance creates."),
                q(OOP, HARD, "What does the Dependency Inversion Principle state?",
                        "Classes should be open for extension but closed for modification",
                        "High-level modules should depend on abstractions, not concrete low-level modules",
                        "A class should have only one responsibility",
                        "Subtypes must be substitutable for their base types",
                        B, "Depend on interfaces so implementations can be swapped. This is how Spring injects dependencies."),

                // ---------- DBMS ----------
                q(DBMS, EASY, "What does a foreign key do?",
                        "Uniquely identifies a row", "Links a column to the primary key of another table", "Encrypts data", "Sorts rows",
                        B, "A foreign key enforces a relationship between two tables."),
                q(DBMS, EASY, "Which ACID property ensures committed data survives a crash?",
                        "Atomicity", "Consistency", "Isolation", "Durability",
                        D, "Durability means committed changes are stored permanently, usually through a write-ahead log."),
                q(DBMS, MEDIUM, "What is a database transaction?",
                        "A single logical unit of work that is completed fully or not at all", "A backup of the database",
                        "A type of table", "An index on a column",
                        A, "A transaction groups operations so they either all commit or all roll back."),
                q(DBMS, MEDIUM, "What is a clustered index?",
                        "An index that sorts the table rows differently each query", "An index that cannot be on a primary key",
                        "An index that defines the physical order of the data, with only one allowed per table", "The same as a non-clustered index",
                        C, "The clustered index is the table's storage order. In MySQL InnoDB the primary key is clustered."),
                q(DBMS, HARD, "A transaction re-runs a query and sees new rows added by another committed transaction. What is this called?",
                        "Phantom read", "Dirty read", "Non-repeatable read", "Deadlock",
                        A, "A phantom read means new rows appear. A non-repeatable read means existing rows have changed."),
                q(DBMS, HARD, "What is a deadlock?",
                        "A slow-running query", "A crashed database server", "A table without an index",
                        "Two or more transactions waiting forever for locks held by each other",
                        D, "Each transaction holds a lock the other needs. The database detects this and rolls one back.")
        );
    }
}