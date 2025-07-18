package campo.server.security

import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.security.MessageDigest

/**
 * 데이터베이스 보안 분석 도구
 * 정적 분석을 통해 보안 취약점을 확인합니다
 */
object DatabaseSecurityAnalyzer {
    
    fun analyzeProject(): SecurityReport {
        val report = SecurityReport()
        
        // 1. 데이터베이스 파일 분석
        analyzeDatabaseFiles(report)
        
        // 2. 환경 변수 분석
        analyzeEnvironmentFiles(report)
        
        // 3. 소스 코드 분석
        analyzeSourceCode(report)
        
        // 4. 파일 권한 분석
        analyzeFilePermissions(report)
        
        return report
    }
    
    private fun analyzeDatabaseFiles(report: SecurityReport) {
        val dbFile = File("auth.sqlite")
        
        if (dbFile.exists()) {
            report.addFinding(
                "DATABASE_FILE_EXISTS",
                "SQLite 데이터베이스 파일이 프로젝트 루트에 있습니다",
                if (dbFile.canRead()) SecurityLevel.HIGH else SecurityLevel.MEDIUM
            )
            
            // 파일 크기 확인
            val fileSize = dbFile.length()
            if (fileSize > 10_000_000) { // 10MB 이상
                report.addFinding(
                    "LARGE_DATABASE_FILE",
                    "데이터베이스 파일이 비정상적으로 큽니다: ${fileSize} bytes",
                    SecurityLevel.MEDIUM
                )
            }
            
            // 파일 무결성 확인
            try {
                val hash = calculateFileHash(dbFile)
                report.addInfo("데이터베이스 파일 해시: $hash")
            } catch (e: Exception) {
                report.addFinding(
                    "DATABASE_HASH_ERROR",
                    "데이터베이스 파일 해시 계산 실패: ${e.message}",
                    SecurityLevel.LOW
                )
            }
        }
    }
    
    private fun analyzeEnvironmentFiles(report: SecurityReport) {
        val envFiles = listOf(
            "src/main/resources/.env",
            ".env",
            "config.properties"
        )
        
        envFiles.forEach { envPath ->
            val envFile = File(envPath)
            if (envFile.exists()) {
                try {
                    val content = envFile.readText()
                    
                    // 민감한 정보 패턴 확인
                    val sensitivePatterns = mapOf(
                        "password" to "비밀번호가 평문으로 저장될 수 있습니다",
                        "secret" to "시크릿 키가 노출될 수 있습니다",
                        "private" to "프라이빗 키가 노출될 수 있습니다",
                        "token" to "토큰이 노출될 수 있습니다"
                    )
                    
                    sensitivePatterns.forEach { (pattern, description) ->
                        if (content.contains(pattern, ignoreCase = true)) {
                            report.addFinding(
                                "SENSITIVE_INFO_IN_ENV",
                                "$envPath 에서 발견: $description",
                                SecurityLevel.HIGH
                            )
                        }
                    }
                    
                    // 약한 비밀번호 패턴 확인
                    val lines = content.split("\n")
                    lines.forEach { line ->
                        if (line.contains("DB_PASS", ignoreCase = true)) {
                            val password = line.substringAfter("=").trim()
                            checkPasswordStrength(password, report)
                        }
                    }
                    
                } catch (e: Exception) {
                    report.addFinding(
                        "ENV_FILE_READ_ERROR",
                        "$envPath 파일 읽기 실패: ${e.message}",
                        SecurityLevel.LOW
                    )
                }
            }
        }
    }
    
