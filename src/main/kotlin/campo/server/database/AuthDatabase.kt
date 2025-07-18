package campo.server.database

import campo.server.data.User
import io.github.cdimascio.dotenv.dotenv
import io.vertx.core.Future
import io.vertx.core.Vertx
import io.vertx.core.internal.logging.LoggerFactory
import io.vertx.ext.auth.prng.VertxContextPRNG
import io.vertx.ext.auth.sqlclient.SqlAuthentication
import io.vertx.ext.auth.sqlclient.SqlAuthenticationOptions
import io.vertx.jdbcclient.JDBCConnectOptions
import io.vertx.jdbcclient.JDBCPool
import io.vertx.sqlclient.PoolOptions
import io.vertx.sqlclient.SqlClient
import io.vertx.sqlclient.Tuple
import java.io.File




class AuthDatabase(vertx: Vertx) {
    private val logger = LoggerFactory.getLogger(javaClass)
    private val pool: SqlClient
    private val sqlAuth: SqlAuthentication

    init {
        val dotenv = dotenv()

        File("./scholarship/").mkdir()

        val connectOptions = JDBCConnectOptions()
            .setJdbcUrl("jdbc:sqlite:${dotenv["DB_URL"]}")
            .setUser(dotenv["DB_USER"])
            .setPassword(dotenv["DB_PASS"])

        val poolOptions = PoolOptions()
            .setMaxSize(16)

        pool = JDBCPool.pool(vertx, connectOptions, poolOptions)
        sqlAuth = SqlAuthentication.create(
            pool,
            SqlAuthenticationOptions().setAuthenticationQuery("SELECT password FROM users WHERE email = ?"))

        createUserTable()

        val dbFile = File("./scholarship/auth.sqlite")
        logger.info("DB file absolute path: " + dbFile.absolutePath)
        logger.info("DB file exists: " + dbFile.exists())
        logger.info("Parent directory exists: " + dbFile.getParentFile().exists())

    }

    fun getSqlAuth() = sqlAuth
    
