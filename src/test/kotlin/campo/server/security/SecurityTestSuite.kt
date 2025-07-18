package campo.server.security

import campo.server.database.AuthDatabase
import campo.server.data.User
import io.vertx.core.Vertx
import io.vertx.junit5.VertxExtension
import io.vertx.junit5.VertxTestContext
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths

@ExtendWith(VertxExtension::class)
@DisplayName("Security Vulnerability Tests")
class SecurityTestSuite {
    
    private lateinit var authDB: AuthDatabase
    
    @BeforeEach
    fun setUp(vertx: Vertx, testContext: VertxTestContext) {
        authDB = AuthDatabase(vertx)
        testContext.completeNow()
    }
    
    @Test
    @DisplayName("SQL Injection 테스트 - 이메일 필드")
    fun testSqlInjectionInEmail(vertx: Vertx, testContext: VertxTestContext) {
        val maliciousEmails = listOf(
            "'; DROP TABLE users; --",
            "admin'; DELETE FROM users WHERE '1'='1",
            "' OR '1'='1'; --",
            "' UNION SELECT password FROM users WHERE email='admin@example.com'; --",
            "'; INSERT INTO users (email, password) VALUES ('hacker', 'password'); --",
            "' OR 1=1 UNION SELECT null, username, password FROM users --"
        )
        
        maliciousEmails.forEach { maliciousEmail ->
            try {
                // getUserByEmail 메서드로 SQL 인젝션 시도
                authDB.getUserByEmail(maliciousEmail)
                    .onComplete { result ->
                        if (result.succeeded()) {
                            println("⚠️  POTENTIAL SQL INJECTION: Email query succeeded with malicious input: $maliciousEmail")
                        } else {
                            println("✅ SQL Injection prevented for email: $maliciousEmail")
                        }
                    }
                
                // emailExists 메서드로 SQL 인젝션 시도
                authDB.emailExists(maliciousEmail)
                    .onComplete { result ->
                        if (result.succeeded()) {
                            println("⚠️  POTENTIAL SQL INJECTION: EmailExists query succeeded with malicious input: $maliciousEmail")
                        } else {
                            println("✅ SQL Injection prevented for emailExists: $maliciousEmail")
                        }
                    }
                    
            } catch (e: Exception) {
                println("✅ Exception caught for malicious email $maliciousEmail: ${e.message}")
            }
        }
        testContext.completeNow()
    }
    
    @Test
    @DisplayName("SQL Injection 테스트 - 사용자 등록")
    fun testSqlInjectionInRegistration(vertx: Vertx, testContext: VertxTestContext) {
        val maliciousInputs = mapOf(
            "email" to "'; DROP TABLE users; --@test.com",
            "school" to "'; DELETE FROM users; --",
            "majorOfSchool" to "' OR '1'='1'; --",
            "location" to "'; INSERT INTO users (email, password) VALUES ('admin', 'hacked'); --"
        )
        
        maliciousInputs.forEach { (field, value) ->
            val maliciousUser = User(
                email = if (field == "email") value else "test@example.com",
                password = "password123",
                school = if (field == "school") value else "Test University",
                classOfSchool = 1,
                majorOfSchool = if (field == "majorOfSchool") value else "Computer Science",
                location = if (field == "location") value else "Seoul",
                levelOfIncome = 5,
                grade = 3.5
            )
            
            try {
                authDB.registerUser(maliciousUser)
                    .onComplete { result ->
                        if (result.succeeded()) {
                            println("⚠️  POTENTIAL SQL INJECTION: Registration succeeded with malicious $field: $value")
                        } else {
                            println("✅ SQL Injection prevented for $field: $value")
                        }
                    }
            } catch (e: Exception) {
                println("✅ Exception caught for malicious $field $value: ${e.message}")
            }
        }
        testContext.completeNow()
    }
    
    @Test
    @DisplayName("데이터베이스 파일 권한 확인")
    fun testDatabaseFilePermissions() {
        val dbFiles = listOf("auth.sqlite", ".env")
        
        dbFiles.forEach { fileName ->
            val file = File(fileName)
            if (file.exists()) {
                println("🔍 Checking file: $fileName")
                println("  - Readable: ${file.canRead()}")
                println("  - Writable: ${file.canWrite()}")
                println("  - Executable: ${file.canExecute()}")
                
                if (file.canRead() && file.canWrite()) {
                    println("⚠️  WARNING: Database file $fileName has read/write permissions!")
                }
                
                // 파일 크기 확인
                println("  - Size: ${file.length()} bytes")
                
                // .env 파일의 경우 내용 확인 (민감한 정보 노출 여부)
                if (fileName == ".env" && file.canRead()) {
                    try {
                        val content = file.readText()
                        if (content.contains("password") || content.contains("secret") || content.contains("key")) {
                            println("⚠️  WARNING: .env file may contain sensitive information!")
                        }
                    } catch (e: Exception) {
                        println("✅ Could not read .env file: ${e.message}")
                    }
                }
            } else {
                println("ℹ️  File $fileName not found")
            }
        }
    }
    