    private fun analyzeSourceCode(report: SecurityReport) {
        val sourceFiles = findKotlinFiles("src/main/kotlin")
        
        sourceFiles.forEach { file ->
            try {
                val content = file.readText()
                
                // SQL 인젝션 취약점 패턴 확인
                val vulnerablePatterns = listOf(
                    "\".*\\$.*\".*FROM.*users" to "SQL 문자열 연결로 인한 SQL 인젝션 가능성",
                    "query\\(.*\\+.*\\)" to "동적 쿼리 생성으로 인한 SQL 인젝션 가능성",
                    "execute\\(.*\\+.*\\)" to "동적 쿼리 실행으로 인한 SQL 인젝션 가능성"
                )
                
                vulnerablePatterns.forEach { (pattern, description) ->
                    if (content.contains(Regex(pattern))) {
                        report.addFinding(
                            "POTENTIAL_SQL_INJECTION",
                            "${file.name}에서 발견: $description",
                            SecurityLevel.HIGH
                        )
                    }
                }
                
                // 하드코딩된 비밀번호 확인 (API 문서 예시 제외)
                val hardcodedPatterns = listOf(
                    "password.*=.*\".*\"",
                    "pwd.*=.*\".*\"", 
                    "secret.*=.*\".*\""
                )
                
                hardcodedPatterns.forEach { pattern ->
                    val regex = Regex(pattern, RegexOption.IGNORE_CASE)
                    val matches = regex.findAll(content)
                    
                    matches.forEach { match ->
                        val matchStart = match.range.first
                        val matchEnd = match.range.last
                        
                        // 매치된 부분 주변의 컨텍스트를 확인 (앞뒤 100자)
                        val contextStart = maxOf(0, matchStart - 100)
                        val contextEnd = minOf(content.length, matchEnd + 100)
                        val context = content.substring(contextStart, contextEnd)
                        
                        // Parameter 어노테이션 안의 예시인지 확인
                        val isParameterExample = context.contains("Parameter(") && 
                                               (context.contains("example") || context.contains("\"password123\"") || context.contains("\"newpassword123\""))
                        
                        if (!isParameterExample) {
                            report.addFinding(
                                "HARDCODED_CREDENTIALS",
                                "${file.name}에서 하드코딩된 인증정보 발견: ${match.value}",
                                SecurityLevel.HIGH
                            )
                        }
                    }
                }
                
                // 로깅으로 인한 정보 유출 확인
                if (content.contains("logger.*password", ignoreCase = true) ||
                    content.contains("println.*password", ignoreCase = true)) {
                    report.addFinding(
                        "PASSWORD_LOGGING",
                        "${file.name}에서 비밀번호 로깅 가능성 발견",
                        SecurityLevel.MEDIUM
                    )
                }
                
            } catch (e: Exception) {
                report.addFinding(
                    "SOURCE_ANALYSIS_ERROR",
                    "${file.name} 분석 실패: ${e.message}",
                    SecurityLevel.LOW
                )
            }
        }
    }
    
    private fun analyzeFilePermissions(report: SecurityReport) {
        val criticalFiles = listOf(
            "auth.sqlite",
            "src/main/resources/.env",
            ".env"
        )
        
        criticalFiles.forEach { filePath ->
            val file = File(filePath)
            if (file.exists()) {
                if (file.canRead() && file.canWrite()) {
                    report.addFinding(
                        "INSECURE_FILE_PERMISSIONS",
                        "$filePath 파일이 읽기/쓰기 권한을 가지고 있습니다",
                        SecurityLevel.MEDIUM
                    )
                }
                
                if (file.canExecute()) {
                    report.addFinding(
                        "EXECUTABLE_DATA_FILE",
                        "$filePath 데이터 파일이 실행 권한을 가지고 있습니다",
                        SecurityLevel.HIGH
                    )
                }
            }
        }
    }
    