    private fun createUserTable() {
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS users (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                email TEXT UNIQUE NOT NULL,
                password TEXT NOT NULL,
                school TEXT NOT NULL,
                class_of_school INTEGER NOT NULL,
                major_of_school TEXT NOT NULL,
                location TEXT NOT NULL,
                level_of_income INTEGER NOT NULL,
                grade REAL NOT NULL,
                created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                updated_at DATETIME DEFAULT CURRENT_TIMESTAMP
            )
        """.trimIndent()
        
        pool.query(createTableSql).execute().onFailure { error ->
            logger.error("사용자 테이블 생성 실패", error)
        }
    }
    
    private fun hashPassword(password: String): String = sqlAuth.hash(
        "pbkdf2",  // hashing algorithm (OWASP recommended)
        VertxContextPRNG.current().nextString(32),  // secure random salt
        password
    )

    
    fun registerUser(user: User): Future<Long> {
        val hashedPassword = hashPassword(user.password)
        
        val sql = """
            INSERT INTO users (email, password, school, class_of_school, major_of_school, location, level_of_income, grade)
            VALUES (?, ?, ?, ?, ?, ?, ?, ?)
        """.trimIndent()
        
        val params = Tuple.of(
            user.email,
            hashedPassword,
            user.school,
            user.classOfSchool,
            user.majorOfSchool,
            user.location,
            user.levelOfIncome,
            user.grade
        )
        
        return pool.preparedQuery(sql).execute(params)
            .map { result ->
                result.property(JDBCPool.GENERATED_KEYS).getLong(0)
            }
            .onSuccess { userId ->
                logger.info("사용자 등록 성공: $userId")
            }
            .onFailure { error ->
                logger.error("사용자 등록 실패", error)
            }
    }

    fun getUserByEmail(email: String): Future<User?> {
        val sql = """
            SELECT email, school, class_of_school, major_of_school, location, level_of_income, grade
            FROM users WHERE email = ?
        """.trimIndent()
        
        val params = Tuple.of(email)
        
        return pool.preparedQuery(sql).execute(params)
            .map { result ->
                if (result.size() > 0) {
                    val row = result.iterator().next()
                    User(
                        email = row.getString("email"),
                        password = "", // 비밀번호는 반환하지 않음
                        school = row.getString("school"),
                        classOfSchool = row.getInteger("class_of_school"),
                        majorOfSchool = row.getString("major_of_school"),
                        location = row.getString("location"),
                        levelOfIncome = row.getInteger("level_of_income"),
                        grade = row.getDouble("grade")
                    )
                } else {
                    null
                }
            }
            .onFailure { error ->
                logger.error("사용자 조회 실패", error)
            }
    }
    
    fun updateUser(email: String, user: User): Future<Boolean> {
        // 먼저 기존 사용자 정보를 조회
        return getUserByEmail(email).compose { existingUser ->
            if (existingUser == null) {
                Future.succeededFuture(false)
            } else {
                // 제공된 값이 유효하면 사용하고, 그렇지 않으면 기존 값 유지
                val updatedPassword = if (user.password.isNotEmpty()) hashPassword(user.password) else null
                val updatedSchool = if (user.school.isNotEmpty()) user.school else existingUser.school
                val updatedClassOfSchool = if (user.classOfSchool > 0) user.classOfSchool else existingUser.classOfSchool
                val updatedMajorOfSchool = if (user.majorOfSchool.isNotEmpty()) user.majorOfSchool else existingUser.majorOfSchool
                val updatedLocation = if (user.location.isNotEmpty()) user.location else existingUser.location
                val updatedLevelOfIncome = if (user.levelOfIncome > 0) user.levelOfIncome else existingUser.levelOfIncome
                val updatedGrade = if (user.grade > 0.0) user.grade else existingUser.grade
                
                val sql = if (updatedPassword != null) {
                    """
                        UPDATE users SET 
                            password = ?, school = ?, class_of_school = ?, major_of_school = ?, 
                            location = ?, level_of_income = ?, grade = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE email = ?
                    """.trimIndent()
                } else {
                    """
                        UPDATE users SET 
                            school = ?, class_of_school = ?, major_of_school = ?, 
                            location = ?, level_of_income = ?, grade = ?, updated_at = CURRENT_TIMESTAMP
                        WHERE email = ?
                    """.trimIndent()
                }
                
                val params = if (updatedPassword != null) {
                    Tuple.of(
                        updatedPassword,
                        updatedSchool,
                        updatedClassOfSchool,
                        updatedMajorOfSchool,
                        updatedLocation,
                        updatedLevelOfIncome,
                        updatedGrade,
                        email
                    )
                } else {
                    Tuple.of(
                        updatedSchool,
                        updatedClassOfSchool,
                        updatedMajorOfSchool,
                        updatedLocation,
                        updatedLevelOfIncome,
                        updatedGrade,
                        email
                    )
                }
                
                pool.preparedQuery(sql).execute(params)
                    .map { result ->
                        result.rowCount() > 0
                    }
            }
        }
        .onSuccess { updated ->
            if (updated) {
                logger.info("사용자 정보 수정 성공: $email")
            } else {
                logger.warn("사용자 정보 수정 실패: 사용자를 찾을 수 없음")
            }
        }
        .onFailure { error ->
            logger.error("사용자 정보 수정 실패", error)
        }
    }
    
    fun deleteUser(email: String): Future<Boolean> {
        val sql = "DELETE FROM users WHERE email = ?"
        val params = Tuple.of(email)
        
        return pool.preparedQuery(sql).execute(params)
            .map { result ->
                result.rowCount() > 0
            }
            .onSuccess { deleted ->
                if (deleted) {
                    logger.info("사용자 삭제 성공: $email")
                } else {
                    logger.warn("사용자 삭제 실패: 사용자를 찾을 수 없음")
                }
            }
            .onFailure { error ->
                logger.error("사용자 삭제 실패", error)
            }
    }
    
    fun emailExists(email: String): Future<Boolean> {
        val sql = "SELECT COUNT(*) as count FROM users WHERE email = ?"
        val params = Tuple.of(email)
        
        return pool.preparedQuery(sql).execute(params)
            .map { result ->
                val row = result.iterator().next()
                row.getInteger("count") > 0
            }
            .onFailure { error ->
                logger.error("이메일 중복 확인 실패", error)
            }
    }

}