    @Test
    @DisplayName("비밀번호 해싱 검증")
    fun testPasswordHashingStrength() {
        val testPasswords = listOf(
            "password123",
            "admin",
            "123456",
            "password",
            "qwerty"
        )
        
        testPasswords.forEach { password ->
            val user = User(
                email = "test@example.com",
                password = password,
                school = "Test University",
                classOfSchool = 1,
                majorOfSchool = "Computer Science", 
                location = "Seoul",
                levelOfIncome = 5,
                grade = 3.5
            )
            
            try {
                authDB.registerUser(user)
                    .onComplete { result ->
                        if (result.succeeded()) {
                            println("✅ Password hashed for: $password")
                            
                            // 해시된 비밀번호가 원본과 다른지 확인
                            authDB.getUserByEmail("test@example.com")
                                .onComplete { userResult ->
                                    if (userResult.succeeded() && userResult.result() != null) {
                                        val retrievedUser = userResult.result()!!
                                        if (retrievedUser.password == password) {
                                            println("⚠️  CRITICAL: Password stored in plaintext!")
                                        } else {
                                            println("✅ Password properly hashed")
                                        }
                                    }
                                }
                        }
                    }
            } catch (e: Exception) {
                println("❌ Error testing password: ${e.message}")
            }
        }
    }
    
    @Test
    @DisplayName("SQLite 데이터베이스 백도어 확인")
    fun testDatabaseBackdoors() {
        val sqliteFile = File("auth.sqlite")
        
        if (sqliteFile.exists()) {
            println("🔍 Analyzing SQLite database...")
            
            // 파일 크기가 비정상적으로 큰지 확인
            val fileSize = sqliteFile.length()
            println("Database size: $fileSize bytes")
            
            if (fileSize > 1_000_000) { // 1MB 이상
                println("⚠️  WARNING: Database file is unusually large!")
            }
            
            // 숨겨진 테이블이나 트리거 확인을 위한 기본적인 체크
            try {
                // 여기서는 파일의 헥스 내용을 간단히 확인
                val bytes = Files.readAllBytes(Paths.get("auth.sqlite"))
                val content = String(bytes, Charsets.ISO_8859_1)
                
                val suspiciousPatterns = listOf(
                    "CREATE TRIGGER",
                    "PRAGMA",
                    "admin",
                    "backdoor",
                    "exec",
                    "system"
                )
                
                suspiciousPatterns.forEach { pattern ->
                    if (content.contains(pattern, ignoreCase = true)) {
                        println("⚠️  SUSPICIOUS: Found pattern '$pattern' in database")
                    }
                }
                
            } catch (e: Exception) {
                println("❌ Error analyzing database: ${e.message}")
            }
        }
    }
    
    @Test
    @DisplayName("환경변수 보안 확인")
    fun testEnvironmentSecurity() {
        val envFile = File("src/main/resources/.env")
        
        if (envFile.exists()) {
            println("🔍 Checking .env file security...")
            
            try {
                val content = envFile.readText()
                val lines = content.split("\n")
                
                lines.forEach { line ->
                    when {
                        line.contains("DB_PASS") -> {
                            val password = line.substringAfter("=").trim()
                            if (password.length < 8) {
                                println("⚠️  WEAK PASSWORD: Database password is too short")
                            }
                            if (password.matches(Regex("^[a-zA-Z0-9]*$"))) {
                                println("⚠️  WEAK PASSWORD: Database password lacks special characters")
                            }
                            if (password.lowercase() in listOf("password", "admin", "root", "123456")) {
                                println("🚨 CRITICAL: Database password is a common weak password!")
                            }
                        }
                        line.contains("secret", ignoreCase = true) -> {
                            println("ℹ️  Found secret configuration: ${line.substringBefore("=")}")
                        }
                    }
                }
                
            } catch (e: Exception) {
                println("❌ Error reading .env file: ${e.message}")
            }
        }
    }
}