    private fun checkPasswordStrength(password: String, report: SecurityReport) {
        val weaknesses = mutableListOf<String>()
        
        if (password.length < 8) {
            weaknesses.add("길이가 8자 미만")
        }
        
        if (!password.any { it.isUpperCase() }) {
            weaknesses.add("대문자 없음")
        }
        
        if (!password.any { it.isLowerCase() }) {
            weaknesses.add("소문자 없음")
        }
        
        if (!password.any { it.isDigit() }) {
            weaknesses.add("숫자 없음")
        }
        
        if (!password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) {
            weaknesses.add("특수문자 없음")
        }
        
        val commonPasswords = listOf("password", "admin", "root", "123456", "qwerty")
        if (commonPasswords.any { password.equals(it, ignoreCase = true) }) {
            weaknesses.add("일반적인 약한 비밀번호")
        }
        
        if (weaknesses.isNotEmpty()) {
            report.addFinding(
                "WEAK_PASSWORD",
                "약한 데이터베이스 비밀번호 발견: ${weaknesses.joinToString(", ")}",
                SecurityLevel.HIGH
            )
        }
    }
    
    private fun findKotlinFiles(directory: String): List<File> {
        val files = mutableListOf<File>()
        val dir = File(directory)
        
        if (dir.exists() && dir.isDirectory) {
            dir.walkTopDown().forEach { file ->
                if (file.isFile && file.extension == "kt") {
                    files.add(file)
                }
            }
        }
        
        return files
    }
    
    private fun calculateFileHash(file: File): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val bytes = Files.readAllBytes(file.toPath())
        val hash = digest.digest(bytes)
        return hash.joinToString("") { "%02x".format(it) }
    }
}

enum class SecurityLevel {
    LOW, MEDIUM, HIGH, CRITICAL
}

data class SecurityFinding(
    val id: String,
    val description: String,
    val level: SecurityLevel
)

class SecurityReport {
    private val findings = mutableListOf<SecurityFinding>()
    private val info = mutableListOf<String>()
    
    fun addFinding(id: String, description: String, level: SecurityLevel) {
        findings.add(SecurityFinding(id, description, level))
    }
    
    fun addInfo(message: String) {
        info.add(message)
    }
    
    fun printReport() {
        println("=".repeat(60))
        println("🔒 보안 분석 보고서")
        println("=".repeat(60))
        
        if (findings.isEmpty()) {
            println("✅ 보안 문제가 발견되지 않았습니다.")
        } else {
            val criticalFindings = findings.filter { it.level == SecurityLevel.CRITICAL }
            val highFindings = findings.filter { it.level == SecurityLevel.HIGH }
            val mediumFindings = findings.filter { it.level == SecurityLevel.MEDIUM }
            val lowFindings = findings.filter { it.level == SecurityLevel.LOW }
            
            if (criticalFindings.isNotEmpty()) {
                println("\n🚨 치명적 보안 문제:")
                criticalFindings.forEach { println("  - ${it.description}") }
            }
            
            if (highFindings.isNotEmpty()) {
                println("\n⚠️  높은 위험도:")
                highFindings.forEach { println("  - ${it.description}") }
            }
            
            if (mediumFindings.isNotEmpty()) {
                println("\n🔸 중간 위험도:")
                mediumFindings.forEach { println("  - ${it.description}") }
            }
            
            if (lowFindings.isNotEmpty()) {
                println("\n🔹 낮은 위험도:")
                lowFindings.forEach { println("  - ${it.description}") }
            }
        }
        
        if (info.isNotEmpty()) {
            println("\nℹ️  추가 정보:")
            info.forEach { println("  - $it") }
        }
        
        println("\n" + "=".repeat(60))
        println("총 ${findings.size}개의 보안 문제가 발견되었습니다.")
    }
    
    fun getFindings(): List<SecurityFinding> = findings.toList()
    fun hasHighRiskFindings(): Boolean = findings.any { it.level in listOf(SecurityLevel.HIGH, SecurityLevel.CRITICAL) }
}

// 실행 가능한 메인 함수
fun main() {
    val report = DatabaseSecurityAnalyzer.analyzeProject()
    report.printReport()
    
    if (report.hasHighRiskFindings()) {
        println("\n🚨 높은 위험도 이상의 보안 문제가 발견되었습니다. 즉시 수정하세요!")
        System.exit(1)
    }
}